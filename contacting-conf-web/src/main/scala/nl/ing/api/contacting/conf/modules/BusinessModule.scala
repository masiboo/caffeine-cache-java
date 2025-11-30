package nl.ing.api.contacting.conf.modules

import cats.effect.IO
import com.codahale.metrics.MetricRegistry
import com.hazelcast.core.HazelcastInstance
import com.ing.api.contacting.configuration.filter.PeerNameRefererFilter
import com.ing.apisdk.toolkit.connectivity.transport.http.Http.ClientWrapper
import com.ing.apisdk.toolkit.service.Manifest
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import nl.ing.api.contacting.business.CacheConfigProvider
import nl.ing.api.contacting.caching.hazelcast.cache.{DMultiMapCache, DMultiMapCacheImpl}
import nl.ing.api.contacting.caching.hazelcast.util.HazelcastContext
import nl.ing.api.contacting.conf.business._
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor
import nl.ing.api.contacting.conf.business.hazelcast.HazelcastInterpreter
import nl.ing.api.contacting.conf.business.permissions.{AsyncBusinessFunctionDS, PermissionAlgebra, PermissionReaderService, PermissionService}
import nl.ing.api.contacting.conf.business.survey._
import nl.ing.api.contacting.conf.entityevents.EntityEventSender
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository._
import nl.ing.api.contacting.conf.repository.cassandra.{AsyncSurveyCallRecordsRepository, FutureConfigRepository, FutureSurveyCallRecordsRepository, SurveyCallRecordsRepository}
import nl.ing.api.contacting.conf.repository.cassandra.permissions.{FutureBusinessFunctionsRepository, FutureEmployeesByAccountRepository}
import nl.ing.api.contacting.conf.repository.cslick.actions._
import nl.ing.api.contacting.conf.streams.SurveyTriggerTopicProcessor
import nl.ing.api.contacting.repository.cslick.actions._
import nl.ing.api.contacting.repository.organisation.{AsyncOrganisationRepo, OrganisationRepoFut, OrganisationRepository}
import nl.ing.api.contacting.tracing.Trace.IOKleisli
import nl.ing.api.contacting.tracing.Trace.Implicits.noop
import nl.ing.twilio.business.TwilioEndpointConfigurer
import org.quartz.Scheduler

import scala.concurrent.Future

/**
 * @author Ayush Mittal
 */
trait BusinessModule {

  this: CoreModule =>

  lazy val hazelcastInstance: HazelcastInstance = springContext.getBean(classOf[HazelcastInstance])

  lazy val distributedCache: DMultiMapCache = new DMultiMapCacheImpl()(HazelcastContext(hazelcastInstance, ExecutionContextConfig.ioExecutionContext))

  lazy val configRepository = new FutureConfigRepository()
  private lazy val configService: ConfigService = new ConfigService(configRepository, this)

  private implicit val cacheConfigProvider:  CacheConfigProvider[Future] = new DefaultCacheConfigProvider(configService)

  lazy val metricRegistry = new MetricRegistry

  private lazy val organisationRepositoryFut: OrganisationRepoFut = new OrganisationRepoFut(organisationAction)(executionContext)
  lazy val organisationRepository: OrganisationRepository[IO] = new AsyncOrganisationRepo[IO](organisationRepositoryFut)
  private lazy val contactingEntityEventSender : EntityEventSender = new EntityEventSender(entityEventProducerConfig.topic,this.entityEventProducer)

  lazy val twilioEndpointConfigurer: TwilioEndpointConfigurer = springContext.getBean(classOf[TwilioEndpointConfigurer])

  private lazy val surveySettingAction = new SurveySettingAction(dBComponent)
  private lazy val surveySettingQueries = new SurveySettingQueries(dBComponent, surveyTaskQAction, surveyPhNumberFormatAction, surveyOrgAction, workerOrgAction, accountAction)
  private lazy val surveyPhNumberFormatAction =  new SurveyPhNumberFormatAction(dBComponent, surveySettingAction)
  private lazy val organisationAction = new OrganisationAction(dBComponent)
  private lazy val surveyOrgAction = new SurveyOrgAction(dBComponent, surveySettingAction, organisationAction)
  private lazy val taskQueueAction = new TaskQueueAction(dBComponent)
  private lazy val surveyTaskQAction = new SurveyTaskQAction(dBComponent, surveySettingAction, taskQueueAction)
  private lazy val workerAction = new WorkerAction(dBComponent)
  private lazy val workerOrgAction = new WorkerOrganisationAction(dBComponent, organisationAction, workerAction)
  private lazy val accountAction = new AccountAction(dBComponent)

  private lazy val futureSurveySettingRepository = new FutureSurveySettingRepository(surveySettingQueries)
  private lazy val surveySettingRepository: SurveySettingRepository[IO] = new AsyncSurveySettingRepository[IO](futureSurveySettingRepository)

  private lazy val futureSurveyPhNumFormatRepository = new FutSurveyPhNumberFormatRepo(surveyPhNumberFormatAction)
  private lazy val surveyPhNumFormatRepository: SurveyPhNumberFormatRepo[IO] = new AsyncSurveyPhNumberFormatRepo[IO](futureSurveyPhNumFormatRepository)

  private lazy val futureSurveyTaskQRepository = new FutureSurveyTaskQRepository(surveyTaskQAction)
  lazy val surveyTaskQRepository: SurveyTaskQRepository[IO] = new AsyncSurveyTaskQRepository[IO](futureSurveyTaskQRepository)

  private lazy val futureSurveyOrgRepository = new FutureSurveyOrgRepository(surveyOrgAction)
  lazy val surveyOrgRepository: SurveyOrgRepository[IO] = new AsyncSurveyOrgRepository[IO](futureSurveyOrgRepository)

  private lazy val surveyRepo: SurveyRepository[IO] = new SurveyRepository[IO](surveySettingRepository, surveyPhNumFormatRepository, surveyTaskQRepository, surveyOrgRepository)

  lazy val futSurveyCallRecordsRepo = new FutureSurveyCallRecordsRepository()
  private lazy val surveyCallRecordsRepo: SurveyCallRecordsRepository[IO] = new AsyncSurveyCallRecordsRepository[IO](futSurveyCallRecordsRepo)
  private lazy val surveyCallRecordsRepoF: SurveyCallRecordsRepository[IOKleisli] = new AsyncSurveyCallRecordsRepository[IOKleisli](futSurveyCallRecordsRepo)

  lazy val surveyService: SurveyService[IO] = new SurveyService[IO](surveyRepo, surveyCallRecordsRepo)

  private lazy val surveyOrgRepositoryF: SurveyOrgRepository[IOKleisli] = new AsyncSurveyOrgRepository[IOKleisli](futureSurveyOrgRepository)
  private lazy val surveyTaskQRepositoryF: SurveyTaskQRepository[IOKleisli] = new AsyncSurveyTaskQRepository[IOKleisli](futureSurveyTaskQRepository)
  private lazy val surveyPhNumFormatRepositoryF: SurveyPhNumberFormatRepo[IOKleisli] = new AsyncSurveyPhNumberFormatRepo[IOKleisli](futureSurveyPhNumFormatRepository)
  private lazy val surveySettingRepositoryF: SurveySettingRepository[IOKleisli] = new AsyncSurveySettingRepository[IOKleisli](futureSurveySettingRepository)
  private lazy val surveyRepoF: SurveyRepository[IOKleisli] = new SurveyRepository[IOKleisli](surveySettingRepositoryF, surveyPhNumFormatRepositoryF, surveyTaskQRepositoryF, surveyOrgRepositoryF)

  lazy val surveyStreamService: SurveyStreamService[IOKleisli] = new SurveyStreamService[IOKleisli](surveyCallRecordsRepoF, surveyRepoF, new Randomizer, blacklistServiceKleisli)

  lazy val tracer: Tracer = springContext.getBean("merakTracer", classOf[Tracer])
  lazy val openTelemetry: OpenTelemetry = springContext.getBean(classOf[OpenTelemetry])

  private lazy val touchPointClient: Http.Client = springContext.getBean("httpClient", classOf[Http.Client]).withOpenTelemetry(openTelemetry)

  private lazy val surveyOrgRepositoryIO: SurveyOrgRepository[IO] = new AsyncSurveyOrgRepository[IO](futureSurveyOrgRepository)
  private lazy val surveyTaskQRepositoryIO: SurveyTaskQRepository[IO] = new AsyncSurveyTaskQRepository[IO](futureSurveyTaskQRepository)
  private lazy val surveyPhNumFormatRepositoryIO: SurveyPhNumberFormatRepo[IO] = new AsyncSurveyPhNumberFormatRepo[IO](futureSurveyPhNumFormatRepository)
  private lazy val surveySettingRepositoryIO: SurveySettingRepository[IO] = new AsyncSurveySettingRepository[IO](futureSurveySettingRepository)
  private lazy val surveyRepoIO: SurveyRepository[IO] = new SurveyRepository[IO](surveySettingRepositoryIO, surveyPhNumFormatRepositoryIO, surveyTaskQRepositoryIO, surveyOrgRepositoryIO)

  private lazy val surveyStreamServiceIO: SurveyStreamService[IO] = new SurveyStreamService[IO](surveyCallRecordsRepo, surveyRepoIO, new Randomizer, blacklistService)

  private lazy val callFlowResultProcessor: CallFlowResultProcessor = new CallFlowResultProcessor(surveyStreamServiceIO)

  private lazy val persistenceScheduler: Scheduler = springContext.getBean("jdbcScheduler", classOf[Scheduler])

  lazy val callFlowQuartzScheduler : CallFlowQuartzScheduler = new CallFlowQuartzScheduler(persistenceScheduler)
  lazy val callFlowInMemoryScheduler: CallFlowInMemoryScheduler = new CallFlowInMemoryScheduler(callFlowTriggerAndProcessor)

  lazy val surveyRouter: SurveyRouter = new SurveyRouter(callFlowInMemoryScheduler,callFlowQuartzScheduler)
  lazy val surveyTriggerTopicProcessor: SurveyTriggerTopicProcessor[IO] = new SurveyTriggerTopicProcessor[IO](surveyStreamServiceIO, surveyRouter)
  private val callflowDest = "/endpoint/api.ing.com/POST/contacting-callflows/outbound-calls/:callFlowName:"
  lazy val callFlowTriggerAndProcessor : CallFlowTriggerAndProcessor = new CallFlowTriggerAndProcessor(touchPointClient.newService(callflowDest), tracer, callFlowResultProcessor)

  private lazy val routingResilientHttpClient: Service[Request, Response] =
    springContext.getBean("routingResilientHttpClient", classOf[Service[Request, Response]])

  lazy val manifest: Manifest = springContext.getBean(classOf[Manifest])

  lazy val routingResilientHttpClientWithRefererFilter: Service[Request, Response] =
    new PeerNameRefererFilter(manifest).andThen(routingResilientHttpClient)

  private lazy val blackListActions: BlacklistActions = new BlacklistActions(this.dBComponent, accountAction)
  private lazy val futureBlacklistRepository: FutureBlacklistRepository = new FutureBlacklistRepository(blackListActions)
  private lazy val blackListRepository: AsyncBlacklistRepository[IO] = new AsyncBlacklistRepository[IO](futureBlacklistRepository)

  lazy val blacklistService = new BlacklistService[IO](blackListRepository)

  private lazy val blackListRepositoryKleisli: AsyncBlacklistRepository[IOKleisli] = new AsyncBlacklistRepository[IOKleisli](futureBlacklistRepository)
  private lazy val blacklistServiceKleisli = new BlacklistService[IOKleisli](blackListRepositoryKleisli)

  lazy val employeeByAccountRepository: FutureEmployeesByAccountRepository =
    new FutureEmployeesByAccountRepository()
  lazy val businessFunctionsRepository = new FutureBusinessFunctionsRepository(distributedCache)
  lazy val permissionService = new PermissionService(configService, businessFunctionsRepository, auditLogger)
  private lazy val businessFunctionDataSourceF =
    new AsyncBusinessFunctionDS[IOKleisli](businessFunctionsRepository, employeeByAccountRepository)
  lazy val permissionAlgebraF = new PermissionAlgebra[IOKleisli](businessFunctionDataSourceF)
  lazy val permissionReader = new PermissionReaderService[IOKleisli](permissionAlgebraF)
  lazy val hazelcastAlgebraF = new HazelcastInterpreter[IO](distributedCache)

}

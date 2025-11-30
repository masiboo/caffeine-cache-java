package nl.ing.api.contacting.conf.modules

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.repository.cassandra.quill.ContactingQuillSession
import nl.ing.api.contacting.conf.repository.cassandra.quill.QuillQueryExecutor

import javax.annotation.PreDestroy
import scala.compat.java8.OptionConverters.RichOptionalGeneric
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success


trait QuillModule {
  this: CoreModule =>
  implicit val quillQueryExecutor: QuillQueryExecutor
  val contactingCassandraContext: ContactingQuillSession
  val contactingCassandraContextCLOne: ContactingQuillSession
}

trait DefaultQuillModule extends QuillModule with LazyLogging {
  this: CoreModule =>

  implicit lazy val quillQueryExecutor: QuillQueryExecutor = new QuillQueryExecutor()(this)
  lazy val contactingCassandraContext = new ContactingQuillSession(session(ConsistencyLevel.LOCAL_QUORUM))
  lazy val contactingCassandraContextCLOne = new ContactingQuillSession(session(ConsistencyLevel.ONE))

  def initCassandra(): Future[_] = {
    Future {
      List(contactingCassandraContext.session, contactingCassandraContextCLOne.session).map(_.getKeyspace.asScala.map(identifier => {
        logger.info(s"Initialized cassandra with keyspace ${identifier.toString}")
        configRepository.findAll().onComplete{
          case Success(_) =>
            logger.info("Warmed up Cassandra by fetching the configs")
          case Failure(exception) =>
            logger.error("Could not warm up Cassandra", exception)
        }(ExecutionContextConfig.ioExecutionContext)
      }))
    }(ExecutionContextConfig.ioExecutionContext)
  }

  @PreDestroy
  def destroy(): Unit = {
    logger.info("Closing cassandra sessions")
    contactingCassandraContext.session.close()
    contactingCassandraContextCLOne.session.close()
  }
}

package nl.ing.api.contacting.conf.repository.cassandra

import com.datastax.oss.driver.api.core.ConsistencyLevel
import nl.ing.api.contacting.conf.domain.ContactingConfigVO
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository.cassandra.quill.QuillQueryExecutor

import scala.concurrent.Future

object ConfigRepository {
  val SYSTEM_FUNCTIONS = "SYSTEM_FUNCTIONS"
}
trait ConfigRepository[F[_]] {

  /**
   * search all configs for the platform
   *
   */
  def findAll(): F[Seq[ContactingConfigVO]]

  def findByKey(key: String): F[Option[ContactingConfigVO]]

  def findByKeys(keys: Seq[String]): F[Seq[ContactingConfigVO]]

  def update(config:Seq[ContactingConfigVO]) : F[Unit]
}

/**
  * Created by le26rc on 11-12-2017.
  */
class FutureConfigRepository(implicit val quillWrapper: QuillQueryExecutor)  extends ConfigRepository[Future] {
  val cl = ConsistencyLevel.LOCAL_QUORUM

  def findAll(): Future[Seq[ContactingConfigVO]] = {
    quillWrapper(Some(cl))(context => {
      import context._
      run(quote(contactingConfigSchema))
    }).map(_.toSeq)
  }

  def findByKey(key: String): Future[Option[ContactingConfigVO]] = {
    quillWrapper(Some(cl))(context => {
      import context._
      run(quote(contactingConfigSchema)
            .filter(c => c.key == lift(key))).map(_.headOption)
    })
  }

  def findByKeys(keys: Seq[String]): Future[Seq[ContactingConfigVO]] = {
    quillWrapper(Some(cl))(context => {
      import context._
      run(quote(contactingConfigSchema)
        .filter(c => liftQuery(keys.toSet).contains(c.key)))
    })
  }

  def update(config:Seq[ContactingConfigVO]) : Future[Unit] = {
    quillWrapper(Some(cl))(context => {
      import context._
      run(quote {
        liftQuery(config.toList).foreach(config => contactingConfigSchema.insert(config))
      })
    })
  }
}

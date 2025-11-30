package nl.ing.api.contacting.conf.repository.cassandra.quill

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.twitter.util.Local
import nl.ing.api.contacting.conf.modules.QuillModule

class QuillQueryExecutor()(implicit quillModule: QuillModule) {
  def apply[T](consistencyLevel: Option[ConsistencyLevel] = Some(ConsistencyLevel.LOCAL_QUORUM))(query: ContactingQuillSession => T): T = {
    val savedLocal = Local.save()
    val queryResult = consistencyLevel match {
      case Some(ConsistencyLevel.ONE) =>
        query(quillModule.contactingCassandraContextCLOne)
      case _ =>
        query(quillModule.contactingCassandraContext)
    }
    Local.restore(savedLocal)
    queryResult
  }
}

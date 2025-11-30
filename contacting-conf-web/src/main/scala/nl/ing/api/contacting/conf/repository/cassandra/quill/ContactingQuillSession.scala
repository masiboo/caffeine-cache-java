package nl.ing.api.contacting.conf.repository.cassandra.quill

import com.datastax.oss.driver.api.core.CqlSession
import io.getquill.CassandraAsyncContext
import io.getquill.SnakeCase

/**
 * Quill Cassandra session context with contacting schemas included
 *
 * @param session            Cassandra session
 */
class ContactingQuillSession(session: CqlSession)
  extends CassandraAsyncContext[SnakeCase](SnakeCase, session, 1000) with ContactingQuillSchema {
}

package nl.ing.api.contacting.conf.repository.cslick

import nl.ing.api.contacting.repository.cslick.DBComponent
import slick.jdbc.JdbcProfile

/**
  * @author Ayush Mittal
  */
trait CoCoDBComponent extends DBComponent {
  val driver : JdbcProfile
  import driver.api._
  val db: Database
}

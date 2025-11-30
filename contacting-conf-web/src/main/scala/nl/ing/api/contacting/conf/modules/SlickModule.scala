package nl.ing.api.contacting.conf.modules

import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import nl.ing.api.contacting.scontext.ContactingSlickAsyncExecutor

import javax.sql.DataSource

/**
  * @author Ayush Mittal
  */
trait SlickModule {
  val dBComponent: CoCoDBComponent
}

trait DefaultSlickModule extends SlickModule {

  this: CoreModule =>

  val dataSource: DataSource = this.springContext.getBean("userDataSource", classOf[DataSource])

  private lazy val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("slick")

  //get dbComponent from databaseConfig
  implicit lazy val dBComponent: CoCoDBComponent = new CoCoDBComponent {
    override val driver = databaseConfig.profile

    import driver.api._

    override val db: Database = Database.forDataSource(dataSource, None, executor = ContactingSlickAsyncExecutor.default("contacting-conf-slick",20))
  }
}
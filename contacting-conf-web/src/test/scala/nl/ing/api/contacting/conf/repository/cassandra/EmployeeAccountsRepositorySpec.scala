package nl.ing.api.contacting.conf.repository.cassandra

import com.datastax.oss.driver.api.core.{ConsistencyLevel, CqlSession}
import nl.ing.api.contacting.conf.domain.{EmployeeAccountsVO, OrganisationalRestriction}
import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import nl.ing.api.contacting.conf.support.{SystemModuleMockingSupport, TestModule}
import nl.ing.api.contacting.shared.client.ContactingAPIClient
import nl.ing.api.contacting.test.cassandra.ContactingCassandraSpec
import org.mockito.Mockito
import org.scalatest.BeforeAndAfter
import org.scalatestplus.junit.JUnitRunner
import org.springframework.context.ApplicationContext

@org.junit.runner.RunWith(value = classOf[JUnitRunner])
class EmployeeAccountsRepositorySpec
  extends ContactingCassandraSpec[TestModule] with SystemModuleMockingSupport with BeforeAndAfter {

  override val timeout = 500000L

  override lazy val keySpaceName: String = "contacting"

  override val module: TestModule = new TestModule {
    override implicit def springContext: ApplicationContext = mock[ApplicationContext]

    override val contactingAPIClient: ContactingAPIClient =
      mock[ContactingAPIClient]
    override val dBComponent: CoCoDBComponent =
      mock[CoCoDBComponent]

    override def session(consistencyLevel: ConsistencyLevel): CqlSession = cassandraUnit.session
  }

  var spyModule = Mockito.spy(module)

  override val recreateDatabase = false

  val data = Some("cassandra/employee-accounts.cql")

  val employeeAcc = EmployeeAccountsVO("MIJNID",
                                       "hr",
                                       preferredAccount = true,
                                       Set("ADMIN", "AGENT"),
                                       Some("bunit"),
                                       Some("department"),
                                       Some("team"),
                                       None,
                                       Map.empty[String, Int],
                                       None)

  "an employee account " should " be created" in {
    whenReady(
      spyModule.employeeByAccountRepository
        .createAccountByEmployee("1224", "hr", Set("ADMIN", "AGENT"), preferred = true)) {
      _ =>
        whenReady(
          spyModule.employeeByAccountRepository
            .createAccountByEmployee("1224", "PS", Set("ADMIN", "AGENT"), preferred = true)) {
          _ =>
            whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("1224", "hr")) {
              employee =>
                employee.get.accountFriendlyName shouldBe "hr"
                employee.get.employeeId shouldBe "1224"
                employee.get.roles shouldBe Some("ADMIN,AGENT")
                employee.get.rolesAsSet() shouldBe Set("ADMIN", "AGENT")
            }
        }
    }
  }

  "an employee account " should " be created and updated with List of organisations" in {
    whenReady(
      spyModule.employeeByAccountRepository
        .createAccountByEmployee("BO55NK", "NL-unit", Set("ADMIN", "AGENT"), preferred = true)) {
      _ =>
        whenReady(
          spyModule.employeeByAccountRepository.upsert(EmployeeAccountsVO(
            "BO55NK",
            "NL-unit",
            preferredAccount = true,
            Some("ADMIN,AGENT"),
            Some("SC"),
            Some("C"),
            Some("CLT"),
            Some(Set(OrganisationalRestriction(1, "CLT", 2, "C", 3, "SC", preferred = true)))
            ))) {
          _ =>
            whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("BO55NK", "NL-unit")) {
              employee =>
                employee.get.accountFriendlyName shouldBe "NL-unit"
                employee.get.employeeId shouldBe "BO55NK"
                employee.get.roles shouldBe Some("ADMIN,AGENT")
                employee.get.organisationalRestrictions.get
                  .mkString(",") shouldBe "OrganisationalRestriction(1,CLT,2,C,3,SC,true)"
            }
        }
    }
  }

  "an employee account " should " be found" in {
    whenReady(
      spyModule.employeeByAccountRepository.createAccountByEmployee("1224", "hr", Set("ADMIN"), preferred = true)) {
      _ =>
        whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("999", "nl_ps")) {
          emp =>
            emp.get.accountFriendlyName shouldBe "nl_ps"
        }
    }
  }

  "an employee account " should " be found with multiple teams" in {
    whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("BO55NK", "NL-dev")) {
      result =>
        result.get.accountFriendlyName shouldBe "NL-dev"
        result.get.organisationalRestrictions.get
          .mkString(",") shouldBe "OrganisationalRestriction(1,T,2,C,3,SC,true),OrganisationalRestriction(4,T1,5,C2,6,SC3,false)"
    }
  }

  "an employee account " should " searched at Consistence Local Local_Quorum if not found at ONE" in {
    whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("BO55NK1", "NL-dev")) {
      result =>
        result shouldBe None
    }
  }

  "an employee accounts " should " be found" in {
    whenReady(
      spyModule.employeeByAccountRepository.createAccountByEmployee("1224", "hr", Set("ADMIN"), preferred = true)) {
      _ =>
        whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("999")) {
          emp =>
            emp.head.accountFriendlyName shouldBe "nl_ps"
        }
    }
  }

  "a employee account " should " be found ignoring case" in {
    whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("abc", "NL-PS-tst")) {
      emp =>
        emp.get.accountFriendlyName shouldBe "NL-PS-tst"
        whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("ABC", "NL-PS-tst")) {
          emp =>
            emp.get.accountFriendlyName shouldBe "NL-PS-tst"
        }
    }
  }

  "an employee account " should " be deleted" in {
    whenReady(
      spyModule.employeeByAccountRepository.createAccountByEmployee("1337", "hr", Set("ADMIN"), preferred = true)) {
      _ =>
        whenReady(spyModule.employeeByAccountRepository.deleteByEmployeeId("1337", "hr")) {
          _ =>
            whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("1337", "hr")) {
              employee =>
                employee shouldBe None
            }
        }
    }
  }

  "an employee account " should " be updated" in {
    whenReady(
      spyModule.employeeByAccountRepository.createAccountByEmployee("MIJNID", "hr", Set("ADMIN"), preferred = true)) {
      _ =>
        whenReady(spyModule.employeeByAccountRepository.upsert(employeeAcc)) {
          _ =>
            whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("MIJNID", "hr")) {
              employee =>
                employee.get.accountFriendlyName shouldBe "hr"
                employee.get.businessUnit shouldBe Some("bunit")
                employee.get.department shouldBe Some("department")
                employee.get.team shouldBe Some("team")
                employee.get.roles shouldBe Some("ADMIN,AGENT")
            }
        }
    }
  }

  "an employee account " should " be created with default preferred" in {
    whenReady(
      spyModule.employeeByAccountRepository.saveWithoutOverridingPreferredAccount(
        employeeAcc.copy(employeeId = "900", preferredAccount = false))) {
      _ =>
        whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("900", "hr")) {
          employee =>
            // one time for saveWithout which does a find, one time for latest findByEmployee above
            employee.get.employeeId shouldBe "900"
            employee.get.accountFriendlyName shouldBe "hr"
            employee.get.businessUnit shouldBe Some("bunit")
            employee.get.department shouldBe Some("department")
            employee.get.team shouldBe Some("team")
            employee.get.roles shouldBe Some("ADMIN,AGENT")
            employee.get.preferredAccount shouldBe true
        }
    }
  }

  "an employee account " should " be created with not touching the preferred" in {
    whenReady(
      spyModule.employeeByAccountRepository.createAccountByEmployee("MYNEWID", "hr", Set("ADMIN"), preferred = true)) {
      _ =>
        whenReady(
          spyModule.employeeByAccountRepository.saveWithoutOverridingPreferredAccount(
            employeeAcc.copy(employeeId = "MYNEWID", preferredAccount = false))) {
          _ => {
            Thread.sleep(200)
            whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("MYNEWID", "hr")) {
              employee =>
                employee.get.employeeId shouldBe "MYNEWID"
                employee.get.accountFriendlyName shouldBe "hr"
                employee.get.businessUnit shouldBe Some("bunit")
                employee.get.department shouldBe Some("department")
                employee.get.team shouldBe Some("team")
                employee.get.roles shouldBe Some("ADMIN,AGENT")
                employee.get.preferredAccount shouldBe true
            }
          }
        }
    }
  }

  "an new employee account " should " be created with not touching the existing preferred" in {
    val employeePL = EmployeeAccountsVO("MIJNID",
      "PL",
      preferredAccount = true,
      Set("ADMIN", "AGENT"),
      Some("pl-bunit"),
      Some("pl-department"),
      Some("pl-team"),
      None,
      Map.empty[String, Int],
      None)
    whenReady(
      spyModule.employeeByAccountRepository.saveWithoutOverridingPreferredAccount(employeeAcc)) {
      _ =>
        Thread.sleep(200)
        whenReady(
          spyModule.employeeByAccountRepository.saveWithoutOverridingPreferredAccount(employeePL)) {
          _ => {
            Thread.sleep(200)
            whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("MIJNID", "hr")) {
              employee =>
                employee.get.employeeId shouldBe "MIJNID"
                employee.get.accountFriendlyName shouldBe "hr"
                employee.get.businessUnit shouldBe Some("bunit")
                employee.get.department shouldBe Some("department")
                employee.get.team shouldBe Some("team")
                employee.get.roles shouldBe Some("ADMIN,AGENT")
                employee.get.preferredAccount shouldBe true
            }
            whenReady(spyModule.employeeByAccountRepository.findByEmployeeId("MIJNID", "PL")) {
              plEmployee =>
                plEmployee.get.employeeId shouldBe "MIJNID"
                plEmployee.get.accountFriendlyName shouldBe "PL"
                plEmployee.get.businessUnit shouldBe Some("pl-bunit")
                plEmployee.get.department shouldBe Some("pl-department")
                plEmployee.get.team shouldBe Some("pl-team")
                plEmployee.get.roles shouldBe Some("ADMIN,AGENT")
                plEmployee.get.preferredAccount shouldBe false
            }
          }
        }
    }

  }

  before {
    Mockito.clearInvocations(spyModule)
  }
}

package nl.ing.api.contacting.conf.repository.cslick.actions

import com.ing.api.contacting.dto.resource.blacklist.{BlacklistFunctionality, BlacklistType}
import nl.ing.api.contacting.conf.repository.cslick.data.BlacklistItemData
import nl.ing.api.contacting.domain.slick.AccountVO
import nl.ing.api.contacting.repository.cslick.Lens.lens
import nl.ing.api.contacting.repository.cslick.actions.AccountAction
import nl.ing.api.contacting.repository.cslick.actions.core.ContactingSlickActions
import nl.ing.api.contacting.repository.cslick.{DBComponent, Lens}
import slick.ast.BaseTypedType
import slick.lifted.ForeignKeyQuery

import java.sql.Timestamp
import java.time.LocalDateTime

class BlacklistActions(val dbComponent: DBComponent, val accountAction: AccountAction) extends ContactingSlickActions[BlacklistItemData, Long] {
  override val jdbcProfile = dbComponent.driver

  import jdbcProfile.api._

  implicit val localDateToTimestamp: BaseColumnType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Timestamp](
      l => Timestamp.valueOf(l),
      d => d.toLocalDateTime
    )

  class BlacklistTable(tag: Tag) extends Table[BlacklistItemData](tag, "BLACKLIST") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def functionality = column[String]("FUNCTIONALITY")

    def entityType = column[String]("ENTITY_TYPE")

    def value = column[String]("ENTITY_VALUE")

    def startDate = column[LocalDateTime]("START_DATE")

    def endDate = column[Option[LocalDateTime]]("END_DATE")

    def accountId = column[Long]("ACCOUNT_ID")

    override def * =
      (id.?, functionality, entityType, value, startDate, endDate, accountId) <> ((BlacklistItemData.apply _).tupled, BlacklistItemData.unapply)

    def accountForeignKey: ForeignKeyQuery[accountAction.AccountTable, AccountVO] =
      foreignKey(
        "FK_ACCOUNT_ID",
        accountId,
        accountAction.tableQuery)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  override type EntityTable = BlacklistTable

  override def tableQuery = TableQuery[BlacklistTable]

  override def primaryColumn(table: BlacklistTable) = table.id

  implicit override protected val btt: BaseTypedType[Long] =
    jdbcProfile.api.longColumnType

  override def contextIdColumn(table: BlacklistTable): jdbcProfile.api.Rep[Long] =
    table.accountId

  override def idLens: Lens[BlacklistItemData, Option[Long]] =
    lens {
      blacklistItem: BlacklistItemData =>
        blacklistItem.id
    } {
      (blacklistItem: BlacklistItemData, id: Option[Long]) =>
        blacklistItem.copy(id = id)
    }

  def allForAccountAndFunctionality(accountId: Long,
                                    now: LocalDateTime,
                                    functionality: String): DBIO[Seq[BlacklistItemData]] =
    accountFunctionalityFilterCompiledQuery((accountId, now, functionality)).result

  private val accountFunctionalityFilterCompiledQuery = Compiled(
    (accountId: Rep[Long], now: Rep[LocalDateTime], functionality: Rep[String]) =>
      allFunctionalityFilter(accountId, now, functionality))

  private def allFunctionalityFilter(accountId: Rep[Long], now: Rep[LocalDateTime], functionality: Rep[String]) = {
    tableQuery.filter(item =>
      item.accountId === accountId && (item.endDate.isEmpty || item.endDate > now) && item.functionality === functionality)
  }

  def allForAccount(accountId: Long, now: LocalDateTime): DBIO[Seq[BlacklistItemData]] =
    accountFilterCompiledQuery((accountId, now)).result

  private val accountFilterCompiledQuery = Compiled(
    (accountId: Rep[Long], now: Rep[LocalDateTime]) => allFilter(accountId, now))

  private def allFilter(accountId: Rep[Long], now: Rep[LocalDateTime]) = {
    tableQuery.filter(item => item.accountId === accountId && (item.endDate.isEmpty || item.endDate > now))
  }

  private val accountDeleteCompiledQuery = Compiled(
    (accountId: Rep[Long], now: Rep[LocalDateTime]) => deleteFilter(accountId, now))

  private def deleteFilter(accountId: Rep[Long], now: Rep[LocalDateTime]) = {
    tableQuery.filter(item => item.accountId === accountId && (item.endDate.isDefined && item.endDate < now))
  }

  def cleanUpBlacklist(accountId: Long): DBIO[Int] =
    accountDeleteCompiledQuery((accountId, LocalDateTime.now)).delete

  def allSurveyBlacklistItems(accountSid: String, now: LocalDateTime) = {
    accountAction.tableQuery.filter(_.sid === accountSid).join(tableQuery.filter(item =>
      (item.endDate.isEmpty || item.endDate > now) &&
        (item.functionality === BlacklistFunctionality.SURVEY.toString || item.functionality === BlacklistFunctionality.ALL.toString) &&
        item.entityType === BlacklistType.PHONE_NUMBER.toString
    )).on(_.id === _.accountId).result
  }
}

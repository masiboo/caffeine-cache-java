package nl.ing.api.contacting.conf.repository.cassandra.quill

import io.getquill.context.Context
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, ContactingConfigVO, EmployeeAccountsVO, SurveyCallRecordVO}

trait ContactingQuillSchema {
  this: Context[_, _] =>

  val contactingConfigSchema = quote(querySchema[ContactingConfigVO]("contacting.contacting_configs"))
  val surveyCallRecordsSchema = quote(querySchema[SurveyCallRecordVO]("contacting.survey_call_records"))
  val businessFunctionsSchema = quote(querySchema[BusinessFunctionVO]("contacting.business_functions_on_teams"))
  val employeesByAccountSchema = quote(querySchema[EmployeeAccountsVO]("contacting.employees_by_account"))
}



package nl.ing.api.contacting.conf.resource.dto

//level is an Int because we do not want jackson to serialize to the name of the level (SELF/TEAM/etc)
case class BusinessFunctionAccess(role: String, level: Int, organisationId: Option[Int] = None)
case class BusinessFunctionsDto(name: String, allowedAccess: Seq[BusinessFunctionAccess])
case class BusinessFunctionsDtoWrapper(data: Seq[BusinessFunctionsDto])


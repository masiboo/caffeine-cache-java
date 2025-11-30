package nl.ing.api.contacting.conf.mapper

import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.domain.SurveyDetailsVO
import nl.ing.api.contacting.conf.domain.SurveyOrgMappingVO
import nl.ing.api.contacting.conf.domain.SurveyPhoneNumberFormatVO
import nl.ing.api.contacting.conf.domain.SurveySettingVO
import nl.ing.api.contacting.conf.domain.SurveyTaskQMappingVO
import nl.ing.api.contacting.conf.domain.SurveyUpdateVO
import nl.ing.api.contacting.conf.exception.RefinedTypeError
import nl.ing.api.contacting.conf.repository.SurveyOrgRepository.SurveyOrgDetails
import nl.ing.api.contacting.conf.repository.SurveyTaskQRepository.SurveyTaskQDetails
import nl.ing.api.contacting.conf.repository.model.SurveyDetails
import nl.ing.api.contacting.conf.repository.model.SurveyPhoneNumberFormat
import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.conf.resource.dto.SurveyDetailsDTO
import nl.ing.api.contacting.conf.resource.dto.SurveyOrgMappingDTO
import nl.ing.api.contacting.conf.resource.dto.SurveyOrganisationDTO
import nl.ing.api.contacting.conf.resource.dto.SurveyOverviewDTO
import nl.ing.api.contacting.conf.resource.dto.SurveyPhoneNumberFormatDTO
import nl.ing.api.contacting.conf.resource.dto.SurveyPhoneNumberFormatUpdateDTO
import nl.ing.api.contacting.conf.resource.dto.SurveySettingDTO
import nl.ing.api.contacting.conf.resource.dto.SurveyTaskQMappingDTO
import nl.ing.api.contacting.conf.resource.dto.SurveyUpdateDTO
import nl.ing.api.contacting.conf.util.EitherUtils._
import nl.ing.api.contacting.conf.util.RefinedUtils._
import nl.ing.api.contacting.domain.slick.OrganisationVO
import org.springframework.web.util.HtmlUtils

/**
 * @author Ayush Mittal
 */
object SurveyMapper extends LazyLogging {

  def surveySettingModelToVo(surveySetting: SurveySetting): Either[RefinedTypeError, SurveySettingVO] =
    for {
      id <- surveySetting.id.toEither(_.toDatabaseId)
      accountId <- surveySetting.accountId.toDatabaseId
      name <- surveySetting.name.toFriendlyName
      minFrequency <- surveySetting.minFrequency.toEither(_.toNumberOfDays)
      delay <- surveySetting.delay.toEither(_.toSeconds)
      offerRatio <- surveySetting.surveyOfferRatio.toEither(_.toPercentage)
      minContactLength <- surveySetting.minContactLength.toEither(_.toSeconds)
      surveyForTransfers = surveySetting.surveyForTransfers
    } yield SurveySettingVO(id, accountId, name, surveySetting.channel, surveySetting.channelDirection, surveySetting.voiceSurveyId, surveySetting.callflowName, minFrequency, delay, offerRatio, minContactLength, surveyForTransfers)

  def surveySettingVoToModel(vo: SurveySettingVO): SurveySetting =
    SurveySetting(vo.id.map(_.value),vo.accountId.value,vo.name.value,vo.channel,vo.channelDirection,vo.voiceSurveyId,
      vo.callflowName,vo.minFrequency.map(_.value),vo.delay.map(_.value), vo.surveyOfferRatio.map(_.value),vo.minContactLength.map(_.value),vo.surveyForTransfers)

  def surveySettingVoToOverviewDTO(vos: Seq[Either[RefinedTypeError, SurveySettingVO]]): Seq[SurveyOverviewDTO] = {
    val partition = vos.splitIntoLeftRight
    if(partition._1.nonEmpty) {
      logger.warn(s"Not all surveysettings were returned, ${partition._1.size} have validation errors")
    }
    surveySettingVoToOverviewDTOs(partition._2)
  }

  private def surveySettingVoToOverviewDTOs(vos: Seq[SurveySettingVO]): Seq[SurveyOverviewDTO] =
    vos.map(surveySettingVoToOverviewDTO)

  def surveySettingVoToOverviewDTO(vo: SurveySettingVO): SurveyOverviewDTO =
    SurveyOverviewDTO(vo.id.map(_.value), vo.name.value, vo.channel, vo.voiceSurveyId)

  private def surveyPhNumFormatModelToVo(model : SurveyPhoneNumberFormat): Either[RefinedTypeError, SurveyPhoneNumberFormatVO] =
    for {
      id <- model.id.toEither(_.toDatabaseId)
      surveyId <- model.surveyId.toDatabaseId
      direction <- (if(!model.direction) "allowed" else "excluded").toSurveyPhNumDirection
    } yield SurveyPhoneNumberFormatVO(id, surveyId,model.format, direction)

  def surveyPhNumFormats(surveyFriendlyName: String, vos : Seq[Either[RefinedTypeError,SurveyPhoneNumberFormatVO]]): Seq[SurveyPhoneNumberFormatVO] = {
    val (errors, items) = vos.splitIntoLeftRight
    if(errors.nonEmpty) {
      logger.warn(s"Not all survey phonenumber formats were returned for survey  $surveyFriendlyName, ${errors.size} have validation errors")
    }
    items
  }

  def surveyPhNumFormatToVoModel(vo : SurveyPhoneNumberFormatVO): SurveyPhoneNumberFormat =
    SurveyPhoneNumberFormat(vo.id.map(_.value), vo.surveyId.value, vo.format, vo.direction.value != "allowed")

  private def surveyPhNumFormatVoToDto(vo: SurveyPhoneNumberFormatVO): SurveyPhoneNumberFormatDTO = {
    SurveyPhoneNumberFormatDTO(vo.id.map(_.value), vo.format)
  }
  private def surveyTaskQModelToVo(model: SurveyTaskQDetails): Either[RefinedTypeError, SurveyTaskQMappingVO] =
    for {
      id <- model._1.id.toEither(_.toDatabaseId)
      surveyId <- model._1.surveyId.toDatabaseId
      taskQueueId <- model._1.taskqueueId.toDatabaseId
    } yield SurveyTaskQMappingVO(id, surveyId, taskQueueId, model._2)

  def surveyTaskQMappings(surveyFriendlyName: String, vos : Seq[Either[RefinedTypeError,SurveyTaskQMappingVO]]): Seq[SurveyTaskQMappingVO] = {
    val partition: (Seq[RefinedTypeError], Seq[SurveyTaskQMappingVO]) = vos.splitIntoLeftRight
    if(partition._1.nonEmpty) {
      logger.warn(s"Not all survey task queue mappings were returned for survey  $surveyFriendlyName, ${partition._1.size} have validation errors")
    }
    partition._2
  }

  private def surveyTaskQVOToDTO(surveyTaskQMappingVO: SurveyTaskQMappingVO): SurveyTaskQMappingDTO =
    SurveyTaskQMappingDTO(surveyTaskQMappingVO.taskQId.value, surveyTaskQMappingVO.tqName)

  private def surveyOrgVOToDTO(vo: SurveyOrgMappingVO): SurveyOrgMappingDTO =
    SurveyOrgMappingDTO(vo.organisationVO.id, orgVoToDto(vo.organisationVO))


  private def surveyOrgModelToVo(model: SurveyOrgDetails): Either[RefinedTypeError, SurveyOrgMappingVO] =
    for {
      id <- model._1._1._1.id.toEither(_.toDatabaseId)
      surveyId <- model._1._1._1.surveyId.toDatabaseId
    } yield SurveyOrgMappingVO(id, surveyId, organisationMapper(model))

  private def organisationMapper(model: SurveyOrgDetails): OrganisationVO =
    model match {
      case (((_, superCircle), None), None) => OrganisationVO(superCircle.id, superCircle.name, superCircle.organisationLevel, None)
      case (((_, circle), Some(superCircle)), None) => OrganisationVO(circle.id, circle.name, circle.organisationLevel, Some(OrganisationVO(superCircle.id, superCircle.name, superCircle.organisationLevel, None)))
      case (((_, team), Some(circle)), Some(superCircle)) =>  OrganisationVO(team.id, team.name, team.organisationLevel, Some(OrganisationVO(circle.id, circle.name, circle.organisationLevel, Some(OrganisationVO(superCircle.id, superCircle.name, superCircle.organisationLevel, None)))))
    }

  private def orgVoToDto(organisationVO: OrganisationVO): SurveyOrganisationDTO =
    SurveyOrganisationDTO(organisationVO.name, organisationVO.organisationLevel.toString, organisationVO.parent.map(orgVoToDto))

  private def surveyOrgMappings(surveyFriendlyName: String, vos : Seq[Either[RefinedTypeError,SurveyOrgMappingVO]]): Seq[SurveyOrgMappingVO] = {
    val partition: (Seq[RefinedTypeError], Seq[SurveyOrgMappingVO]) = vos.splitIntoLeftRight
    if(partition._1.nonEmpty) {
      logger.warn(s"Not all survey organisation mappings were returned for survey  $surveyFriendlyName, ${partition._1.size} have validation errors")
    }
    partition._2
  }

  def surveyDetailsToVo(surveyDetails: SurveyDetails): Either[RefinedTypeError, SurveyDetailsVO] = {
    for{
      settingVo <- surveySettingModelToVo(surveyDetails.setting)
      phoneNumFormats = surveyPhNumFormats(settingVo.name.value ,surveyDetails.phNumFormat.map(surveyPhNumFormatModelToVo))
      taskQMapping = surveyTaskQMappings(settingVo.name.value, surveyDetails.taskQMapping.map(surveyTaskQModelToVo))
      orgMapping = surveyOrgMappings(settingVo.name.value, surveyDetails.orgMapping.map(surveyOrgModelToVo))
    } yield SurveyDetailsVO(settingVo, phoneNumFormats, taskQMapping, orgMapping)
  }

  def surveyDetailsVOToOverviewDto(surveyDetailsVO: SurveyDetailsVO): SurveyDetailsDTO = {
    val settingDTO: SurveySettingDTO = surveySettingVOToSettingDto(surveyDetailsVO.surveySettingVO)
    val (allowed: Seq[SurveyPhoneNumberFormatVO],excluded: Seq[SurveyPhoneNumberFormatVO]) = surveyDetailsVO.phNumFormats.partition(_.direction.value == "allowed")
    val (allowedPhNumDto: Seq[SurveyPhoneNumberFormatDTO],excludedPhNumDto: Seq[SurveyPhoneNumberFormatDTO]) = (allowed.map(surveyPhNumFormatVoToDto),excluded.map(surveyPhNumFormatVoToDto))
    val taskQMappingDto: Seq[SurveyTaskQMappingDTO] = surveyDetailsVO.taskQMappingVO.map(surveyTaskQVOToDTO)
    val orgMappingDto: Seq[SurveyOrgMappingDTO] = surveyDetailsVO.orgMappingVO.map(surveyOrgVOToDTO)

    SurveyDetailsDTO(settingDTO, allowedPhNumDto, excludedPhNumDto, taskQMappingDto, orgMappingDto)
  }

  def surveySettingVOToSettingDto(vo: SurveySettingVO): SurveySettingDTO =
    SurveySettingDTO(vo.id.map(_.value),vo.name.value, vo.channel, vo.channelDirection, vo.voiceSurveyId, vo.callflowName,
      vo.minFrequency.map(_.value),vo.delay.map(_.value),vo.surveyOfferRatio.map(_.value),vo.minContactLength.map(_.value),vo.surveyForTransfers)

  def surveySettingDTOToVo(dto: SurveySettingDTO, accountId: Long): Either[RefinedTypeError,SurveySettingVO] =
    for {
      id <- dto.id.toEither(_.toDatabaseId)
      accountId <- accountId.toDatabaseId
      name <- dto.name.toFriendlyName
      minFrequency <- dto.minFrequency.toEither(_.toNumberOfDays)
      delay <- dto.delay.toEither(_.toSeconds)
      surveyOfferRatio <- dto.surveyOfferRatio.toEither(_.toPercentage)
      minContactLength <- dto.minContactLength.toEither(_.toSeconds)
      channel = HtmlUtils.htmlEscape(dto.channel)
      channelDirection = HtmlUtils.htmlEscape(dto.channelDirection)
      voiceSurveyId = HtmlUtils.htmlEscape(dto.voiceSurveyId)
      callflowName = dto.callflowName.map(s => HtmlUtils.htmlEscape(s))
    } yield SurveySettingVO(id, accountId, name, channel, channelDirection, voiceSurveyId, callflowName,
      minFrequency, delay, surveyOfferRatio, minContactLength, dto.surveyForTransfers)

  def surveyUpdateDTOToVo(dto: SurveyUpdateDTO, accountId: Long, surveyId: Long): Either[RefinedTypeError, SurveyUpdateVO] = for {
    settingVo <- surveySettingDTOToVo(dto.settings, accountId)
    formatsAdded = dto.formatsAdded.map(added => surveyPhoneFormatUpdateDTOToVO(added, surveyId)).splitIntoLeftRight._2
    formatsRemoved = dto.formatsRemoved.map(added => surveyPhoneFormatUpdateDTOToVO(added, surveyId)).splitIntoLeftRight._2
  } yield SurveyUpdateVO(settingVo, formatsAdded, formatsRemoved)

  private def surveyPhoneFormatUpdateDTOToVO(dto: SurveyPhoneNumberFormatUpdateDTO, surveyId: Long): Either[RefinedTypeError,SurveyPhoneNumberFormatVO] = for {
    id <- dto.id.toEither(_.toDatabaseId)
    surveyid <- surveyId.toDatabaseId
    direction <- (if(dto.direction==0) "allowed" else "excluded").toSurveyPhNumDirection
  } yield SurveyPhoneNumberFormatVO(id, surveyid, dto.format, direction)

}

package nl.ing.api.contacting.conf.resource.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import nl.ing.api.contacting.conf.domain.SurveyCallRecordVO
import nl.ing.api.contacting.conf.exception.ContactingBusinessErrorWithMsg
import nl.ing.api.contacting.conf.exception.IllegalArgument
import nl.ing.api.contacting.conf.exception.InvalidPhoneNumberFormat
import nl.ing.api.contacting.util.exception.ContactingBusinessError


/**
 * @author Ayush Mittal
 */
case class SurveyOverviewDTO(id: Option[Long], name: String, channel: String, voiceSurveyId: String)

case class AllSurveyOverviewDTO(dtos: Seq[SurveyOverviewDTO])

case class SurveySettingDTO(@JsonDeserialize(contentAs = classOf[java.lang.Long]) id: Option[Long],
                            name: String,
                            channel: String,
                            channelDirection: String,
                            voiceSurveyId: String,
                            callflowName: Option[String],
                            minFrequency: Option[Int],
                            @JsonDeserialize(contentAs = classOf[java.lang.Long]) delay: Option[Long],
                            @JsonDeserialize(contentAs = classOf[java.lang.Float]) surveyOfferRatio: Option[Float],
                            @JsonDeserialize(contentAs = classOf[java.lang.Long]) minContactLength: Option[Long],
                            surveyForTransfers: Boolean
                           )

object SurveySettingDTO{
  import nl.ing.api.contacting.conf.domain.SurveyConstants._
  def validateCallflowForChannel(dto: SurveySettingDTO): Either[ContactingBusinessError, Unit] = {
    if(dto.channel.equalsIgnoreCase(CHANNEL_CALL))
      dto.callflowName.map(_ => ()).toRight(IllegalArgument("Callflow is mandatory when channel is call"))
    else Right(())
  }

  def validateName(dto: SurveySettingDTO): Either[ContactingBusinessErrorWithMsg, Unit] = {
    dto.name == null || dto.name.length > 255 match {
      case true => Left(IllegalArgument("survey setting name length must be between 0 and 255"))
      case _ => Right(())
    }
  }
}

case class SurveyUpdateDTO(settings: SurveySettingDTO, formatsAdded: Seq[SurveyPhoneNumberFormatUpdateDTO], formatsRemoved: Seq[SurveyPhoneNumberFormatUpdateDTO])

object SurveyUpdateDTO{
  /**
   * It will validate the following conditions
   * i. Must starts with + and followed by a digit.
   * ii.There can be at least 1 or at most 14 more digits after the first two characters. (E164 format can have total 15 digits)
   * iii. The format can be end with a wildcard symbol '*'
   * iv. There can be at least 0 or at most 13 more digits after the first two character if end with the wild card.
   * @param surveyUpdateVO
   * @return
   */
  def validatePhFormat(surveyUpdateVO: SurveyUpdateDTO): Either[InvalidPhoneNumberFormat, Unit] = {
    val validationResult = (surveyUpdateVO.formatsAdded ++ surveyUpdateVO.formatsRemoved).forall{
      format =>
        val orRegex = "^\\+?[1-9]\\d{1,14}$|^\\+?[1-9]\\d{0,13}[\\*]$".r
        orRegex.findAllMatchIn(format.format).nonEmpty
    }
    if(validationResult){
      Right(())
    } else {
      Left(InvalidPhoneNumberFormat("Phone number format not acceptable"))
    }
  }
}

case class SurveyPhoneNumberFormatUpdateDTO(@JsonDeserialize(contentAs = classOf[java.lang.Long]) id: Option[Long], format: String, direction: Int)

case class SurveyAssociationUpdateDTO(@JsonDeserialize(contentAs = classOf[java.lang.Long]) idsAdded: List[Long], @JsonDeserialize(contentAs = classOf[java.lang.Long]) idsRemoved: List[Long])

case class SurveyDetailsDTO(settings: SurveySettingDTO, allowedPhNumFormats: Seq[SurveyPhoneNumberFormatDTO], excludedPhNumFormats: Seq[SurveyPhoneNumberFormatDTO], taskQMapping: Seq[SurveyTaskQMappingDTO],
                           orgMapping: Seq[SurveyOrgMappingDTO])

case class SurveyPhoneNumberFormatDTO(id: Option[Long], format: String)

case class SurveyTaskQMappingDTO(id: Long, taskQName: String)

case class SurveyOrgMappingDTO(id: Option[Long], org: SurveyOrganisationDTO)

case class SurveyOrganisationDTO(name: String, level: String, parent: Option[SurveyOrganisationDTO])

case class OfferedSurveyCallsDTO(records: Seq[SurveyCallRecordVO])

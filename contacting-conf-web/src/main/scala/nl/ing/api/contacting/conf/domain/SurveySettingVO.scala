package nl.ing.api.contacting.conf.domain

import nl.ing.api.contacting.conf.domain.types.ContactingTypes._
/**
 * @author Ayush Mittal
 */

/**
 *
 * @param id
 * @param accountId
 * @param name
 * @param channel
 * @param channelDirection
 * @param voiceSurveyId
 * @param callflowName
 * @param minFrequency : min frequency in days
 * @param delay : delay in seconds
 * @param surveyOfferRatio : offer ratio percentage
 * @param minContactLength : contact length in seconds
 * @param surveyForTransfers
 */
case class SurveySettingVO(id: Option[DatabaseId],
                           accountId: DatabaseId,
                           name: FriendlyName,
                           channel: String,
                           channelDirection: String,
                           voiceSurveyId: String,
                           callflowName: Option[String],
                           minFrequency: Option[NumberOfDays],
                           delay: Option[Seconds],
                           surveyOfferRatio: Option[Percentage],
                           minContactLength: Option[Seconds],
                           surveyForTransfers: Boolean)

package nl.ing.api.contacting.conf.repository.model

/**
 * @author Ayush Mittal
 */
case class SurveySetting(id: Option[Long],
                         accountId: Long,
                         name: String,
                         channel: String,
                         channelDirection: String,
                         voiceSurveyId: String,
                         callflowName: Option[String],
                         minFrequency: Option[Int],
                         delay: Option[Long],
                         surveyOfferRatio: Option[Float],
                         minContactLength: Option[Long],
                         surveyForTransfers: Boolean)

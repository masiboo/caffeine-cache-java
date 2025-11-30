package nl.ing.api.contacting.conf.repository.model

import nl.ing.api.contacting.conf.repository.SurveyOrgRepository.SurveyOrgDetails
import nl.ing.api.contacting.conf.repository.SurveyTaskQRepository.SurveyTaskQDetails

/**
 * @author Ayush Mittal
 */
case class SurveyDetails(setting: SurveySetting, phNumFormat: Seq[SurveyPhoneNumberFormat], taskQMapping: Seq[SurveyTaskQDetails], orgMapping: Seq[SurveyOrgDetails])

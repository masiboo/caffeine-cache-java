package nl.ing.api.contacting.conf.domain

import java.time.Instant


case class SurveyCallRecordVO(account_friendly_name: String, phone_num: String, survey_name: String, offered_datetime: Instant)


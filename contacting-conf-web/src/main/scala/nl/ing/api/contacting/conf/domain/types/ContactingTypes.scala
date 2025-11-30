package nl.ing.api.contacting.conf.domain.types

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.MatchesRegex

object ContactingTypes {

  type DatabaseId = Refined[Long, Positive]

  type NumberOfDays = Refined[Int, Positive]

  type Seconds = Refined[Long, Positive]

  type FriendlyNameRegex = MatchesRegex[W.`"[0-9a-zA-Z_ ]*"`.T]

  type FriendlyName = String Refined FriendlyNameRegex

  type Percentage = Refined[Float, Positive]

  type SurveyPhNumDirectionRegex = MatchesRegex[W.`"allowed|excluded"`.T]

  type SurveyPhNumDirection = String Refined SurveyPhNumDirectionRegex
}

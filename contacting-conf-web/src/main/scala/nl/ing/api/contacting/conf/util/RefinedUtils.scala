package nl.ing.api.contacting.conf.util

import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.DatabaseId
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.FriendlyName
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.FriendlyNameRegex
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.NumberOfDays
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.Percentage
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.Seconds
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.SurveyPhNumDirection
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.SurveyPhNumDirectionRegex
import nl.ing.api.contacting.conf.exception.RefinedTypeError

object RefinedUtils {

  implicit class refineLong(elem: Long) {
    def toDatabaseId: Either[RefinedTypeError, DatabaseId] = {
      refineV[Positive](elem).left.map(RefinedTypeError)
    }

    def toSeconds: Either[RefinedTypeError, Seconds] = {
      refineV[Positive](elem).left.map(RefinedTypeError)
    }
  }

  implicit class refineInt(elem: Int) {
    def toNumberOfDays: Either[RefinedTypeError, NumberOfDays] = {
      refineV[Positive](elem).left.map(RefinedTypeError)
    }
  }

  implicit class refineDouble(elem: Float) {
    def toPercentage: Either[RefinedTypeError, Percentage] = {
      refineV[Positive](elem).left.map(RefinedTypeError)
    }
  }

  implicit class refineString(elem: String) {
    def toFriendlyName: Either[RefinedTypeError, FriendlyName] = {
      refineV[FriendlyNameRegex](elem).left.map(RefinedTypeError)
    }

    def toSurveyPhNumDirection: Either[RefinedTypeError, SurveyPhNumDirection] = {
      refineV[SurveyPhNumDirectionRegex](elem).left.map(RefinedTypeError)
    }
  }
}

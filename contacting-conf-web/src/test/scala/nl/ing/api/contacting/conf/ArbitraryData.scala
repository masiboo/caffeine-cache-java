package nl.ing.api.contacting.conf

import nl.ing.api.contacting.conf.repository.model.OrganisationSettingModel
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

/**
 * @author Ayush Mittal
 */
trait ArbitraryData {

  implicit val organisationSettingValues: Arbitrary[OrganisationSettingModel] = Arbitrary[OrganisationSettingModel] {
    for {
      key <- Gen.alphaNumStr.suchThat(_.nonEmpty)
      value <- Gen.alphaNumStr.suchThat(_.nonEmpty)
      enabledOrNot <- Gen.oneOf(true, false)
      orgId <- Gen.chooseNum(1, 100)
      capability <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
    } yield OrganisationSettingModel(None, key, value, 1, orgId, enabledOrNot,capability)
  }
}

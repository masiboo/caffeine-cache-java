package nl.ing.api.contacting.conf.resource.dto

/**
 * @author Ayush M
 */
case class RegionDTO(name: String) extends AnyVal

case class Regions(regions: List[RegionDTO])

object ExistingTwilioRegions {

  val ireland = RegionDTO("ie1")
  val australia = RegionDTO("au1")
  val us = RegionDTO("us1")

  val allRegions: Regions = Regions(List(ireland,australia,us))
}

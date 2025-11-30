package nl.ing.api.contacting.conf.mapper

import com.ing.api.contacting.dto.resource.organisation.OrganisationDto
import com.ing.api.contacting.dto.resource.organisation.OrganisationSaveDto
import nl.ing.api.contacting.domain.slick.OrganisationVO
import nl.ing.api.contacting.repository.model.OrganisationLevelEnumeration
import nl.ing.api.contacting.repository.model.OrganisationModel
import nl.ing.api.contacting.repository.organisation.OrganisationRepository.Circle
import nl.ing.api.contacting.repository.organisation.OrganisationRepository.SuperCircle
import nl.ing.api.contacting.repository.organisation.OrganisationRepository.Team

/**
 * @author Ayush Mittal
 */
object OrganisationMapper {

  private case class FlattenOrganisation(superCircle: SuperCircle, circle: Option[Circle], team: Option[Team])

  def toDto(organisationVO: OrganisationVO): OrganisationDto = {
    OrganisationDto(organisationVO.id.getOrElse(0L),
      organisationVO.name,
      organisationVO.organisationLevel.id,
      organisationVO.parent.map(toDto),
      organisationVO.children.map(toDto))
  }

  def modelToVO(organisationModel: OrganisationModel): OrganisationVO = {
    OrganisationVO(organisationModel.id,
                   organisationModel.name,
                   organisationModel.organisationLevel,
                   None)
  }

  def organisationModelTreeToVo(tree: Seq[((SuperCircle, Option[Circle]), Option[Team])]): Seq[OrganisationVO] = {
    val allOrganisations = toFlattenedOrg(tree)
    allOrganisations.groupBy(_.superCircle.id).map {
      sc =>
        val superCircle = sc._2.head.superCircle
        val circles: Set[Circle] = sc._2.flatMap(_.circle).toSet
        val circlesVO = circles.map {
          circle =>
            val teams: Set[Team] = sc._2.flatMap(_.team)
              .filter(_.parentId == circle.id).toSet
            OrganisationVO(circle.id,
                           circle.name,
                           circle.organisationLevel,
                           None,
                           teams.map(modelToVO).toList.sortBy(_.name))
        }
        OrganisationVO(superCircle.id,
                       superCircle.name,
                       superCircle.organisationLevel,
                       None,
                       circlesVO.toList.sortBy(_.name))
    }.toSeq.sortBy(_.name)
  }


  private def toFlattenedOrg(tree: Seq[((SuperCircle, Option[Circle]), Option[Team])]) =
    tree.map { r => FlattenOrganisation(r._1._1, r._1._2, r._2) }

  def saveDtoToModel(organisationSaveDto: OrganisationSaveDto, accountId: Long): OrganisationModel = {
    OrganisationModel(id = organisationSaveDto.id,
                      name = organisationSaveDto.name,
                      accountId = accountId,
                      parentId = organisationSaveDto.parentId,
                      organisationLevel = OrganisationLevelEnumeration.apply(organisationSaveDto.level))
  }
}

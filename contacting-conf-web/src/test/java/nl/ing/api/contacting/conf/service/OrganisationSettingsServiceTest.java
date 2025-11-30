package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.organisation.settings.EmployeeOrganisationSettings;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSetting;
import com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto;
import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.mapper.OrganisationSettingMapperJava;
import nl.ing.api.contacting.conf.repository.OrganisationSettingsAuditRepository;
import nl.ing.api.contacting.conf.repository.OrganisationSettingsJpaRepository;
import nl.ing.api.contacting.trust.rest.feature.permissions.OrganisationalRestrictionLevel;
import nl.ing.api.contacting.trust.rest.feature.permissions.RestrictionOrganisationalUnitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static nl.ing.api.contacting.conf.helper.OrganisationSettingTestData.getFlatOrganisationUnitDto;
import static nl.ing.api.contacting.conf.helper.OrganisationSettingTestData.getOrganisationSettingsEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganisationSettingsServiceTest {

    private OrganisationSettingsJpaRepository organisationSettingsRepository;
    private OrganisationSettingsAuditRepository organisationAuditRepository;
    private SettingsMetadataServiceJava settingsMetadataService;
    private OrganisationSettingsServiceJava organisationSettingsService;
    private ContactingContext context;

    @BeforeEach
    void setUp() {
        organisationAuditRepository = mock(OrganisationSettingsAuditRepository.class);
        settingsMetadataService = mock(SettingsMetadataServiceJava.class);
        organisationSettingsService = mock(OrganisationSettingsServiceJava.class);
        organisationSettingsRepository = mock(OrganisationSettingsJpaRepository.class);

        context = new ContactingContext(1L, null);
        organisationSettingsService = new OrganisationSettingsServiceJava(
                organisationAuditRepository,
                settingsMetadataService,
                organisationSettingsRepository
        );
    }

    @Nested
    @DisplayName("getOrganisationSettings")
    class GetOrganisationSettings {
        private Set<SettingCapability> capabilities;

        @BeforeEach
        void setup() {
            capabilities = Set.of(SettingCapability.CHAT, SettingCapability.VIDEO);
        }

        @Test
        @DisplayName("should return settings filtered by capabilities")
        void shouldReturnSettingsFilteredByCapabilities() {

            OrganisationSettingsEntity entity = getOrganisationSettingsEntity();

            when(organisationAuditRepository.findByCapabilities(capabilities, context.accountId()))
                    .thenReturn(List.of(entity));

            List<OrganisationSettingVO> result = organisationSettingsService.getOrganisationSettings(capabilities, context);

            assertEquals(1, result.size());
            assertEquals("TIMEZONE", result.get(0).key());
        }

        @Test
        @DisplayName("should return all settings for context")
        void shouldReturnAllSettingsForContext() {
            OrganisationSettingsEntity entity = getOrganisationSettingsEntity();
            when(organisationAuditRepository.findAll(context)).thenReturn(List.of(entity));

            List<OrganisationSettingVO> result = organisationSettingsService.getOrganisationSettings(context);

            assertEquals(1, result.size());
            assertEquals("TIMEZONE", result.get(0).key());
        }
    }

    @Nested
    @DisplayName("getSettingsForOrganisation")
    class GetSettingsForOrganisation {

        @Test
        @DisplayName("should return settings for organisation hierarchy")
        void shouldReturnSettingsForOrganisationHierarchy() {
            List<OrganisationSettingVO> allSettings = List.of(
                    new OrganisationSettingVO(
                            Optional.of(1L),
                            "TEST_KEY",
                            "test value",
                            4L,
                            1L,
                            false,
                            List.of(SettingCapability.CHAT)),
                    new OrganisationSettingVO(
                            Optional.of(2L),
                            "TEST_KEY",
                            "test value",
                            3L,
                            1L,
                            false,
                            List.of(SettingCapability.CHAT)),
                    new OrganisationSettingVO(
                            Optional.of(3L),
                            "TEST_KEY",
                            "test value",
                            1L,
                            1L,
                            false,
                            List.of(SettingCapability.CHAT))

            );

            com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto orgUnit = getFlatOrganisationUnitDto();

            List<OrganisationSettingVO> result =
                    OrganisationSettingsServiceJava.getSettingsForOrganisation(allSettings, orgUnit);

            assertEquals(3, result.size());
        }
    }

    @Nested
    @DisplayName("deleteOrganisationSetting")
    class DeleteOrganisationSetting {

        @Test
        @DisplayName("should delete existing setting")
        void shouldDeleteExistingSetting() {
            Long id = 1L;
            OrganisationSettingsEntity entity = getOrganisationSettingsEntity();

            when(organisationAuditRepository.findById(id, context)).thenReturn(Optional.of(entity));
            doNothing().when(organisationAuditRepository).deleteById(id);

            assertDoesNotThrow(() -> organisationSettingsService.deleteOrganisationSetting(id, context));
            verify(organisationAuditRepository).deleteById(id);
        }
    }
    @Test
    @DisplayName("return org settings for employee when no account settings are present")
    void getOrganisationSettingsForEmployee() {
        // Arrange
        com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto unitTeam1 = new FlatOrganisationUnitDto(1, "clt", 2, "circle", 3, "supercircle");
          scala.collection.immutable.Set<RestrictionOrganisationalUnitDto> restrictionSet =
                scala.collection.JavaConverters.asScalaBuffer(
                        List.of(new RestrictionOrganisationalUnitDto(
                                new FlatOrganisationUnitDto(1, "clt", 2, "circle", 3, "supercircle"),
                                "SUPERVISOR",
                                OrganisationalRestrictionLevel.fromLevel(5)
                        ))
                ).toSet();

        OrganisationSettingVO outboundSettingForTeam1 = new OrganisationSettingVO(Optional.of(1L), "outbound_allowed", "WK123456", 1L, 4L, true, List.of());

        when(organisationAuditRepository.findAll(context)).thenReturn(List.of(
                OrganisationSettingMapperJava.toEntity(outboundSettingForTeam1)
        ));

        // Act
        EmployeeOrganisationSettings result = organisationSettingsService.getOrganisationSettingsForEmployee(
                context,
                restrictionSet,
                scala.Option.apply(unitTeam1)
        );

        // Assert
        assertEquals(1, result.orgSettings().size());
        var orgSettingsForTeam1 = result.orgSettings().stream().filter(s -> s.orgId() == 1L).findFirst().orElseThrow();
        var settingsForTeam1 = orgSettingsForTeam1.settings();
        assertEquals(1, settingsForTeam1.size());
        assertTrue(settingsForTeam1.contains(new OrganisationSetting("outbound_allowed", true, "WK123456")));

        var orgSettingsForTeam2 = result.orgSettings().stream().filter(s -> s.orgId() == 1L).findFirst().orElseThrow();
        var settingsForTeam2 = orgSettingsForTeam2.settings();
        assertEquals(1, settingsForTeam2.size());

    }
}
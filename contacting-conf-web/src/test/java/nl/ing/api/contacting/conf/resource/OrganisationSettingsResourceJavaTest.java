package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDto;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.helper.OrganisationSettingTestData;
import nl.ing.api.contacting.conf.service.OrganisationSettingsServiceJava;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationSettingsResourceJavaTest {

    @Mock
    private OrganisationSettingsServiceJava mockService;

    private OrganisationSettingsResourceJava resource;

    @BeforeEach
    void setUp() {
        resource = new TestableOrganisationSettingsResource(mockService);
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all organisation settings successfully")
        void shouldReturnAllOrganisationSettings() throws Exception {
            Set<SettingCapability> capabilities = Set.of(SettingCapability.CHAT);
            OrganisationSettingVO createdSetting = new OrganisationSettingVO(
                    Optional.of(2L), "TEST_KEY", "test value", 2L, 2L, false, List.of(SettingCapability.CHAT));

            when(mockService.getOrganisationSettingsWithCapabilities(eq(SettingCapability.CHAT.value()), any(ContactingContext.class)))
                    .thenReturn(List.of(createdSetting));

            CompletableFuture<Response> responseFuture = resource.getAllOrgSettings(SettingCapability.CHAT.value());

            Response response = responseFuture.get();

            assertEquals(200, response.getStatus());

            Map<String, Object> responseMap = (Map<String, Object>) response.getEntity();
            List<OrganisationSettingVO> returnedSettings = (List<OrganisationSettingVO>) responseMap.get("data");
            assertEquals(1, returnedSettings.size());
            assertEquals("TEST_KEY", returnedSettings.get(0).key());
        }

        @Test
        @DisplayName("should return empty list when no settings exist")
        void shouldReturnEmptyList() throws Exception {

            when(mockService.getOrganisationSettingsWithCapabilities(eq(SettingCapability.CHAT.value()), any(ContactingContext.class)))
                    .thenReturn(List.of());

            CompletableFuture<Response> responseFuture = resource.getAllOrgSettings(SettingCapability.CHAT.value());
            Response response = responseFuture.get();

            assertEquals(200, response.getStatus());

        }

        @Test
        @DisplayName("create an organisation setting")
        void shouldCreateOrganisationSetting() throws Exception {
            OrganisationSettingDto dto = OrganisationSettingTestData.getOrganisationSettingDto();
            OrganisationSettingVO savedOrgSetting =
                    new OrganisationSettingVO(
                            Optional.of(1L), "TEST_KEY", "value1",
                            1L, 1, true, List.of());

            when(mockService.createOrganisationSetting(eq(dto), any(ContactingContext.class)))
                    .thenReturn(savedOrgSetting);

            CompletableFuture<Response> responseFuture = resource.saveOrganisationSetting(dto);
            Response response = responseFuture.get();
            OrganisationSettingVO result = (OrganisationSettingVO) response.getEntity();
            assertEquals(201, response.getStatus());
            assertEquals(dto.key(), result.key());
        }

        @Test
        @DisplayName("update an organisation setting")
        void shouldUpdateOrganisationSetting() throws Exception {

            OrganisationSettingDto dto = OrganisationSettingTestData.getOrganisationSettingDto();

            OrganisationSettingVO expectedVO = new OrganisationSettingVO(
                    Optional.of(2L), "TEST_KEY", "test value", 2L, 2L, false, List.of(SettingCapability.CHAT));
            when(mockService.updateOrganisationSetting(eq(dto), eq(1L), any(ContactingContext.class)))
                    .thenReturn(expectedVO);

            CompletableFuture<Response> responseFuture = resource.updateOrganisationSetting(1L, dto);
            Response response = responseFuture.get();

            assertEquals(200, response.getStatus());
            OrganisationSettingVO result = (OrganisationSettingVO) response.getEntity();
            assertEquals(expectedVO.key(), result.key());
        }

        @Test
        @DisplayName("return the failure when the update fails")
        void shouldReturnFailureWhenUpdateFails() throws Exception {
            OrganisationSettingDto dto = OrganisationSettingTestData.getOrganisationSettingDto();

            when(mockService.updateOrganisationSetting(eq(dto), eq(1L), any(ContactingContext.class)))
                    .thenThrow(Errors.notFound("Requested key is not present in settings metadata"));

            CompletableFuture<Response> responseFuture = resource.updateOrganisationSetting(1L, dto);
            Response response = responseFuture.get();

            assertEquals(404, response.getStatus());

            assertTrue(response.getEntity().toString().contains("Requested key is not present in settings metadata"));
        }

    }

    @Test
    @DisplayName("should delete organisation setting and return no content")
    void shouldDeleteOrganisationSettingAndReturnNoContent() {
        Long id = 123L;

        // No need to stub deleteOrganisationSetting as it returns void
        Response response = resource.deleteOrganisationSetting(id);

        assertEquals(204, response.getStatus());
    }

    static class TestableOrganisationSettingsResource extends OrganisationSettingsResourceJava {
        TestableOrganisationSettingsResource(OrganisationSettingsServiceJava service) {
            super(service);
        }

        @Override
        protected ContactingContext getContactingContext() {
            return new ContactingContext(101L, null);
        }
    }
}

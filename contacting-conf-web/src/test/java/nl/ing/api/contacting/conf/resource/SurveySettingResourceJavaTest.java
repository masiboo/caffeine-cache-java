package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyDetailsVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveySettingVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.*;
import nl.ing.api.contacting.conf.helper.SurveySettingTestData;
import nl.ing.api.contacting.conf.service.SurveyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SurveySettingResourceJavaTest {

    @Mock
    private SurveyService mockService;

    private SurveySettingResourceJava resource;

    @BeforeEach
    void setUp() {
        resource = new TestableSurveySettingResourceJava(mockService);
    }

    @Nested
    @DisplayName("getSurveySettingsForAccount")
    class GetSurveySettingsForAccount {
        @Test
        @DisplayName("should return 200 with survey list")
        void shouldReturnSurveyList() throws Exception {
            List<SurveyOverviewDTO> mockOverviewList = SurveySettingTestData.mockSurveyOverviewList();
            when(mockService.getAllSurveySettings(any())).thenReturn(mockOverviewList);

            CompletableFuture<Response> responseFuture = resource.getSurveySettingsForAccount();
            Response response = responseFuture.get();

            assertEquals(200, response.getStatus());
            assertNotNull(response.getEntity());
            assertInstanceOf(AllSurveyOverviewDTO.class, response.getEntity());
            verify(mockService).getAllSurveySettings(any());
        }
    }

    @Nested
    @DisplayName("getSurveySettingById")
    class GetSurveySettingById {
        @Test
        @DisplayName("should return 200 with survey details")
        void shouldReturnSurveyDetails() throws Exception {
            SurveyDetailsVO mockDetailsVO = SurveySettingTestData.surveyDetailsVO();
            when(mockService.getSurveyDetailsVO(anyLong(), any()))
                   .thenReturn(CompletableFuture.completedFuture(Optional.of(mockDetailsVO)));

            CompletableFuture<Response> responseFuture = resource.getSurveySettingById(1L);
            Response response = responseFuture.get();

            assertEquals(200, response.getStatus());
            assertNotNull(response.getEntity());
            verify(mockService).getSurveyDetailsVO(eq(1L), any());
        }

        @Test
        @DisplayName("should return 404 when survey not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(mockService.getSurveyDetailsVO(anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

            CompletableFuture<Response> responseFuture = resource.getSurveySettingById(12211L);
            Response response = responseFuture.get();
            assertEquals(404, response.getStatus());
            verify(mockService).getSurveyDetailsVO(eq(12211L), any());
        }

        @Test
        @DisplayName("should throw exception for invalid ID")
        void shouldThrowExceptionForInvalidId() {
            assertThrows(RuntimeException.class, () ->
                    resource.getSurveySettingById(-1L));
       }
    }

    @Nested
   @DisplayName("createSurveySetting")
    class CreateSurveySetting {
        @Test
        @DisplayName("should return 201 with location header")
        void shouldCreateSurveySettingAndReturnLocation() throws Exception {
            SurveySettingDTO dto = SurveySettingTestData.surveySettingDTO();
            SurveySettingVO mockVO = SurveySettingTestData.surveySettingVO();
            when(mockService.createSurveySetting(any(SurveySettingVO.class)))
                    .thenReturn(mockVO);

            CompletableFuture<Response> responseFuture = resource.createSurveySetting(dto);
            Response response = responseFuture.get();

            assertEquals(201, response.getStatus());
            assertNotNull(response.getLocation());
           verify(mockService).createSurveySetting(any(SurveySettingVO.class));
        }
        @Test
        @DisplayName("should throw exception when callflow missing for call channel")
        void shouldThrowExceptionWhenCallflowMissing() {
            SurveySettingDTO dto = SurveySettingTestData.surveySettingDTOWithoutCallflow();

           assertThrows(RuntimeException.class, () ->
                    resource.createSurveySetting(dto));
        }

        @Test
        @DisplayName("should throw exception for invalid callflow name")
        void shouldThrowExceptionForInvalidName() {
            SurveySettingDTO dto = SurveySettingTestData.surveySettingDTOWithInvalidName();

            assertThrows(RuntimeException.class, () ->
                    resource.createSurveySetting(dto));
        }
    }

    @Nested
    @DisplayName("updateSurveySetting")
    class UpdateSurveySetting {
        @Test
        @DisplayName("should return 200 with updated survey")
        void shouldUpdateSurveySettingSuccessfully() throws Exception {
            SurveyUpdateDTO dto = SurveySettingTestData.surveyUpdateDTO();
            SurveySettingVO mockVO = SurveySettingTestData.surveySettingVO();
            when(mockService.updateSurveySetting(any(), any()))
                    .thenReturn(mockVO);

            CompletableFuture<Response> responseFuture = resource.updateSurveySetting(1L, dto);
            Response response = responseFuture.get();

           assertEquals(200, response.getStatus());
            assertNotNull(response.getEntity());
            verify(mockService).updateSurveySetting(any(), any());
        }
        @Test
        @DisplayName("should throw exception for invalid phone format")
        void shouldThrowExceptionForInvalidPhoneFormat() {
            SurveyUpdateDTO dto = SurveySettingTestData.surveyUpdateDTOWithInvalidPhone();

           assertThrows(RuntimeException.class, () ->
                    resource.updateSurveySetting(1L, dto));
        }

       @Test
        @DisplayName("should throw exception for invalid ID")
        void shouldThrowExceptionForInvalidId() {
            SurveyUpdateDTO dto = SurveySettingTestData.surveyUpdateDTO();

            assertThrows(RuntimeException.class, () ->
                    resource.updateSurveySetting(-1L, dto));
        }
    }

    @Nested
    @DisplayName("updateSurveySettingOrgs")
    class UpdateSurveySettingOrgs {
        @Test
       @DisplayName("should return 204 for successful update")
        void shouldUpdateOrgsSuccessfully() throws Exception {
            SurveyAssociationUpdateDTO dto = SurveySettingTestData.associationUpdateDTO();
            doNothing().when(mockService).addRemoveOrgs(anyLong(), any());

            CompletableFuture<Response> responseFuture = resource.updateSurveySettingOrgs(1L, dto);
            Response response = responseFuture.get();

            assertEquals(204, response.getStatus());
            verify(mockService).addRemoveOrgs(eq(1L), eq(dto));
        }

        @Test
        @DisplayName("should handle empty associations")
        void shouldHandleEmptyAssociations() throws Exception {
            SurveyAssociationUpdateDTO dto = SurveySettingTestData.emptyAssociationUpdateDTO();
            doNothing().when(mockService).addRemoveOrgs(anyLong(), any());
            CompletableFuture<Response> responseFuture = resource.updateSurveySettingOrgs(1L, dto);
            Response response = responseFuture.get();

            assertEquals(204, response.getStatus());
            verify(mockService).addRemoveOrgs(eq(1L), eq(dto));
        }

        @Test
        @DisplayName("should throw exception for invalid ID")
        void shouldThrowExceptionForInvalidId() {
            SurveyAssociationUpdateDTO dto = SurveySettingTestData.associationUpdateDTO();

            assertThrows(RuntimeException.class, () ->
                    resource.updateSurveySettingOrgs(-1L, dto));
        }
    }

    @Nested
   @DisplayName("updateSurveySettingTaskQueues")
    class UpdateSurveySettingTaskQueues {
        @Test
       @DisplayName("should return 204 for successful update")
        void shouldUpdateTaskQueuesSuccessfully() throws Exception {
            SurveyAssociationUpdateDTO dto = SurveySettingTestData.associationUpdateDTO();
            doNothing().when(mockService).addRemoveTaskQueues(anyLong(), any());

            CompletableFuture<Response> responseFuture = resource.updateSurveySettingTaskQueues(1L, dto);
            Response response = responseFuture.get();

            assertEquals(204, response.getStatus());
            verify(mockService).addRemoveTaskQueues(eq(1L), eq(dto));
        }

        @Test
        @DisplayName("should throw exception for invalid ID")
        void shouldThrowExceptionForInvalidId() {
            SurveyAssociationUpdateDTO dto = SurveySettingTestData.associationUpdateDTO();

           assertThrows(RuntimeException.class, () ->
                    resource.updateSurveySettingTaskQueues(-1L, dto));
        }
    }

   @Nested
    @DisplayName("deleteSurveySetting")
    class DeleteSurveySetting {
        @Test
        @DisplayName("should return 204 for successful deletion")
        void shouldDeleteSurveySettingSuccessfully() throws Exception {
            doNothing().when(mockService).deleteSurveySetting(anyLong(), any());
            CompletableFuture<Response> responseFuture = resource.deleteSurveySetting(1L);
            Response response = responseFuture.get();

            assertEquals(204, response.getStatus());
            verify(mockService).deleteSurveySetting(eq(1L), any());
        }

        @Test
        @DisplayName("should throw exception for invalid ID")
        void shouldThrowExceptionForInvalidId() {
            assertThrows(RuntimeException.class, () ->
                    resource.deleteSurveySetting(-1L));
        }

        @Test
        @DisplayName("should handle service exceptions")
        void shouldHandleServiceExceptions() throws Exception {
            doThrow(new RuntimeException("Database error"))
                    .when(mockService).deleteSurveySetting(anyLong(), any());

            CompletableFuture<Response> responseFuture = resource.deleteSurveySetting(1L);
            Response response = responseFuture.get();

            assertEquals(500, response.getStatus());
            verify(mockService).deleteSurveySetting(eq(1L), any());
        }


    }

    static class TestableSurveySettingResourceJava extends SurveySettingResourceJava {
        TestableSurveySettingResourceJava(SurveyService service) {
            super(service);
        }

        @Override
        protected ContactingContext getContactingContext() {
            return new ContactingContext(101L, null);
        }
    }
}

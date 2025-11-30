package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.conf.domain.entity.SurveyOrgMappingEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveyPhoneNumberFormatEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveyTaskQueueMappingEntity;
import nl.ing.api.contacting.conf.domain.model.surveysetting.*;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyAssociationUpdateDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyOverviewDTO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.repository.*;
import nl.ing.api.contacting.conf.repository.cassandra.SurveyCallRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveySettingJpaRepository surveySettingRepo;

    @Mock
    private SurveyPhoneNumberFormatJpaRepository surveyPhNumberFormatRepo;

    @Mock
    private SurveyTaskQueueJpaRepository surveyTaskQRepository;

    @Mock
    private SurveyOrgMappingJpaRepository surveyOrgRepository;

    @Mock
    private SurveyTaskQueueMappingJpaRepository surveyTaskQMappingRepository;

    @InjectMocks
    private SurveyService surveyService;

    private ContactingContext contactingContext;
    private SurveySettingsEntity surveyEntity;
    private SurveySettingVO surveySettingVO;

    @BeforeEach
    void setUp() {
        contactingContext = new ContactingContext(11L, null);
        
        surveyEntity = new SurveySettingsEntity();
        surveyEntity.setId(11L);
        surveyEntity.setAccountId(11L);
        surveyEntity.setName("Test Survey");
        surveyEntity.setChannel("call");
        surveyEntity.setChannelDirection("in");
        surveyEntity.setVoiceSurveyId("voiceSurveyId");
        surveyEntity.setSurveyForTransfers(true);

        surveySettingVO = new SurveySettingVO(
           Optional.of( 11L), 11L, "Test Survey", "call", "in", "voiceSurveyId",
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), true
        );
    }

    @Test
    @DisplayName("getSurveyDetailsVO - should return survey details VO when survey exists")
    void getSurveyDetailsVO_ShouldReturnSurveyDetailsVO_WhenSurveyExists() {
        // Given
        Long surveyId = 11L;
        List<SurveyPhoneNumberFormatEntity> phoneFormats = List.of();
        List<SurveyTaskQMappingWithName> taskQueues = List.of();
        List<SurveyOrgDetails> orgs = List.of();

        when(surveySettingRepo.findByIdAndAccountId(surveyId, contactingContext.accountId()))
            .thenReturn(Optional.of(surveyEntity));
        when(surveyPhNumberFormatRepo.findBySurveyId(surveyId)).thenReturn(phoneFormats);
        when(surveyTaskQRepository.findTaskQueuesBySurveyId(surveyId)).thenReturn(taskQueues);
        when(surveyOrgRepository.findBySurveyIdWithHierarchy(surveyId)).thenReturn(orgs);

        // When
        CompletableFuture<Optional<SurveyDetailsVO>> result = 
            surveyService.getSurveyDetailsVO(surveyId, contactingContext);

        // Then
        assertNotNull(result);
        assertTrue(result.join().isPresent());
        assertEquals(Optional.of(surveyId), result.join().get().surveySettingVO().id());
    }

    @Test
    @DisplayName("getSurveyDetailsVO - should return empty optional when survey does not exist")
    void getSurveyDetailsVO_ShouldReturnEmpty_WhenSurveyDoesNotExist() {
        // Given
        Long surveyId = 999L;
        when(surveySettingRepo.findByIdAndAccountId(surveyId, contactingContext.accountId()))
            .thenReturn(Optional.empty());

        // When
        CompletableFuture<Optional<SurveyDetailsVO>> result = 
            surveyService.getSurveyDetailsVO(surveyId, contactingContext);

        // Then
        assertTrue(result.join().isEmpty());
    }

    @Test
    @DisplayName("getSurveyDetails - should return survey details when survey exists")
    void getSurveyDetails_ShouldReturnSurveyDetails_WhenSurveyExists() {
        // Given
        Long surveyId = 11L;
        Long accountId = 11L;
        List<SurveyPhoneNumberFormatEntity> phoneFormats = List.of();
        List<SurveyTaskQMappingWithName> taskQueues = List.of();
        List<SurveyOrgDetails> orgs = List.of();

        when(surveySettingRepo.findByIdAndAccountId(surveyId, accountId))
            .thenReturn(Optional.of(surveyEntity));
        when(surveyPhNumberFormatRepo.findBySurveyId(surveyId)).thenReturn(phoneFormats);
        when(surveyTaskQRepository.findTaskQueuesBySurveyId(surveyId)).thenReturn(taskQueues);
        when(surveyOrgRepository.findBySurveyIdWithHierarchy(surveyId)).thenReturn(orgs);

        // When
        CompletableFuture<Optional<SurveyDetails>> result = 
            surveyService.getSurveyDetails(surveyId, accountId);

        // Then
        assertNotNull(result);
        assertTrue(result.join().isPresent());
        assertEquals(surveyEntity, result.join().get().setting());
    }

    @Test
    @DisplayName("getAllSurveySettings - should return all survey settings for account")
    void getAllSurveySettings_ShouldReturnAllSurveySettings() {
        // Given
        List<SurveySettingsEntity> entities = List.of(surveyEntity);
        when(surveySettingRepo.findByAccountId(contactingContext.accountId())).thenReturn(entities);

        // When
        List<SurveyOverviewDTO> result = surveyService.getAllSurveySettings(contactingContext);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(surveySettingRepo).findByAccountId(contactingContext.accountId());
    }

    @Test
    @DisplayName("createSurveySetting - should create new survey setting")
    void createSurveySetting_ShouldCreateNewSurveySetting() {
        // Given
        SurveySettingVO voToCreate = new SurveySettingVO(
            Optional.empty(), 11L, "New Survey", "call", "in", "voiceSurveyId",
            Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), true
        );

        SurveySettingsEntity savedEntity = new SurveySettingsEntity();
        savedEntity.setId(12L);
        savedEntity.setAccountId(11L);
        savedEntity.setName("New Survey");
        savedEntity.setChannel("call");
        savedEntity.setChannelDirection("in");
        savedEntity.setVoiceSurveyId("voiceSurveyId");
        savedEntity.setSurveyForTransfers(true);

        when(surveySettingRepo.save(any(SurveySettingsEntity.class))).thenReturn(savedEntity);

        // When
        SurveySettingVO result = surveyService.createSurveySetting(voToCreate);

        // Then
        assertNotNull(result);
        assertEquals(Optional.of(12L), result.id());
        assertEquals("New Survey", result.name());
        verify(surveySettingRepo).save(any(SurveySettingsEntity.class));
    }

    @Test
    @DisplayName("updateSurveySetting - should update existing survey setting")
    void updateSurveySetting_ShouldUpdateExistingSurveySetting() {
        // Given
        SurveyPhoneNumberFormatVO addFormat1 = new SurveyPhoneNumberFormatVO(
            Optional.empty(), 11L, "+9*", "allowed"
        );
        SurveyPhoneNumberFormatVO removeFormat1 = new SurveyPhoneNumberFormatVO(
            Optional.of(11L), 11L, "+31", "allowed"
        );

        SurveyUpdateVO updateVO = new SurveyUpdateVO(
            surveySettingVO,
            List.of(addFormat1),
            List.of(removeFormat1)
        );

        when(surveySettingRepo.findByIdAndAccountId(11L, contactingContext.accountId()))
            .thenReturn(Optional.of(surveyEntity));
        when(surveySettingRepo.save(any(SurveySettingsEntity.class))).thenReturn(surveyEntity);
        when(surveyPhNumberFormatRepo.saveAll(anyList())).thenReturn(List.of());

        // When
        SurveySettingVO result = surveyService.updateSurveySetting(updateVO, contactingContext);

        // Then
        assertNotNull(result);
        verify(surveySettingRepo).findByIdAndAccountId(11L, contactingContext.accountId());
        verify(surveySettingRepo).save(any(SurveySettingsEntity.class));
        verify(surveyPhNumberFormatRepo).saveAll(anyList());
        verify(surveyPhNumberFormatRepo).deleteByIds(List.of(11L));
    }

    @Test
    @DisplayName("updateSurveySetting - should throw error when survey not found")
    void updateSurveySetting_ShouldThrowError_WhenSurveyNotFound() {
        // Given
        SurveyUpdateVO updateVO = new SurveyUpdateVO(surveySettingVO, List.of(), List.of());
        
        when(surveySettingRepo.findByIdAndAccountId(11L, contactingContext.accountId()))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            surveyService.updateSurveySetting(updateVO, contactingContext)
        );
        verify(surveySettingRepo).findByIdAndAccountId(11L, contactingContext.accountId());
        verifyNoMoreInteractions(surveySettingRepo);
    }

    @Test
    @DisplayName("pass validation for valid phone format types")
    void passValidationForValidPhoneFormatTypes() {
        // Given
        SurveyPhoneNumberFormatVO addFormat1 = new SurveyPhoneNumberFormatVO(
            Optional.of(11L), 11L, "+9*", "allowed"
        );
        SurveyPhoneNumberFormatVO addFormat2 = new SurveyPhoneNumberFormatVO(
            Optional.of(11L), 11L, "+919910*", "allowed"
        );
        SurveyPhoneNumberFormatVO removeFormat1 = new SurveyPhoneNumberFormatVO(
            Optional.of(11L), 11L, "+31", "allowed"
        );
        SurveyPhoneNumberFormatVO removeFormat2 = new SurveyPhoneNumberFormatVO(
            Optional.of(11L), 11L, "+316878*", "allowed"
        );

        SurveyUpdateVO surveyUpdateVO = new SurveyUpdateVO(
            surveySettingVO,
            List.of(addFormat1, addFormat2),
            List.of(removeFormat1, removeFormat2)
        );

        when(surveySettingRepo.findByIdAndAccountId(11L, contactingContext.accountId()))
            .thenReturn(Optional.of(surveyEntity));
        when(surveySettingRepo.save(any(SurveySettingsEntity.class))).thenReturn(surveyEntity);
        when(surveyPhNumberFormatRepo.saveAll(anyList())).thenReturn(List.of());

        // When
        SurveySettingVO result = surveyService.updateSurveySetting(surveyUpdateVO, contactingContext);

        // Then
        assertNotNull(result);
        verify(surveyPhNumberFormatRepo).saveAll(anyList());
        verify(surveyPhNumberFormatRepo).deleteByIds(anyList());
    }

    @Test
    @DisplayName("deleteSurveySetting - should delete survey setting")
    void deleteSurveySetting_ShouldDeleteSurveySetting() {
        // Given
        Long surveyId = 11L;

        // When
        surveyService.deleteSurveySetting(surveyId, contactingContext);

        // Then
        verify(surveySettingRepo).deleteByIdAndAccountId(surveyId, contactingContext.accountId());
    }

    @Test
    @DisplayName("addRemoveOrgs - should add and remove organizations")
    void addRemoveOrgs_ShouldAddAndRemoveOrganizations() {
        // Given
        Long surveyId = 11L;
        List<Long> idsToAdd = List.of(1L, 2L);
        List<Long> idsToRemove = List.of(3L, 4L);
        SurveyAssociationUpdateDTO dto = new SurveyAssociationUpdateDTO(idsToAdd, idsToRemove);

        // When
        surveyService.addRemoveOrgs(surveyId, dto);

        // Then
        verify(surveyOrgRepository).deleteBySurveyIdAndOrgIds(surveyId, idsToRemove);
        verify(surveyOrgRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("addRemoveOrgs - should only add when no removals")
    void addRemoveOrgs_ShouldOnlyAdd_WhenNoRemovals() {
        // Given
        Long surveyId = 11L;
        List<Long> idsToAdd = List.of(1L, 2L);
        SurveyAssociationUpdateDTO dto = new SurveyAssociationUpdateDTO(idsToAdd, List.of());

        // When
        surveyService.addRemoveOrgs(surveyId, dto);

        // Then
        verify(surveyOrgRepository, never()).deleteBySurveyIdAndOrgIds(anyLong(), anyList());
        verify(surveyOrgRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("addRemoveTaskqueues - should add and remove task queues")
    void addRemoveTaskqueues_ShouldAddAndRemoveTaskQueues() {
        // Given
        Long surveyId = 11L;
        List<Long> idsToAdd = List.of(1L, 2L);
        List<Long> idsToRemove = List.of(3L, 4L);
        SurveyAssociationUpdateDTO dto = new SurveyAssociationUpdateDTO(idsToAdd, idsToRemove);

        // When
        surveyService.addRemoveTaskQueues(surveyId, dto);

        // Then
        verify(surveyTaskQMappingRepository).deleteBySurveyIdAndTaskQueueIds(surveyId, idsToRemove);
        verify(surveyTaskQMappingRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("addRemoveTaskqueues - should only remove when no additions")
    void addRemoveTaskqueues_ShouldOnlyRemove_WhenNoAdditions() {
        // Given
        Long surveyId = 11L;
        List<Long> idsToRemove = List.of(3L, 4L);
        SurveyAssociationUpdateDTO dto = new SurveyAssociationUpdateDTO(List.of(), idsToRemove);

        // When
        surveyService.addRemoveTaskQueues(surveyId, dto);

        // Then
        verify(surveyTaskQMappingRepository).deleteBySurveyIdAndTaskQueueIds(surveyId, idsToRemove);
        verify(surveyTaskQMappingRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("addDeleteFormats - should handle empty format lists")
    void updateSurveySetting_ShouldHandleEmptyFormatLists() {
        // Given
        SurveyUpdateVO updateVO = new SurveyUpdateVO(surveySettingVO, List.of(), List.of());
        
        when(surveySettingRepo.findByIdAndAccountId(11L, contactingContext.accountId()))
            .thenReturn(Optional.of(surveyEntity));
        when(surveySettingRepo.save(any(SurveySettingsEntity.class))).thenReturn(surveyEntity);

        // When
        SurveySettingVO result = surveyService.updateSurveySetting(updateVO, contactingContext);

        // Then
        assertNotNull(result);
        verify(surveyPhNumberFormatRepo).saveAll(List.of());
        verify(surveyPhNumberFormatRepo, never()).deleteByIds(anyList());
    }

    @Test
    @DisplayName("getSurveyDetails - should handle concurrent operations properly")
    void getSurveyDetails_ShouldHandleConcurrentOperations() {
        // Given
        Long surveyId = 11L;
        Long accountId = 11L;
        
        when(surveySettingRepo.findByIdAndAccountId(surveyId, accountId))
            .thenReturn(Optional.of(surveyEntity));
        when(surveyPhNumberFormatRepo.findBySurveyId(surveyId)).thenReturn(List.of());
        when(surveyTaskQRepository.findTaskQueuesBySurveyId(surveyId)).thenReturn(List.of());
        when(surveyOrgRepository.findBySurveyIdWithHierarchy(surveyId)).thenReturn(List.of());

        // When
        CompletableFuture<Optional<SurveyDetails>> result = 
            surveyService.getSurveyDetails(surveyId, accountId);

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.get());
        assertTrue(result.join().isPresent());
    }
}

package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.RequiredArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.SurveyOrgMappingEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveyPhoneNumberFormatEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveyTaskQueueMappingEntity;
import nl.ing.api.contacting.conf.domain.model.surveysetting.*;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyAssociationUpdateDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyOverviewDTO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.SurveyMapperJava;
import nl.ing.api.contacting.conf.repository.*;
import nl.ing.api.contacting.conf.repository.cassandra.SurveyCallRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveySettingJpaRepository surveySettingRepo;
    private final SurveyPhoneNumberFormatJpaRepository surveyPhNumberFormatRepo;
    private final SurveyTaskQueueJpaRepository surveyTaskQRepository;
    private final SurveyOrgMappingJpaRepository surveyOrgRepository;
    private final SurveyTaskQueueMappingJpaRepository surveyTaskQMappingRepository;
    private final SurveyCallRecordRepository surveyCallRecordRepository;

    //Using scala configured executor. This executor service is suitable for DB operations
    private static final Executor dbExecutor = nl.ing.api.contacting.conf.modules.ExecutionContextConfig.listeningExecutorService();

    public List<SurveyCallRecordVO> getOfferedSurveyCalls(String accountFriendlyName, String phoneNumber) {
        return surveyCallRecordRepository.findByKeyAccountFriendlyNameAndKeyPhoneNum(accountFriendlyName, phoneNumber)
                .stream().map(SurveyMapperJava::surveyCallRecordEntityToVO).toList();

    }


    public CompletableFuture<Optional<SurveyDetailsVO>> getSurveyDetailsVO(Long surveyId, ContactingContext contactingContext) {
        return getSurveyDetails(surveyId, contactingContext.accountId())
                .thenApply(opt -> opt.map(SurveyMapperJava::surveyDetailsToVo));
    }

    public CompletableFuture<Optional<SurveyDetails>> getSurveyDetails(Long surveyId, Long accountId) {

        // Fetch survey settings synchronously
        Optional<SurveySettingsEntity> surveyOpt = surveySettingRepo.findByIdAndAccountId(surveyId, accountId);
        if (surveyOpt.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        SurveySettingsEntity survey = surveyOpt.get();

        // Fetch related entities in parallel
        CompletableFuture<List<SurveyPhoneNumberFormatEntity>> phFormatsFuture =
                CompletableFuture.supplyAsync(() -> surveyPhNumberFormatRepo.findBySurveyId(surveyId), dbExecutor);

        CompletableFuture<List<SurveyTaskQMappingWithName>> taskQueuesFuture =
                CompletableFuture.supplyAsync(() -> surveyTaskQRepository.findTaskQueuesBySurveyId(surveyId), dbExecutor);

        CompletableFuture<List<SurveyOrgDetails>> orgsFuture =
                CompletableFuture.supplyAsync(() -> surveyOrgRepository.findBySurveyIdWithHierarchy(surveyId), dbExecutor);

        // Combine results
        return CompletableFuture.allOf(phFormatsFuture, taskQueuesFuture, orgsFuture)
                .thenApply(v -> Optional.of(new SurveyDetails(
                        survey,
                        phFormatsFuture.join(),
                        taskQueuesFuture.join(),
                        orgsFuture.join()
                )));
    }


    @Transactional(readOnly = true)
    public List<SurveyOverviewDTO> getAllSurveySettings(ContactingContext contactingContext) {

        return surveySettingRepo.findByAccountId(contactingContext.accountId()).stream()
                .map(SurveyMapperJava::surveySettingEntityToVo)
                .map(SurveyMapperJava::surveySettingVoToOverviewDTO)
                .toList();
    }

    @Transactional
    public SurveySettingVO createSurveySetting(SurveySettingVO vo) {
        SurveySettingsEntity entityToSave = SurveyMapperJava.surveySettingVoToEntity(vo);
        entityToSave.setId(null); // Ensure ID is null for creation
        return SurveyMapperJava.surveySettingEntityToVo(surveySettingRepo.save(entityToSave));
    }

    @Transactional
    public SurveySettingVO updateSurveySetting(SurveyUpdateVO surveyUpdateVO, ContactingContext contactingContext) {
        SurveySettingsEntity entityToSave = SurveyMapperJava.surveySettingVoToEntity(surveyUpdateVO.settings());
        SurveySettingsEntity existingSurvey = surveySettingRepo.findByIdAndAccountId(entityToSave.getId(), contactingContext.accountId())
                .orElseThrow(() -> Errors.valueMissing("Survey with id " + entityToSave.getId() + " not found"));
        entityToSave.setId(existingSurvey.getId());
        SurveySettingsEntity savedEntity = surveySettingRepo.save(entityToSave);
        addDeleteFormats(surveyUpdateVO);
        return SurveyMapperJava.surveySettingEntityToVo(savedEntity);
    }

    private void addDeleteFormats(SurveyUpdateVO surveyUpdateVO) {
        List<SurveyPhoneNumberFormatEntity> formatsToAdd = surveyUpdateVO.formatsAdded().stream()
                .map(SurveyMapperJava::surveyPhNumFormatVoToEntity)
                .peek(surveyPhoneNumberFormatEntity -> surveyPhoneNumberFormatEntity.setId(null))
                .toList();
        surveyPhNumberFormatRepo.saveAll(formatsToAdd);
        List<Long> idsToDelete = surveyUpdateVO.formatsRemoved().stream()
                .map(SurveyPhoneNumberFormatVO::id)
                .flatMap(Optional::stream)
                .toList();
        if (!idsToDelete.isEmpty()) {
            surveyPhNumberFormatRepo.deleteByIds(idsToDelete);
        }
    }

    @Transactional
    public void deleteSurveySetting(Long id, ContactingContext contactingContext) {
        surveySettingRepo.deleteByIdAndAccountId(id, contactingContext.accountId());
    }

    @Transactional
    public void addRemoveOrgs(Long surveyId, SurveyAssociationUpdateDTO dto) {
        if (!dto.idsRemoved().isEmpty()) {
            surveyOrgRepository.deleteBySurveyIdAndOrgIds(surveyId, dto.idsRemoved());
        }

        if (!dto.idsAdded().isEmpty()) {
            List<SurveyOrgMappingEntity> entitiesToAdd = dto.idsAdded().stream()
                    .map(orgId -> new SurveyOrgMappingEntity(surveyId, orgId))
                    .toList();
            surveyOrgRepository.saveAll(entitiesToAdd);
        }
    }

    @Transactional
    public void addRemoveTaskQueues(Long surveyId, SurveyAssociationUpdateDTO dto) {

        // Remove task queues first
        if (!dto.idsRemoved().isEmpty()) {
            surveyTaskQMappingRepository.deleteBySurveyIdAndTaskQueueIds(surveyId, dto.idsRemoved());
        }

        // Add new task queues
        if (!dto.idsAdded().isEmpty()) {
            List<SurveyTaskQueueMappingEntity> entitiesToAdd = dto.idsAdded().stream()
                    .map(taskQueueId -> new SurveyTaskQueueMappingEntity(surveyId, taskQueueId))
                    .toList();
            surveyTaskQMappingRepository.saveAll(entitiesToAdd);
        }
    }
}

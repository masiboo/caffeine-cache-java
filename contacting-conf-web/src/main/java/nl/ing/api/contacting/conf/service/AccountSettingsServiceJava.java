package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.audit.AuditedEntity;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.AccountSetting;
import com.ing.api.contacting.dto.java.resource.AllAccountSettings;
import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import com.ing.api.contacting.dto.java.resource.PlatformAccountSettings;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.caching.util.Flags;
import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingConsumers;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingVO;
import nl.ing.api.contacting.conf.domain.model.admintool.AccountSettingDTO;
import nl.ing.api.contacting.conf.domain.model.admintool.AllAccountSettingsDTO;
import nl.ing.api.contacting.conf.domain.model.admintool.AllAccountSettingsVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataVO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.AccountSettingsMapper;
import nl.ing.api.contacting.conf.mapper.PlatformAccountSettingsMapper;
import nl.ing.api.contacting.conf.repository.AccountSettingsAuditRepository;
import nl.ing.api.contacting.conf.repository.PlatformAccountSettingsCacheRepository;
import nl.ing.api.contacting.shared.client.ContactingAPIClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static scala.jdk.javaapi.CollectionConverters.asJava;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSettingsServiceJava {

    private final ContactingAPIClient contactingAPIClient;
    //Wrapped Audit and custom jpa methods together for easier use
    private final AccountSettingsAuditRepository accountSettingsRepository;
    private final PlatformAccountSettingsCacheRepository platformAccountSettingsCacheRepository;
    private final SettingsMetadataServiceJava settingsMetadataService;


    /**
     * Get all account settings for the given account
     */
    @Transactional(readOnly = true)
    public AllAccountSettings getAllSettings(ContactingContext context) {
        List<AccountSettingsEntity> accountSettings = accountSettingsRepository.findAllByAccount(context);
        List<AccountSetting> accountSettingList = Optional.ofNullable(accountSettings)
                .stream()
                .flatMap(List::stream)
                .map(AccountSettingsMapper::fromEntity)
                .map(AccountSettingsMapper::toAccountSetting)
                .toList();

        List<PlatformAccountSetting> platformSettings = getPlatformAccountSettings(context).platformAccountSettings();
        return new AllAccountSettings(accountSettingList, platformSettings);
    }

    /**
     * Get all account settings for the given capabilities
     */
    @Transactional(readOnly = true)
    public AllAccountSettings getAllSettings(ContactingContext context, Set<SettingCapability> capabilities) {
        List<AccountSettingsEntity> accountSettings = accountSettingsRepository.findByCapabilities(capabilities, context.accountId());
        List<AccountSetting> accountSettingList = accountSettings.stream()
                .map(AccountSettingsMapper::fromEntity)
                .map(AccountSettingsMapper::toAccountSetting)
                .toList();

        List<PlatformAccountSetting> platformSettings = getPlatformAccountSettings(context).platformAccountSettings();
        return new AllAccountSettings(accountSettingList, platformSettings);
    }

    /**
     * Get all platform account setting
     */
    @Transactional(readOnly = true)
    public PlatformAccountSettings getPlatformAccountSettings(ContactingContext context) {
        List<PlatformAccountSettingsEntity> list = platformAccountSettingsCacheRepository.findByAccountId(context);
        List<PlatformAccountSetting> platformAccountSettings = Optional.ofNullable(list)
                .stream()
                .flatMap(List::stream)
                .map(PlatformAccountSettingsMapper::toPlatformAccountSetting)
                .toList();
        return new PlatformAccountSettings(platformAccountSettings);
    }

    public CompletableFuture<List<AccountDto>> getAccounts() {
        //TODO Switch from scala to java client once library is ready
        Flags flags = Flags.defaultFlags();
        Future<scala.collection.immutable.List<AccountDto>> scalaFuture = contactingAPIClient.getAccounts(flags);
        CompletableFuture<scala.collection.immutable.List<AccountDto>> javaFuture = FutureConverters.toJava(scalaFuture).toCompletableFuture();
        return javaFuture.thenApply(scalaList -> asJava(scalaList).stream().toList());
    }

    /**
     * Update platform account settings
     */
    @Transactional
    public PlatformAccountSetting updatePlatformSettings(PlatformAccountSetting platformAccountSetting, ContactingContext context) {
        List<AccountDto> accounts = getAccounts().join();
        accounts.stream()
                .filter(account -> account.id() == platformAccountSetting.accountId())
                .findFirst()
                .orElseThrow(() -> Errors.notFound("Account with id " + platformAccountSetting.accountId() + " does not exist"));
        PlatformAccountSettingsEntity saved = verifyAndSavePlatformSettings(platformAccountSetting, context);
        return PlatformAccountSettingsMapper.toPlatformAccountSetting(saved);
    }

    private PlatformAccountSettingsEntity verifyAndSavePlatformSettings(PlatformAccountSetting incomingEntity, ContactingContext context) {
        incomingEntity.id().orElseThrow(() -> Errors.valueMissing("accountSettings VO id is not defined"));
        PlatformAccountSettingsEntity existingEntity = platformAccountSettingsCacheRepository
                .findById(incomingEntity.id().get(), context)
                .orElseThrow(() -> Errors.notFound("account setting with id "+ incomingEntity.id() +" does not exist"));
        existingEntity.update(incomingEntity.key(), incomingEntity.value());
        return platformAccountSettingsCacheRepository.save(existingEntity, context);
    }

    /**
     * Get all customer account settings for the given account
     */
    public List<AccountSettingVO> getAccountSettingsForCustomers(ContactingContext context) {
        List<AccountSettingsEntity> entities = accountSettingsRepository.getCustomerSettings(context.accountId(), AccountSettingConsumers.CUSTOMER.value());
        return entities.stream().map(AccountSettingsMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public AccountSettingsEntity findById(Long id, ContactingContext context) {
        return accountSettingsRepository.findById(id, context)
                .orElseThrow(() -> Errors.notFound("account settings with id " + id + " not found for account id: " + context.accountId()));
    }

    @Transactional
    public void deleteAccountSetting(Long id, ContactingContext context) {
        accountSettingsRepository.deleteAndAudit(id, context);
    }

    public List<AuditedEntity<AccountSettingDTO, Long>> getAuditedVersions(Long entityId, int numRows) {
        List<AuditedEntity<AccountSettingsEntity, Long>> entities = accountSettingsRepository.getAuditHistory(entityId, numRows);
        return entities.stream()
                .map(entity ->
                        new AuditedEntity<>(
                                entity.id(),
                                entity.revId(),
                                entity.accountId(),
                                entity.auditType(),
                                entity.entityId(),
                                entity.entity().map(AccountSettingsMapper::fromEntity)
                                        .map(AccountSettingsMapper::toDTO),
                                entity.entityJsonData(),
                                entity.modifiedBy(),
                                entity.modifiedTime()
                        ))
                .toList();
    }

    /**
     * Create or update an account setting
     */
    @Transactional
    public AccountSettingDTO upsertAccountSetting(
            AccountSettingVO accountSettingVO,
            ContactingContext context) {

        if (!Objects.equals(accountSettingVO.accountId(), context.accountId())) {
            log.warn("Account ID mismatch: context accountId {} vs accountSettingVO accountId {}",
                    context.accountId(), accountSettingVO.accountId());
            throw Errors.forbidden("You are not allowed to modify account settings for another account");
        }

        Optional<SettingsMetadataVO> metaOpt = settingsMetadataService.findByName(accountSettingVO.key());
        SettingsMetadataVO settingsMetaDataVO = metaOpt
                .orElseThrow(() -> Errors.badRequest("Requested key is not present in settings metadata"));

        AccountSettingVO updatedSetting = new AccountSettingVO(
                accountSettingVO.id(),
                accountSettingVO.key(),
                accountSettingVO.value(),
                settingsMetaDataVO.capability(),
                settingsMetaDataVO.consumers(),
                accountSettingVO.accountId()
        );

        if (SettingsMetadataServiceJava.regexDontMatch(settingsMetaDataVO, updatedSetting.value())) {
            throw Errors.badRequest("Requested value does not match regex " + settingsMetaDataVO.regex() + " in settings metadata");
        }

        if (SettingsMetadataServiceJava.optionsDontMatch(settingsMetaDataVO, updatedSetting.value())) {
            throw Errors.badRequest("Requested value is not present in settings metadata");
        }

        if (updatedSetting.id().isPresent()) {
            findById(updatedSetting.id().get(), context); // to check existence
            AccountSettingsEntity updatedEntity = accountSettingsRepository.updateAndAudit(AccountSettingsMapper.toEntity(updatedSetting), context);
            return AccountSettingsMapper.toDTO(AccountSettingsMapper.fromEntity(updatedEntity));
        } else {
            AccountSettingsEntity createdEntity = accountSettingsRepository.saveAndAudit(AccountSettingsMapper.toEntity(updatedSetting), context);
            return AccountSettingsMapper.toDTO(AccountSettingsMapper.fromEntity(createdEntity));
        }
    }

    @Transactional(readOnly = true)
    public AllAccountSettingsDTO getAccountSettingsForAdminTool(ContactingContext context) {
        List<AccountSettingsEntity> accountSettings = accountSettingsRepository.findAllByAccount(context);
        List<AccountSettingVO> accountSettingsVo = Optional.ofNullable(accountSettings)
                .stream()
                .flatMap(List::stream)
                .map(AccountSettingsMapper::fromEntity)
                .toList();

        List<PlatformAccountSetting> platformAccountSettings = getPlatformAccountSettings(context).platformAccountSettings();
        return new AllAccountSettingsVO(accountSettingsVo, platformAccountSettings).toDTO();
    }
}

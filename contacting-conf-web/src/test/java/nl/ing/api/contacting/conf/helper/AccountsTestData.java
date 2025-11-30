package nl.ing.api.contacting.conf.helper;

import com.ing.api.contacting.dto.java.audit.AuditType;
import com.ing.api.contacting.dto.java.audit.AuditedEntity;
import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.AccountSetting;
import com.ing.api.contacting.dto.java.resource.AllAccountSettings;
import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import com.ing.api.contacting.dto.java.resource.PlatformAccountSettings;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import nl.ing.api.contacting.conf.domain.InputTypeJava;
import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingConsumers;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingVO;
import nl.ing.api.contacting.conf.domain.model.admintool.AccountSettingDTO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsOptionsVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AccountsTestData {

    public static AllAccountSettings getAllAccountSettings() {
        return new AllAccountSettings(
                List.of(new AccountSetting(Optional.of(1L), "dummyKey", "dummyValue", 100L),
                        new AccountSetting(Optional.of(1L), "dummyKey", "dummyValue", 100L)),
                List.of(
                        new PlatformAccountSetting(Optional.of(1L), "dummyKey", "dummyValue", 100L),
                        new PlatformAccountSetting(Optional.of(1L), "dummyKey", "dummyValue", 100L))
        );

    }


    public static AccountSettingVO getAccountSettingVO() {
        return new AccountSettingVO(
                Optional.of(1L),
                "TIMEZONE",
                "ASIA/DELHI",
                List.of(SettingCapability.VIDEO, SettingCapability.CHAT),
                List.of(AccountSettingConsumers.CUSTOMER, AccountSettingConsumers.API),
                1001L
        );


    }

    public static AccountDto getAccountDto() {
        return new AccountDto(
                1L,
                "AC123XYZ",
                "NL-dev",
                "WS123XYZ",
                "Europe/Amsterdam",
                100L,
                "1079",
                null,
                null
        );
    }

    public static AccountSetting getAccountSetting() {
        return new AccountSetting(Optional.of(1L), "dummyKey", "dummyValue", 100L);
    }

    public static AccountSettingsEntity getAccountSettingEntity() {
        return new AccountSettingsEntity(
                1L,
                "TIMEZONE",
                "Asia/Delhi",
                "video",
                "customer",
                100L);
    }

    public static PlatformAccountSettingsEntity getPlatformSettingEntity() {
        return new PlatformAccountSettingsEntity(
                1L,
                "lease_line_flag",
                "true",
                100L);
    }

    public static ContactingContext getContactingContext() {
        return new ContactingContext(
                1001L,
                new AuditContext(
                        "test_user",
                        Optional.of(LocalDateTime.now()),
                        Optional.of(2002L),
                        Optional.empty() // Assuming AuditInfo is not needed for dummy
                ));
    }

    public static PlatformAccountSetting getPlatformAccountSetting() {
        return new PlatformAccountSetting(
                Optional.of(1L),
                "lease_line_flag",
                "true",
                100L
        );
    }

    public static PlatformAccountSetting getPlatformAccountSettingWithEmptyId() {
        return new PlatformAccountSetting(
                Optional.empty(),
                "lease_line_flag",
                "true",
                100L
        );
    }

    public static PlatformAccountSettings getPlatformAccountSettings() {
        PlatformAccountSetting setting = PlatformAccountSetting.builder()
                .id(1L)
                .key("dummyKey")
                .value("dummyValue")
                .accountId(12345L)
                .build();
        return new PlatformAccountSettings(List.of(setting));
    }

    public static AccountSettingsEntity getAccountSettingsEntity() {
        return new AccountSettingsEntity(
                1L,
                "dummyKey",
                "dummyValue",
                "CHAT",
                "CUSTOMER",
                100L
        );
    }

    public static AccountSettingDTO getAdminToolElementsAccountSettingDTO() {
        return new AccountSettingDTO(
                Optional.of(1L),
                "TIMEZONE",
                "ASIA/DELHI",
                Optional.of(List.of(SettingCapability.CHAT.value(), SettingCapability.DIALER.value())),
                Optional.of(List.of(AccountSettingConsumers.CUSTOMER.value(), AccountSettingConsumers.CUSTOMER.value())),
                1001L
        );
    }

    public static AccountSettingDTO getErroAdminToolElementsAccountSettingDTO() {
        return new AccountSettingDTO(
                null,
                "TIMEZONE",
                "ASIA/DELHI",
                Optional.of(List.of(SettingCapability.CHAT.value(), SettingCapability.DIALER.value())),
                Optional.of(List.of(AccountSettingConsumers.CUSTOMER.value(), AccountSettingConsumers.CUSTOMER.value())),
                1001L
        );
    }

    public static AccountSettingDTO getAccountSettingDTO() {
        // Example values, adjust as needed
        return new AccountSettingDTO(
                Optional.of(1L),
                "TIMEZONE",
                "ASIA/DELHI",
                Optional.of(List.of("VIDEO", "CHAT")),
                Optional.of(List.of("CUSTOMER", "API")),
                1001L
        );
    }

    public static AccountSettingDTO getAccountSettingDTOWithErrors() {
        // Return a DTO whose toVO() returns errors
        return new AccountSettingDTO(
                Optional.of(1L),
                "INVALID_KEY",
                null,
                Optional.of(List.of()),
                Optional.of(List.of()),
                1L
        );
    }

    public static CompletableFuture<List<AccountSettingsEntity>> getCompletableFutureAccountSettingsEntity() {
        AccountSettingsEntity entity1 = new AccountSettingsEntity(
                1L, "TIMEZONE", "Asia/Delhi", "video", "customer", 100L
        );
        AccountSettingsEntity entity2 = new AccountSettingsEntity(
                2L, "LANGUAGE", "en", "chat", "api", 101L
        );
        List<AccountSettingsEntity> entities = List.of(entity1, entity2);
        return CompletableFuture.completedFuture(entities);
    }

    public static List<AccountSettingsEntity> getAccountSettingsEntitiesFromCompletableFuture(CompletableFuture<List<AccountSettingsEntity>> completableFutureAccountSettingsEntity) {
        try {
            return completableFutureAccountSettingsEntity.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get AccountSettingsEntity list from CompletableFuture", e);
        }
    }

    public static CompletableFuture<List<PlatformAccountSettingsEntity>> getCompletableFuturePlatformAccountSettingsEntity() {
        PlatformAccountSettingsEntity entity1 = PlatformAccountSettingsEntity.builder()
                .id(1L)
                .key("PLATFORM_TIMEZONE")
                .value("Europe/Amsterdam")
                .accountId(1000L)
                .build();

        PlatformAccountSettingsEntity entity2 = PlatformAccountSettingsEntity.builder()
                .id(2L)
                .key("PLATFORM_LANGUAGE")
                .value("nl")
                .accountId(1001L)
                .build();

        List<PlatformAccountSettingsEntity> entities = List.of(entity1, entity2);
        return CompletableFuture.completedFuture(entities);
    }

    public static List<PlatformAccountSettingsEntity> getPlatformAccountSettingsEntitiesFromCompletableFuture(CompletableFuture<List<PlatformAccountSettingsEntity>> completableFuturePlatformAccountSettingsEntity) {
        try {
            return completableFuturePlatformAccountSettingsEntity.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PlatformAccountSettingsEntity list from CompletableFuture", e);
        }
    }

    public static PlatformAccountSettingsEntity getPlatformAccountSettingsEntity() {
        return PlatformAccountSettingsEntity.builder()
                .id(1L)
                .key("PLATFORM_TIMEZONE")
                .value("Europe/Amsterdam")
                .accountId(1000L)
                .build();
    }

    public static List<PlatformAccountSetting> getPlatformAccountSettingList() {
        PlatformAccountSetting setting1 = PlatformAccountSetting.builder()
                .id(1L)
                .key("PLATFORM_TIMEZONE")
                .value("Europe/Amsterdam")
                .accountId(1000L)
                .build();

        PlatformAccountSetting setting2 = PlatformAccountSetting.builder()
                .id(2L)
                .key("PLATFORM_LANGUAGE")
                .value("nl")
                .accountId(1001L)
                .build();

        return List.of(setting1, setting2);
    }

    public static CompletableFuture<AllAccountSettings> getAllAccountSettingsCompletableFuture() {
        AccountSetting accountSetting1 = new AccountSetting(Optional.of(1L), "TIMEZONE", "Asia/Delhi", 100L);
        AccountSetting accountSetting2 = new AccountSetting(Optional.of(2L), "LANGUAGE", "en", 100L);

        PlatformAccountSetting platformSetting1 = PlatformAccountSetting.builder()
                .id(1L)
                .key("PLATFORM_TIMEZONE")
                .value("Europe/Amsterdam")
                .accountId(1000L)
                .build();
        PlatformAccountSetting platformSetting2 = PlatformAccountSetting.builder()
                .id(2L)
                .key("PLATFORM_LANGUAGE")
                .value("nl")
                .accountId(1000L)
                .build();

        AllAccountSettings settings = new AllAccountSettings(
                List.of(accountSetting1, accountSetting2),
                List.of(platformSetting1, platformSetting2)
        );

        return CompletableFuture.completedFuture(settings);
    }

    public static List<AccountSetting> getAccountSettingList() {
        AccountSetting setting1 = new AccountSetting(
                Optional.of(1L),
                "TIMEZONE",
                "Asia/Delhi",
                1001L
        );

        AccountSetting setting2 = new AccountSetting(
                Optional.of(2L),
                "LANGUAGE",
                "en",
                1001L
        );

        return List.of(setting1, setting2);
    }

    public static List<AuditedEntity<AccountSettingsEntity, Long>> getAuditEntitiesList() {
        List<AuditedEntity<AccountSettingsEntity, Long>> auditEntities = new ArrayList<>();

        // Create account settings entities for the audit records
        AccountSettingsEntity entity1 = new AccountSettingsEntity(
                1L,           // id
                "TIMEZONE",   // key
                "Europe/Amsterdam", // value
                "video",      // capability
                "customer",   // consumer
                101L          // accountId
        );

        AccountSettingsEntity entity2 = new AccountSettingsEntity(
                1L,           // id (same ID as it's the same entity at different points in time)
                "TIMEZONE",   // key
                "Asia/Delhi", // updated value
                "video",      // capability
                "customer",   // consumer
                101L          // accountId
        );

        // Create the audited entities
        auditEntities.add(new AuditedEntity<>(
                1L,                  // id
                1L,                  // revId
                Optional.of(101L),   // accountId
                AuditType.ADD,       // auditType
                1L,                  // entityId as Long
                Optional.of(entity1), // entity
                Optional.of("{\"key\":\"TIMEZONE\",\"value\":\"Europe/Amsterdam\"}"), // entityJsonData
                "system",            // modifiedBy
                LocalDateTime.now() // modifiedTime
        ));

        auditEntities.add(new AuditedEntity<>(
                2L,                  // id
                2L,                  // revId
                Optional.of(101L),   // accountId
                AuditType.UPDATE,    // auditType
                1L,                  // entityId as Long
                Optional.of(entity2), // entity
                Optional.of("{\"key\":\"TIMEZONE\",\"value\":\"Asia/Delhi\"}"), // entityJsonData
                "system",            // modifiedBy
                LocalDateTime.now()  // modifiedTime
        ));

        return auditEntities;
    }

    public static SettingsMetadataVO getSettingsMetadataVO() {
        return new SettingsMetadataVO(
                Optional.of(1L),
                "TIMEZONE",
                InputTypeJava.DROPDOWN,
                Optional.of("^(Europe|Asia|America)/[A-Za-z_/]+$"),
                List.of(new SettingsOptionsVO(
                        Optional.of(1L),
                        "Europe/Amsterdam",
                        "Amsterdam"
                ), new SettingsOptionsVO(
                        Optional.of(2L),
                        "Asia/Delhi",
                        "Delhi"
                )),
                List.of(SettingCapability.VIDEO),
                List.of(AccountSettingConsumers.CUSTOMER)
        );
    }

}




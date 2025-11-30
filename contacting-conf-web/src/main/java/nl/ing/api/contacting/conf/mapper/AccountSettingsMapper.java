package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.AccountSetting;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingConsumers;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingVO;
import nl.ing.api.contacting.conf.domain.model.admintool.AccountSettingDTO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper for AccountSettingVO and related types.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AccountSettingsMapper {

    public static AccountSettingsEntity toEntity(AccountSettingVO vo) {
        return new AccountSettingsEntity(
                vo.id().orElse(null),
                vo.key(),
                vo.value(),
                vo.capability().isEmpty() ? null :
                        vo.capability().stream()
                                .map(Enum::toString)
                                .collect(Collectors.joining(",")),
                vo.consumers().isEmpty() ? null :
                        vo.consumers().stream()
                                .map(Enum::toString)
                                .collect(Collectors.joining(",")),
                vo.accountId()
        );
    }

    public static AccountSettingDTO toDTO(AccountSettingVO vo) {
        Optional<List<String>> capabilityOpt = vo.capability().isEmpty()
                ? Optional.empty()
                : Optional.of(vo.capability().stream().map(SettingCapability::value).toList());

        Optional<List<String>> consumersOpt = vo.consumers().isEmpty()
                ? Optional.empty()
                : Optional.of(vo.consumers().stream().map(AccountSettingConsumers::value).toList());

        return new AccountSettingDTO(vo.id(), vo.key(), vo.value(), capabilityOpt, consumersOpt, vo.accountId());
    }

    public static AccountSettingVO fromDTO(AccountSettingDTO dto) {
        List<SettingCapability> capabilities = dto.capability()
                .map(list -> list.stream().map(SettingCapability::fromValue).toList())
                .orElseGet(List::of);
        List<AccountSettingConsumers> consumers = dto.consumers()
                .map(list -> list.stream().map(AccountSettingConsumers::fromValue).toList())
                .orElseGet(List::of);
        return new AccountSettingVO(
                dto.id(),
                dto.key(),
                dto.value(),
                capabilities,
                consumers,
                dto.accountId()
        );
    }

    public static AccountSetting toAccountSetting(AccountSettingVO vo) {
        return new AccountSetting(vo.id(), vo.key(), vo.value(), vo.accountId());
    }

    public static AccountSettingVO fromEntity(AccountSettingsEntity entity) {
        return new AccountSettingVO(
                Optional.ofNullable(entity.getId()),
                entity.getKey(),
                entity.getValue(),
                Optional.ofNullable(entity.getCapabilities())
                        .map(capabilities -> Arrays.stream(capabilities.split(","))
                                .map(capability -> SettingCapability.valueOf(capability.trim().toUpperCase()))
                                .toList())
                        .orElseGet(List::of),
                Optional.ofNullable(entity.getConsumers())
                        .map(consumers -> Arrays.stream(consumers.split(","))
                                .map(consumer -> AccountSettingConsumers.valueOf(consumer.trim().toUpperCase()))
                                .toList())
                        .orElseGet(List::of),
                entity.getAccountId()
        );
    }
}

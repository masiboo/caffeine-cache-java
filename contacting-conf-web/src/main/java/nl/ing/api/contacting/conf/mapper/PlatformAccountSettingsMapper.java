package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Optional;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PlatformAccountSettingsMapper {

    public static PlatformAccountSetting toPlatformAccountSetting(PlatformAccountSettingsEntity entity) {
        if (entity == null) {
            throw Errors.serverError("PlatformAccountSettingsEntity cannot be null");
        }
        return new PlatformAccountSetting(
                Optional.ofNullable(entity.getId()),
                entity.getKey(),
                entity.getValue(),
                entity.getAccountId()
        );
    }


}

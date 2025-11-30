package nl.ing.api.contacting.conf.domain.model.blacklist;

import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistFunctionality;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistType;

import java.time.LocalDateTime;
import java.util.Optional;

public record BlacklistItemVO(
        Long id,
        BlacklistFunctionality functionality,
        BlacklistType entityType,
        String value,
        LocalDateTime startDate,
        Optional<LocalDateTime> endDate
) {

    public Optional<Long> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<LocalDateTime> getEndDate() {
        return endDate;
    }
}


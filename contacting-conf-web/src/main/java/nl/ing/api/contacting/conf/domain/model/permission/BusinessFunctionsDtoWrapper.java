package nl.ing.api.contacting.conf.domain.model.permission;

import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionsDto;

import java.util.List;

public record BusinessFunctionsDtoWrapper(List<BusinessFunctionsDto> data) {
}
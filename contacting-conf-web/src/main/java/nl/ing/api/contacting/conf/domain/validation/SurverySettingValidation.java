package nl.ing.api.contacting.conf.domain.validation;

import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.java.domain.OrganisationVO;


import java.util.Objects;
import java.util.Optional;

public final class SurverySettingValidation {

    private static final String ALLOWED = "allowed";
    private static final String EXCLUDED = "excluded";
    private SurverySettingValidation() {}

    public static void validateId(Optional<Long> id) {
        Optional<Long> safeId = Optional.ofNullable(id).orElse(Optional.empty());
        safeId.ifPresent(val -> {
            if (val <= 0) throw Errors.badRequest("id must be positive");
        });
    }

    public static void validateSurveyId(long surveyId) {
        if (surveyId <= 0) {
            throw Errors.badRequest("surveyId must be positive");
        }
    }

    public static void validateFormat(String format) {
        if (format == null) {
            throw Errors.badRequest("format must not be null");
        }
    }

    public static void validateDirection(String direction) {
        if (direction == null || !(direction.equals(ALLOWED) || direction.equals(EXCLUDED))) {
            throw Errors.badRequest("direction must be 'allowed' or 'excluded'");
        }
    }

    public static void validateTaskQId(long taskQId) {
        if (taskQId <= 0L) {
            throw Errors.badRequest("taskQId must be positive");
        }
    }

    public static void validateTqName(String tqName) {
        if (tqName == null || tqName.isBlank()) {
            throw Errors.badRequest("tqName must not be null or blank");
        }
    }

    public static void validateOrganisationVO(OrganisationVO organisationVO) {
        if (organisationVO == null) {
            throw Errors.badRequest("organisationVO must not be null");
        }
    }
}

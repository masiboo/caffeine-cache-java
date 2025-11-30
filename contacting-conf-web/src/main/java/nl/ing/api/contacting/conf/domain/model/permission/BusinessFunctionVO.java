package nl.ing.api.contacting.conf.domain.model.permission;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Business functions data class for Permissions based on business functions
 *
 * @param accountFriendlyName twilio sub account friendly name
 * @param businessFunction    the business function like user-admin or listen-recordings
 * @param role               the required role to access the business function
 * @param restriction        the restriction to perform the business function. Like you are to allowed to listen to recordings within your circle
 * @param organisationId     the organization ID
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BusinessFunctionVO(
        String accountFriendlyName,
        String businessFunction,
        String role,
        OrganisationalRestrictionLevel restriction,
        int organisationId
) {}
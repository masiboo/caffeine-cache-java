package nl.ing.api.contacting.conf.resource;

import com.datastax.oss.driver.shaded.guava.common.base.Strings;
import com.ing.api.contacting.dto.java.audit.AuditEntity;
import com.ing.api.contacting.dto.java.resource.AllAccountSettings;
import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingVO;
import nl.ing.api.contacting.conf.domain.model.admintool.AccountSettingDTO;
import nl.ing.api.contacting.conf.domain.model.admintool.AllAccountSettingsDTO;
import nl.ing.api.contacting.conf.domain.model.admintool.CustomerAccountSettingsDTO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.AccountSettingsMapper;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.AccountSettingsServiceJava;
import nl.ing.api.contacting.conf.util.ResponseWrapper;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.*;

@Path("/contacting-conf/account-settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AccountSettingsResourceJava extends BaseResourceJava {
    private static Integer DEFAULT_MAX_ROWS = 21;

    private AccountSettingsServiceJava accountSettingsService;

    @Autowired
    public AccountSettingsResourceJava(AccountSettingsServiceJava accountSettingsService) {
        this.accountSettingsService = accountSettingsService;
    }

    @GET
    @ApiOperation(
            value = "Get All account settings",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for getting all account settings; platform and non-platform," +
                    "to be used by all non-admin tool consumers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get All", response = AllAccountSettings.class)
    })
    @Permissions({
            ContactingBusinessFunctions.CONTACT_HANDLING,
            ContactingBusinessFunctions.USER_ADMINISTRATION,
            ContactingBusinessFunctions.TWILIO_LOGIN,
            ContactingBusinessFunctions.SYSTEM_TOOLING
    })
    public CompletableFuture<Response> getAll(@QueryParam("capabilities") @ApiParam(value = "capabilities") String capabilities) {
        return okAsync(() ->
        {
            if (Strings.isNullOrEmpty(capabilities)) {
                return accountSettingsService.getAllSettings(getContactingContext());
            } else {
                Set<SettingCapability> enumCapabilities = Arrays.stream(capabilities.split(","))
                        .map(String::trim)
                        .filter(Predicate.not(String::isBlank))
                        .map(String::toUpperCase)
                        .map(SettingCapability::fromValue)
                        .collect(Collectors.toSet());
                return accountSettingsService.getAllSettings(getContactingContext(), enumCapabilities);
            }
        });
    }

    @GET
    @Produces("application/vnd.ing.contacting.account.settings+json")
    @ApiOperation(
            value = "Get for admin tool",
            notes = "[Business Function: TWILIO_LOGIN] Endpoint for getting all account settings; platform and non-platform from admin tool")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get All", response = AllAccountSettingsDTO.class)
    })
    @Permissions({
            ContactingBusinessFunctions.TWILIO_LOGIN
    })
    public CompletableFuture<Response> getAllForAdminTool() {
        return okAsync(() ->
                accountSettingsService.getAccountSettingsForAdminTool(getContactingContext())
        );
    }


    @GET
    @Path("/customers")
    @ApiOperation(
            value = "get all customer settings",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for getting all customer account settings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get All", response = CustomerAccountSettingsDTO.class)
    })
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    public CompletableFuture<Response> getCustomers() {
        return okAsync(() ->
                new CustomerAccountSettingsDTO(
                        accountSettingsService.getAccountSettingsForCustomers(getContactingContext()).stream()
                                .map(AccountSettingsMapper::toDTO)
                                .toList()
                )
        );
    }

    @PUT
    @Path("/platform/{id}")
    @ApiOperation(
            value = "Update platform setting",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for updating platform account settings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get All", response = PlatformAccountSetting.class)
    })
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    public CompletableFuture<Response> updatePlatformSetting(@PathParam("id") @ApiParam Long id, @RequestBody PlatformAccountSetting ps) {

        PlatformAccountSetting updatedPlatformSetting = PlatformAccountSetting.builder()
                .accountId(ps.accountId())
                .key(ps.key())
                .value(ps.value())
                .id(id)
                .build();

        return created(() ->
                accountSettingsService.updatePlatformSettings(updatedPlatformSetting,
                        getContactingContext().withAccountId(ps.accountId())));
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(
            value = "Delete account setting",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for deleting account settings; only non-platform")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Deleted successfully"),
            @ApiResponse(code = 404, message = "Account setting not found")
    })
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    public CompletableFuture<Void> delete(@PathParam("id") @ApiParam Long id) {
        //Perform a find first to throw 404 if not found (outside future context)
        accountSettingsService.findById(id, getContactingContext());
        return CompletableFuture.runAsync(() -> accountSettingsService.deleteAccountSetting(id, getContactingContext()));
    }

    @GET
    @Path("/revisions/{id}")
    @ApiOperation(
            value = "Get revisions of account settings",
            notes = "[Business Function:ACCOUNT_SETTING_MANAGEMENT] Endpoint for getting all account settings revisions"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Fetched all revisions", response = AuditEntity.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Account settings not found")
    })
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public CompletableFuture<Response> getAuditedVersions(
            @PathParam("id") @ApiParam(value = "account settings id", required = true) Long id,
            @QueryParam("numRows") @ApiParam(value = "number of revisions to fetch, default 21", required = false) Integer numRows) {
        int rows = Optional.ofNullable(numRows).orElse(DEFAULT_MAX_ROWS);
        return okDataAsync(() ->
                accountSettingsService.getAuditedVersions(id, rows)
        );
    }

    @POST
    @ApiOperation(value = "Create an account setting",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for creating account settings; only non-platform")
    @ApiResponses({
            @ApiResponse(code = 200, message = "created", response = AccountSettingDTO.class),
            @ApiResponse(code = 400, message = "bad request"),
            @ApiResponse(code = 404, message = "account setting does not exist")
    })
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public CompletableFuture<Response> create(AccountSettingDTO accountSettings) {
        AccountSettingVO updatedVO = AccountSettingsMapper.fromDTO(accountSettings).withId(Optional.empty());
        return created(() ->
                accountSettingsService.upsertAccountSetting(updatedVO, getContactingContext())
        );
    }

    @PUT
    @Path("{id}")
    @ApiOperation(value = "Update by id",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for updating account settings; only non-platform")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Update by id", response = AccountSettingDTO.class),
            @ApiResponse(code = 404, message = "account setting does not exist")
    })
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public CompletableFuture<Response> update(@PathParam("id") @ApiParam(value = "id", required = true) Long id, AccountSettingDTO accountSettings) {
        if (id <= 0) {
            return CompletableFuture.completedFuture(ResponseWrapper.badRequest("Path parameter id is not valid"));
        }
        AccountSettingVO updatedVO = AccountSettingsMapper.fromDTO(accountSettings).withId(Optional.of(id));
        return okAsync(() ->
                accountSettingsService.upsertAccountSetting(updatedVO, getContactingContext())
        );
    }
}

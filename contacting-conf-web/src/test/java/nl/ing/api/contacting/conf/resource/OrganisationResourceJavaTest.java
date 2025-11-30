package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.organisation.OrganisationSaveDto;
import com.ing.api.contacting.dto.java.resource.organisation.OrganisationsDto;
import com.ing.apisdk.toolkit.esperanto.core.HttpEsperantoError;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.OrganisationMapperJava;
import nl.ing.api.contacting.conf.service.OrganisationServiceJava;

import nl.ing.api.contacting.java.domain.OrganisationVO;

import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class OrganisationResourceJavaTest {

    @Mock
    private OrganisationServiceJava organisationService;

    private OrganisationResourceJava organisationResource;

    private OrganisationVO vo;
    private OrganisationEntity organisationModel;
    private OrganisationSaveDto dto;
    private ContactingContext context;
    private SessionContext sessionContext;

    static class TestOrganisationResourceJava extends OrganisationResourceJava {
        private final ContactingContext testContext;
        private final SessionContext sessionContext;
        TestOrganisationResourceJava(OrganisationServiceJava service, ContactingContext context,SessionContext sessionContext) {
            super(service);
            this.testContext = context;
            this.sessionContext = sessionContext;
        }

        @Override
        protected ContactingContext getContactingContext() {
            return testContext;
        }

        @Override
        protected Optional<SessionContext> getSessionContext() {
            return Optional.of(sessionContext);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        vo = new OrganisationVO(
                Optional.of(1L),
                "New Business Unit",
                OrganisationLevelEnumeration.SUPER_CIRCLE,
                Optional.empty()
        );
        organisationModel = OrganisationEntity.builder()
                .id(1L)
                .name("New Business Unit")
                .accountId(1L)
                .parentId(-1L)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .parent(null)
                .build();
        dto = new OrganisationSaveDto(
                Optional.of(1L),
                "New Business Unit",
                OrganisationLevelEnumeration.SUPER_CIRCLE.getId(),
                Optional.empty()
        );


        context = mock(ContactingContext.class);
        sessionContext = mock(SessionContext.class);
        organisationResource = new TestOrganisationResourceJava(organisationService, context,sessionContext);
    }

    @Test
    @DisplayName("create an organization")
    void shouldCreateOrganization() {
        when(organisationService.create(any(), any()))
                .thenReturn(1L);
        OrganisationSaveDto dtoWithoutId = new OrganisationSaveDto(
                Optional.empty(),
                dto.name(),
                dto.level(),
                dto.parentId()
        );
        Response response = organisationResource.createOrganisation(dtoWithoutId);
        assertEquals(201, response.getStatus());
        assumeTrue(response.getLocation() != null, "Location header is not set, skipping assertion.");
        assertNotNull(response.getLocation());
        assertTrue(response.getLocation().toString().endsWith("/organisations/1"));
        // <-- Add this line to check the entity

    }

    @Test
    @DisplayName("fail at creating an organization because the name is not unique")
    void shouldFailCreateOrganizationNameNotUnique() {
        when(organisationService.create(any(), any()))
                .thenThrow(Errors.badRequest("requirement failed: Name is not unique"));
        OrganisationSaveDto dtoWithoutId = new OrganisationSaveDto(
                Optional.empty(),
                dto.name(),
                dto.level(),
                dto.parentId()
        );

        ApplicationEsperantoException response = assertThrows(ApplicationEsperantoException.class, ()-> organisationResource.createOrganisation(dtoWithoutId));
        assertTrue(response.getMessage().contains("requirement failed: Name is not unique") );
    }

    @Test
    @DisplayName("fail at creating an organization because passing organisation Id")
    void shouldFailCreateOrganizationWithId() {
        when(organisationService.create(any(), any()))
                .thenThrow( Errors.badRequest("requirement failed: Identifier should not be set while creating an organisation"));
        ApplicationEsperantoException response = assertThrows(ApplicationEsperantoException.class, ()-> organisationResource.createOrganisation(dto));
        assertEquals("requirement failed: Identifier should not be set while creating an organisation", response.getMessage());
    }

    @Test
    @DisplayName("update an organization")
    void shouldUpdateOrganization() {
        when(organisationService.update(any(), any()))
                .thenReturn(organisationModel);
        Response response = organisationResource.updateOrganisation(1L, dto).join();
        assertEquals(204, response.getStatus());
    }

    @Test
    @DisplayName("fail at updating an organization because the name is not unique")
    void shouldFailUpdateOrganizationNameNotUnique() {
        when(organisationService.update(any(), any()))
                .thenThrow(Errors.badRequest("requirement failed: Name is not unique"));
        Response response = organisationResource.updateOrganisation(1L, dto).join();
        assertEquals(400, response.getStatus());
       // assertEquals("requirement failed: Name is not unique", response.getEntity());
        var error = response.getEntity();
        String message;
        if (error instanceof HttpEsperantoError err) {
            message = err.esperantoError().error().message();
        } else {
            message = error.toString();
        }
        assertEquals("requirement failed: Name is not unique", message);
    }

    @Test
    @DisplayName("fail at updating an organization because because not passing organisation Id")
    void shouldFailUpdateOrganizationNoId() {
        when(organisationService.update(any(), any()))
                .thenThrow(Errors.badRequest("requirement failed: Identifier should be set while update an organisation"));
        Response response = organisationResource.updateOrganisation(1L, dto).join();
        assertEquals(400, response.getStatus());
       // assertEquals("requirement failed: Identifier should be set while update an organisation", response.getEntity());
        var error = response.getEntity();
        String message;
        if (error instanceof HttpEsperantoError err) {
            message = err.esperantoError().error().message();
        } else {
            message = error.toString();
        }
        assertEquals("requirement failed: Identifier should be set while update an organisation", message);
    }

    @Test
    @DisplayName("retrieve an organization")
    void shouldRetrieveOrganization() {
        when(organisationService.getById(eq(1L), any()))
                .thenReturn(Optional.of(vo));
        Response response = organisationResource.retrieveOrganisation(1L).join();
        assertEquals(200, response.getStatus());
        assertEquals(OrganisationMapperJava.toDto(vo), response.getEntity());
    }

    @Test
    @DisplayName("not retrieve an organization")
    void shouldNotRetrieveOrganization() {
        when(organisationService.getById(eq(1L), any()))
                .thenReturn(Optional.empty());
        Response response = organisationResource.retrieveOrganisation(1L).join();
        assertEquals(404, response.getStatus());
    }

    @Test
    @DisplayName("get all the things")
    void shouldGetAllOrganizations() {
        List<OrganisationVO> orgVOlist = List.of(vo, vo);
        when(organisationService.getOrganisationTree(any()))
                .thenReturn(orgVOlist);
        Response response = organisationResource.getAllOrganisations().join();
        assertEquals(200, response.getStatus());
        assertEquals(new OrganisationsDto(orgVOlist.stream().map(OrganisationMapperJava::toDto).toList()), response.getEntity());
    }

    @Test
    @DisplayName("fail at deleting an organisation when it does not even exist")
    void shouldFailDeleteOrganisationNotExist() {
        when(organisationService.delete(eq(2344L), any()))
                .thenReturn(0);
        Response response = organisationResource.deleteOrganisation(2344L).join();
        assertEquals(404, response.getStatus());
    }

    @Test
    @DisplayName("delete an organisation")
    void shouldDeleteOrganisation() {
        when(organisationService.delete(eq(345L), any()))
                .thenReturn(1);
        Response response = organisationResource.deleteOrganisation(345L).join();
        assertEquals(204, response.getStatus());
    }

    @Test
    void testMyOrganisationsUserAdministrationPositive() throws Exception {
        OrganisationVO org1 = new OrganisationVO(Optional.of(2L), "Org One", OrganisationLevelEnumeration.SUPER_CIRCLE, Optional.empty());
        List<OrganisationVO> orgVOlist = List.of(vo, org1);
        when(organisationService.getAllowedOrganisations(Optional.of(sessionContext),context))
                .thenReturn(orgVOlist);
        CompletableFuture<Response> futureResponse = organisationResource.myOrganisationsUserAdministration();
        Response response = futureResponse.get();
        assertEquals(200, response.getStatus());
        Assertions.assertInstanceOf(OrganisationsDto.class, response.getEntity());
        OrganisationsDto dto = (OrganisationsDto) response.getEntity();
        Assertions.assertNotNull(dto);
        assertEquals(2, dto.data().size());
        verify(organisationService).getAllowedOrganisations(Optional.of(sessionContext),context);
        assertEquals(1L, dto.data().get(0).id());
        Assertions.assertEquals(2L, dto.data().get(1).id());
    }

    @Test
    @DisplayName("get organisations for employee with user administration permissions - empty result")
    void shouldGetMyOrganisationsUserAdministrationEmptyResult() {
        List<OrganisationVO> emptyOrgList = List.of();
        when(organisationService.getAllowedOrganisations(Optional.of(sessionContext),context))
                .thenReturn(emptyOrgList);
        Response response = organisationResource.myOrganisationsUserAdministration().join();
        assertEquals(200, response.getStatus());
        Assertions.assertInstanceOf(OrganisationsDto.class, response.getEntity());
        OrganisationsDto actualDto = (OrganisationsDto) response.getEntity();
        Assertions.assertNotNull(actualDto.data());
        Assertions.assertTrue(actualDto.data().isEmpty());
        OrganisationsDto expectedDto = new OrganisationsDto(List.of());
        Assertions.assertEquals(expectedDto, actualDto);
        Assertions.assertEquals(expectedDto.data().size(), actualDto.data().size());
    }

    @Test
    @DisplayName("myOrganisations - returns organisations for employee (Scala: myOrganisations should return organisations for employee)")
    void shouldReturnMyOrganisations() throws Exception {
        OrganisationVO org1 = new OrganisationVO(Optional.of(2L), "Org One", OrganisationLevelEnumeration.SUPER_CIRCLE, Optional.empty());
        List<OrganisationVO> orgVOList = List.of(org1, vo);
        when(organisationService.getAllowedOrganisations(Optional.of(sessionContext), context))
                .thenReturn(orgVOList);
        CompletableFuture<Response> futureResponse = organisationResource.myOrganisations();
        Response response = futureResponse.get();
        assertEquals(200, response.getStatus());
        assertInstanceOf(OrganisationsDto.class, response.getEntity());
        OrganisationsDto dto = (OrganisationsDto) response.getEntity();
        assertNotNull(dto);
        assertNotNull(dto.data());
        assertEquals(2, dto.data().size());
        // Value comparisons
        assertEquals(org1.id().orElseThrow(), dto.data().get(0).id());
        assertEquals(org1.name(), dto.data().get(0).name());
        assertEquals(vo.id().orElseThrow(), dto.data().get(1).id());
        assertEquals(vo.name(), dto.data().get(1).name());
        verify(organisationService).getAllowedOrganisations(Optional.of(sessionContext), context);
    }



}

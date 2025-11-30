package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.attribute.AttributeDto;
import com.ing.api.contacting.dto.java.resource.attribute.AttributeDtos;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.AttributeMapperJava;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.AttributeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AttributeResourceJavaTest {

    @Mock
    private AttributeService attributeService;

    @InjectMocks
    private AttributeResourceJava attributeResourceJava;

    private TestBaseResource resource;

    @BeforeEach
    void setUp() {
        resource = spy(new TestBaseResource());
        attributeResourceJava = spy(new AttributeResourceJava(attributeService) {
            @Override
            protected ContactingContext getContactingContext() {
                return resource.getContactingContext();
            }
        });
    }

    @Nested
    @DisplayName("getGroupedAttributes")
    class GetGroupedAttributesTest {

        @Test
        @DisplayName("should return grouped attributes successfully")
        void shouldReturnGroupedAttributesSuccessfully() {
            // Arrange
            var dto = new AttributeDto(
                    Optional.of(1L),
                    "test",
                    "value",
                    "label",
                    "labelValue",
                    Optional.of("content"),
                    Optional.of(1)
            );
            var groupedDtos = new AttributeDtos(List.of(dto)).toGroupedDtos();

            when(attributeService.getAll(any())).thenReturn(List.of(dto));

            try (var mockedConstruction = mockConstruction(AttributeDtos.class,
                    (mock, ctx) -> when(mock.toGroupedDtos()).thenReturn(groupedDtos))) {

                // Act
                var response = attributeResourceJava.getGroupedAttributes(true).join();

                // Assert
                assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
                assertNotNull(response.getEntity());
                assertEquals(groupedDtos, response.getEntity());
                verify(attributeService).getAll(any());
            }
        }

    }

    @Nested
    @DisplayName("getAttribute")
    class GetAttributeTest {

        @Test
        @DisplayName("should return attribute when found")
        void shouldReturnAttributeWhenFound() {
            var entity = new AttributeEntity(1L, "test", "value", "label", "labelValue", "content", 1, 1L);
            var dto = AttributeMapperJava.toDTO(entity);

            when(attributeService.findById(eq(1L), any())).thenReturn(dto);
            try (var mapperMock = mockStatic(AttributeMapperJava.class)) {
                mapperMock.when(() -> AttributeMapperJava.toDTO(entity)).thenReturn(dto);

                var response = attributeResourceJava.getAttribute(1L, false).join();

                assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
                assertEquals(dto, response.getEntity());
            }
        }

    }

    @Nested
    @DisplayName("createAttribute")
    class CreateAttributeTest {

        @Test
        @DisplayName("should create attribute successfully")
        void shouldCreateAttributeSuccessfully() {
            var dto = mock(AttributeDto.class);
            var entity = mock(AttributeEntity.class);

            try (var mapperMock = mockStatic(AttributeMapperJava.class)) {
                mapperMock.when(() -> AttributeMapperJava.toEntity(eq(dto), any())).thenReturn(entity);
                when(attributeService.save(any(), any())).thenReturn(1L);

                var response = attributeResourceJava.createAttribute(dto).join();

                assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
                assertEquals("1", response.getEntity());
            }
        }

        @Test
        @DisplayName("should handle validation error")
        void shouldHandleValidationError() {
            var dto = new AttributeDto(
                    Optional.empty(),
                    "s",
                    "null",
                    "label",
                    "labelValue",
                    Optional.empty(),
                    Optional.empty()
            );
            var mockEntity = mock(AttributeEntity.class);

            try (var mapperMock = mockStatic(AttributeMapperJava.class)) {
                mapperMock.when(() -> AttributeMapperJava.toEntity(eq(dto), any())).thenReturn(mockEntity);
                when(attributeService.save(any(AttributeDto.class), any())).thenThrow(Errors.valueMissing("Invalid data"));

                var exception = assertThrows(ApplicationEsperantoException.class, () -> {
                    attributeResourceJava.createAttribute(dto).join();
                });

                assertEquals("Invalid data", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("updateAttribute")
    class UpdateAttributeTest {

        @Test
        @DisplayName("should update attribute successfully")
        void shouldUpdateAttributeSuccessfully() {
            var dto = mock(AttributeDto.class);

            when(attributeService.update(eq(1L), eq(dto), any())).thenReturn(dto); // Changed from dto to entity

            var response = attributeResourceJava.updateAttribute(1L, dto).join();

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(dto, response.getEntity()); // Now matches the mock return value
        }


        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() {
            var dto = mock(AttributeDto.class);

            when(attributeService.update(eq(2L), eq(dto), any()))
                .thenThrow(Errors.notFound("attribute not found"));

            var response = attributeResourceJava.updateAttribute(2L, dto).join();

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Nested
    @DisplayName("updateAttributes")
    class UpdateAttributesTest {

        @Test
        @DisplayName("should update multiple attributes successfully")
        void shouldUpdateMultipleAttributesSuccessfully() {
            var dtos = List.of(mock(AttributeDto.class), mock(AttributeDto.class));

            doNothing().when(attributeService).updateAttributes(eq(dtos), any());

            var response = attributeResourceJava.updateAttributes(dtos).join();

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(null, response.getEntity());
        }

        @Test
        @DisplayName("should handle batch update error")
        void shouldHandleBatchUpdateError() {
            // Given: one valid and one invalid AttributeDto (missing id)
            var validDto = new AttributeDto(
                    Optional.of(1L),
                    "test1",
                    "value1",
                    "label1",
                    "labelValue1",
                    Optional.of("content1"),
                    Optional.of(1)
            );
            var invalidDto = new AttributeDto(
                    Optional.empty(), // missing id to trigger error
                    "test2",
                    "value2",
                    "label2",
                    "labelValue2",
                    Optional.of("content2"),
                    Optional.of(2)
            );
            var dtos = List.of(validDto, invalidDto);

            doThrow(Errors.badRequest("Attribute id missing for update"))
                    .when(attributeService).updateAttributes(eq(dtos), any(ContactingContext.class));

            // When & Then: assert the exception is thrown
            var exception = assertThrows(ApplicationEsperantoException.class, () -> {
                attributeResourceJava.updateAttributes(dtos).join();
            });

            assertEquals("Attribute id missing for update", exception.getMessage());
            verify(attributeService).updateAttributes(eq(dtos), any(ContactingContext.class));
        }


    }

    @Nested
    @DisplayName("deleteAttribute")
    class DeleteAttributeTest {

        @Test
        @DisplayName("should delete attribute successfully")
        void shouldDeleteAttributeSuccessfully() {
            doNothing().when(attributeService).deleteById(eq(1L), any());

            var response = attributeResourceJava.deleteAttribute(1L).toCompletableFuture().join();

            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent attribute")
        void shouldReturn404WhenDeletingNonExistentAttribute() {
            doThrow(Errors.notFound("attribute not found"))
                .when(attributeService).deleteById(eq(2L), any());

            var response = attributeResourceJava.deleteAttribute(2L).toCompletableFuture().join();

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    private static class TestBaseResource extends BaseResourceJava {
        @Override
        protected ContactingContext getContactingContext() {
            return mock(ContactingContext.class);
        }
    }
}
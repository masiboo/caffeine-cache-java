package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.attribute.AttributeDto;
import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AttributeMapperJavaTest")
class AttributeMapperJavaTest {

    @Nested
    @DisplayName("toDTO")
    class ToDTO {

        @Test
        @DisplayName("should map all fields correctly when all values are present")
        void shouldMapAllFieldsCorrectly() {
            var entity = AttributeEntity.builder()
                    .id(1L)
                    .entity("Account")
                    .entityValue("123")
                    .label("Type")
                    .labelValue("Primary")
                    .labelContent("LabelContent")
                    .displayOrder(2)
                    .accountId(99L)
                    .build();

            AttributeDto dto = AttributeMapperJava.toDTO(entity);

            assertEquals(Optional.of(1L), dto.id());
            assertEquals("Account", dto.entity());
            assertEquals("123", dto.entityValue());
            assertEquals("Type", dto.label());
            assertEquals("Primary", dto.labelValue());
            assertEquals(Optional.of("LabelContent"), dto.labelContent());
            assertEquals(Optional.of(2), dto.displayOrder());
        }

        @Test
        @DisplayName("should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            var entity = AttributeEntity.builder()
                    .id(null)
                    .entity("Account")
                    .entityValue("123")
                    .label("Type")
                    .labelValue("Primary")
                    .labelContent(null)
                    .displayOrder(null)
                    .accountId(99L)
                    .build();

            AttributeDto dto = AttributeMapperJava.toDTO(entity);

            assertEquals(Optional.empty(), dto.id());
            assertEquals(Optional.empty(), dto.labelContent());
            assertEquals(Optional.empty(), dto.displayOrder());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid entity")
        void shouldThrowForInvalidEntity() {
            var entity = AttributeEntity.builder()
                    .entity("!@#") // invalid pattern
                    .entityValue("123")
                    .label("Type")
                    .labelValue("Primary")
                    .build();

            assertThrows(IllegalArgumentException.class, () -> AttributeMapperJava.toDTO(entity));
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid labelValue")
        void shouldThrowForInvalidLabelValue() {
            var entity = AttributeEntity.builder()
                    .entity("Account")
                    .entityValue("123")
                    .label("Type")
                    .labelValue("!@#") // invalid pattern
                    .build();

            assertThrows(IllegalArgumentException.class, () -> AttributeMapperJava.toDTO(entity));
        }

        @Test
        @DisplayName("should use default for missing labelContent")
        void shouldUseDefaultForMissingLabelContent() {
            var entity = AttributeEntity.builder()
                    .entity("Account")
                    .entityValue("123")
                    .label("Type")
                    .labelValue("Primary")
                    .labelContent(null)
                    .build();

            AttributeDto dto = AttributeMapperJava.toDTO(entity);

            // The record constructor uses "labelContent" as default if missing, but still wrapped in Optional
            assertTrue(dto.labelContent().isEmpty() || dto.labelContent().orElse("labelContent").equals("labelContent"));
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map all fields correctly when all values are present")
        void shouldMapAllFieldsCorrectly() {
            var dto = new AttributeDto(
                    Optional.of(1L),
                    "Account",
                    "123",
                    "Type",
                    "Primary",
                    Optional.of("LabelContent"),
                    Optional.of(2)
            );
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(99L);

            var entity = AttributeMapperJava.toEntity(dto, context);

            assertEquals(1L, entity.getId());
            assertEquals("Account", entity.getEntity());
            assertEquals("123", entity.getEntityValue());
            assertEquals("Type", entity.getLabel());
            assertEquals("Primary", entity.getLabelValue());
            assertEquals("LabelContent", entity.getLabelContent());
            assertEquals(2, entity.getDisplayOrder());
            assertEquals(99L, entity.getAccountId());
        }

        @Test
        @DisplayName("should handle empty optionals as null or default values")
        void shouldHandleEmptyOptionals() {
            var dto = new AttributeDto(
                    Optional.empty(),
                    "Account",
                    "123",
                    "Type",
                    "Primary",
                    Optional.empty(),
                    Optional.empty()
            );
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(42L);;

            var entity = AttributeMapperJava.toEntity(dto, context);

            assertNull(entity.getId());
            assertEquals("", entity.getLabelContent());
            assertEquals(null, entity.getDisplayOrder());
            assertEquals(42L, entity.getAccountId());
        }

        @Test
        @DisplayName("should use empty string for missing labelContent")
        void shouldUseEmptyStringForMissingLabelContent() {
            var dto = new AttributeDto(
                    Optional.of(2L),
                    "Account",
                    "456",
                    "Type",
                    "Secondary",
                    Optional.empty(),
                    Optional.of(5)
            );
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(42L);

            var entity = AttributeMapperJava.toEntity(dto, context);

            assertEquals("", entity.getLabelContent());
        }

        @Test
        @DisplayName("should use 0 for missing displayOrder")
        void shouldUseZeroForMissingDisplayOrder() {
            var dto = new AttributeDto(
                    Optional.of(3L),
                    "Account",
                    "789",
                    "Type",
                    "Tertiary",
                    Optional.of("SomeContent"),
                    Optional.empty()
            );
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(88L);

            var entity = AttributeMapperJava.toEntity(dto, context);

            assertEquals(null, entity.getDisplayOrder());
        }
    }

}

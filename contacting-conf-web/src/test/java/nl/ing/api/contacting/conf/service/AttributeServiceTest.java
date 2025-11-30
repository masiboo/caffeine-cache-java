package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.attribute.AttributeDto;
import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;
import nl.ing.api.contacting.conf.mapper.AttributeMapperJava;
import nl.ing.api.contacting.conf.repository.AttributeCacheRepository;
import nl.ing.api.contacting.conf.repository.AttributeJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AttributeServiceTest")
class AttributeServiceTest {

    @Mock
    private AttributeJpaRepository repo;

    @InjectMocks
    private AttributeService attributeService;

    @Mock
    private AttributeCacheRepository attributeCacheRepository;


    private AutoCloseable mocks;

    private final ContactingContext context = mock(ContactingContext.class);

    private final AttributeEntity attribute = AttributeEntity.builder()
            .id(1L)
            .entity("entity1")
            .entityValue("entityValue1")
            .label("key1")
            .labelValue("value1")
            .labelContent("content1")
            .displayOrder(2)
            .accountId(1L)
            .build();

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(context.accountId()).thenReturn(42L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }


    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all attributes from cache")
        void shouldReturnAllAttributesFromCache() {
            var attributes = List.of(attribute);
            when(attributeCacheRepository.findAllForAccount(context)).thenReturn(attributes);

            var result = attributeService.getAll(context);

            assertThat(result).isEqualTo(attributes.stream().map(AttributeMapperJava::toDTO).toList());
            verify(attributeCacheRepository).findAllForAccount(context);
        }

        @Test
        @DisplayName("should return empty list when no attributes")
        void shouldReturnEmptyListWhenNoAttributes() {
            when(attributeCacheRepository.findAllForAccount(context)).thenReturn(List.of());

            var result = attributeService.getAll(context);

            assertThat(result).isEmpty();
        }
    }


    @Nested
    @DisplayName("findById")
    class FindById {

        private final Long attributeId = 1L;
        private final ContactingContext context = mock(ContactingContext.class);

        @Test
        @DisplayName("should return attribute when found")
        void shouldReturnAttributeWhenFound() {
            var attributeId = 1L;
            var context = mock(ContactingContext.class);
            var entity = AttributeEntity.builder()
                    .id(attributeId)
                    .entity("entity1")
                    .entityValue("entityValue1")
                    .label("key1")
                    .labelValue("value1")
                    .displayOrder(1)
                    .accountId(42L)
                    .build();

            when(attributeCacheRepository.findById(attributeId, context)).thenReturn(Optional.of(entity));
            when(context.accountId()).thenReturn(1L);

            var result = attributeService.findById(attributeId, context);

            assertThat(result).isEqualTo(AttributeMapperJava.toDTO(entity));
            verify(attributeCacheRepository).findById(attributeId, context);


        }

        @Test
        @DisplayName("should return empty when attribute not found")
        void shouldThrowNotFoundExceptionWhenAttributeDoesNotExist() {
            when(attributeCacheRepository.findById(attributeId, context)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> attributeService.findById(attributeId, context))
                    .isInstanceOf(nl.ing.api.contacting.conf.exception.ApplicationEsperantoException.class)
                    .hasMessageContaining("attribute with id 1 not found");

        }
    }

    @Nested
    @DisplayName("save")
    class SaveTests {
        @Test
        @DisplayName("should save and return attribute")
        void shouldSaveAndReturnAttribute() {
            var dto = AttributeMapperJava.toDTO(attribute);
            var mappedEntity = AttributeEntity.builder()
                    .id(1L)
                    .entity("entity1")
                    .entityValue("entityValue1")
                    .label("key1")
                    .labelValue("value1")
                    .labelContent("content1")
                    .displayOrder(2)
                    .accountId(1L)
                    .build();

            when(context.accountId()).thenReturn(1L);

            try (var mockedMapper = mockStatic(AttributeMapperJava.class)) {
                mockedMapper.when(() -> AttributeMapperJava.toEntity(dto, context))
                        .thenReturn(mappedEntity);
                when(repo.save(mappedEntity)).thenReturn(mappedEntity);

                var result = attributeService.save(dto, context);

                assertEquals(mappedEntity.getId(), result);
                verify(repo).save(mappedEntity);
            }
        }
    }

    @Nested
    @DisplayName("when attribute exists for account")
    class WhenAttributeExists {

        @Test
        @DisplayName("should delete attribute")
        void shouldDeleteAttribute() {
            var attributeId = 1L;
            var accountId = 42L;
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(accountId);

            var entity = mock(AttributeEntity.class);
            when(repo.findByIdAndAccountId(attributeId, accountId)).thenReturn(Optional.of(entity));

            attributeService.deleteById(attributeId, context);

            verify(repo).delete(entity);
        }
    }

    @Nested
    @DisplayName("when attribute does not exist for account")
    class WhenAttributeDoesNotExist {

        @Test
        @DisplayName("should throw not found error")
        void shouldThrowNotFound() {
            var attributeId = 2L;
            var accountId = 99L;
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(accountId);

            when(repo.findByIdAndAccountId(attributeId, accountId)).thenReturn(Optional.empty());

            var ex = assertThrows(RuntimeException.class, () -> attributeService.deleteById(attributeId, context));
            assertEquals("Attribute resource not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update attribute when id and entity exist")
        void shouldUpdateAttributeWhenIdAndEntityExist() {
            var id = 1L;
            var attributeDto = mock(AttributeDto.class);
            when(attributeDto.id()).thenReturn(Optional.of(id));
            var attributeEntity = mock(AttributeEntity.class);
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(1L);

            when(repo.findByIdAndAccountId(id, context.accountId()))
                    .thenReturn(Optional.of(attributeEntity));

            try (var mockedMapper = mockStatic(AttributeMapperJava.class)) {
                mockedMapper.when(() -> AttributeMapperJava.toEntity(attributeDto, context))
                        .thenReturn(attributeEntity);

                when(repo.save(attributeEntity)).thenReturn(attributeEntity);

                var result = attributeService.update(id, attributeDto, context);

                assertThat(result).isEqualTo(AttributeMapperJava.toDTO(attributeEntity));
                verify(attributeEntity).setId(id);
                verify(repo).save(attributeEntity);
            }
        }

        @Test
        @DisplayName("should throw badRequest when attribute id is missing")
        void shouldThrowBadRequestWhenAttributeIdMissing() {
                var id = 1L;
                var attributeDto = mock(AttributeDto.class);
                when(attributeDto.id()).thenReturn(Optional.empty());
                var context = mock(ContactingContext.class);
                when(context.accountId()).thenReturn(1L);

                assertThatThrownBy(() -> attributeService.update(id, attributeDto, context))
                        .isInstanceOf(nl.ing.api.contacting.conf.exception.ApplicationEsperantoException.class)
                        .hasMessageContaining("Attribute id missing for update");

            }

        @Test
        @DisplayName("should throw notFound when entity does not exist")
        void shouldThrowNotFoundWhenEntityDoesNotExist() {
            var id = 1L;
            var attributeDto = mock(AttributeDto.class);
            when(attributeDto.id()).thenReturn(Optional.of(id));
            var context = mock(ContactingContext.class);
            when(context.accountId()).thenReturn(1L);

            when(repo.findByIdAndAccountId(id, context.accountId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> attributeService.update(id, attributeDto, context))
                    .isInstanceOf(nl.ing.api.contacting.conf.exception.ApplicationEsperantoException.class)
                    .hasMessageContaining("attribute not found");
        }
    }


    @Nested
    @DisplayName("updateAttributes(List<AttributeDto>, ContactingContext)")
    class UpdateAttributes {

        @Mock
        private ContactingContext contactingContext;
        private AttributeService attributeServiceSpy;

        @BeforeEach
        void setUp() {

            MockitoAnnotations.openMocks(this);
            attributeServiceSpy = spy(attributeService);
        }

        @Test
        @DisplayName("should update all attributes when all DTOs have IDs")
        void shouldUpdateAllAttributes() {
            var dto1 = mock(AttributeDto.class);
            var dto2 = mock(AttributeDto.class);
            when(dto1.id()).thenReturn(Optional.of(1L));
            when(dto2.id()).thenReturn(Optional.of(2L));


            doReturn(mock(AttributeEntity.class)).when(attributeServiceSpy)
                    .updateAttribute(anyLong(), any(AttributeDto.class), any(ContactingContext.class));

            attributeServiceSpy.updateAttributes(List.of(dto1, dto2), contactingContext);


            verify(attributeServiceSpy, times(1)).updateAttribute(1L, dto1, contactingContext);
            verify(attributeServiceSpy, times(1)).updateAttribute(2L, dto2, contactingContext);
        }

        @Test
        @DisplayName("should throw badRequest if any attributeDto.id is empty")
        void shouldThrowBadRequestIfAnyIdMissing() {
            var dto1 = mock(AttributeDto.class);
            var dto2 = mock(AttributeDto.class);
            when(dto1.id()).thenReturn(Optional.of(1L));
            when(dto2.id()).thenReturn(Optional.empty());

            var ex = assertThrows(RuntimeException.class,
                    () -> attributeService.updateAttributes(List.of(dto1, dto2), contactingContext));
            assertTrue(ex.getMessage().contains("Attribute id missing for update"));
        }
    }


}

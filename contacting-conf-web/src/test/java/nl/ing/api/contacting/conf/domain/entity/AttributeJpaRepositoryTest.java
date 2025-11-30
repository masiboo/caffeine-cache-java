package nl.ing.api.contacting.conf.domain.entity;

import nl.ing.api.contacting.conf.repository.AttributeJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AttributeJpaRepositoryTest")
class AttributeJpaRepositoryTest {

    @Mock
    private AttributeJpaRepository attributeJpaRepository;

    AttributeJpaRepositoryTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("findByAccountId")
    class FindByAccountIdTest {

        @Test
        @DisplayName("should return attributes for given accountId")
        void shouldReturnAttributesForGivenAccountId() {
            var accountId = 1L;
            var expected = List.of(
                    AttributeEntity.builder()
                            .id(1L)
                            .accountId(accountId)
                            .label("key1")
                            .labelValue("value1")
                            .build(),
                    AttributeEntity.builder()
                            .id(2L)
                            .accountId(accountId)
                            .label("key2")
                            .labelValue("value2")
                            .build()
            );
            when(attributeJpaRepository.findByAccountId(accountId)).thenReturn(expected);

            var result = attributeJpaRepository.findByAccountId(accountId);

            assertThat(result).isEqualTo(expected);
            verify(attributeJpaRepository).findByAccountId(accountId);
        }

        @Test
        @DisplayName("should return empty list when no attributes found")
        void shouldReturnEmptyListWhenNoAttributesFound() {
            var accountId = 2L;
            when(attributeJpaRepository.findByAccountId(accountId)).thenReturn(List.of());

            var result = attributeJpaRepository.findByAccountId(accountId);

            assertThat(result).isEmpty();
            verify(attributeJpaRepository).findByAccountId(accountId);
        }
    }

    @Nested
    @DisplayName("findByIdAndAccountId")
    class FindByIdAndAccountIdTest {

        @Test
        @DisplayName("should return attribute when found")
        void shouldReturnAttributeWhenFound() {
            var id = 1L;
            var accountId = 10L;
            var expected = AttributeEntity.builder()
                    .id(id)
                    .accountId(accountId)
                    .label("key")
                    .labelValue("value")
                    .build();

            when(attributeJpaRepository.findByIdAndAccountId(id, accountId))
                    .thenReturn(Optional.of(expected));

            var result = attributeJpaRepository.findByIdAndAccountId(id, accountId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expected);
            verify(attributeJpaRepository).findByIdAndAccountId(id, accountId);
        }

        @Test
        @DisplayName("should return empty when attribute not found")
        void shouldReturnEmptyWhenAttributeNotFound() {
            var id = 2L;
            var accountId = 20L;

            when(attributeJpaRepository.findByIdAndAccountId(id, accountId))
                    .thenReturn(Optional.empty());

            var result = attributeJpaRepository.findByIdAndAccountId(id, accountId);

            assertThat(result).isEmpty();
            verify(attributeJpaRepository).findByIdAndAccountId(id, accountId);
        }
    }

}

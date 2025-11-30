package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistFunctionality;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistItemDto;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistType;
import nl.ing.api.contacting.conf.domain.entity.BlacklistEntity;
import nl.ing.api.contacting.conf.domain.model.blacklist.BlacklistItemVO;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import nl.ing.api.contacting.conf.repository.BlacklistJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {
    @Mock
    private BlacklistJpaRepository repository;

    private BlacklistService service;
    private ContactingContext context;

    @BeforeEach
    void setUp() {
        service = new BlacklistService(repository);
        context = new ContactingContext(123L, null);
    }

    @Nested
    @DisplayName("getAllBlacklistItems")
    class GetAllBlacklistItems {
        private BlacklistJpaRepository repository;
        private BlacklistService service;
        private ContactingContext context;

        @BeforeEach
        void setUp() {
            repository = mock(BlacklistJpaRepository.class);
            service = new BlacklistService(repository);
            context = new ContactingContext(123L, null);
        }

        private BlacklistEntity sampleEntity() {
            BlacklistEntity e = new BlacklistEntity();
            e.setId(123L);
            e.setAccountId(context.accountId());
            e.setFunctionality("SURVEY");
            e.setEntityType("PHONE_NUMBER");
            e.setValue("+31123456789");
            e.setStartDate(LocalDateTime.of(2025, 10, 25, 10, 0));
            e.setEndDate(null);
            return e;
        }

        private BlacklistItemVO sampleVO() {
            return new BlacklistItemVO(
                    1L,
                    BlacklistFunctionality.SURVEY,
                    BlacklistType.PHONE_NUMBER,
                    "+31123456789",
                    LocalDateTime.of(2025, 10, 25, 10, 0),
                    Optional.empty()
            );
        }

        @Test
        void getAllBlacklistItems_returnsDtoListWithAllFields() {
            when(repository.findActiveByAccount(eq(context.accountId()), any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleEntity()));

            List<BlacklistItemDto> result = service.getAllBlacklistItems(context);

            assertThat(result).hasSize(1);
            BlacklistItemDto dto = result.get(0);
            assertThat(dto.id().get()).isEqualTo(123L);
            assertThat(dto.functionality().value()).isEqualTo("SURVEY");
            assertThat(dto.value()).isEqualTo("+31123456789");
            verify(repository).findActiveByAccount(eq(context.accountId()), any(LocalDateTime.class));
        }

        @Test
        void getAllByFunctionality_allFunctionality_returnsTwoActiveItems() {
            BlacklistEntity entity1 = sampleEntity();
            BlacklistEntity entity2 = new BlacklistEntity();
            entity2.setId(124L);
            entity2.setAccountId(context.accountId());
            entity2.setFunctionality("SURVEY");
            entity2.setEntityType("PHONE_NUMBER");
            entity2.setValue("+31123456780");
            entity2.setStartDate(LocalDateTime.of(2025, 10, 25, 11, 0));
            entity2.setEndDate(null);

            when(repository.findActiveByAccount(eq(context.accountId()), any()))
                    .thenReturn(List.of(entity1, entity2));

            List<BlacklistItemDto> result = service.getAllByFunctionality("ALL", context);

            assertThat(result).hasSize(2);
            verify(repository).findActiveByAccount(eq(context.accountId()), any());
        }

        @Test
        void getAllByFunctionality_surveyFunctionality_returnsOneItem() {
            BlacklistEntity entity = sampleEntity();
            when(repository.findByAccountIdAndFunctionalityAndActive(eq(context.accountId()), eq("SURVEY"), any()))
                    .thenReturn(List.of(entity));

            List<BlacklistItemDto> result = service.getAllByFunctionality("SURVEY", context);

            assertThat(result).hasSize(1);
            verify(repository).findByAccountIdAndFunctionalityAndActive(eq(context.accountId()), eq("SURVEY"), any());
        }


        @Test
        void getAllByFunctionality_allFunctionality_returnsActiveItems() {
            BlacklistEntity entity = sampleEntity();
            when(repository.findActiveByAccount(eq(context.accountId()), any()))
                    .thenReturn(List.of(entity));

            List<BlacklistItemDto> result = service.getAllByFunctionality("ALL", context);

            assertThat(result).hasSize(1);
            BlacklistItemDto dto = result.get(0);
            assertThat(dto.id().get()).isEqualTo(entity.getId());
            assertThat(dto.functionality().value()).isEqualTo(entity.getFunctionality());
            assertThat(dto.value()).isEqualTo(entity.getValue());
            verify(repository).findActiveByAccount(eq(context.accountId()), any());
        }

        @Test
        void getAllByFunctionality_specificFunctionality_returnsFilteredItems() {
            BlacklistEntity entity = sampleEntity();
            when(repository.findByAccountIdAndFunctionalityAndActive(eq(context.accountId()), eq("SURVEY"), any()))
                    .thenReturn(List.of(entity));

            List<BlacklistItemDto> result = service.getAllByFunctionality("SURVEY", context);

            assertThat(result).hasSize(1);
            BlacklistItemDto dto = result.get(0);
            assertThat(dto.id().get()).isEqualTo(entity.getId());
            assertThat(dto.functionality().value()).isEqualTo(entity.getFunctionality());
            assertThat(dto.value()).isEqualTo(entity.getValue());
            verify(repository).findByAccountIdAndFunctionalityAndActive(eq(context.accountId()), eq("SURVEY"), any());
        }

        @Test
        void getAllByFunctionality_invalidFunctionality_throwsError() {
            assertThrows(ApplicationEsperantoException.class, () ->
                    service.getAllByFunctionality("INVALID", context));
        }

        @Test
        void createBlackListItem_savesEntityAndReturnsAllFields() {
            BlacklistEntity entity = sampleEntity();
            when(repository.save(any())).thenReturn(entity);

            BlacklistItemVO saved = service.createBlackListItem(sampleVO(), context);

            assertThat(saved.getId().get()).isEqualTo(entity.getId());
            verify(repository).save(any());
        }

        @Test
        void updateBlackListItem_updatesEntityAndReturnsVO() {
            BlacklistEntity entity = sampleEntity();
            when(repository.save(any())).thenReturn(entity);

            BlacklistItemVO updated = service.updateBlackListItem(sampleVO(), context);

            assertThat(updated.getId().get()).isEqualTo(entity.getId());
            assertThat(updated.functionality().value()).isEqualTo(entity.getFunctionality());
            assertThat(updated.value()).isEqualTo(entity.getValue());
            verify(repository).save(any());
        }


        @Test
        void deleteBlackListItem_callsRepository() {
            Long id = 1L;
            doNothing().when(repository).deleteById(id);

            service.deleteBlackListItem(id);

            verify(repository).deleteById(id);
        }
    }
}

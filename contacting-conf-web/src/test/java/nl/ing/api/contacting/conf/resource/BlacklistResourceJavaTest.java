package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistFunctionality;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistType;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistItemDto;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.domain.model.blacklist.BlacklistItemVO;
import nl.ing.api.contacting.conf.service.BlacklistService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlacklistResourceJavaTest {

    @Mock
    private BlacklistService mockService;

    private BlacklistResourceJava resource;

    @BeforeEach
    void setUp() {
        resource = new TestableBlacklistResourceJava(mockService);
    }

    @Nested
    @DisplayName("getByFunctionality")
    class GetByFunctionality {
        @Test
        @DisplayName("should return blacklist items by functionality")
        void shouldReturnBlacklistItemsByFunctionality() throws Exception {

            List<BlacklistItemDto> mockedDtos = List.of(TestData.blacklistItemDto());
            when(mockService.getAllByFunctionality(any(), any())).thenReturn(mockedDtos);

            CompletableFuture<Response> responseFuture = resource.getByFunctionality("LOGIN");
            Response response = responseFuture.get();
            assertEquals(200, response.getStatus());
        }
    }

    @Nested
    @DisplayName("getAllBlackListItems")
    class GetAllBlackListItems {
        @Test
        @DisplayName("should return all blacklist items")
        void shouldReturnAllBlacklistItems() throws Exception {
            List<BlacklistItemDto> mockedDtos = List.of(TestData.blacklistItemDto());
            when(mockService.getAllBlacklistItems(any())).thenReturn(mockedDtos);

            CompletableFuture<Response> responseFuture = resource.getAllBlackListItems();
            Response response = responseFuture.get();
            assertEquals(200, response.getStatus());
        }
    }

    @Nested
    @DisplayName("createBlackListItem")
    class CreateBlackListItem {
        @Test
        @DisplayName("should create blacklist item and return id")
        void shouldCreateBlacklistItem() throws Exception {
            BlacklistItemVO vo = TestData.blacklistItemVO();
            when(mockService.createBlackListItem(any(), any())).thenReturn(vo);

            BlacklistItemDto dto = TestData.blacklistItemDto();
            CompletableFuture<Response> responseFuture = resource.createBlackListItem(dto);
            Response response = responseFuture.get();
            assertEquals(201, response.getStatus());
        }
    }

    @Nested
    @DisplayName("updateBlackListItem")
    class UpdateBlackListItem {
        @Test
        @DisplayName("should update blacklist item and return dto")
        void shouldUpdateBlacklistItem() throws Exception {
            BlacklistItemVO vo = TestData.blacklistItemVO();
            when(mockService.updateBlackListItem(any(), any())).thenReturn(vo);

            BlacklistItemDto dto = TestData.blacklistItemDto();
            CompletableFuture<Response> responseFuture = resource.updateBlackListItem(1L, dto);
            Response response = responseFuture.get();
            assertEquals(200, response.getStatus());
        }
    }

    @Nested
    @DisplayName("deleteBlackListItem")
    class DeleteBlackListItem {
        @Test
        @DisplayName("should delete blacklist item")
        void shouldDeleteBlacklistItem() throws Exception {
            doNothing().when(mockService).deleteBlackListItem(anyLong());
            CompletableFuture<Response> future = resource.deleteBlackListItem(1L);
            future.get();
            verify(mockService).deleteBlackListItem(eq(1L));
        }
    }

    static class TestableBlacklistResourceJava extends BlacklistResourceJava {
        TestableBlacklistResourceJava(BlacklistService service) {
            super(service);
        }
        @Override
        protected ContactingContext getContactingContext() {
            return new ContactingContext(101L, null);
        }
    }

    static class TestData {
        static BlacklistItemVO blacklistItemVO() {
            return new BlacklistItemVO(
                    1L,
                    BlacklistFunctionality.ALL,
                    BlacklistType.PHONE_NUMBER,
                    "testuser",
                    LocalDateTime.now(),
                    Optional.empty()
            );
        }
        static BlacklistItemDto blacklistItemDto() {
            // Minimal stub, adjust fields as needed for your DTO
            return new BlacklistItemDto(
                    Optional.of(1L),
                    BlacklistFunctionality.ALL,
                    BlacklistType.PHONE_NUMBER,
                    "testuser",
                    LocalDateTime.now().toString(),
                    Optional.empty()
            );
        }
    }
}


package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataOptionsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingsMetadataOptionsRepositoryJavaImplTest {

    private SettingsMetadataOptionsJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(SettingsMetadataOptionsJpaRepository.class);
    }

    @Test
    void testFindAll_Positive() throws Exception {
        SettingsMetadataWithOptions option = mock(SettingsMetadataWithOptions.class);
        when(repository.findAllWithMetadata()).thenReturn(List.of(option));

        CompletableFuture<List<SettingsMetadataWithOptions>> future = CompletableFuture.completedFuture(repository.findAllWithMetadata());
        List<SettingsMetadataWithOptions> result = future.get();

        assertEquals(1, result.size());
        assertTrue(result.contains(option));
    }

    @Test
    void testFindAll_Negative() throws Exception {
        when(repository.findAllWithMetadata()).thenReturn(Collections.emptyList());

        CompletableFuture<List<SettingsMetadataWithOptions>> future = CompletableFuture.completedFuture(repository.findAllWithMetadata());
        List<SettingsMetadataWithOptions> result = future.get();

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByName_Positive() throws Exception {
        SettingsMetadataWithOptions option = mock(SettingsMetadataWithOptions.class);
        when(repository.findByMetadataName("testName")).thenReturn(List.of(option));

        CompletableFuture<List<SettingsMetadataWithOptions>> future = CompletableFuture.completedFuture(repository.findByMetadataName("testName"));

        List<SettingsMetadataWithOptions> result = future.get();

        assertEquals(1, result.size());
        assertTrue(result.contains(option));
    }

    @Test
    void testFindByName_Negative() throws Exception {
        when(repository.findByMetadataName("testName")).thenReturn(Collections.emptyList());

        CompletableFuture<List<SettingsMetadataWithOptions>> future = CompletableFuture.completedFuture(repository.findByMetadataName("testName"));

        List<SettingsMetadataWithOptions> result = future.get();

        assertTrue(result.isEmpty());
    }
}
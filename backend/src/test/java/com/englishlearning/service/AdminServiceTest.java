package com.englishlearning.service;

import com.englishlearning.domain.enums.Role;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.dto.AdminExerciseRequest;
import com.englishlearning.exception.ResourceNotFoundException;
import com.englishlearning.repository.DictionaryEntryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.WordOfDayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del servicio admin. La autorización por rol la cubre Spring Security
 * en SecurityConfig (basta con un test de integración de PreAuthorize en el
 * controller); aquí cubrimos las dos cosas que importan a este servicio:
 * (1) los stats que componemos en getStats() y
 * (2) que CRUD lanza 404 cuando el id no existe.
 */
class AdminServiceTest {

    private UserRepository userRepository;
    private ExerciseRepository exerciseRepository;
    private DictionaryEntryRepository dictionaryEntryRepository;
    private WordOfDayRepository wordOfDayRepository;
    private AdminService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        exerciseRepository = mock(ExerciseRepository.class);
        dictionaryEntryRepository = mock(DictionaryEntryRepository.class);
        wordOfDayRepository = mock(WordOfDayRepository.class);
        service = new AdminService(userRepository, exerciseRepository,
                dictionaryEntryRepository, wordOfDayRepository);
    }

    @Test
    @DisplayName("getStats agrega counts de usuarios, admins, ejercicios y diccionario")
    void getStatsAggregates() {
        when(userRepository.count()).thenReturn(120L);
        when(userRepository.countByRole(eq(Role.ADMIN))).thenReturn(3L);
        when(exerciseRepository.count()).thenReturn(500L);
        when(dictionaryEntryRepository.count()).thenReturn(40L);

        var stats = service.getStats();

        assertEquals(120L, stats.totalUsers());
        assertEquals(3L, stats.totalAdminUsers());
        assertEquals(500L, stats.totalExercises());
        assertEquals(40L, stats.totalDictionaryEntries());
    }

    @Test
    @DisplayName("createExercise persiste el ejercicio con los campos del request")
    void createExerciseHappyPath() {
        AdminExerciseRequest req = new AdminExerciseRequest(
                1L, 2L, 5, "Mi ejercicio", "API", 8, 6, 50, false);
        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Exercise created = service.createExercise(req);

        assertEquals("Mi ejercicio", created.getTitle());
        assertEquals(5, created.getPosition());
        assertEquals(50, created.getXpReward());
        verify(exerciseRepository, times(1)).save(any(Exercise.class));
    }

    @Test
    @DisplayName("updateExercise sobre id inexistente lanza ResourceNotFoundException")
    void updateExerciseMissingId() {
        when(exerciseRepository.findById(eq(999L))).thenReturn(Optional.empty());

        AdminExerciseRequest req = new AdminExerciseRequest(
                1L, 2L, 5, "X", "Y", 8, 6, 50, false);
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateExercise(999L, req));
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteExercise sobre id inexistente lanza 404 sin tocar el repo")
    void deleteExerciseMissingId() {
        when(exerciseRepository.existsById(eq(999L))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteExercise(999L));
        verify(exerciseRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteExercise sobre id existente delega en repository")
    void deleteExerciseHappyPath() {
        when(exerciseRepository.existsById(eq(42L))).thenReturn(true);

        service.deleteExercise(42L);

        verify(exerciseRepository, times(1)).deleteById(42L);
    }

    @Test
    @DisplayName("deleteDictionaryEntry y deleteWordOfDay validan existencia antes de borrar")
    void deletesValidateExistence() {
        when(dictionaryEntryRepository.existsById(eq(7L))).thenReturn(false);
        when(wordOfDayRepository.existsById(eq(7L))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteDictionaryEntry(7L));
        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteWordOfDay(7L));
    }
}

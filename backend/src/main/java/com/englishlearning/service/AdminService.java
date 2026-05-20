package com.englishlearning.service;

import com.englishlearning.domain.enums.Role;
import com.englishlearning.domain.model.DictionaryEntry;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.domain.model.WordOfDay;
import com.englishlearning.dto.AdminDictionaryRequest;
import com.englishlearning.dto.AdminExerciseRequest;
import com.englishlearning.dto.AdminStatsResponse;
import com.englishlearning.dto.AdminUserResponse;
import com.englishlearning.dto.AdminWordOfDayRequest;
import com.englishlearning.exception.ResourceNotFoundException;
import com.englishlearning.repository.DictionaryEntryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.WordOfDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final DictionaryEntryRepository dictionaryEntryRepository;
    private final WordOfDayRepository wordOfDayRepository;

    public List<AdminUserResponse> listUsers() {
        List<Object[]> rows = userRepository.findAllWithStats();
        List<AdminUserResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            User user = (User) row[0];
            UserStreak streak = (UserStreak) row[1];
            result.add(new AdminUserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName(),
                    user.getRole(),
                    user.getCreatedAt(),
                    streak != null ? streak.getTotalXp() : 0,
                    streak != null ? streak.getCurrentStreak() : 0
            ));
        }
        return result;
    }

    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalAdminUsers = userRepository.countByRole(Role.ADMIN);
        long totalExercises = exerciseRepository.count();
        long totalDictionaryEntries = dictionaryEntryRepository.count();
        return new AdminStatsResponse(
                totalUsers, totalAdminUsers, totalExercises, totalDictionaryEntries);
    }

    public List<Exercise> listExercises() {
        return exerciseRepository.findAll();
    }

    @Transactional
    public Exercise createExercise(AdminExerciseRequest request) {
        return exerciseRepository.save(Exercise.builder()
                .levelId(request.levelId())
                .blockId(request.blockId())
                .position(request.position())
                .title(request.title())
                .topic(request.topic())
                .questionsCount(request.questionsCount())
                .estimatedMinutes(request.estimatedMinutes())
                .xpReward(request.xpReward())
                .locked(request.locked() != null ? request.locked() : false)
                .build());
    }

    @Transactional
    public Exercise updateExercise(Long id, AdminExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
        exercise.setLevelId(request.levelId());
        exercise.setBlockId(request.blockId());
        exercise.setPosition(request.position());
        exercise.setTitle(request.title());
        exercise.setTopic(request.topic());
        exercise.setQuestionsCount(request.questionsCount());
        exercise.setEstimatedMinutes(request.estimatedMinutes());
        exercise.setXpReward(request.xpReward());
        exercise.setLocked(request.locked() != null ? request.locked() : false);
        return exerciseRepository.save(exercise);
    }

    @Transactional
    public void deleteExercise(Long id) {
        if (!exerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise not found: " + id);
        }
        exerciseRepository.deleteById(id);
    }

    public List<DictionaryEntry> listDictionary() {
        return dictionaryEntryRepository.findAll();
    }

    @Transactional
    public DictionaryEntry createDictionaryEntry(AdminDictionaryRequest request) {
        return dictionaryEntryRepository.save(DictionaryEntry.builder()
                .word(request.word())
                .phonetic(request.phonetic())
                .partOfSpeech(request.partOfSpeech())
                .definitionEs(request.definitionEs())
                .exampleEn(request.exampleEn())
                .exampleEs(request.exampleEs())
                .categoryType(request.categoryType())
                .levelCode(request.levelCode())
                .build());
    }

    @Transactional
    public DictionaryEntry updateDictionaryEntry(Long id, AdminDictionaryRequest request) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dictionary entry not found: " + id));
        entry.setWord(request.word());
        entry.setPhonetic(request.phonetic());
        entry.setPartOfSpeech(request.partOfSpeech());
        entry.setDefinitionEs(request.definitionEs());
        entry.setExampleEn(request.exampleEn());
        entry.setExampleEs(request.exampleEs());
        entry.setCategoryType(request.categoryType());
        entry.setLevelCode(request.levelCode());
        return dictionaryEntryRepository.save(entry);
    }

    @Transactional
    public void deleteDictionaryEntry(Long id) {
        if (!dictionaryEntryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dictionary entry not found: " + id);
        }
        dictionaryEntryRepository.deleteById(id);
    }

    public List<WordOfDay> listWordsOfDay() {
        return wordOfDayRepository.findAll();
    }

    @Transactional
    public WordOfDay createWordOfDay(AdminWordOfDayRequest request) {
        return wordOfDayRepository.save(WordOfDay.builder()
                .onDate(request.onDate())
                .word(request.word())
                .phonetic(request.phonetic())
                .partOfSpeech(request.partOfSpeech())
                .definitionEs(request.definitionEs())
                .exampleEn(request.exampleEn())
                .exampleEs(request.exampleEs())
                .build());
    }

    @Transactional
    public WordOfDay updateWordOfDay(Long id, AdminWordOfDayRequest request) {
        WordOfDay word = wordOfDayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Word of Day not found: " + id));
        word.setOnDate(request.onDate());
        word.setWord(request.word());
        word.setPhonetic(request.phonetic());
        word.setPartOfSpeech(request.partOfSpeech());
        word.setDefinitionEs(request.definitionEs());
        word.setExampleEn(request.exampleEn());
        word.setExampleEs(request.exampleEs());
        return wordOfDayRepository.save(word);
    }

    @Transactional
    public void deleteWordOfDay(Long id) {
        if (!wordOfDayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Word of Day not found: " + id);
        }
        wordOfDayRepository.deleteById(id);
    }
}
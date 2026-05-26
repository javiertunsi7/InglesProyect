package com.englishlearning.config;

import com.englishlearning.config.seed.SeedCatalog;
import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.enums.QuestionType;
import com.englishlearning.domain.enums.Role;
import com.englishlearning.domain.model.Block;
import com.englishlearning.domain.model.Category;
import com.englishlearning.domain.model.DictionaryEntry;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.Level;
import com.englishlearning.domain.model.Question;
import com.englishlearning.domain.model.QuestionOption;
import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.domain.model.WordOfDay;
import com.englishlearning.repository.BlockRepository;
import com.englishlearning.repository.CategoryRepository;
import com.englishlearning.repository.DictionaryEntryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.LevelRepository;
import com.englishlearning.repository.QuestionOptionRepository;
import com.englishlearning.repository.QuestionRepository;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.UserStreakRepository;
import com.englishlearning.repository.WordOfDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database on startup with the full content catalog (categories,
 * levels, thematic blocks, exercises, questions, options), one demo user with
 * realistic gamification stats and the daily word.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final BlockRepository blockRepository;
    private final ExerciseRepository exerciseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final UserRepository userRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserStreakRepository streakRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final WordOfDayRepository wordOfDayRepository;
    private final DictionaryEntryRepository dictionaryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            seedContent();
        } else {
            log.info("La base de datos ya tiene contenido. Saltando carga inicial.");
        }
        if (wordOfDayRepository.count() == 0) {
            seedWordsOfDay();
        }
        if (dictionaryRepository.count() == 0) {
            seedDictionary();
        }
        seedDemoUserAndProgress();
    }

    private void seedContent() {
        Category general = categoryRepository.save(Category.builder()
                .type(CategoryType.GENERAL)
                .displayName("Inglés General")
                .tagline("Conversación cotidiana")
                .description("Inglés cotidiano: gramática, vocabulario, conversación y comprensión auditiva.")
                .position(1)
                .build());
        Category tech = categoryRepository.save(Category.builder()
                .type(CategoryType.TECH)
                .displayName("Inglés Técnico")
                .tagline("Conversación técnica")
                .description("Inglés enfocado a programadores: APIs, arquitectura, documentación y entrevistas.")
                .position(2)
                .build());

        SeedCatalog.generalLevels().forEach(seed -> persistLevel(general, seed));
        SeedCatalog.techLevels().forEach(seed -> persistLevel(tech, seed));
        log.info("Catálogo cargado: {} niveles, {} ejercicios, {} preguntas.",
                levelRepository.count(), exerciseRepository.count(), questionRepository.count());
    }

    private void persistLevel(Category category, SeedCatalog.LevelSeed seed) {
        Level level = levelRepository.save(Level.builder()
                .categoryId(category.getId())
                .code(seed.code())
                .position(seed.position())
                .displayName(seed.displayName())
                .headline(seed.headline())
                .description(seed.description())
                .estimatedHours(seed.estimatedHours())
                .totalExercises(50)
                .locked(seed.locked())
                .build());

        for (SeedCatalog.BlockSeed blockSeed : seed.blocks()) {
            Block block = blockRepository.save(Block.builder()
                    .levelId(level.getId())
                    .position(blockSeed.position())
                    .title(blockSeed.title())
                    .subtitle(blockSeed.subtitle())
                    .startExercise(blockSeed.startExercise())
                    .endExercise(blockSeed.endExercise())
                    .build());
            int position = blockSeed.startExercise();
            for (SeedCatalog.ExerciseSeed exSeed : blockSeed.exercises()) {
                Exercise exercise = exerciseRepository.save(Exercise.builder()
                        .levelId(level.getId())
                        .blockId(block.getId())
                        .position(position)
                        .title(exSeed.title())
                        .topic(exSeed.topic())
                        .questionsCount(exSeed.questions().size())
                        .estimatedMinutes(exSeed.estimatedMinutes())
                        .xpReward(exSeed.xpReward())
                        .locked(seed.locked())
                        .build());
                int qPosition = 1;
                for (SeedCatalog.QuestionSeed qSeed : exSeed.questions()) {
                    Question question = questionRepository.save(Question.builder()
                            .exerciseId(exercise.getId())
                            .position(qPosition++)
                            .type(qSeed.type())
                            .prompt(qSeed.prompt())
                            .promptHighlight(qSeed.promptHighlight())
                            .context(qSeed.context())
                            .hint(qSeed.hint())
                            .correctAnswer(qSeed.correctAnswer())
                            .explanation(qSeed.explanation())
                            .audioText(qSeed.audioText())
                            .build());
                    // MULTIPLE_CHOICE y MATCHING persisten opciones; las de MATCHING
                    // llevan además matchGroup para identificar la pareja.
                    if (qSeed.type() == QuestionType.MULTIPLE_CHOICE
                            || qSeed.type() == QuestionType.MATCHING) {
                        int optPosition = 0;
                        char letter = 'A';
                        for (SeedCatalog.OptionSeed optSeed : qSeed.options()) {
                            optionRepository.save(QuestionOption.builder()
                                    .questionId(question.getId())
                                    .position(optPosition++)
                                    .label(String.valueOf(letter++))
                                    .value(optSeed.value())
                                    .isCorrect(optSeed.correct())
                                    .matchGroup(optSeed.matchGroup())
                                    .build());
                        }
                    }
                }
                position++;
            }
        }
    }

    private void seedWordsOfDay() {
        LocalDate today = LocalDate.now();
        List<WordOfDay> words = new ArrayList<>();
        words.add(WordOfDay.builder()
                .onDate(today)
                .word("deploy")
                .phonetic("/dɪˈplɔɪ/")
                .partOfSpeech("verb")
                .definitionEs("Publicar código a producción; desplegar.")
                .exampleEn("We deploy to production every Friday afternoon.")
                .exampleEs("Desplegamos a producción cada viernes por la tarde.")
                .build());
        words.add(WordOfDay.builder()
                .onDate(today.minusDays(1))
                .word("merge")
                .phonetic("/mɜːrdʒ/")
                .partOfSpeech("verb")
                .definitionEs("Combinar dos ramas o cambios en uno solo; fusionar.")
                .exampleEn("Please merge your branch into main after the review.")
                .exampleEs("Por favor fusiona tu rama en main después de la revisión.")
                .build());
        words.add(WordOfDay.builder()
                .onDate(today.minusDays(2))
                .word("rollback")
                .phonetic("/ˈroʊl.bæk/")
                .partOfSpeech("noun")
                .definitionEs("Revertir un cambio o despliegue al estado anterior.")
                .exampleEn("We had to perform a rollback after the failed deploy.")
                .exampleEs("Tuvimos que hacer un rollback tras el despliegue fallido.")
                .build());
        wordOfDayRepository.saveAll(words);
    }

    private void seedDictionary() {
        List<DictionaryEntry> entries = new ArrayList<>();

        // --- TECH · A1/A2 (vocabulario básico de desarrollo) ---
        entries.add(tech(LevelCode.A1, "bug",       "/bʌɡ/",         "noun", "Error o defecto en el código.",
                "I fixed a bug in the login form.",     "Arreglé un bug en el formulario de login."));
        entries.add(tech(LevelCode.A1, "issue",     "/ˈɪʃ.uː/",      "noun", "Incidencia o ticket; problema reportado.",
                "Please open an issue if you find a bug.", "Abre una issue si encuentras un bug."));
        entries.add(tech(LevelCode.A1, "code",      "/koʊd/",        "noun", "Código fuente.",
                "Write clean code.",                     "Escribe código limpio."));
        entries.add(tech(LevelCode.A1, "commit",    "/kəˈmɪt/",      "noun", "Confirmación de cambios en el repositorio.",
                "Make small, focused commits.",          "Haz commits pequeños y enfocados."));
        entries.add(tech(LevelCode.A1, "branch",    "/bræntʃ/",      "noun", "Rama de código.",
                "Create a branch from main.",            "Crea una rama desde main."));
        entries.add(tech(LevelCode.A1, "repository","/rɪˈpɒz.ɪ.tər.i/","noun","Repositorio donde vive el código.",
                "Clone the repository to start.",        "Clona el repositorio para empezar."));
        entries.add(tech(LevelCode.A2, "merge",     "/mɜːrdʒ/",       "verb", "Combinar dos ramas en una.",
                "Merge your branch after review.",       "Fusiona tu rama tras la revisión."));
        entries.add(tech(LevelCode.A2, "deploy",    "/dɪˈplɔɪ/",      "verb", "Publicar a producción.",
                "We deploy on Tuesday afternoons.",      "Desplegamos los martes por la tarde."));
        entries.add(tech(LevelCode.A2, "framework", "/ˈfreɪm.wɜːrk/", "noun", "Marco de trabajo (React, Django…).",
                "Pick the right framework for the job.", "Elige el framework adecuado para la tarea."));
        entries.add(tech(LevelCode.A2, "endpoint",  "/ˈend.pɔɪnt/",   "noun", "Ruta expuesta por una API.",
                "This endpoint returns a list of users.","Este endpoint devuelve una lista de usuarios."));
        entries.add(tech(LevelCode.A2, "cache",     "/kæʃ/",          "noun", "Almacén temporal para acelerar accesos.",
                "Use a cache to reduce latency.",        "Usa una caché para reducir latencia."));
        entries.add(tech(LevelCode.A2, "latency",   "/ˈleɪ.tən.si/",  "noun", "Tiempo que tarda una petición.",
                "Latency is high during peak hours.",    "La latencia es alta en horas pico."));
        entries.add(tech(LevelCode.A2, "backend",   "/ˈbæk.end/",     "noun", "Parte del sistema no visible al usuario.",
                "The backend handles authentication.",   "El backend gestiona la autenticación."));
        entries.add(tech(LevelCode.A2, "frontend",  "/ˈfrʌnt.end/",   "noun", "Parte visible al usuario.",
                "Our frontend uses React.",              "Nuestro frontend usa React."));
        entries.add(tech(LevelCode.A2, "pull request","/pʊl rɪˈkwest/","noun","Petición de revisión y merge.",
                "Open a pull request to share your work.","Abre un pull request para compartir tu trabajo."));
        entries.add(tech(LevelCode.A2, "rollback",  "/ˈroʊl.bæk/",    "noun", "Revertir un despliegue.",
                "We performed a rollback after the outage.","Hicimos un rollback tras la caída."));

        // --- TECH · B1 (intermedio) ---
        entries.add(tech(LevelCode.B1, "container", "/kənˈteɪ.nər/", "noun", "Entorno ligero y aislado de ejecución (Docker).",
                "We run each service in its own container.","Cada servicio corre en su propio contenedor."));
        entries.add(tech(LevelCode.B1, "pipeline",  "/ˈpaɪp.laɪn/",  "noun", "Tubería de CI/CD con etapas automatizadas.",
                "The pipeline runs tests on every push.","La pipeline ejecuta los tests en cada push."));
        entries.add(tech(LevelCode.B1, "fork",      "/fɔːrk/",        "noun", "Copia de un repo bajo tu propio espacio.",
                "Fork the repo to propose changes.",     "Haz fork del repo para proponer cambios."));
        entries.add(tech(LevelCode.B1, "scope",     "/skoʊp/",        "noun", "Ámbito de visibilidad de una variable.",
                "This variable is out of scope here.",   "Esta variable está fuera de scope aquí."));
        entries.add(tech(LevelCode.B1, "throughput","/ˈθruː.pʊt/",    "noun", "Cantidad de trabajo procesado por segundo.",
                "We doubled throughput by adding workers.","Doblamos el throughput añadiendo workers."));
        entries.add(tech(LevelCode.B1, "feature flag","/ˈfiː.tʃər flæɡ/","noun","Interruptor para activar/desactivar una funcionalidad.",
                "Hide the beta behind a feature flag.",  "Esconde la beta tras un feature flag."));

        // --- GENERAL · A1/A2 (cotidiano) ---
        entries.add(general(LevelCode.A1, "hello",   "/heˈloʊ/",   "interjection", "Saludo informal: hola.",
                "Hello! How are you?",                  "¡Hola! ¿Cómo estás?"));
        entries.add(general(LevelCode.A1, "morning", "/ˈmɔːr.nɪŋ/", "noun",         "Mañana, parte del día.",
                "Good morning, Ana.",                   "Buenos días, Ana."));
        entries.add(general(LevelCode.A1, "thanks",  "/θæŋks/",     "interjection","Agradecimiento corto.",
                "Thanks for your help.",                "Gracias por tu ayuda."));
        entries.add(general(LevelCode.A1, "please",  "/pliːz/",     "interjection","Por favor.",
                "Could you repeat, please?",            "¿Podrías repetir, por favor?"));
        entries.add(general(LevelCode.A1, "today",   "/təˈdeɪ/",    "adverb",       "Hoy.",
                "I have a class today.",                "Tengo clase hoy."));
        entries.add(general(LevelCode.A1, "water",   "/ˈwɔː.tər/",  "noun",         "Agua.",
                "Can I have some water, please?",       "¿Me pones agua, por favor?"));
        entries.add(general(LevelCode.A1, "friend",  "/frend/",     "noun",         "Amigo o amiga.",
                "She is my best friend.",               "Es mi mejor amiga."));
        entries.add(general(LevelCode.A1, "home",    "/hoʊm/",      "noun",         "Hogar, casa.",
                "I am going home.",                     "Me voy a casa."));
        entries.add(general(LevelCode.A2, "borrow",  "/ˈbɒr.oʊ/",   "verb",         "Pedir prestado.",
                "Can I borrow a pen?",                  "¿Me prestas un boli?"));
        entries.add(general(LevelCode.A2, "neighbour","/ˈneɪ.bər/", "noun",         "Vecino o vecina.",
                "My neighbour is very kind.",           "Mi vecina es muy amable."));
        entries.add(general(LevelCode.A2, "weather", "/ˈweð.ər/",   "noun",         "Tiempo atmosférico.",
                "The weather is nice today.",           "Hoy hace buen tiempo."));
        entries.add(general(LevelCode.A2, "ticket",  "/ˈtɪk.ɪt/",   "noun",         "Billete o entrada.",
                "I need a ticket to Madrid.",           "Necesito un billete a Madrid."));
        entries.add(general(LevelCode.A2, "however", "/haʊˈev.ər/", "adverb",       "Sin embargo.",
                "It rained, however we went out.",      "Llovió, sin embargo salimos."));

        // --- GENERAL · B1 ---
        entries.add(general(LevelCode.B1, "achieve", "/əˈtʃiːv/",   "verb",        "Lograr, conseguir.",
                "She achieved her goals quickly.",      "Logró sus metas rápidamente."));
        entries.add(general(LevelCode.B1, "research","/ˈriː.sɜːrtʃ/", "noun",      "Investigación.",
                "He does research in biology.",         "Hace investigación en biología."));
        entries.add(general(LevelCode.B1, "outcome", "/ˈaʊt.kʌm/",  "noun",        "Resultado, desenlace.",
                "The outcome was positive.",            "El resultado fue positivo."));

        dictionaryRepository.saveAll(entries);
        log.info("Diccionario sembrado con {} entradas.", entries.size());
    }

    private DictionaryEntry tech(LevelCode level, String word, String phonetic,
                                 String pos, String def, String exEn, String exEs) {
        return DictionaryEntry.builder()
                .word(word).phonetic(phonetic).partOfSpeech(pos)
                .definitionEs(def).exampleEn(exEn).exampleEs(exEs)
                .categoryType(CategoryType.TECH).levelCode(level)
                .build();
    }

    private DictionaryEntry general(LevelCode level, String word, String phonetic,
                                    String pos, String def, String exEn, String exEs) {
        return DictionaryEntry.builder()
                .word(word).phonetic(phonetic).partOfSpeech(pos)
                .definitionEs(def).exampleEn(exEn).exampleEs(exEs)
                .categoryType(CategoryType.GENERAL).levelCode(level)
                .build();
    }

    private void seedDemoUserAndProgress() {
        String email = "demo@english.local";
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = userRepository.save(User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode("demo1234"))
                    .displayName("Usuario Demo")
                    .role(Role.USER)
                    .createdAt(Instant.now())
                    .build());
            log.info("Usuario demo creado: {} / demo1234", email);
        }

        if (streakRepository.findByUserId(user.getId()).isEmpty()) {
            streakRepository.save(UserStreak.builder()
                    .userId(user.getId())
                    .currentStreak(14)
                    .longestStreak(21)
                    .totalXp(2480)
                    .lastActiveDate(LocalDate.now())
                    .build());
        }
        seedDailyStats(user.getId());
        seedTechProgress(user.getId());
    }

    private void seedDailyStats(Long userId) {
        LocalDate today = LocalDate.now();
        if (dailyStatsRepository.findByUserIdAndOnDate(userId, today).isPresent()) return;
        dailyStatsRepository.save(UserDailyStats.builder()
                .userId(userId)
                .onDate(today)
                .xpEarned(70)
                .minutesPracticed(7)
                .exercisesCompleted(1)
                .dailyGoalMinutes(10)
                .dailyGoalXp(100)
                .build());
        // Backfill the rest of the week so the streak panel looks alive.
        int[] xp = {120, 90, 0, 60, 0, 80, 70};
        int[] mins = {12, 9, 0, 6, 0, 8, 7};
        for (int i = 1; i < 7; i++) {
            LocalDate day = today.minusDays(i);
            dailyStatsRepository.save(UserDailyStats.builder()
                    .userId(userId)
                    .onDate(day)
                    .xpEarned(xp[i])
                    .minutesPracticed(mins[i])
                    .exercisesCompleted(xp[i] > 0 ? 1 : 0)
                    .dailyGoalMinutes(10)
                    .dailyGoalXp(100)
                    .build());
        }
    }

    private void seedTechProgress(Long userId) {
        Category tech = categoryRepository.findByType(CategoryType.TECH).orElse(null);
        if (tech == null) return;
        Level a2 = levelRepository.findByCategoryIdAndCode(tech.getId(), LevelCode.A2).orElse(null);
        if (a2 == null) return;
        List<Exercise> exercises = exerciseRepository.findByLevelIdOrderByPositionAsc(a2.getId());
        if (exercises.isEmpty()) return;
        // Mark 23 first exercises as completed with 2-3 stars and #24 as in-progress.
        for (int i = 0; i < Math.min(23, exercises.size()); i++) {
            Exercise exercise = exercises.get(i);
            if (progressRepository.findByUserIdAndExerciseId(userId, exercise.getId()).isPresent()) continue;
            int totalQ = Math.max(exercise.getQuestionsCount(), 1);
            int stars = (i % 3 == 0) ? 2 : 3;
            progressRepository.save(UserExerciseProgress.builder()
                    .userId(userId)
                    .exerciseId(exercise.getId())
                    .status(ProgressStatus.COMPLETED)
                    .stars(stars)
                    .correctAnswers(totalQ)
                    .totalAnswers(totalQ)
                    .questionsDone(totalQ)
                    .xpEarned(exercise.getXpReward())
                    .lastSeenAt(Instant.now().minusSeconds(3600L * (24 - i)))
                    .completedAt(Instant.now().minusSeconds(3600L * (24 - i)))
                    .build());
        }
        if (exercises.size() >= 24) {
            Exercise current = exercises.get(23);
            if (progressRepository.findByUserIdAndExerciseId(userId, current.getId()).isEmpty()) {
                int totalQ = Math.max(current.getQuestionsCount(), 1);
                progressRepository.save(UserExerciseProgress.builder()
                        .userId(userId)
                        .exerciseId(current.getId())
                        .status(ProgressStatus.IN_PROGRESS)
                        .stars(0)
                        .correctAnswers(0)
                        .totalAnswers(0)
                        .questionsDone(0)
                        .xpEarned(0)
                        .lastSeenAt(Instant.now())
                        .build());
            }
        }
    }
}

package com.englishlearning.config.seed;

import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.enums.QuestionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Static seed catalog for the whole learning content. Hand-written for the
 * lower CEFR levels so the demo feels real, then padded programmatically up
 * to 50 exercises per level (10 per block, 5 blocks per level).
 */
public final class SeedCatalog {

    private SeedCatalog() {}

    public static List<LevelSeed> generalLevels() {
        return List.of(
                generalA1(), generalA2(), generalB1(), generalB2(),
                lockedLevel(LevelCode.C1, 5, "Avanzado",
                        "Uso fluido y flexible del idioma.",
                        "Domina conversaciones complejas y argumentación sofisticada.", 40, GENERAL_TOPICS),
                lockedLevel(LevelCode.C2, 6, "Maestría",
                        "Dominio casi nativo en cualquier registro.",
                        "Diferencia matices, registros y referencias culturales.", 50, GENERAL_TOPICS)
        );
    }

    public static List<LevelSeed> techLevels() {
        return List.of(
                techA1(), techA2(), techB1(), techB2(),
                lockedLevel(LevelCode.C1, 5, "Avanzado",
                        "Uso fluido y flexible en contextos profesionales.",
                        "Debate de arquitectura, mentoring y comunicación de incidencias.", 40, TECH_TOPICS),
                lockedLevel(LevelCode.C2, 6, "Maestría",
                        "Dominio casi nativo en cualquier registro.",
                        "Liderazgo técnico, due diligence y comunicación ejecutiva.", 50, TECH_TOPICS)
        );
    }

    // ----------------------------------------------------------------------
    // TECH levels (hand-written for A1..B2 with realistic content).
    // ----------------------------------------------------------------------

    private static LevelSeed techA1() {
        return new LevelSeed(LevelCode.A1, 1, "Principiante",
                "Frases básicas, presentaciones y vocabulario esencial.",
                "Lo mínimo para presentarte y describir tu rol técnico.", 12, false,
                List.of(
                        techBlockA1Foundations(),
                        techBlockA1Tools(),
                        techBlockA1People(),
                        techBlockA1Daily(),
                        techBlockA1Learning()
                ));
    }

    private static LevelSeed techA2() {
        return new LevelSeed(LevelCode.A2, 2, "Elemental",
                "Situaciones cotidianas y descripciones simples.",
                "Vocabulario de día a día como dev: tareas, repos y stand-ups.", 18, false,
                List.of(
                        techBlockA2Foundations(),
                        techBlockA2Teamwork(),
                        techBlockA2Apis(),
                        techBlockA2Production(),
                        techBlockA2Interviews()
                ));
    }

    private static LevelSeed techB1() {
        return new LevelSeed(LevelCode.B1, 3, "Intermedio",
                "Expresar ideas claras sobre temas familiares.",
                "Discusiones de diseño, code reviews y refactors.", 24, false,
                List.of(
                        techBlock(1, "Arquitectura y diseño", "Patrones y trade-offs · Ej. 1-10", 1,
                                techTopicSet(TECH_B1_DESIGN, 10)),
                        techBlock(2, "Testing y calidad", "Pruebas, lint y CI · Ej. 11-20", 11,
                                techTopicSet(TECH_B1_TESTING, 10)),
                        techBlock(3, "Observabilidad", "Logs, métricas y trazas · Ej. 21-30", 21,
                                techTopicSet(TECH_B1_OBSERVABILITY, 10)),
                        techBlock(4, "Bases de datos", "SQL, índices y rendimiento · Ej. 31-40", 31,
                                techTopicSet(TECH_B1_DATABASES, 10)),
                        techBlock(5, "Carrera técnica", "Crecimiento, mentoring y feedback · Ej. 41-50", 41,
                                techTopicSet(TECH_B1_CAREER, 10))
                ));
    }

    private static LevelSeed techB2() {
        return new LevelSeed(LevelCode.B2, 4, "Intermedio alto",
                "Textos complejos, argumentación y debate técnico.",
                "Comunicación con stakeholders, RFC y debates técnicos.", 32, false,
                List.of(
                        techBlock(1, "Sistemas distribuidos", "Consistencia y disponibilidad · Ej. 1-10", 1,
                                techTopicSet(TECH_B2_DISTRIBUTED, 10)),
                        techBlock(2, "Cloud y plataformas", "AWS, GCP, Azure · Ej. 11-20", 11,
                                techTopicSet(TECH_B2_CLOUD, 10)),
                        techBlock(3, "Performance", "Latencia, throughput y caching · Ej. 21-30", 21,
                                techTopicSet(TECH_B2_PERFORMANCE, 10)),
                        techBlock(4, "Seguridad", "Auth, OWASP y secretos · Ej. 31-40", 31,
                                techTopicSet(TECH_B2_SECURITY, 10)),
                        techBlock(5, "Liderazgo técnico", "Tech leads y RFC · Ej. 41-50", 41,
                                techTopicSet(TECH_B2_LEADERSHIP, 10))
                ));
    }

    // ---- TECH A1 blocks (curated) ---------------------------------------

    private static BlockSeed techBlockA1Foundations() {
        List<ExerciseSeed> exercises = new ArrayList<>();
        // Ejercicio 1 — vitrina con los 4 tipos nuevos (LISTENING, DICTATION,
        // WORD_ORDER, MATCHING) mezclados con los tradicionales. Sirve además
        // como prueba visual de los renders y del TTS auto-play.
        exercises.add(new ExerciseSeed("Presentaciones", "Saludos y datos básicos", 8, 40, List.of(
                tr("Hola, ¿cómo estás?", "hello, how are you", null, null),
                rev("¿Cómo te llamas?", "what is your name", null, null),
                listen("¿Qué saludo escuchas?", "Good morning, everyone.", "good morning, everyone",
                        "Reproducir el audio y escribir lo que dice."),
                dict("How are you today?", "how are you today",
                        "Dictado: lo que escuchas, eso escribes."),
                wo("Ordena la frase: hablo inglés un poco", "i speak a little english",
                        "Orden básico: sujeto + verbo + complementos."),
                match("Empareja saludos con su traducción:",
                        "Good morning", "Buenos días",
                        "Good night",   "Buenas noches",
                        "See you",      "Hasta luego"),
                tr("Soy desarrollador.", "i am a developer", "Frase clave en entrevistas.", null),
                fb("Completa: 'My ___ is Ana.'", "name", null, "Frase muy común en presentaciones.")
        )));
        for (int i = 2; i <= 10; i++) {
            exercises.add(genericExercise("Vocabulario base #" + i,
                    "Saludos, números y profesiones", BASIC_PAIRS, i));
        }
        return new BlockSeed(1, "Primeras palabras",
                "Saludos, números y profesiones · Ej. 1-10", 1, 10, exercises);
    }

    private static BlockSeed techBlockA1Tools() {
        List<ExerciseSeed> exercises = new ArrayList<>();
        exercises.add(new ExerciseSeed("Herramientas básicas", "Editor, terminal y navegador", 6, 30, List.of(
                tr("editor", "editor", null, null),
                rev("teclado", "keyboard", null, null),
                mc("Elige la palabra para 'pantalla':", "screen",
                        opt("Screen", true), opt("Mouse", false), opt("Speaker", false)),
                fb("Open the ___ to write commands.", "terminal", null, "Donde escribes comandos."),
                tr("Voy a abrir el editor.", "i am going to open the editor", null, null),
                rev("Cierra la pestaña.", "close the tab", null, null)
        )));
        for (int i = 2; i <= 10; i++) {
            exercises.add(genericExercise("Lugares y objetos #" + i,
                    "Oficina y materiales", OFFICE_PAIRS, i));
        }
        return new BlockSeed(2, "Tu mesa de trabajo",
                "Editor, terminal y oficina · Ej. 11-20", 11, 20, exercises);
    }

    private static BlockSeed techBlockA1People() {
        return new BlockSeed(3, "Tu equipo",
                "Colegas, roles y horarios · Ej. 21-30", 21, 30,
                techTopicSet(PEOPLE_TOPICS, 10));
    }

    private static BlockSeed techBlockA1Daily() {
        return new BlockSeed(4, "Rutina del día",
                "Stand-up y tareas básicas · Ej. 31-40", 31, 40,
                techTopicSet(DAILY_TOPICS, 10));
    }

    private static BlockSeed techBlockA1Learning() {
        return new BlockSeed(5, "Aprendizaje continuo",
                "Tutoriales, libros y cursos · Ej. 41-50", 41, 50,
                techTopicSet(LEARNING_TOPICS, 10));
    }

    // ---- TECH A2 blocks (curated, matches the design screens) -----------

    private static BlockSeed techBlockA2Foundations() {
        List<ExerciseSeed> exercises = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            exercises.add(genericExercise("Software vocabulary #" + i,
                    "Léxico básico de desarrollo", TECH_A2_FOUNDATIONS, i));
        }
        return new BlockSeed(1, "Fundamentos del software",
                "Léxico básico de desarrollo · Ej. 1-10", 1, 10, exercises);
    }

    private static BlockSeed techBlockA2Teamwork() {
        List<ExerciseSeed> exercises = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            exercises.add(genericExercise("Trabajo en equipo #" + i,
                    "Comunicación y revisión de código", TECH_A2_TEAMWORK, i));
        }
        return new BlockSeed(2, "Trabajo en equipo",
                "Comunicación y revisión de código · Ej. 11-20", 11, 20, exercises);
    }

    private static BlockSeed techBlockA2Apis() {
        List<ExerciseSeed> exercises = new ArrayList<>();
        // Exercise 21 (block 3 first) -> hand-written to match the design.
        exercises.add(new ExerciseSeed("Vocabulario API", "APIs y endpoints", 8, 50, List.of(
                tr("¿Qué significa 'endpoint' en español?", "punto final",
                        "Contexto: REST API · URL pública.",
                        "Es donde termina y empieza la comunicación cliente-servidor.",
                        "endpoint"),
                rev("¿Cómo se dice 'petición' en inglés?", "request",
                        "Lo envía el cliente para pedir datos.", null),
                mc("Elige el significado correcto de 'payload':", "datos enviados",
                        opt("Carga de un servidor", false),
                        opt("Datos enviados en la petición", true),
                        opt("Tipo de autenticación", false)),
                tr("status code", "código de estado",
                        "Contexto: HTTP response.",
                        "200, 404, 500... el número que indica el resultado.", "status code"),
                fb("A '___ key' protege tus llamadas al API.", "api",
                        "Token público que identifica a tu app.",
                        "Se envía en el header Authorization o como query param."),
                rev("¿Cómo se dice 'cabecera' en inglés?", "header",
                        "Información que viaja junto a la petición.", null),
                mc("¿Qué método usarías para crear un recurso?", "POST",
                        opt("GET", false), opt("POST", true), opt("DELETE", false)),
                tr("rate limit", "límite de peticiones",
                        "Restringe cuántas llamadas puedes hacer.",
                        "Si lo superas, recibes un 429 Too Many Requests.", "rate limit")
        )));
        // Exercise 22-23 use a hand-written prompt list, the rest is generic.
        exercises.add(new ExerciseSeed("REST básico", "Verbos HTTP", 6, 50, List.of(
                mc("¿Qué método se usa para leer un recurso?", "GET",
                        opt("GET", true), opt("POST", false), opt("PUT", false)),
                tr("PUT request", "petición de actualización", null, null),
                rev("¿Cómo se dice 'eliminar'?", "delete", null, null),
                mc("¿Qué método actualiza un recurso parcialmente?", "PATCH",
                        opt("PATCH", true), opt("DELETE", false), opt("HEAD", false)),
                tr("idempotent", "idempotente",
                        "Una operación es idempotente si repetirla no cambia el resultado.",
                        "GET, PUT y DELETE son idempotentes; POST normalmente no.", null),
                fb("Repeated PUT calls should produce the same ___.", "result", null, null)
        )));
        exercises.add(new ExerciseSeed("Errores HTTP", "Códigos 4xx y 5xx", 6, 50, List.of(
                mc("¿Qué error indica 'no encontrado'?", "404",
                        opt("404", true), opt("500", false), opt("401", false)),
                tr("internal server error", "error interno del servidor", null, null),
                rev("¿Cómo se dice 'no autorizado'?", "unauthorized", null, null),
                mc("¿Qué código indica 'demasiadas peticiones'?", "429",
                        opt("429", true), opt("403", false), opt("404", false)),
                tr("bad request", "petición incorrecta", null, null),
                fb("A 401 ___ means you are not authenticated.", "unauthorized", null, null)
        )));
        for (int i = 4; i <= 10; i++) {
            exercises.add(genericExercise("API · Ejercicio #" + (20 + i),
                    "Endpoints, esquemas y errores", TECH_A2_APIS, i));
        }
        return new BlockSeed(3, "APIs y datos",
                "Endpoints, esquemas y errores · Ej. 21-30", 21, 30, exercises);
    }

    private static BlockSeed techBlockA2Production() {
        List<ExerciseSeed> exercises = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            exercises.add(genericExercise("Producción y deploy #" + i,
                    "Infra, observabilidad y on-call", TECH_A2_PRODUCTION, i));
        }
        return new BlockSeed(4, "Producción y deploy",
                "Infra, observabilidad y on-call · Ej. 31-40", 31, 40, exercises);
    }

    private static BlockSeed techBlockA2Interviews() {
        List<ExerciseSeed> exercises = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            exercises.add(genericExercise("Entrevistas #" + i,
                    "System design y comportamiento", TECH_A2_INTERVIEWS, i));
        }
        return new BlockSeed(5, "Entrevistas y carrera",
                "System design y comportamiento · Ej. 41-50", 41, 50, exercises);
    }

    // ----------------------------------------------------------------------
    // GENERAL levels
    // ----------------------------------------------------------------------

    private static LevelSeed generalA1() {
        return new LevelSeed(LevelCode.A1, 1, "Principiante",
                "Frases básicas, presentaciones y vocabulario esencial.",
                "Lo mínimo para presentarte y hablar de tu día a día.", 12, false,
                generalBlocksFor(LevelCode.A1));
    }

    private static LevelSeed generalA2() {
        return new LevelSeed(LevelCode.A2, 2, "Elemental",
                "Situaciones cotidianas y descripciones simples.",
                "Pedir información, describir gente y planes simples.", 18, false,
                generalBlocksFor(LevelCode.A2));
    }

    private static LevelSeed generalB1() {
        return new LevelSeed(LevelCode.B1, 3, "Intermedio",
                "Expresar ideas claras sobre temas familiares.",
                "Conversaciones de trabajo y viajes con confianza.", 24, false,
                generalBlocksFor(LevelCode.B1));
    }

    private static LevelSeed generalB2() {
        return new LevelSeed(LevelCode.B2, 4, "Intermedio alto",
                "Textos complejos, argumentación y debate técnico.",
                "Defender tu opinión y entender películas sin subtítulos.", 32, false,
                generalBlocksFor(LevelCode.B2));
    }

    private static List<BlockSeed> generalBlocksFor(LevelCode code) {
        return List.of(
                new BlockSeed(1, "Presentaciones",
                        "Saludos y datos personales · Ej. 1-10", 1, 10,
                        topicExercises(GENERAL_PRESENTING, 10)),
                new BlockSeed(2, "Vida cotidiana",
                        "Familia, casa y rutina · Ej. 11-20", 11, 20,
                        topicExercises(GENERAL_DAILY, 10)),
                new BlockSeed(3, "Trabajo y estudios",
                        "Profesión y aprendizaje · Ej. 21-30", 21, 30,
                        topicExercises(GENERAL_WORK, 10)),
                new BlockSeed(4, "Tiempo libre",
                        "Hobbies, viajes y comida · Ej. 31-40", 31, 40,
                        topicExercises(GENERAL_LEISURE, 10)),
                new BlockSeed(5, "Mundo y opinión",
                        "Noticias y debates simples · Ej. 41-50", 41, 50,
                        topicExercises(GENERAL_OPINION, 10))
        );
    }

    // ----------------------------------------------------------------------
    // Helpers and shared vocab pools.
    // ----------------------------------------------------------------------

    private static LevelSeed lockedLevel(LevelCode code, int position, String displayName,
                                         String headline, String description,
                                         int estimatedHours, String[][] pool) {
        List<BlockSeed> blocks = List.of(
                lockedBlock(1, "Bloque 1", "Contenido por desbloquear · Ej. 1-10", 1, pool),
                lockedBlock(2, "Bloque 2", "Contenido por desbloquear · Ej. 11-20", 11, pool),
                lockedBlock(3, "Bloque 3", "Contenido por desbloquear · Ej. 21-30", 21, pool),
                lockedBlock(4, "Bloque 4", "Contenido por desbloquear · Ej. 31-40", 31, pool),
                lockedBlock(5, "Bloque 5", "Contenido por desbloquear · Ej. 41-50", 41, pool)
        );
        return new LevelSeed(code, position, displayName, headline, description, estimatedHours, true, blocks);
    }

    private static BlockSeed lockedBlock(int position, String title, String subtitle, int start, String[][] pool) {
        return new BlockSeed(position, title, subtitle, start, start + 9,
                topicExercises(pool, 10));
    }

    private static List<ExerciseSeed> topicExercises(String[][] pool, int count) {
        List<ExerciseSeed> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(genericExercise("Práctica #" + i, "Vocabulario y frases", pool, i));
        }
        return list;
    }

    private static List<ExerciseSeed> techTopicSet(String[][] pool, int count) {
        List<ExerciseSeed> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(genericExercise("Práctica técnica #" + i, "Vocabulario aplicado", pool, i));
        }
        return list;
    }

    private static BlockSeed techBlock(int position, String title, String subtitle, int start, List<ExerciseSeed> exercises) {
        return new BlockSeed(position, title, subtitle, start, start + 9, exercises);
    }

    private static ExerciseSeed genericExercise(String title, String topic, String[][] pool, int seed) {
        int n = pool.length;
        int a = (seed * 3) % n;
        int b = (seed * 5 + 1) % n;
        int c = (seed * 7 + 2) % n;
        int d = (seed * 11 + 3) % n;
        int e = (seed * 13 + 4) % n;
        String[] one = pool[a];
        String[] two = pool[b];
        String[] three = pool[c];
        String[] four = pool[d];
        String[] five = pool[e];
        boolean hasSpeaking = seed % 3 == 0;
        List<QuestionSeed> qs = new ArrayList<>();
        qs.add(tr("¿Qué significa '" + one[0] + "' en español?", one[1], one.length > 2 ? one[2] : null,
                "Vocabulario clave del tema '" + topic + "'.", one[0]));
        qs.add(rev("¿Cómo se dice '" + two[1] + "' en inglés?", two[0],
                two.length > 2 ? two[2] : null, null));
        qs.add(mc("Elige el significado correcto de '" + three[0] + "':", three[1],
                opt(three[1], true),
                opt(altMeaning(pool, c, 1), false),
                opt(altMeaning(pool, c, 2), false)));
        qs.add(fb("Completa: '" + four[0] + "' is similar to ___.", synonym(pool, d),
                four.length > 2 ? four[2] : null, null));
        if (hasSpeaking) {
            qs.add(spk("Di en inglés: '" + five[1] + "'", five[0],
                    five.length > 2 ? five[2] : null,
                    "Practica la pronunciación de '" + five[0] + "'."));
        }
        return new ExerciseSeed(title, topic, qs.size(), 50, qs);
    }

    private static String altMeaning(String[][] pool, int idx, int offset) {
        return pool[(idx + offset) % pool.length][1];
    }

    private static String synonym(String[][] pool, int idx) {
        return pool[(idx + 4) % pool.length][0];
    }

    private static QuestionSeed tr(String prompt, String correct, String context, String explanation) {
        return tr(prompt, correct, context, explanation, extractHighlight(prompt));
    }

    private static QuestionSeed tr(String prompt, String correct, String context, String explanation, String highlight) {
        return new QuestionSeed(QuestionType.TRANSLATION, prompt, highlight, context, null, correct, explanation, List.of());
    }

    private static QuestionSeed rev(String prompt, String correct, String hint, String explanation) {
        return new QuestionSeed(QuestionType.REVERSE_TRANSLATION, prompt, extractHighlight(prompt), null, hint,
                correct, explanation, List.of());
    }

    private static QuestionSeed mc(String prompt, String correctValue, OptionSeed... options) {
        List<OptionSeed> list = List.of(options);
        return new QuestionSeed(QuestionType.MULTIPLE_CHOICE, prompt, extractHighlight(prompt),
                null, null, correctValue, null, list);
    }

    private static QuestionSeed fb(String prompt, String correct, String context, String hint) {
        return new QuestionSeed(QuestionType.FILL_BLANK, prompt, null, context, hint,
                correct, null, List.of());
    }

    private static QuestionSeed spk(String prompt, String correct, String context, String explanation) {
        return new QuestionSeed(QuestionType.SPEAKING, prompt, extractHighlight(prompt), context, null,
                correct, explanation, List.of());
    }

    /** LISTENING: el usuario oye {@code audioText} y responde a {@code prompt}. */
    private static QuestionSeed listen(String prompt, String audioText, String correct, String explanation) {
        return new QuestionSeed(QuestionType.LISTENING, prompt, null, null, null,
                correct, explanation, List.of(), audioText);
    }

    /** DICTATION: el usuario oye {@code audioText} y lo transcribe ({@code correct} = mismo texto). */
    private static QuestionSeed dict(String audioText, String correct, String explanation) {
        return new QuestionSeed(QuestionType.DICTATION, "Escucha y escribe.", null, null, null,
                correct, explanation, List.of(), audioText);
    }

    /** WORD_ORDER: el cliente recibe las palabras de {@code correct} mezcladas y el usuario las ordena. */
    private static QuestionSeed wo(String prompt, String correct, String explanation) {
        return new QuestionSeed(QuestionType.WORD_ORDER, prompt, null, null, null,
                correct, explanation, List.of(), null);
    }

    /**
     * MATCHING: define {@code n} parejas (left↔right) bajo distintos {@code matchGroup}.
     * Cada par se persiste como dos {@link OptionSeed}s con el mismo {@code matchGroup}.
     * El answer correcto es la lista de pairIds en orden ascendente (p1,p2,p3,...) — pero
     * el wire frontend↔backend tiene un bug abierto (ver tarea #48).
     */
    private static QuestionSeed match(String prompt, String... leftRightPairs) {
        if (leftRightPairs.length % 2 != 0) {
            throw new IllegalArgumentException("match() necesita pares left,right");
        }
        List<OptionSeed> options = new java.util.ArrayList<>();
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < leftRightPairs.length; i += 2) {
            String pairId = "p" + (i / 2 + 1);
            options.add(new OptionSeed(leftRightPairs[i], true, pairId));      // left
            options.add(new OptionSeed(leftRightPairs[i + 1], true, pairId));  // right
            if (i > 0) answer.append(',');
            answer.append(pairId);
        }
        return new QuestionSeed(QuestionType.MATCHING, prompt, null, null, null,
                answer.toString(), null, options, null);
    }

    private static OptionSeed opt(String value, boolean correct) {
        return new OptionSeed(value, correct);
    }

    /**
     * Heuristic: take the first quoted substring in the prompt (single quotes)
     * so the renderer can italicize it. Returns null when there is none.
     */
    private static String extractHighlight(String prompt) {
        int start = prompt.indexOf('\'');
        if (start < 0) return null;
        int end = prompt.indexOf('\'', start + 1);
        if (end < 0) return null;
        return prompt.substring(start + 1, end);
    }

    // ---- Vocabulary pools -------------------------------------------------
    // Each row: [english, spanish, optional context]

    private static final String[][] BASIC_PAIRS = {
            {"hello", "hola"}, {"good morning", "buenos días"}, {"good night", "buenas noches"},
            {"thank you", "gracias"}, {"please", "por favor"}, {"name", "nombre"},
            {"developer", "desarrollador"}, {"team", "equipo"}, {"office", "oficina"},
            {"meeting", "reunión"}, {"week", "semana"}, {"today", "hoy"}, {"yesterday", "ayer"}
    };
    private static final String[][] OFFICE_PAIRS = {
            {"keyboard", "teclado"}, {"mouse", "ratón"}, {"screen", "pantalla"},
            {"laptop", "portátil"}, {"folder", "carpeta"}, {"file", "archivo"},
            {"tab", "pestaña"}, {"window", "ventana"}, {"terminal", "terminal"},
            {"browser", "navegador"}, {"editor", "editor"}
    };
    private static final String[][] PEOPLE_TOPICS = {
            {"manager", "jefe", "Tu superior directo."},
            {"colleague", "colega", "Compañero de equipo."},
            {"intern", "becario"}, {"mentor", "mentor"}, {"client", "cliente"},
            {"stakeholder", "parte interesada"}, {"designer", "diseñador"},
            {"product owner", "responsable de producto"}, {"team lead", "líder de equipo"},
            {"new hire", "nueva incorporación"}
    };
    private static final String[][] DAILY_TOPICS = {
            {"stand-up", "reunión diaria", "Reunión corta diaria."},
            {"daily", "diaria"}, {"task", "tarea"}, {"ticket", "incidencia"},
            {"sprint", "iteración"}, {"backlog", "lista de pendientes"},
            {"deadline", "fecha límite"}, {"priority", "prioridad"},
            {"blocker", "bloqueante"}, {"update", "actualización"}
    };
    private static final String[][] LEARNING_TOPICS = {
            {"course", "curso"}, {"tutorial", "tutorial"}, {"book", "libro"},
            {"workshop", "taller"}, {"conference", "conferencia"},
            {"talk", "charla"}, {"slide", "diapositiva"}, {"exercise", "ejercicio"},
            {"example", "ejemplo"}, {"practice", "práctica"}
    };

    private static final String[][] TECH_A2_FOUNDATIONS = {
            {"variable", "variable"}, {"function", "función"}, {"loop", "bucle"},
            {"array", "arreglo"}, {"string", "cadena"}, {"boolean", "booleano"},
            {"class", "clase"}, {"object", "objeto"}, {"method", "método"},
            {"parameter", "parámetro"}, {"return", "devolver"}, {"argument", "argumento"},
            {"interface", "interfaz"}, {"module", "módulo"}, {"package", "paquete"},
            {"dependency", "dependencia"}, {"library", "biblioteca"}, {"framework", "marco de trabajo"}
    };
    private static final String[][] TECH_A2_TEAMWORK = {
            {"pull request", "solicitud de cambios"}, {"merge", "fusionar"},
            {"branch", "rama"}, {"commit", "confirmación"}, {"review", "revisar"},
            {"comment", "comentario"}, {"approve", "aprobar"}, {"reject", "rechazar"},
            {"feedback", "retroalimentación"}, {"refactor", "refactorizar"},
            {"pair programming", "programación en pareja"}, {"merge conflict", "conflicto de fusión"},
            {"resolve", "resolver"}, {"draft", "borrador"}, {"reviewer", "revisor"}
    };
    private static final String[][] TECH_A2_APIS = {
            {"endpoint", "punto final", "URL pública de la API."},
            {"request", "petición"}, {"response", "respuesta"},
            {"payload", "carga útil"}, {"status code", "código de estado"},
            {"header", "cabecera"}, {"body", "cuerpo"}, {"query param", "parámetro de consulta"},
            {"path", "ruta"}, {"resource", "recurso"}, {"schema", "esquema"},
            {"contract", "contrato"}, {"versioning", "versionado"}, {"rate limit", "límite de peticiones"},
            {"timeout", "tiempo de espera"}, {"retry", "reintento"}, {"webhook", "webhook"},
            {"token", "token"}, {"scope", "alcance"}, {"oauth", "OAuth"}
    };
    private static final String[][] TECH_A2_PRODUCTION = {
            {"deploy", "desplegar"}, {"rollback", "reversión"}, {"release", "lanzamiento"},
            {"environment", "entorno"}, {"staging", "preproducción"},
            {"production", "producción"}, {"server", "servidor"}, {"container", "contenedor"},
            {"image", "imagen"}, {"pipeline", "tubería"}, {"job", "trabajo"},
            {"alert", "alerta"}, {"incident", "incidente"}, {"on-call", "guardia"},
            {"runbook", "manual de operaciones"}, {"latency", "latencia"},
            {"uptime", "tiempo activo"}, {"downtime", "tiempo inactivo"}
    };
    private static final String[][] TECH_A2_INTERVIEWS = {
            {"behavioral", "conductual"}, {"system design", "diseño de sistemas"},
            {"algorithm", "algoritmo"}, {"complexity", "complejidad"},
            {"trade-off", "compensación"}, {"requirement", "requisito"},
            {"constraint", "restricción"}, {"scalability", "escalabilidad"},
            {"interview", "entrevista"}, {"offer", "oferta"}, {"salary", "salario"},
            {"recruiter", "reclutador"}, {"resume", "currículum"}, {"strength", "fortaleza"},
            {"weakness", "debilidad"}, {"culture fit", "ajuste cultural"}
    };
    private static final String[][] TECH_B1_DESIGN = {
            {"design pattern", "patrón de diseño"}, {"singleton", "instancia única"},
            {"factory", "fábrica"}, {"observer", "observador"}, {"strategy", "estrategia"},
            {"decorator", "decorador"}, {"coupling", "acoplamiento"}, {"cohesion", "cohesión"},
            {"separation of concerns", "separación de responsabilidades"}, {"abstraction", "abstracción"},
            {"inheritance", "herencia"}, {"composition", "composición"}, {"polymorphism", "polimorfismo"},
            {"trade-off", "compensación"}, {"contract", "contrato"}
    };
    private static final String[][] TECH_B1_TESTING = {
            {"unit test", "prueba unitaria"}, {"integration test", "prueba de integración"},
            {"end-to-end", "de extremo a extremo"}, {"coverage", "cobertura"}, {"mock", "imitación"},
            {"stub", "doble parcial"}, {"fixture", "fixture"}, {"assertion", "afirmación"},
            {"linter", "linter"}, {"continuous integration", "integración continua"},
            {"continuous delivery", "entrega continua"}, {"flaky test", "prueba inestable"},
            {"regression", "regresión"}, {"smoke test", "prueba de humo"}, {"snapshot", "instantánea"}
    };
    private static final String[][] TECH_B1_OBSERVABILITY = {
            {"log", "registro"}, {"metric", "métrica"}, {"trace", "traza"},
            {"alert", "alerta"}, {"dashboard", "tablero"}, {"threshold", "umbral"},
            {"latency", "latencia"}, {"throughput", "rendimiento"}, {"error rate", "tasa de errores"},
            {"availability", "disponibilidad"}, {"SLO", "objetivo de nivel de servicio"},
            {"SLA", "acuerdo de nivel de servicio"}, {"SLI", "indicador de nivel de servicio"},
            {"telemetry", "telemetría"}, {"sampling", "muestreo"}
    };
    private static final String[][] TECH_B1_DATABASES = {
            {"index", "índice"}, {"query", "consulta"}, {"transaction", "transacción"},
            {"isolation level", "nivel de aislamiento"}, {"foreign key", "clave foránea"},
            {"primary key", "clave primaria"}, {"join", "unión"}, {"normalization", "normalización"},
            {"denormalization", "desnormalización"}, {"replication", "replicación"},
            {"sharding", "fragmentación"}, {"partition", "partición"}, {"cache", "caché"},
            {"deadlock", "interbloqueo"}, {"migration", "migración"}
    };
    private static final String[][] TECH_B1_CAREER = {
            {"promotion", "ascenso"}, {"performance review", "evaluación de desempeño"},
            {"one-on-one", "uno a uno"}, {"mentorship", "tutoría"}, {"growth", "crecimiento"},
            {"feedback", "retroalimentación"}, {"goal", "objetivo"}, {"impact", "impacto"},
            {"ownership", "responsabilidad"}, {"leadership", "liderazgo"},
            {"individual contributor", "colaborador individual"}, {"tech lead", "líder técnico"},
            {"staff engineer", "ingeniero senior staff"}, {"principal engineer", "ingeniero principal"},
            {"manager", "responsable"}
    };
    private static final String[][] TECH_B2_DISTRIBUTED = {
            {"consensus", "consenso"}, {"availability", "disponibilidad"},
            {"consistency", "consistencia"}, {"partition tolerance", "tolerancia a particiones"},
            {"eventual consistency", "consistencia eventual"}, {"leader election", "elección de líder"},
            {"replication", "replicación"}, {"quorum", "cuórum"}, {"split brain", "cerebro dividido"},
            {"gossip protocol", "protocolo de chismes"}, {"vector clock", "reloj vectorial"},
            {"hinted handoff", "entrega sugerida"}, {"backpressure", "contrapresión"},
            {"idempotency", "idempotencia"}, {"saga", "saga"}
    };
    private static final String[][] TECH_B2_CLOUD = {
            {"region", "región"}, {"availability zone", "zona de disponibilidad"},
            {"vpc", "nube privada virtual"}, {"iam", "gestión de identidad y acceso"},
            {"bucket", "depósito"}, {"object storage", "almacenamiento de objetos"},
            {"queue", "cola"}, {"topic", "tema"}, {"event bus", "bus de eventos"},
            {"function as a service", "función como servicio"}, {"serverless", "sin servidor"},
            {"managed service", "servicio gestionado"}, {"autoscaling", "autoescalado"},
            {"load balancer", "balanceador de carga"}, {"cdn", "red de distribución de contenido"}
    };
    private static final String[][] TECH_B2_PERFORMANCE = {
            {"throughput", "rendimiento"}, {"latency", "latencia"}, {"p99", "percentil 99"},
            {"cache hit", "acierto de caché"}, {"cache miss", "fallo de caché"},
            {"warm-up", "calentamiento"}, {"cold start", "arranque en frío"},
            {"profiler", "perfilador"}, {"hot path", "ruta caliente"},
            {"bottleneck", "cuello de botella"}, {"parallelism", "paralelismo"},
            {"batching", "agrupamiento"}, {"streaming", "transmisión continua"},
            {"backoff", "espera exponencial"}, {"compression", "compresión"}
    };
    private static final String[][] TECH_B2_SECURITY = {
            {"authentication", "autenticación"}, {"authorization", "autorización"},
            {"token", "token"}, {"refresh token", "token de refresco"},
            {"csrf", "falsificación de petición entre sitios"}, {"xss", "scripting entre sitios"},
            {"sql injection", "inyección de sql"}, {"least privilege", "mínimo privilegio"},
            {"secret", "secreto"}, {"vault", "bóveda"}, {"audit log", "registro de auditoría"},
            {"encryption at rest", "cifrado en reposo"}, {"encryption in transit", "cifrado en tránsito"},
            {"hashing", "hash"}, {"salting", "sal"}
    };
    private static final String[][] TECH_B2_LEADERSHIP = {
            {"rfc", "rfc"}, {"design review", "revisión de diseño"},
            {"north star metric", "métrica estrella"}, {"okr", "objetivos y resultados clave"},
            {"roadmap", "hoja de ruta"}, {"trade-off", "compensación"},
            {"stakeholder", "parte interesada"}, {"alignment", "alineamiento"},
            {"escalation", "escalamiento"}, {"incident review", "revisión post-incidente"},
            {"blameless postmortem", "postmortem sin culpa"}, {"runbook", "manual de operaciones"},
            {"playbook", "guía de actuación"}, {"on-call rotation", "rotación de guardia"},
            {"tech debt", "deuda técnica"}
    };

    private static final String[][] TECH_TOPICS = TECH_A2_APIS;

    private static final String[][] GENERAL_PRESENTING = {
            {"name", "nombre"}, {"age", "edad"}, {"country", "país"}, {"city", "ciudad"},
            {"job", "trabajo"}, {"hobby", "afición"}, {"family", "familia"},
            {"language", "idioma"}, {"friend", "amigo"}, {"colleague", "colega"}
    };
    private static final String[][] GENERAL_DAILY = {
            {"breakfast", "desayuno"}, {"lunch", "almuerzo"}, {"dinner", "cena"},
            {"to wake up", "despertarse"}, {"to sleep", "dormir"}, {"to cook", "cocinar"},
            {"to clean", "limpiar"}, {"to read", "leer"}, {"to walk", "caminar"},
            {"to drive", "conducir"}, {"shopping", "compras"}, {"weekend", "fin de semana"}
    };
    private static final String[][] GENERAL_WORK = {
            {"office", "oficina"}, {"meeting", "reunión"}, {"colleague", "colega"},
            {"boss", "jefe"}, {"salary", "salario"}, {"task", "tarea"},
            {"deadline", "fecha límite"}, {"project", "proyecto"}, {"client", "cliente"},
            {"holiday", "vacaciones"}, {"presentation", "presentación"}, {"report", "informe"}
    };
    private static final String[][] GENERAL_LEISURE = {
            {"film", "película"}, {"book", "libro"}, {"music", "música"},
            {"concert", "concierto"}, {"trip", "viaje"}, {"beach", "playa"},
            {"restaurant", "restaurante"}, {"recipe", "receta"}, {"hobby", "afición"},
            {"sport", "deporte"}, {"running", "correr"}, {"game", "juego"}
    };
    private static final String[][] GENERAL_OPINION = {
            {"to think", "pensar"}, {"to believe", "creer"}, {"opinion", "opinión"},
            {"news", "noticias"}, {"government", "gobierno"}, {"economy", "economía"},
            {"environment", "medio ambiente"}, {"climate", "clima"}, {"election", "elección"},
            {"policy", "política"}, {"to agree", "estar de acuerdo"}, {"to disagree", "no estar de acuerdo"}
    };
    private static final String[][] GENERAL_TOPICS = GENERAL_OPINION;

    // ---- Public data records ---------------------------------------------

    public record LevelSeed(LevelCode code, int position, String displayName, String headline,
                            String description, int estimatedHours, boolean locked, List<BlockSeed> blocks) {}

    public record BlockSeed(int position, String title, String subtitle, int startExercise, int endExercise,
                            List<ExerciseSeed> exercises) {}

    public record ExerciseSeed(String title, String topic, int estimatedMinutes, int xpReward,
                               List<QuestionSeed> questions) {}

    public record QuestionSeed(QuestionType type, String prompt, String promptHighlight,
                               String context, String hint, String correctAnswer, String explanation,
                               List<OptionSeed> options, String audioText) {
        /** Constructor de compatibilidad: los seeds previos (TR/REV/MC/FB/SPK) no tienen audioText. */
        public QuestionSeed(QuestionType type, String prompt, String promptHighlight,
                            String context, String hint, String correctAnswer, String explanation,
                            List<OptionSeed> options) {
            this(type, prompt, promptHighlight, context, hint, correctAnswer, explanation, options, null);
        }
    }

    public record OptionSeed(String value, boolean correct, String matchGroup) {
        /** Constructor de compatibilidad: MC no usa matchGroup, solo MATCHING. */
        public OptionSeed(String value, boolean correct) {
            this(value, correct, null);
        }
    }
}

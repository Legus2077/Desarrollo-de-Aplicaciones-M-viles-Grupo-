package com.example.cyberquest2;

import android.content.Context;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Banco de preguntas de CyberQuest.
 */
public class QuestionBank {

    public static final String ARCHIVO_POR_DEFECTO = "banco_preguntas.json";

    /** Un modulo del test con la cantidad de preguntas que se muestran. */
    public static class Module {
        private final int id;
        private final String name;
        private final String shortName;
        private final int quota;

        public Module(int id, String name, String shortName, int quota) {
            this.id = id;
            this.name = name;
            this.shortName = shortName;
            this.quota = quota;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

        public int getQuota() {
            return quota;
        }
    }

    /**
     * Un nivel del test. El banco define los cuatro niveles, cada uno con
     * 40 preguntas, y el progreso del usuario se mide sobre los cuatro.
     */
    public static class Level {
        private final String id;
        private final String name;
        private final int order;
        private final int threshold;

        public Level(String id, String name, int order, int threshold) {
            this.id = id;
            this.name = name;
            this.order = order;
            this.threshold = threshold;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getOrder() {
            return order;
        }

        /** Porcentaje minimo para superar el nivel. */
        public int getThreshold() {
            return threshold;
        }
    }

    private final int version;
    private final List<Module> modules;
    private final List<Question> questions;
    private final List<Level> levels = new ArrayList<>();
    private final Map<String, String> levelNames = new LinkedHashMap<>();
    private final Map<String, Integer> levelThresholds = new LinkedHashMap<>();

    private QuestionBank(int version, List<Module> modules, List<Question> questions) {
        this.version = version;
        this.modules = modules;
        this.questions = questions;
    }

    // ------------------------------------------------------------------
    // Carga del banco
    // ------------------------------------------------------------------

    public static QuestionBank fromAssets(Context context, String fileName)
            throws IOException, JSONException {
        return fromJson(readAsset(context, fileName));
    }

    public static QuestionBank fromJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        int version = root.optInt("version", 1);

        List<Module> modules = new ArrayList<>();
        JSONArray jsonModules = root.optJSONArray("modulos");
        if (jsonModules != null) {
            for (int i = 0; i < jsonModules.length(); i++) {
                JSONObject m = jsonModules.getJSONObject(i);
                modules.add(new Module(
                        m.getInt("id"),
                        m.getString("nombre"),
                        m.optString("nombreCorto", m.getString("nombre")),
                        m.getInt("cuota")));
            }
        }

        List<Question> questions = new ArrayList<>();
        JSONArray jsonQuestions = root.getJSONArray("preguntas");
        for (int i = 0; i < jsonQuestions.length(); i++) {
            JSONObject q = jsonQuestions.getJSONObject(i);
            JSONArray jsonOptions = q.getJSONArray("opciones");
            List<String> options = new ArrayList<>();
            for (int j = 0; j < jsonOptions.length(); j++) {
                options.add(jsonOptions.getString(j));
            }
            int moduleId = q.getInt("modulo");
            questions.add(new Question(
                    q.optString("id", "P" + i),
                    q.optString("nivel", "basico"),
                    moduleId,
                    nameOfModule(modules, moduleId),
                    q.isNull("escenario") ? null : q.optString("escenario", null),
                    q.getString("pregunta"),
                    options,
                    q.getInt("correcta"),
                    q.isNull("explicacion") ? null : q.optString("explicacion", null)));
        }

        QuestionBank bank = new QuestionBank(version, modules, questions);

        JSONArray jsonLevels = root.optJSONArray("niveles");
        if (jsonLevels != null) {
            for (int i = 0; i < jsonLevels.length(); i++) {
                JSONObject l = jsonLevels.getJSONObject(i);
                String id = l.getString("id");
                String name = l.optString("nombre", id);
                int threshold = l.optInt("umbral", 80);
                bank.levelNames.put(id, name);
                bank.levelThresholds.put(id, threshold);
                bank.levels.add(new Level(id, name,
                        l.optInt("orden", bank.levels.size() + 1), threshold));
            }
            Collections.sort(bank.levels, new Comparator<Level>() {
                @Override
                public int compare(Level a, Level b) {
                    return Integer.compare(a.getOrder(), b.getOrder());
                }
            });
        }
        return bank;
    }

    private static String nameOfModule(List<Module> modules, int moduleId) {
        for (Module m : modules) {
            if (m.getId() == moduleId) {
                return m.getShortName();
            }
        }
        return "Modulo " + moduleId;
    }

    private static String readAsset(Context context, String fileName) throws IOException {
        try (InputStream in = context.getAssets().open(fileName);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    // ------------------------------------------------------------------
    // Armado del test
    // ------------------------------------------------------------------

    /**
     * Arma un test para el nivel indicado: toma al azar la cuota de cada
     * modulo, mezcla las opciones de cada pregunta y desordena el conjunto
     * final para que los modulos no salgan agrupados.
     *
     * @throws IllegalStateException si algun modulo no tiene suficientes
     *         preguntas en el banco para cubrir su cuota.
     */
    public List<Question> buildTest(String level) {
        List<Question> test = new ArrayList<>();

        for (Module module : modules) {
            List<Question> pool = new ArrayList<>();
            for (Question q : questions) {
                if (q.getLevel().equals(level) && q.getModuleId() == module.getId()) {
                    pool.add(q);
                }
            }
            if (pool.size() < module.getQuota()) {
                throw new IllegalStateException("El banco del modulo \"" + module.getShortName()
                        + "\" tiene " + pool.size() + " preguntas y el test necesita "
                        + module.getQuota() + ".");
            }
            Collections.shuffle(pool);
            for (int i = 0; i < module.getQuota(); i++) {
                test.add(shuffleOptions(pool.get(i)));
            }
        }

        Collections.shuffle(test);
        return test;
    }

    /** Mezcla el orden de las opciones y recalcula cual es la correcta. */
    private Question shuffleOptions(Question question) {
        List<String> original = question.getOptions();
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < original.size(); i++) {
            order.add(i);
        }
        Collections.shuffle(order);

        List<String> mixed = new ArrayList<>();
        int newCorrectIndex = -1;
        for (int position = 0; position < order.size(); position++) {
            int origin = order.get(position);
            mixed.add(original.get(origin));
            if (origin == question.getCorrectOptionIndex()) {
                newCorrectIndex = position;
            }
        }
        return question.withOptions(mixed, newCorrectIndex);
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Cantidad total de preguntas que se muestran en un test (10). */
    public int getTestSize() {
        int total = 0;
        for (Module m : modules) {
            total += m.getQuota();
        }
        return total;
    }

    /** Porcentaje minimo para aprobar el nivel (RF06, RF07 y RF08). */
    public int getPassThreshold(String level) {
        Integer threshold = levelThresholds.get(level);
        return threshold != null ? threshold : 80;
    }

    /** Nombre visible del nivel, por ejemplo "Test Basico". */
    public String getLevelName(String level) {
        String name = levelNames.get(level);
        return name != null ? name : level;
    }

    /** Niveles definidos en el banco, ordenados de menor a mayor (1 = Basico). */
    public List<Level> getLevels() {
        return levels;
    }

    public int getVersion() {
        return version;
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Question> getAllQuestions() {
        return questions;
    }

    /** Cantidad de preguntas disponibles en el banco para un nivel y modulo. */
    public int countByModule(String level, int moduleId) {
        int count = 0;
        for (Question q : questions) {
            if (q.getLevel().equals(level) && q.getModuleId() == moduleId) {
                count++;
            }
        }
        return count;
    }
}

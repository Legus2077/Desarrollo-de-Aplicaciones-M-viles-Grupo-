package com.example.models;

import java.util.List;

/**
 * Una pregunta del banco de CyberQuest.
 *
 * Ademas del enunciado y las opciones, guarda el modulo y el nivel al que
 * pertenece, un escenario opcional (usado por los casos del modulo de
 * phishing, donde primero se muestra el mensaje/correo sospechoso) y la
 * explicacion de la respuesta correcta.
 */
public class Question {

    private final String id;
    private final String level;
    private final int moduleId;
    private final String moduleName;
    private final String scenario;
    private final String text;
    private final List<String> options;
    private final int correctOptionIndex;
    private final String explanation;

    public Question(String id, String level, int moduleId, String moduleName, String scenario,
                    String text, List<String> options, int correctOptionIndex, String explanation) {
        this.id = id;
        this.level = level;
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.scenario = scenario;
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
    }

    public String getId() {
        return id;
    }

    public String getLevel() {
        return level;
    }

    public int getModuleId() {
        return moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getScenario() {
        return scenario;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    /** true si la pregunta muestra un caso o mensaje antes del enunciado. */
    public boolean hasScenario() {
        return scenario != null && !scenario.trim().isEmpty();
    }

    /**
     * Devuelve una copia de la pregunta con otro orden de opciones.
     * Se usa al mezclar las alternativas para que la respuesta correcta
     * no quede siempre en la misma letra.
     */
    public Question withOptions(List<String> newOptions, int newCorrectIndex) {
        return new Question(id, level, moduleId, moduleName, scenario, text,
                newOptions, newCorrectIndex, explanation);
    }
}

package com.example.models;

import java.util.List;

public class Question {
    private final String text;
    private final List<String> options;
    private final int correctOptionIndex;

    public Question(String text, List<String> options, int correctOptionIndex) {
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
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
}

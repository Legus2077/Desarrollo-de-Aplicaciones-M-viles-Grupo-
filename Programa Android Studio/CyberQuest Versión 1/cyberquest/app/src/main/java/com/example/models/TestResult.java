package com.example.models;

public class TestResult {
    private final String score;
    private final String title;
    private final String detail;
    private final boolean isLowScore;

    public TestResult(String score, String title, String detail, boolean isLowScore) {
        this.score = score;
        this.title = title;
        this.detail = detail;
        this.isLowScore = isLowScore;
    }

    public String getScore() {
        return score;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isLowScore() {
        return isLowScore;
    }
}

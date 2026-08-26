package com.xiuxian.plugin;

public enum SubStage {
    EARLY("初期"),
    MID("中期"),
    LATE("後期"),
    PERFECT("圓滿");

    private final String label;

    SubStage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
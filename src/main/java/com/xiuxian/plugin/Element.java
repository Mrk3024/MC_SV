package com.xiuxian.plugin;

public enum Element {
    JIN("金", "§7"),
    MU("木", "§a"),
    SHUI("水", "§b"),
    HUO("火", "§c"),
    TU("土", "§6");

    private final String label;
    private final String color;

    Element(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
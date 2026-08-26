package com.xiuxian.plugin;

public enum RealmTier {
    XIA("下境界", "人界"),
    ZHONG("中境界", "靈界"),
    SHANG("上境界", "仙界");

    private final String label;
    private final String worldName;

    RealmTier(String label, String worldName) {
        this.label = label;
        this.worldName = worldName;
    }

    public String getLabel() {
        return label;
    }

    public String getWorldName() {
        return worldName;
    }
}
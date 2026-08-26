package com.xiuxian.plugin;

public enum DushiType {

    COMMON("§7", "普通賭石", 20001),
    ADVANCED("§5", "高級賭石", 20002),
    MYTHIC("§6§l", "神話賭石", 20003),
    WUXING("§d", "五行石種", 20004);

    private final String color;
    private final String label;
    private final int customModelData;

    DushiType(String color, String label, int customModelData) {
        this.color = color;
        this.label = label;
        this.customModelData = customModelData;
    }

    // PDC 標籤沿用 "DUSHI_" + enum 名稱，跟原本字串完全相同，向下相容既有道具
    public String getId() {
        return "DUSHI_" + name();
    }

    public String getDisplayName() {
        return color + "[" + label + "]";
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public static DushiType fromId(String id) {
        if (id == null) return null;
        for (DushiType type : values()) {
            if (type.getId().equals(id)) return type;
        }
        return null;
    }
}
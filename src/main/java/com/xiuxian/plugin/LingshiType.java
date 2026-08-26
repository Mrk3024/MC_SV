package com.xiuxian.plugin;

public enum LingshiType {

    // ---- 通用靈石（無屬性，沿用原本設計，數值/model data 不變） ----
    LOW(null, Tier.LOW, "§f", "下品靈石", 50, 10001),
    MID(null, Tier.MID, "§b", "中品靈石", 150, 10002),
    HIGH(null, Tier.HIGH, "§a", "上品靈石", 400, 10003),
    TOP(null, Tier.TOP, "§e§l", "極品靈石", 1000, 10004),

    // ---- 五行 - 金 ----
    JIN_LOW(Element.JIN, Tier.LOW, "§7", "下品金靈石", 50, 11001),
    JIN_MID(Element.JIN, Tier.MID, "§7", "中品金靈石", 150, 11002),
    JIN_HIGH(Element.JIN, Tier.HIGH, "§7", "上品金靈石", 400, 11003),
    JIN_TOP(Element.JIN, Tier.TOP, "§7§l", "極品金靈石", 1000, 11004),

    // ---- 五行 - 木 ----
    MU_LOW(Element.MU, Tier.LOW, "§a", "下品木靈石", 60, 11005),
    MU_MID(Element.MU, Tier.MID, "§a", "中品木靈石", 165, 11006),
    MU_HIGH(Element.MU, Tier.HIGH, "§a", "上品木靈石", 430, 11007),
    MU_TOP(Element.MU, Tier.TOP, "§a§l", "極品木靈石", 1050, 11008),

    // ---- 五行 - 水 ----
    SHUI_LOW(Element.SHUI, Tier.LOW, "§b", "下品水靈石", 50, 11009),
    SHUI_MID(Element.SHUI, Tier.MID, "§b", "中品水靈石", 150, 11010),
    SHUI_HIGH(Element.SHUI, Tier.HIGH, "§b", "上品水靈石", 400, 11011),
    SHUI_TOP(Element.SHUI, Tier.TOP, "§b§l", "極品水靈石", 1000, 11012),

    // ---- 五行 - 火 ----
    HUO_LOW(Element.HUO, Tier.LOW, "§c", "下品火靈石", 80, 11013),
    HUO_MID(Element.HUO, Tier.MID, "§c", "中品火靈石", 195, 11014),
    HUO_HIGH(Element.HUO, Tier.HIGH, "§c", "上品火靈石", 460, 11015),
    HUO_TOP(Element.HUO, Tier.TOP, "§c§l", "極品火靈石", 1100, 11016),

    // ---- 五行 - 土 ----
    TU_LOW(Element.TU, Tier.LOW, "§6", "下品土靈石", 40, 11017),
    TU_MID(Element.TU, Tier.MID, "§6", "中品土靈石", 135, 11018),
    TU_HIGH(Element.TU, Tier.HIGH, "§6", "上品土靈石", 370, 11019),
    TU_TOP(Element.TU, Tier.TOP, "§6§l", "極品土靈石", 950, 11020);

    private final Element element;
    private final Tier tier;
    private final String color;
    private final String label;
    private final int manaValue;
    private final int customModelData;

    LingshiType(Element element, Tier tier, String color, String label, int manaValue, int customModelData) {
        this.element = element;
        this.tier = tier;
        this.color = color;
        this.label = label;
        this.manaValue = manaValue;
        this.customModelData = customModelData;
    }

    // PDC 標籤沿用 "LINGSHI_" + enum 名稱，通用靈石的 id 跟原本字串完全相同，向下相容既有存檔/道具
    public String getId() {
        return "LINGSHI_" + name();
    }

    public String getDisplayName() {
        return color + "[" + label + "]";
    }

    public Element getElement() {
        return element;
    }

    public Tier getTier() {
        return tier;
    }

    public int getManaValue() {
        return manaValue;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public static LingshiType fromId(String id) {
        if (id == null) return null;
        for (LingshiType type : values()) {
            if (type.getId().equals(id)) return type;
        }
        return null;
    }

    // 依「屬性 + 品階」取得對應的五行靈石（通用靈石請直接用 LOW/MID/HIGH/TOP）
    public static LingshiType of(Element element, Tier tier) {
        for (LingshiType type : values()) {
            if (type.element == element && type.tier == tier) return type;
        }
        return null;
    }
}
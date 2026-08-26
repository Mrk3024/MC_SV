package com.xiuxian.plugin;

public enum MajorRealm {

    // 參數順序：tier, 顯示名稱, 小境界階數上限,
    //          起始/結束靈力上限, 起始/結束生命上限, 起始/結束攻擊加成,
    //          是否可飛行, 飛行速度(Bukkit setFlySpeed用,0~1), 是否摔落免傷,
    //          跨境界跳躍值(進入這個大境界時，一次疊加在前一個大境界最後一階的需求上),
    //          每一階遞增量(境界內部，每往下一階多需要多少修為)

    FOUNDATION(RealmTier.XIA, "築基期", 3,
            500, 1200, 35, 55, 7, 12,
            false, 0f, false, 500, 300),

    CORE_FORMATION(RealmTier.XIA, "結丹期", 4,
            1200, 4000, 55, 110, 12, 22,
            true, 0.10f, false, 2000, 800),

    NASCENT_SOUL(RealmTier.XIA, "元嬰期", 5,
            4000, 15000, 110, 220, 22, 45,
            true, 0.15f, false, 10000, 3000),

    SPIRIT_TRANSFORMATION(RealmTier.XIA, "化神期", 6,
            15000, 50000, 220, 450, 45, 90,
            true, 0.20f, false, 50000, 15000),

    // ===== 以下靈界/仙界目前尚未開放突破，數值先用同樣結構佔位，開放前需重新設計 =====
    VOID_REFINEMENT(RealmTier.ZHONG, "煉虛期", 6,
            50000, 150000, 450, 900, 90, 180,
            true, 0.25f, true, 250000, 75000),

    UNITY(RealmTier.ZHONG, "合體期", 6,
            150000, 450000, 900, 1500, 180, 360,
            true, 0.30f, true, 1250000, 375000),

    GREAT_ASCENSION(RealmTier.ZHONG, "大乘期", 6,
            450000, 1200000, 1500, 2000, 360, 640,
            true, 0.35f, true, 6000000, 1800000),

    TRIBULATION_TRANSCENDENCE(RealmTier.SHANG, "渡劫", 6,
            1200000, 3000000, 2000, 2000, 640, 1200,
            true, 0.40f, true, 30000000, 9000000),

    TRUE_IMMORTAL(RealmTier.SHANG, "真仙", 6,
            3000000, 9000000, 2000, 2000, 1200, 2400,
            true, 0.45f, true, 150000000, 45000000),

    TRUE_IMMORTAL_PEAK(RealmTier.SHANG, "真仙巔峰／半步金仙", 6,
            9000000, 25000000, 2000, 2000, 2400, 4800,
            true, 0.50f, true, 750000000, 225000000),

    GOLDEN_IMMORTAL(RealmTier.SHANG, "金仙", 6,
            25000000, 70000000, 2000, 2000, 4800, 9600,
            true, 0.55f, true, 3500000000L, 1000000000L),

    TAI_YI(RealmTier.SHANG, "太乙", 6,
            70000000, 200000000, 2000, 2000, 9600, 19200,
            true, 0.60f, true, 15000000000L, 4000000000L),

    DA_LUO(RealmTier.SHANG, "大羅始祖／大羅境", 6,
            200000000, 600000000, 2000, 2000, 19200, 38400,
            true, 0.70f, true, 70000000000L, 20000000000L),

    DAO_ANCESTOR(RealmTier.SHANG, "道祖", 6,
            600000000, Integer.MAX_VALUE, 2000, 2000, 38400, 76800,
            true, 1.00f, true, 300000000000L, 100000000000L);

    private final RealmTier tier;
    private final String displayName;
    private final int maxRankPerSubStage;
    private final int startMana;
    private final int endMana;
    private final double startHealth;
    private final double endHealth;
    private final double startAttack;
    private final double endAttack;
    private final boolean canFly;
    private final float flightSpeed;
    private final boolean fallImmune;
    private final long entryJump;
    private final long stepIncrement;

    MajorRealm(RealmTier tier, String displayName, int maxRankPerSubStage,
               int startMana, int endMana, double startHealth, double endHealth,
               double startAttack, double endAttack,
               boolean canFly, float flightSpeed, boolean fallImmune,
               long entryJump, long stepIncrement) {
        this.tier = tier;
        this.displayName = displayName;
        this.maxRankPerSubStage = maxRankPerSubStage;
        this.startMana = startMana;
        this.endMana = endMana;
        this.startHealth = startHealth;
        this.endHealth = endHealth;
        this.startAttack = startAttack;
        this.endAttack = endAttack;
        this.canFly = canFly;
        this.flightSpeed = flightSpeed;
        this.fallImmune = fallImmune;
        this.entryJump = entryJump;
        this.stepIncrement = stepIncrement;
    }

    public RealmTier getTier() { return tier; }
    public String getDisplayName() { return displayName; }
    public int getMaxRankPerSubStage() { return maxRankPerSubStage; }
    public int getStartMana() { return startMana; }
    public int getEndMana() { return endMana; }
    public double getStartHealth() { return startHealth; }
    public double getEndHealth() { return endHealth; }
    public double getStartAttack() { return startAttack; }
    public double getEndAttack() { return endAttack; }
    public boolean canFly() { return canFly; }
    public float getFlightSpeed() { return flightSpeed; }
    public boolean isFallImmune() { return fallImmune; }
    public long getEntryJump() { return entryJump; }
    public long getStepIncrement() { return stepIncrement; }

    // 總階梯數 = 4 個小境界（初期/中期/後期/圓滿） × 該大境界的階數上限
    public int getTotalSteps() {
        return SubStage.values().length * maxRankPerSubStage;
    }
}
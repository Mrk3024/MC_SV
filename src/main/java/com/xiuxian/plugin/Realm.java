package com.xiuxian.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代表一個具體的修煉階梯。
 * 煉氣期：1~13 層，沿用原本簡單設計。
 * 築基期以上：大境界(MajorRealm) × 小境界(SubStage：初期/中期/後期/圓滿) × 1~N 階，
 * 由 MajorRealm 的設定在啟動時自動展開，不用手動列舉。
 */
public final class Realm {

    private static final String[] CN_NUM = {"一", "二", "三", "四", "五", "六"};

    private static final List<Realm> ALL = new ArrayList<>();
    private static final Map<String, Realm> BY_KEY = new HashMap<>();

    // 目前系統暫時只開放突破到此階梯（化神期圓滿六階），之後接雷劫/大道機制後再往後解鎖
    public static final Realm MAX_UNLOCKED_REALM;

    static {
        long runningRequired = 0;

        // ===== 煉氣期 1~13 層 =====
        String[] qiLayerNames = {"一層", "二層", "三層", "四層", "五層", "六層", "七層", "八層", "九層", "十層", "十一層", "十二層", "十三層"};
        for (int i = 0; i < qiLayerNames.length; i++) {
            runningRequired = 100 + i * 50L;

            register(new Realm(
                    "QI_REFINING_" + (i + 1),
                    RealmTier.XIA,
                    "煉氣期" + qiLayerNames[i],
                    runningRequired,
                    100 + i * 30,
                    20 + i,
                    i * 0.5,
                    false, 0f, false
            ));
        }

        // ===== 築基期以上：大境界 × 小境界 × 階 =====
        // 修為門檻用「全程累加」方式計算：每進入一個新大境界先疊加一筆跳躍值（entryJump），
        // 保證比前一個大境界最後一階還高；境界內部每一階再疊加一次遞增量（stepIncrement）。
        // 這樣整條路徑保證嚴格遞增，不會出現後面的門檻反而比前面低的情況。
        for (MajorRealm major : MajorRealm.values()) {
            int totalSteps = major.getTotalSteps();
            int stepIndex = 0;
            runningRequired += major.getEntryJump();

            for (SubStage sub : SubStage.values()) {
                for (int rank = 1; rank <= major.getMaxRankPerSubStage(); rank++) {
                    if (stepIndex > 0) {
                        runningRequired += major.getStepIncrement();
                    }

                    double progress = totalSteps <= 1 ? 1.0 : (double) stepIndex / (totalSteps - 1);

                    int mana = (int) Math.round(lerp(major.getStartMana(), major.getEndMana(), progress));
                    double health = lerp(major.getStartHealth(), major.getEndHealth(), progress);
                    double attack = lerp(major.getStartAttack(), major.getEndAttack(), progress);

                    String key = major.name() + "_" + sub.name() + "_" + rank;
                    String displayName = major.getDisplayName() + sub.getLabel() + CN_NUM[rank - 1] + "階";

                    register(new Realm(
                            key, major.getTier(), displayName, runningRequired,
                            mana, health, attack,
                            major.canFly(), major.getFlightSpeed(), major.isFallImmune()
                    ));

                    stepIndex++;
                }
            }
        }

        Realm max = byKey("SPIRIT_TRANSFORMATION_PERFECT_6");
        if (max == null) {
            throw new IllegalStateException("找不到化神期圓滿六階，Realm 初始化有誤");
        }
        MAX_UNLOCKED_REALM = max;
    }

    private static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    private static void register(Realm realm) {
        realm.index = ALL.size();
        ALL.add(realm);
        BY_KEY.put(realm.key, realm);
    }

    public static Realm byKey(String key) {
        return BY_KEY.get(key);
    }

    // 相容舊版存檔：舊格式只存扁平的大境界名稱（例如 "FOUNDATION"），
    // 現在拆成初期/中期/後期/圓滿 × 階數後已經沒有這個 key 了，
    // 讀不到新格式時退而求其次，對應到該大境界的「初期一階」
    public static Realm resolve(String key) {
        if (key == null) return null;
        Realm exact = byKey(key);
        if (exact != null) return exact;
        return byKey(key + "_EARLY_1");
    }

    public static List<Realm> all() {
        return ALL;
    }

    // ---- 實例欄位 ----

    private final String key;
    private final RealmTier tier;
    private final String displayName;
    private final long cultivationRequired; // 突破至「下一階」所需的修為值
    private final int baseMaxMana;
    private final double baseMaxHealth;
    private final double attackBonus;
    private final boolean canFly;
    private final float flightSpeed;
    private final boolean fallImmune;
    private int index; // 在 ALL 清單中的順序，由 register() 設定

    private Realm(String key, RealmTier tier, String displayName, long cultivationRequired,
                  int baseMaxMana, double baseMaxHealth, double attackBonus,
                  boolean canFly, float flightSpeed, boolean fallImmune) {
        this.key = key;
        this.tier = tier;
        this.displayName = displayName;
        this.cultivationRequired = cultivationRequired;
        this.baseMaxMana = baseMaxMana;
        this.baseMaxHealth = baseMaxHealth;
        this.attackBonus = attackBonus;
        this.canFly = canFly;
        this.flightSpeed = flightSpeed;
        this.fallImmune = fallImmune;
    }

    public String getKey() { return key; }
    public RealmTier getTier() { return tier; }
    public String getDisplayName() { return displayName; }
    public long getCultivationRequired() { return cultivationRequired; }
    public int getBaseMaxMana() { return baseMaxMana; }
    public double getBaseMaxHealth() { return baseMaxHealth; }
    public double getAttackBonus() { return attackBonus; }
    public boolean canFly() { return canFly; }
    public float getFlightSpeed() { return flightSpeed; }
    public boolean isFallImmune() { return fallImmune; }
    public int getIndex() { return index; }

    // 下一個階梯；已是清單最後一個則回傳 null
    public Realm next() {
        int nextIndex = index + 1;
        if (nextIndex >= ALL.size()) return null;
        return ALL.get(nextIndex);
    }

    public boolean isMaxUnlocked() {
        return this == MAX_UNLOCKED_REALM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
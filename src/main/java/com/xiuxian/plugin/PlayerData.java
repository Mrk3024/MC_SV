package com.xiuxian.plugin;

public class PlayerData {
    private Realm realm = Realm.byKey("QI_REFINING_1"); // 預設初始境界
    private int mana = 100;                    // 當前靈力
    private int maxMana = 100;                 // 靈力上限
    private long cultivation = 0;               // 當前修為（吸納靈力轉換而來，用來突破境界）

    public PlayerData() {}

    // Getters & Setters
    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        // 確保靈力不會超過上限，也不會低於 0
        this.mana = Math.max(0, Math.min(mana, maxMana));
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public long getCultivation() {
        return cultivation;
    }

    public void setCultivation(long cultivation) {
        this.cultivation = Math.max(0, cultivation);
    }

    public void addCultivation(long amount) {
        this.cultivation = Math.max(0, this.cultivation + amount);
    }
}
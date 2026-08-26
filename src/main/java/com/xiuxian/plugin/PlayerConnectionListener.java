package com.xiuxian.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final PlayerDataManager dataManager;

    public PlayerConnectionListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    // 玩家上線時：從 YAML 讀取修仙數據，補跳任何累積夠格的突破，再套用目前境界對應的靈力/生命/攻擊/飛行數值
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.loadPlayerData(player);
        PlayerData data = dataManager.getPlayerData(player);
        LingshiListener.checkBreakthrough(player, data);
        RealmStatsManager.applyStats(player, data);
    }

    // 玩家離線時：存檔並釋放記憶體中的資料
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dataManager.savePlayerData(player);
        dataManager.unloadPlayerData(player);
    }
}
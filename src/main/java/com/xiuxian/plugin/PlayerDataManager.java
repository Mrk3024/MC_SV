package com.xiuxian.plugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    // 儲存玩家 UUID 對應的修仙數據
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    // 每位玩家一個 YAML 檔的存放資料夾：plugins/XiuXianPlugin/playerdata/<uuid>.yml
    private final File playerDataFolder;

    public PlayerDataManager(XiuXianMain plugin) {
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    // 取得玩家數據，若不存在則自動創建初始數據（保險用；正常情況下應該已在 onPlayerJoin 時載入）
    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData());
    }

    // 玩家登入時呼叫：從 YAML 讀取資料，若無存檔則使用 PlayerData 的預設值
    public void loadPlayerData(Player player) {
        File file = new File(playerDataFolder, player.getUniqueId() + ".yml");
        PlayerData data = new PlayerData();

        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            String realmName = config.getString("realm");
            if (realmName != null) {
                Realm loadedRealm = Realm.resolve(realmName);
                if (loadedRealm != null) {
                    data.setRealm(loadedRealm);
                } else {
                    // 存檔裡的境界 key 完全無法辨識（連舊格式相容都對不上），退回預設境界
                    Bukkit.getLogger().warning("[修仙插件] 玩家 " + player.getName() + " 的境界存檔資料無法辨識（" + realmName + "），已重置為初始境界。");
                }
            }

            data.setMaxMana(config.getInt("maxMana", data.getMaxMana()));
            data.setMana(config.getInt("mana", data.getMana()));
            data.setCultivation(config.getLong("cultivation", data.getCultivation()));
        }

        playerDataMap.put(player.getUniqueId(), data);
    }

    // 玩家登出或伺服器關閉時呼叫：把記憶體中的資料寫回 YAML
    public void savePlayerData(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;

        File file = new File(playerDataFolder, player.getUniqueId() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("realm", data.getRealm().getKey());
        config.set("mana", data.getMana());
        config.set("maxMana", data.getMaxMana());
        config.set("cultivation", data.getCultivation());

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[修仙插件] 無法儲存玩家 " + player.getName() + " 的資料：" + e.getMessage());
        }
    }

    // 存檔完成後把該玩家資料從記憶體移除，避免離線玩家的資料一直佔用記憶體
    public void unloadPlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    // 伺服器關閉時的保險機制：把目前記憶體中所有在線玩家的資料都存一次
    public void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                savePlayerData(player);
            }
        }
    }

    // 每秒飛行消耗的靈力（僅結丹期／金丹期以上、且玩家目前正在飛行時才會扣）
    private static final int FLIGHT_MANA_COST_PER_SECOND = 5;

    // 啟動定時任務：每秒更新一次所有線上玩家的 Actionbar、自然恢復靈力、飛行消耗靈力
    public void startDisplayTask(XiuXianMain plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = getPlayerData(player);

                    // 1. 飛行中：消耗靈力，靈力耗盡就強制墜落；沒在飛行才自然恢復靈力 (+2/秒)
                    if (player.isFlying() && data.getRealm().canFly()) {
                        if (data.getMana() < FLIGHT_MANA_COST_PER_SECOND) {
                            data.setMana(0);
                            player.setFlying(false);
                            // 光呼叫 setFlying(false) 在 allowFlight 仍為 true 時，用戶端有時會在下一兩個
                            // tick 內自動把飛行狀態接回去，導致玩家沒有真正進入自由落體、吃不到摔落傷害。
                            // 這裡連 allowFlight 也一起暫時關掉，逼用戶端老實計算物理墜落，
                            // 過 3 秒後再依當下境界判斷要不要把飛行資格還給他。
                            player.setAllowFlight(false);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (player.isOnline() && getPlayerData(player).getRealm().canFly()) {
                                    player.setAllowFlight(true);
                                }
                            }, 60L);
                            player.sendMessage("§c[修仙] 靈力耗盡，你從空中墜落！");
                        } else {
                            data.setMana(data.getMana() - FLIGHT_MANA_COST_PER_SECOND);
                        }
                    } else if (data.getMana() < data.getMaxMana()) {
                        data.setMana(data.getMana() + 2);
                    }

                    // 2. 構建 Actionbar 顯示字串（境界、靈力、修為/突破所需修為）
                    Realm nextRealm = data.getRealm().next();
                    String cultivationPart;
                    if (nextRealm == null || data.getRealm().getIndex() >= Realm.MAX_UNLOCKED_REALM.getIndex()) {
                        cultivationPart = "§7（已達目前開放上限）";
                    } else {
                        cultivationPart = String.format("§d【修為】§f%d/%d",
                                data.getCultivation(), data.getRealm().getCultivationRequired());
                    }

                    String message = String.format("§b【境界】§f%s  §3【靈力】§a%d/%d  %s",
                            data.getRealm().getDisplayName(), data.getMana(), data.getMaxMana(), cultivationPart);

                    // 3. 發送至玩家物品欄上方 (Actionbar)
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 秒執行一次
    }
}
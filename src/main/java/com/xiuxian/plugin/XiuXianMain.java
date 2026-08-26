package com.xiuxian.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class XiuXianMain extends JavaPlugin implements CommandExecutor {

    private static XiuXianMain instance;
    private PlayerDataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. 初始化數據管理器與 Actionbar 定時任務
        dataManager = new PlayerDataManager(this);
        dataManager.startDisplayTask(this);

        // 2. 【註冊監聽器】LingshiListener、登入/登出存讀檔、境界特效（摔落免傷/飛行權限保險）
        getServer().getPluginManager().registerEvents(new LingshiListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new RealmEffectsListener(), this);

        // 3. 【註冊指令】把 /dushi 指令接上處理邏輯
        getCommand("dushi").setExecutor(this);

        getLogger().info("§a[修仙插件] 插件已成功載入！天地靈氣開始復甦...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("dushi")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此指令只能由玩家執行。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("§c用法：/dushi <common|advanced|mythic>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "common":
                player.getInventory().addItem(XiuXianItems.createDushi("DUSHI_COMMON", "§7[普通賭石]", 20001));
                player.sendMessage("§7[修仙] 獲得了一塊 §7[普通賭石]§7！");
                break;
            case "advanced":
                player.getInventory().addItem(XiuXianItems.createDushi("DUSHI_ADVANCED", "§5[高級賭石]", 20002));
                player.sendMessage("§5[修仙] 獲得了一塊 §5[高級賭石]§5！");
                break;
            case "mythic":
                player.getInventory().addItem(XiuXianItems.createDushi("DUSHI_MYTHIC", "§6§l[神話賭石]", 20003));
                player.sendMessage("§6[修仙] 獲得了一塊 §6§l[神話賭石]§6！");
                break;
            default:
                player.sendMessage("§c未知的賭石等級，請使用：common、advanced 或 mythic");
                break;
        }

        return true;
    }

    @Override
    public void onDisable() {
        // 保險機制：伺服器關閉時把所有還在記憶體中的玩家資料存一次
        dataManager.saveAll();
        getLogger().info("§c[修仙插件] 插件已卸載，靈氣潰散。");
    }

    public static XiuXianMain getInstance() {
        return instance;
    }

    public PlayerDataManager getDataManager() {
        return dataManager;
    }
}
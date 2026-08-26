package com.xiuxian.plugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class LingshiListener implements Listener {

    private final Random random = new Random();

    // 1. 玩家挖掘普通石頭時有機率掉落賭石
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();

        if (type == Material.STONE || type == Material.DEEPSLATE ||
                type == Material.GRANITE || type == Material.ANDESITE) {

            // 使用 random.nextDouble() * 100 來支援精確的小數點機率 (0% ~ 100%)
            double chance = random.nextDouble() * 100;
            Player player = event.getPlayer();

            if (chance < 1.0) {
                // 1% 機率：神話賭石
                dropDushi(event, DushiType.MYTHIC);
                player.sendMessage("§6[修仙] 霞光萬道！你竟然挖出了傳說中的 " + DushiType.MYTHIC.getDisplayName() + "§6！");
            } else if (chance < 3.0) {
                // 2% 機率：高級賭石
                dropDushi(event, DushiType.ADVANCED);
                player.sendMessage("§5[修仙] 天地微光！你挖出了一塊神異的 " + DushiType.ADVANCED.getDisplayName() + "§5！");
            } else if (chance < 4.5) {
                // 1.5% 機率：五行石種
                dropDushi(event, DushiType.WUXING);
                player.sendMessage("§d[修仙] 五色霞光交織！你挖出了一塊 " + DushiType.WUXING.getDisplayName() + "§d！");
            } else if (chance < 6.5) {
                // 2% 機率：普通賭石
                dropDushi(event, DushiType.COMMON);
                player.sendMessage("§e[修仙] 你在石縫中切出了一塊 " + DushiType.COMMON.getDisplayName() + "§e！");
            }
        }
    }

    private void dropDushi(BlockBreakEvent event, DushiType type) {
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(),
                XiuXianItems.createDushi(type));
    }

    // 2. 玩家右鍵使用道具（吸收靈石 或 切割賭石）
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // 主手副手都拿著道具時，PlayerInteractEvent 會各觸發一次，這裡只處理主手，避免同一次右鍵扣兩次道具
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();

        // 蹲下 + 空手右鍵：把當前靈力全部吸納轉化為修為（跟拿著靈石右鍵吸收靈力是兩件事）
        if (player.isSneaking() && (event.getItem() == null || event.getItem().getType() == Material.AIR)) {
            handleAbsorb(player);
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String type = meta.getPersistentDataContainer().get(XiuXianItems.getItemKey(), PersistentDataType.STRING);
        if (type == null) return;

        event.setCancelled(true);
        PlayerData data = XiuXianMain.getInstance().getDataManager().getPlayerData(player);

        // 1. 先比對是不是靈石（通用或五行皆可，統一透過 Enum 查找，不用再一個個列 if）
        LingshiType lingshiType = LingshiType.fromId(type);
        if (lingshiType != null) {
            giveMana(player, data, item, lingshiType.getManaValue(), lingshiType.getDisplayName());
            return;
        }

        // 2. 再比對是不是賭石
        DushiType dushiType = DushiType.fromId(type);
        if (dushiType != null) {
            cutDushi(player, item, dushiType);
        }
    }

    // 蹲下空手右鍵吸納：把目前靈力全部轉化為修為（1:1），並檢查是否達到突破門檻
    private void handleAbsorb(Player player) {
        PlayerData data = XiuXianMain.getInstance().getDataManager().getPlayerData(player);

        if (data.getMana() <= 0) {
            player.sendMessage("§c你目前沒有靈力可供吸納。");
            return;
        }

        int absorbed = data.getMana();
        data.setMana(0);
        data.addCultivation(absorbed);
        player.sendMessage("§b[修仙] 你盤膝而坐，吸納了 " + absorbed + " 點靈力，轉化為修為！（修為 +" + absorbed + "）");

        checkBreakthrough(player, data);
    }

    // 檢查修為是否達到突破門檻，達到就自動突破——用迴圈處理，修為一次夠跳好幾階時會連續突破到位
    // （目前上限鎖在化神期圓滿六階）。設為 public static，讓登入流程也能直接呼叫補跳。
    public static void checkBreakthrough(Player player, PlayerData data) {
        Realm breakthroughFrom = null; // 記錄這次連續突破前的起點，只用來組訊息
        Realm last = null;

        while (true) {
            Realm current = data.getRealm();

            if (current.getIndex() >= Realm.MAX_UNLOCKED_REALM.getIndex()) {
                break; // 已達目前系統開放的境界上限，暫不往後突破
            }

            long required = current.getCultivationRequired();
            if (data.getCultivation() < required) {
                break;
            }

            Realm next = current.next();
            if (next == null) break;

            if (breakthroughFrom == null) breakthroughFrom = current;

            data.setCultivation(data.getCultivation() - required); // 溢出的修為保留到下一階繼續累積
            data.setRealm(next);
            last = next;
        }

        if (last != null) {
            RealmStatsManager.applyBreakthroughBonus(player, data);
            player.sendMessage("§6§l[突破] 恭喜！你從 " + breakthroughFrom.getDisplayName() + " 一路突破至 " + last.getDisplayName() + "！");
        }
    }

    private void cutDushi(Player player, ItemStack item, DushiType dushiType) {
        item.setAmount(item.getAmount() - 1);
        int roll = random.nextInt(100);

        switch (dushiType) {
            case COMMON: {
                LingshiType result = roll < 70 ? LingshiType.LOW : roll < 95 ? LingshiType.MID : LingshiType.HIGH;
                giveDushiResult(player, "§7[賭石]", result);
                break;
            }
            case ADVANCED: {
                LingshiType result = roll < 50 ? LingshiType.MID : roll < 85 ? LingshiType.HIGH : LingshiType.TOP;
                giveDushiResult(player, "§5[高級賭石]", result);
                break;
            }
            case MYTHIC: {
                LingshiType result = roll < 60 ? LingshiType.HIGH : LingshiType.TOP;
                giveDushiResult(player, "§6[神話賭石]", result);
                break;
            }
            case WUXING: {
                // 品階機率跟普通賭石一致（70% 下品 / 25% 中品 / 5% 上品），屬性五行均等隨機
                Tier tier = roll < 70 ? Tier.LOW : roll < 95 ? Tier.MID : Tier.HIGH;
                Element element = Element.values()[random.nextInt(Element.values().length)];
                LingshiType result = LingshiType.of(element, tier);
                giveDushiResult(player, "§d[五行石種]", result);
                break;
            }
        }
    }

    private void giveDushiResult(Player player, String sourceLabel, LingshiType result) {
        player.getInventory().addItem(XiuXianItems.createLingshi(result));
        player.sendMessage(sourceLabel + " 開出了 " + result.getDisplayName() + "§f！");
    }

    private void giveMana(Player player, PlayerData data, ItemStack item, int amount, String name) {
        if (data.getMana() >= data.getMaxMana()) {
            player.sendMessage("§c你的靈力已飽和，無法繼續吸收！");
            return;
        }
        data.setMana(data.getMana() + amount);
        item.setAmount(item.getAmount() - 1);
        player.sendMessage("§a[修仙] 吸收了 " + name + "§a，靈力 +" + amount + "！");
    }
}
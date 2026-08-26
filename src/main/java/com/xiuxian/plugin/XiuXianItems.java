package com.xiuxian.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class XiuXianItems {

    public static NamespacedKey getItemKey() {
        return new NamespacedKey(XiuXianMain.getInstance(), "xiuxian_item_type");
    }

    // --- 統一的靈石生成方法 ---
    public static ItemStack createLingshi(String grade, String name, int modelData, int manaValue) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(
                    "§7蘊含純淨天地靈氣的晶石。",
                    "§e右鍵點擊：§a恢復 " + manaValue + " 點靈力"
            ));
            meta.setCustomModelData(modelData);
            // 寫入固定的防偽標籤
            meta.getPersistentDataContainer().set(getItemKey(), PersistentDataType.STRING, grade);
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- 用 LingshiType Enum 生成靈石（通用/五行皆可） ---
    public static ItemStack createLingshi(LingshiType type) {
        return createLingshi(type.getId(), type.getDisplayName(), type.getCustomModelData(), type.getManaValue());
    }

    // --- 統一的賭石生成方法 ---
    public static ItemStack createDushi(String grade, String name, int modelData) {
        ItemStack item = new ItemStack(Material.FLINT); // 使用圓石作為基礎外觀
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(
                    "§7外表平平無奇的石塊，內部似乎有靈氣流動。",
                    "§e右鍵點擊：§c切割賭石（揭曉結果）"
            ));
            meta.setCustomModelData(modelData);
            // 寫入固定的防偽標籤
            meta.getPersistentDataContainer().set(getItemKey(), PersistentDataType.STRING, grade);
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- 用 DushiType Enum 生成賭石（通用/五行石種皆可） ---
    public static ItemStack createDushi(DushiType type) {
        return createDushi(type.getId(), type.getDisplayName(), type.getCustomModelData());
    }
}
package com.xiuxian.plugin;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RealmStatsManager {

    // 固定 UUID + 具名 modifier，套用攻擊加成前一定先移除舊的，避免每次呼叫疊加
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("8a1f7c2e-6b3d-4e11-9a2e-0d5f6c7b8e10");
    private static final String ATTACK_MODIFIER_NAME = "xiuxian_realm_attack_bonus";

    // 依玩家目前境界套用：靈力上限、生命值上限、攻擊加成、飛行資格
    // 呼叫時機：玩家登入時、以及每次突破境界之後
    public static void applyStats(Player player, PlayerData data) {
        Realm realm = data.getRealm();

        // 1. 靈力上限
        data.setMaxMana(realm.getBaseMaxMana());

        // 2. 生命值上限
        AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(realm.getBaseMaxHealth());
            if (player.getHealth() > healthAttr.getValue()) {
                player.setHealth(healthAttr.getValue());
            }
        }

        // 3. 攻擊力加成（先移除舊的 modifier 再加新的，避免重複套用疊加）
        AttributeInstance attackAttr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.getModifiers().stream()
                    .filter(m -> m.getUniqueId().equals(ATTACK_MODIFIER_UUID))
                    .findFirst()
                    .ifPresent(attackAttr::removeModifier);

            if (realm.getAttackBonus() > 0) {
                attackAttr.addModifier(new AttributeModifier(
                        ATTACK_MODIFIER_UUID,
                        ATTACK_MODIFIER_NAME,
                        realm.getAttackBonus(),
                        AttributeModifier.Operation.ADD_NUMBER
                ));
            }
        }

        // 4. 飛行資格與飛行速度：結丹期（金丹期）以前不能飛；能飛的話速度隨境界提升
        if (realm.canFly()) {
            player.setAllowFlight(true);
            player.setFlySpeed(realm.getFlightSpeed());
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFlySpeed(0.1f); // 重置回原版預設值
        }
    }

    // 突破當下呼叫：套用新境界數值，並回滿血作為突破獎勵
    public static void applyBreakthroughBonus(Player player, PlayerData data) {
        applyStats(player, data);
        AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            player.setHealth(healthAttr.getValue());
        }
    }
}
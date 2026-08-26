package com.xiuxian.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class RealmEffectsListener implements Listener {

    // 煉虛期以上摔落免傷；結丹/元嬰/化神靈力耗盡墜落，摔落傷害照算，可能摔死
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        PlayerData data = XiuXianMain.getInstance().getDataManager().getPlayerData(player);

        if (data.getRealm().isFallImmune()) {
            event.setCancelled(true);
        }
    }

    // 保險機制：境界不允許飛行時，強制取消飛行狀態（避免外部因素造成的飛行權限殘留）
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        PlayerData data = XiuXianMain.getInstance().getDataManager().getPlayerData(player);

        if (event.isFlying() && !data.getRealm().canFly()) {
            event.setCancelled(true);
            player.setAllowFlight(false);
        }
    }
}
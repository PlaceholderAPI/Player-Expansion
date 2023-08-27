package com.extendedclip.papi.expansion.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public record PlayerListener(PlayerExpansion expansion) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        expansion.joinTimes.put(e.getPlayer(),System.currentTimeMillis());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        expansion.joinTimes.remove(e.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) expansion.damagesGiven.put(player,e.getFinalDamage());
    }

}

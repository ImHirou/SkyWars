package me.secretlovers.skywars.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityListeners implements Listener {

    @EventHandler
    public void onEvent(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) event.setCancelled(true);
    }

}

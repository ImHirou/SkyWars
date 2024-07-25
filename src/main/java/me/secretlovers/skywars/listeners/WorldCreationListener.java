package me.secretlovers.skywars.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WorldCreationListener implements Listener {

    @EventHandler(priority= EventPriority.MONITOR)
    public void noLagOnLoad(org.bukkit.event.world.WorldInitEvent event) {
        World world = event.getWorld();
        world.setKeepSpawnInMemory(false);
    }

}

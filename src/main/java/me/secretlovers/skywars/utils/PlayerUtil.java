package me.secretlovers.skywars.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@UtilityClass
public class PlayerUtil {

    public void clearPlayer(Player p) {
        p.getInventory().clear();
        p.getActivePotionEffects().clear();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setGameMode(GameMode.SURVIVAL);
        p.getItemOnCursor().setType(Material.AIR);
    }

}

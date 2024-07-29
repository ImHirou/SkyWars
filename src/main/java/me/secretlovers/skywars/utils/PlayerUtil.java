package me.secretlovers.skywars.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

@UtilityClass
public class PlayerUtil {

    public void clearPlayer(Player p) {
        p.getInventory().clear();
        p.getActivePotionEffects().clear();
        p.setHealth(20);
        p.setFoodLevel(20);
        for(PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
        p.setGameMode(GameMode.SURVIVAL);
        p.getItemOnCursor().setType(Material.AIR);
    }

}

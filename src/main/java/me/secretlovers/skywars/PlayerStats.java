package me.secretlovers.skywars;

import lombok.Getter;
import lombok.Setter;
import me.secretlovers.skywars.game.kits.Kit;
import org.bukkit.entity.Player;

@Getter
@Setter
public class PlayerStats {

    private int kills;
    private int chestsOpened;
    private int blocksPlaced;

    public PlayerStats(Player p) {
        kills = 0;
        chestsOpened = 0;
        blocksPlaced = 0;
    }

    public void addKill() {
        kills++;
    }

    public void addChestOpen() {
        chestsOpened++;
    }

    public void addBlockPlaced() {
        blocksPlaced++;
    }

}

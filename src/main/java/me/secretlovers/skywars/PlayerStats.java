package me.secretlovers.skywars;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerStats {

    private int kills;
    private int chestsOpened;
    private int blocksPlaced;

    public PlayerStats() {
        kills = 0;
        chestsOpened = 0;
        blocksPlaced = 0;
    }

    private void addKill() {
        kills++;
    }

    private void addChestOpen() {
        chestsOpened++;
    }

    private void addBlockPlaced() {
        blocksPlaced++;
    }

}

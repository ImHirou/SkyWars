package me.secretlovers.skywars.game;


import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Getter
public class GameManager {
    private static int ids = 0;

    private HashMap<Integer, Game> games = new HashMap<>();
    private HashMap<Player, Game> playerToGame = new HashMap<>();
    public GameManager() {

    }

    public void addGame(String mapName) {
        games.put(ids, new Game(ids, mapName));
        ids++;
    }

    public void removeGame(int id) {
        games.remove(id);
    }

}

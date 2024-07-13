package me.secretlovers.skywars.game.team;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import me.secretlovers.skywars.utils.LocationUtil;
import me.secretlovers.skywars.utils.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

@Getter
@Setter
public class Team {

    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> alivePlayers = new ArrayList<>();
    private String color;
    private Material material;
    private Location spawnLocation;
    private int maxPlayers;

    public Team(JsonObject json, World world, int maxPlayers) {
        spawnLocation = LocationUtil.fromJson(json.getAsJsonObject("spawnLocation"), world);
        this.maxPlayers = maxPlayers;
        color = json.get("color").getAsString();
        try {
            material = Material.valueOf(color.toUpperCase(Locale.ROOT) + "_WOOL");
        } catch (Exception e) {
            material = Material.WHITE_WOOL;
        }
    }

    public boolean addPlayer(Player p) {
        if(players.contains(p)) return false;
        if(players.size() < maxPlayers) {
            players.add(p);
            alivePlayers.add(p);
            return true;
        }
        return false;
    }
    public void removePlayer(Player p) {
        players.remove(p);
        alivePlayers.remove(p);
    }

    public void spawn() {
        if(spawnLocation == null) {
            System.out.println("SPAWN LOC IS NULL");
            return;
        }

        for(Player p : players) {
            PlayerUtil.clearPlayer(p);
           if (spawnLocation != null || spawnLocation.getWorld() != null) {
                p.teleport(spawnLocation);
            } else if(spawnLocation == null){
                System.out.println("not loaded spawnloc");
            } else {
                System.out.println("World not loaded");
            }
        }
    }

    public boolean isFull() {
        return maxPlayers == players.size();
    }
    public boolean isAlive() { return !alivePlayers.isEmpty(); }

}

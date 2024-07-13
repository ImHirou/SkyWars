package me.secretlovers.skywars.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.team.Team;
import me.secretlovers.skywars.map.LocalGameMap;
import me.secretlovers.skywars.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Game {

    private final int id;
    private LocalGameMap gameMap;
    private ArrayList<Team> teams = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> spectators = new ArrayList<>();
    private HashMap<Player, Team> playerTeam = new HashMap<>();
    private GameState gameState;
    private BossBar bossBar;
    private final int maxPlayers;

    public Game(int id, String mapName) {
        this.id = id;
        File mapDirectory = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/maps");
        gameMap = new LocalGameMap(mapDirectory, mapName, true);
        gameState = GameState.UNKNOWN;
        JsonObject teamSection = SkyWars.getInstance().getJsonConfig().getAsJsonObject("maps").
                getAsJsonObject(mapName);
        for(Map.Entry<String, JsonElement> element : teamSection.getAsJsonObject("teams").entrySet())
            teams.add(new Team(element.getValue().getAsJsonObject(), gameMap.getWorld(), teamSection.get("maxPlayers").getAsInt()));
        maxPlayers = teamSection.get("maxPlayers").getAsInt() * teams.size();
        bossBar = Bukkit.createBossBar("Players to start: " + 2, BarColor.GREEN, BarStyle.SOLID, BarFlag.DARKEN_SKY);
        bossBar.setVisible(true);
    }

    public void changeGameState(GameState state) {
        if(gameState == state) return;
        if(gameState == GameState.PLAYING && state == GameState.STARTING) return;

        gameState = state;

        switch (state) {
            case WAITING:
                WaitingPhase();
                break;
            case STARTING:
                StartingPhase();
                break;
            case PLAYING:
                PlayingPhase();
                break;
            case FINISH:
                FinishPhase();
                break;
        }
    }

    private void WaitingPhase() {

        new BukkitRunnable() {

            int timer = 30; //seconds to start

            @Override
            public void run() {

                    if(gameState != GameState.WAITING) cancel();

                    timer--;
                    updateBossBar(timer, 30);
                    if(players.size() < 2) {
                        changeGameState(GameState.UNKNOWN);
                        cancel();
                    }
                    if(timer <=0) {
                        changeGameState(GameState.STARTING);
                        cancel();
                    }
            }

        }.runTaskTimer(SkyWars.getInstance(), 20, 20);

    }

    private void StartingPhase() {

        new BukkitRunnable() {

            int iterations = 0;

            @Override
            public void run() {

                if(gameState != GameState.STARTING) cancel();

                updateBossBar(1, 1);

                for(Player p: players) {
                    if(!playerTeam.containsKey(p)) {
                        for(Team team : teams) {
                            if(!team.isFull()) {
                                team.addPlayer(p);
                                playerTeam.put(p, team);
                                break;
                            }
                        }
                    }
                }

                if(gameMap.isLoaded()) {
                    System.out.println("game map is loaded");
                    for(Team team : teams) {
                        team.getSpawnLocation().getChunk().load();
                        team.spawn();
                    }
                    changeGameState(GameState.PLAYING);
                    cancel();
                }

                else
                    if(iterations == 0)
                        gameMap.restoreFromSource();

                iterations++;
            }

        }.runTaskTimer(SkyWars.getInstance(), 5, 5);

    }

    private void PlayingPhase() {


        new BukkitRunnable() {

            int timer = 300; //in seconds

            @Override
            public void run() {

                if(gameState != GameState.PLAYING) cancel();

                timer--;
                updateBossBar(timer, 300);
                int alive = 0;
                for(Team team : teams) {
                    if(team.isAlive()) alive++;
                    if(alive >= 2) break;
                }
                if(timer <=0 || alive == 1) {
                    changeGameState(GameState.FINISH);
                    cancel();
                }

                if(timer == 150) SkyWars.getInstance().getChestManager().resetChests();


            }

        }.runTaskTimer(SkyWars.getInstance(), 20, 20);

    }

    private void FinishPhase() {
        int alive = 0;
        for(Team team : teams)
            if(team.isAlive()) alive++;

        if(alive == 1) {
            //TODO 1 team win
        }
        else {
            //TODO tie
        }



        new BukkitRunnable() {

            int timer = 10;

            @Override
            public void run() {

                if(gameState != GameState.FINISH) cancel();

                timer--;
                updateBossBar(timer, 10);
                if(timer <= 0) {
                    for(Player player : players) removePlayer(player);
                    gameMap.unload();
                    bossBar.removeAll();
                    bossBar.setVisible(false);
                    SkyWars.getInstance().getGameManager().removeGame(id);
                    if(!gameMap.isLoaded()) {
                        cancel();
                    } else {
                        gameMap.unload();
                    }
                }

            }

        }.runTaskTimer(SkyWars.getInstance(), 20, 20);

    }

    public void addPlayer(Player p) {
        if(players.contains(p)) return;
        if(SkyWars.getInstance().getGameManager().getPlayerToGame().containsKey(p))
            SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).removePlayer(p);
        bossBar.addPlayer(p);
        players.add(p);
        updateBossBar(1 ,1);
        SkyWars.getInstance().getGameManager().getPlayerToGame().put(p, this);
        PlayerUtil.clearPlayer(p);
        p.teleport(new Location(gameMap.getWorld(), 0, gameMap.getWorld().getHighestBlockYAt(0, 0), 0));
        if(players.size() >= 2) changeGameState(GameState.WAITING);
    }

    public void killPlayer(Player p) {
        if(!players.contains(p)) return;
        spectators.add(p);
        playerTeam.get(p).getAlivePlayers().remove(p);
        p.setGameMode(GameMode.SPECTATOR);
    }

    public void removePlayer(Player p) {
        players.remove(p);
        spectators.remove(p);
        bossBar.removePlayer(p);
        updateBossBar(1, 1);
        SkyWars.getInstance().getGameManager().getPlayerToGame().remove(p);
        p.teleport(Bukkit.getWorld("world").getSpawnLocation());
    }

    void updateBossBar(int time, int baseNum) {
        switch (gameState) {
            case UNKNOWN:
                if((double) players.size() / 2 > 1.0) bossBar.setProgress(1.0);
                else bossBar.setProgress((double) players.size() / 2);
                break;
            case WAITING:
                bossBar.setTitle("Seconds to start " + time);
                bossBar.setColor(BarColor.BLUE);
                if((double) time / baseNum < 0.0) bossBar.setProgress(1.0);
                else bossBar.setProgress( (double) time / baseNum);
                break;
            case STARTING:
                bossBar.setTitle("Start");
                bossBar.setProgress(1.0);
                break;
            case PLAYING:
                bossBar.setTitle("Finish in " + time + " seconds");
                bossBar.setColor(BarColor.YELLOW);
                if((double) time / baseNum < 0.0) bossBar.setProgress(1.0);
                else bossBar.setProgress( (double) time / baseNum);
                break;
            case FINISH:
                bossBar.setTitle("FINISH");
                if(time % 4 == 0) bossBar.setColor(BarColor.BLUE);
                else if(time % 4 == 1) bossBar.setColor(BarColor.RED);
                else if(time % 4 == 2) bossBar.setColor(BarColor.GREEN);
                else bossBar.setColor(BarColor.YELLOW);
                if((double) time / baseNum < 0.0) bossBar.setProgress(1.0);
                else bossBar.setProgress( (double) time / baseNum);
                break;
            default:
                break;
        }
    }

}

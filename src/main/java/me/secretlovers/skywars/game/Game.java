package me.secretlovers.skywars.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.secretlovers.skywars.PlayerStats;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerData;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.kits.Kit;
import me.secretlovers.skywars.game.team.Team;
import me.secretlovers.skywars.map.SWArena;
import me.secretlovers.skywars.utils.PlayerUtil;
import net.megacraft.multi.game.AbstractGame;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Game{

    private final int id;
    private final String mapName;
    private SWArena arena;

    private ArrayList<Team> teams = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> spectators = new ArrayList<>();

    private HashMap<Player, Team> playerTeam = new HashMap<>();
    private HashMap<Player, PlayerStats> playerStats = new HashMap<>();

    private GameState gameState;
    private final BossBar bossBar;
    private final int maxPlayers;
    private final int playersToStart;

    public Game(int id, String mapName) {
        this.id = id;
        this.mapName = mapName;
        arena = new SWArena();
        File mapDirectory = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/maps/" + mapName);
        arena.loadWorld(mapDirectory, mapName);
        gameState = GameState.UNKNOWN;
        JsonObject teamSection = SkyWars.getInstance().getJsonConfig().getAsJsonObject("maps").
                getAsJsonObject(mapName);
        for(Map.Entry<String, JsonElement> element : teamSection.getAsJsonObject("teams").entrySet())
            teams.add(new Team(element.getValue().getAsJsonObject(),
                    teamSection.get("maxPlayers").getAsInt()));

        maxPlayers = teamSection.get("maxPlayers").getAsInt() * teams.size();
        playersToStart = Integer.min(2, maxPlayers/2);
        bossBar = Bukkit.createBossBar("Players to start: " + playersToStart, BarColor.GREEN, BarStyle.SOLID, BarFlag.DARKEN_SKY);
        bossBar.setVisible(true);
        setUpScoreboards();
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
                    if(players.size() < playersToStart) {
                        timer = 30;
                    } else {
                        for(Player p : players) {
                            Scoreboard board = p.getScoreboard();
                            Objective obj = board.getObjective(p.getDisplayName());
                            Score time = obj.getScore("Start in: " + timer + "s");
                            time.setScore(10);
                        }
                    }
                    if(timer <= 0) {
                        changeGameState(GameState.STARTING);
                        cancel();
                    }
            }

        }.runTaskTimer(SkyWars.getInstance(), 20, 20);

    }

    private void StartingPhase() {

        new BukkitRunnable() {

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

                if(((SWArena) getArena()).isLoaded(mapName)) {
                    System.out.println("game map is loaded");
                    for (Team team : teams) {
                        team.spawn(getArena().getWorlds().get(mapName));
                        for(Player p : team.getPlayers()) {
                            Kit.getKits().get(PlayerManager.data.get(p).getSelectedKit()).give(p);
                            System.out.println(PlayerManager.data.get(p).getSelectedKit());
                        }
                    }
                    changeGameState(GameState.PLAYING);
                    cancel();
                }
            }

        }.runTask(SkyWars.getInstance());

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
            for(Player p : players) {
                if(!spectators.contains(p)) {
                    p.sendMessage(ChatColor.GREEN + "You WIN!");
                    p.sendMessage(ChatColor.GOLD + "+ 1000 Gold");
                    PlayerManager.data.get(p).setGold(PlayerManager.data.get(p).getGold()+1000);
                } else {
                    p.sendMessage(ChatColor.RED + "You lose..");
                    p.sendMessage(ChatColor.GOLD + "+ 200 Gold");
                    PlayerManager.data.get(p).setGold(PlayerManager.data.get(p).getGold()+200);
                }
            }
        }
        else {
            //TODO tie
        }

        for(Player p : players) {
            PlayerData data = PlayerManager.data.get(p);
            PlayerStats stats = playerStats.get(p);
                data.setKills(data.getKills() + stats.getKills());
                data.setChestsOpened(data.getChestsOpened() + stats.getChestsOpened());
                data.setBlocksPlaced(data.getBlocksPlaced() + stats.getBlocksPlaced());
                SkyWars.getInstance().getPlayerManager().savePlayer(data);
        }

        new BukkitRunnable() {

            int timer = 10;

            @Override
            public void run() {

                if(gameState != GameState.FINISH) cancel();

                timer--;
                updateBossBar(timer, 10);
                if(timer <= 0) {
                    ArrayList<Player> np = (ArrayList<Player>) players.clone();
                    for(Player p : np)
                        removePlayer(p);
                    bossBar.removeAll();
                    arena.unload();
                    SkyWars.getInstance().getGameManager().addGame(mapName);
                    SkyWars.getInstance().getGameManager().removeGame(id);
                    cancel();
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
        playerStats.put(p, new PlayerStats(p));

        updateBossBar(1 ,1);

        SkyWars.getInstance().getGameManager().getPlayerToGame().put(p, this);

        PlayerUtil.clearPlayer(p);
        p.teleport(new Location(getArena().getWorlds().get(mapName), 0, getArena().getWorlds().get(mapName).getHighestBlockYAt(0, 0), 0));
        giveTeamChoiceWool(p);

        if(players.size() >= playersToStart) changeGameState(GameState.WAITING);
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
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        SkyWars.getInstance().getGameManager().getPlayerToGame().remove(p);
        SkyWars.getInstance().getLobby().teleportPlayer(p);
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

    private void giveTeamChoiceWool(Player p) {
        ItemStack itemStack = new ItemStack(Material.WHITE_WOOL);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Team Choice");
        itemStack.setItemMeta(itemMeta);
        p.getInventory().setItem(4, itemStack);
    }

    private void setUpScoreboards() {

        new BukkitRunnable() {

            @Override
            public void run() {

                ScoreboardManager scManager = Bukkit.getScoreboardManager();

                for(Player p : players) {

                    Scoreboard board = scManager.getNewScoreboard();
                    Objective obj = board.registerNewObjective(p.getDisplayName(), "shotam");
                    obj.setDisplayName(ChatColor.RED + "SkyWars");
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                    Score gold = obj.getScore("Gold: " + ChatColor.GOLD + PlayerManager.data.get(p).getGold());
                    gold.setScore(8);
                    Score map = obj.getScore("Map: " + ChatColor.GREEN + mapName);
                    map.setScore(4);
                    obj.getScore("   ").setScore(9);
                    obj.getScore("  ").setScore(3);
                    obj.getScore(" ").setScore(2);
                    obj.getScore(ChatColor.YELLOW + "MegaCraft").setScore(1);
                    if(gameState == GameState.WAITING) {
                        if(playersToStart < players.size()) {
                            Score pts = obj.getScore("Players: " + players.size() + "/" + playersToStart);
                            pts.setScore(10);
                        }
                    }
                    else if(gameState == GameState.PLAYING) {
                        Score kills = obj.getScore("Kills: " + playerStats.get(p).getKills());
                        Score chestsOpened = obj.getScore("Chests: " + playerStats.get(p).getChestsOpened());
                        Score blocksPlaced = obj.getScore("Blocks: " + playerStats.get(p).getBlocksPlaced());
                        kills.setScore(7);
                        chestsOpened.setScore(6);
                        blocksPlaced.setScore(5);
                    }
                    p.setScoreboard(board);
                }
                if(gameState == GameState.FINISH) {
                    cancel();
                }
            }

        }.runTaskTimer(SkyWars.getInstance(), 20, 20);

    }
}

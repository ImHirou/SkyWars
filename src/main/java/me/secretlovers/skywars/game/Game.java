package me.secretlovers.skywars.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.secretlovers.skywars.PlayerStats;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerData;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.kits.Kit;
import me.secretlovers.skywars.game.phases.*;
import me.secretlovers.skywars.game.team.Team;
import me.secretlovers.skywars.map.SWArena;
import me.secretlovers.skywars.utils.PlayerUtil;
import org.bukkit.*;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.*;

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
    private HashMap<Player, Scoreboard> playerScoreboard = new HashMap<>();

    private Queue<Phase> phases = new LinkedList<>();
    private Phase currentPhase = null;

    private BossBar bossBar;
    private final int maxPlayers;
    private final int playersToStart;

    public Game(int id, String mapName) {
        this.id = id;
        this.mapName = mapName;
        arena = new SWArena();
        File mapDirectory = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/maps/" + mapName);
        arena.loadWorld(mapDirectory, mapName);
        JsonObject teamSection = SkyWars.getInstance().getJsonConfig().getAsJsonObject("maps").
                getAsJsonObject(mapName);
        for(Map.Entry<String, JsonElement> element : teamSection.getAsJsonObject("teams").entrySet())
            teams.add(new Team(element.getValue().getAsJsonObject(),
                    teamSection.get("maxPlayers").getAsInt()));

        bossBar = Bukkit.createBossBar("MegaCraft", BarColor.RED, BarStyle.SEGMENTED_6, BarFlag.DARKEN_SKY);
        maxPlayers = teamSection.get("maxPlayers").getAsInt() * teams.size();
        playersToStart = Integer.min(2, maxPlayers/2);
        initPhases();
        System.out.println(phases);
    }

    private void initPhases() {
        phases.add(new WaitingPhase(15));
        phases.add(new RefillPhase(90));
        phases.add(new GlowingPhase(90));
        phases.add(new RefillPhase(60));
        phases.add(new BorderPhase());
        phases.add(new FinishPhase());
        nextPhase();
    }

    public void nextPhase() {
        if(currentPhase != null) {
            currentPhase.getEnding().accept(this);
        }
        Phase nextPhase = phases.poll();
        currentPhase = nextPhase;
        nextPhase.getStarting().accept(this);
    }

    public void setPhase(Phase phase, boolean endLast) {
        if(endLast)
            if(currentPhase != null)
                currentPhase.getEnding().accept(this);
        currentPhase = phase;
        phase.getStarting().accept(this);
    }

    public void addPlayer(Player p) {
        if(currentPhase.getClass() != WaitingPhase.class) return;
        if(players.contains(p)) return;
        if(SkyWars.getInstance().getGameManager().getPlayerToGame().containsKey(p))
            SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).removePlayer(p);

        players.add(p);
        playerStats.put(p, new PlayerStats(p));

        SkyWars.getInstance().getGameManager().getPlayerToGame().put(p, this);

        PlayerUtil.clearPlayer(p);
        p.teleport(new Location(getArena().getWorlds().get(mapName), 0, getArena().getWorlds().get(mapName).getHighestBlockYAt(0, 0), 0));
        giveTeamChoiceWool(p);
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
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        SkyWars.getInstance().getGameManager().getPlayerToGame().remove(p);
        SkyWars.getInstance().getLobby().teleportPlayer(p);
    }

    private void giveTeamChoiceWool(Player p) {
        ItemStack itemStack = new ItemStack(Material.WHITE_WOOL);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Team Choice");
        itemStack.setItemMeta(itemMeta);
        p.getInventory().setItem(4, itemStack);
    }
}

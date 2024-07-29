package me.secretlovers.skywars.game.phases;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.kits.Kit;
import me.secretlovers.skywars.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.function.Consumer;

public class WaitingPhase implements Phase{

    private int time;
    private String name;

    public WaitingPhase(int t) {
        time = t;
        name = "Waiting";
    }

    @Override
    public Consumer<Game> getStarting() {
        return game -> {
            new BukkitRunnable() {
                int timer = time;
                @Override
                public void run() {

                    if(game.getCurrentPhase().getClass() != WaitingPhase.class) cancel();

                    if(game.getPlayersToStart() <= game.getPlayers().size()) {
                        timer--;
                    } else {
                        timer = time;
                    }
                    if(timer <= 0) {
                        game.nextPhase();
                        cancel();
                    }

                    setScoreboard(game, timer);
                    setBossBar(game);

                }

            }.runTaskTimer(SkyWars.getInstance(), 0, 20);
        };
    }

    @Override
    public Consumer<Game> getEnding() {
        return game -> {

            for (Player p : game.getPlayers()) {
                if (!game.getPlayerTeam().containsKey(p)) {
                    for (Team team : game.getTeams()) {
                        if (!team.isFull()) {
                            team.addPlayer(p);
                            game.getPlayerTeam().put(p, team);
                            break;
                        }
                    }
                }
            }

            if (game.getArena().isLoaded(game.getMapName())) {
                System.out.println("game map is loaded");
                for (Team team : game.getTeams()) {
                    team.spawn(game.getArena().getWorlds().get(game.getMapName()));
                    for (Player p : team.getPlayers()) {
                        Kit.getKits().get(PlayerManager.data.get(p).getSelectedKit()).give(p);
                        System.out.println(PlayerManager.data.get(p).getSelectedKit());
                    }
                }


            }
            game.getBossBar().removeAll();

        };
    }

    @Override
    public String getName() {
        return name;
    }

    private void setScoreboard(Game game, int time) {

        for(Player p : game.getPlayers()) {

            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj;
            if(board.getObjective(p.getDisplayName()) != null)
                obj = board.getObjective(p.getDisplayName());
            else
                obj = board.registerNewObjective(p.getDisplayName(), "shotam");

            obj.setDisplayName(ChatColor.RED + "SkyWars");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            Score start;
            if(game.getPlayers().size() < game.getPlayersToStart())
                start = obj.getScore("Players: " + game.getPlayers().size() + "/" + game.getPlayersToStart());
            else
                start = obj.getScore("Start in: " + time);
            start.setScore(12);
            Score gold = obj.getScore("Gold: " + ChatColor.GOLD + PlayerManager.data.get(p).getGold());
            gold.setScore(10);
            Score map = obj.getScore("Map: " + ChatColor.GREEN + game.getMapName());
            map.setScore(6);
            obj.getScore("").setScore(15);
            obj.getScore(" ").setScore(14);
            obj.getScore("  ").setScore(13);
            obj.getScore("   ").setScore(11);
            obj.getScore("    ").setScore(9);
            obj.getScore("     ").setScore(8);
            obj.getScore("      ").setScore(7);
            obj.getScore("       ").setScore(5);
            obj.getScore("        ").setScore(4);
            obj.getScore("         ").setScore(3);
            obj.getScore("          ").setScore(2);
            obj.getScore(ChatColor.YELLOW + "MegaCraft").setScore(1);
            p.setScoreboard(board);

        }

    }

    private void setBossBar(Game game) {
        BossBar bossBar = game.getBossBar();
        bossBar.setVisible(true);
        bossBar.setProgress(1.0);
        for(Player p : game.getPlayers())
            bossBar.addPlayer(p);
        bossBar.setColor(BarColor.RED);
        bossBar.setTitle(ChatColor.YELLOW + "MegaCraft");
        bossBar.setStyle(BarStyle.SEGMENTED_6);
    }

}

package me.secretlovers.skywars.game.phases;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.function.Consumer;

public class BorderPhase implements Phase{
    @Override
    public Consumer<Game> getStarting() {
        return game -> {

            WorldBorder border = game.getArena().getWorlds().get(game.getMapName()).getWorldBorder();
            border.setWarningDistance(4);

            new BukkitRunnable() {

                int iterations = 0;

                @Override
                public void run() {

                    if(game.getCurrentPhase().getClass() != BorderPhase.class) cancel();

                    iterations++;
                    double size = Math.max(150-iterations/20.0, 8);
                    border.setSize(size);
                    border.setDamageAmount((iterations / 50.0)+1);
                    if(iterations%20 == 0) {
                        int alive = 0;
                        for (Team team : game.getTeams()) {
                            if (team.isAlive()) alive++;
                            if (alive >= 2) break;
                        }
                        if (alive == 1) {
                            game.setPhase(new FinishPhase(), false);
                            cancel();
                        }

                        setScoreboard(game, (int) size);
                    }
                }

            }.runTaskTimer(SkyWars.getInstance(), 0 ,1);
        };
    }

    @Override
    public Consumer<Game> getEnding() {
        return game -> {

        };
    }

    @Override
    public String getName() {
        return "Border";
    }

    private void setScoreboard(Game game, int bSize) {

        for(Player p : game.getPlayers()) {

            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj;
            if(board.getObjective(p.getDisplayName()) != null)
                obj = board.getObjective(p.getDisplayName());
            else
                obj = board.registerNewObjective(p.getDisplayName(), "shotam");


            obj.setDisplayName(ChatColor.RED + "SkyWars");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            Score phase = obj.getScore("Event: " + ChatColor.GREEN + getName());
            phase.setScore(13);
            ChatColor color = bSize >= 125 ? ChatColor.GREEN :
                    bSize>=90 ? ChatColor.DARK_GREEN :
                            bSize>=65 ? ChatColor.YELLOW :
                                    bSize>=40 ? ChatColor.GOLD :
                                            bSize>=20 ? ChatColor.RED :
                                                    ChatColor.DARK_RED;
            Score size = obj.getScore("Border size: " + color + "" + bSize);
            size.setScore(12);
            Score gold = obj.getScore("Gold: " + ChatColor.GOLD + PlayerManager.data.get(p).getGold());
            gold.setScore(10);
            Score kills = obj.getScore("Kills: " + ChatColor.RED + game.getPlayerStats().get(p).getKills());
            kills.setScore(8);
            Score chest = obj.getScore("Chests: " + ChatColor.YELLOW + game.getPlayerStats().get(p).getChestsOpened());
            chest.setScore(7);
            Score block = obj.getScore("Blocks: " + game.getPlayerStats().get(p).getBlocksPlaced());
            block.setScore(6);
            Score map = obj.getScore("Map: " + ChatColor.GREEN + game.getMapName());
            map.setScore(4);
            obj.getScore("").setScore(15);
            obj.getScore(" ").setScore(14);
            obj.getScore("  ").setScore(11);
            obj.getScore("   ").setScore(9);
            obj.getScore("    ").setScore(5);
            obj.getScore("     ").setScore(3);
            obj.getScore("      ").setScore(2);
            obj.getScore(ChatColor.YELLOW + "MegaCraft").setScore(1);
            p.setScoreboard(board);

        }
    }

}

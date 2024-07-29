package me.secretlovers.skywars.game.phases;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.function.Consumer;

public class GlowingPhase implements Phase{

    private int time;
    private String name;

    public GlowingPhase(int t) {
        time = t;
        name = "Glowing";
    }

    @Override
    public Consumer<Game> getStarting() {
        return game -> {

            new BukkitRunnable() {

                int timer = time;

                @Override
                public void run() {
                    timer--;

                    if(game.getCurrentPhase().getClass() != GlowingPhase.class) cancel();

                    int alive = 0;

                    for(Team team : game.getTeams()) {
                        if(team.isAlive()) alive++;
                        if(alive >= 2) break;
                    }
                    if(alive == 1) {
                        game.setPhase(new FinishPhase(), false);
                        cancel();
                    }
                    if(timer <= 0) {
                        game.nextPhase();
                        cancel();
                    }

                    setScoreboard(game, timer);

                }

            }.runTaskTimer(SkyWars.getInstance(), 0, 20);

        };

    }

    @Override
    public Consumer<Game> getEnding() {
        return game -> {
            for (Player p : game.getPlayers()) {
                if(game.getSpectators().contains(p)) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 99999, 1, false, false, false));
            }
        };
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
            Score phase = obj.getScore("Next event: " + ChatColor.GREEN + name);
            phase.setScore(13);
            Score start = obj.getScore("In: " + ChatColor.GREEN + time + "s");
            start.setScore(12);
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

    @Override
    public String getName() {
        return name;
    }

}

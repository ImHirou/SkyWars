package me.secretlovers.skywars.game.phases;

import me.secretlovers.skywars.PlayerStats;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerData;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.GameState;
import me.secretlovers.skywars.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.function.Consumer;

public class FinishPhase implements Phase{

    @Override
    public Consumer<Game> getStarting() {
        return game -> {

            int alive = 0;
            for(Team team : game.getTeams())
                if(team.isAlive()) alive++;

            if(alive == 1) {
                for(Player p : game.getPlayers()) {
                    if(!game.getSpectators().contains(p)) {
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

            for(Player p : game.getPlayers()) {
                PlayerData data = PlayerManager.data.get(p);
                PlayerStats stats = game.getPlayerStats().get(p);
                data.setKills(data.getKills() + stats.getKills());
                data.setChestsOpened(data.getChestsOpened() + stats.getChestsOpened());
                data.setBlocksPlaced(data.getBlocksPlaced() + stats.getBlocksPlaced());
                SkyWars.getInstance().getPlayerManager().savePlayer(data);
            }

            new BukkitRunnable() {

                int timer = 10;

                @Override
                public void run() {

                    timer--;

                    if(timer <= 0) {
                        ArrayList<Player> np = (ArrayList<Player>) game.getPlayers().clone();
                        for(Player p : np)
                            game.removePlayer(p);
                        //game.getBossBar().removeAll();
                        game.getArena().unload();
                        SkyWars.getInstance().getGameManager().addGame(game.getMapName());
                        SkyWars.getInstance().getGameManager().removeGame(game.getId());
                        cancel();
                    }

                }

            }.runTaskTimer(SkyWars.getInstance(), 0, 20);

        };
    }

    @Override
    public Consumer<Game> getEnding() {
        return game -> {
            for(Player p : game.getPlayers()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        };
    }

    @Override
    public String getName() {
        return "Finish";
    }

}

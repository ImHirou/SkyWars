package me.secretlovers.skywars.listeners;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onEvent(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!isInGame(p)) return;
        if (isSpectator(p)) event.setCancelled(true);
        if (SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).getGameState() != GameState.PLAYING) event.setCancelled(true);
    }

    @EventHandler
    public void onEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player )) return;
        if (!(event.getEntity() instanceof Player )) return;

        Player damager = (Player) event.getDamager();
        Player player = (Player) event.getEntity();

        if (!isInGame(damager)) return;
        if (isSpectator(damager)) event.setCancelled(true);
        Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(damager);
        if (game.getGameState() != GameState.PLAYING) event.setCancelled(true);
        if (game.getPlayerTeam().get(damager).getPlayers().contains(player)) event.setCancelled(true);
        System.out.println(game.getPlayerTeam().get(damager).getPlayers());
    }

    @EventHandler
    public void onEvent(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (!isInGame(p)) return;
        if (isSpectator(p)) event.setCancelled(true);
        if (SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).getGameState() != GameState.PLAYING) event.setCancelled(true);
        if (event.getBlock().getType() == Material.CHEST) event.setCancelled(true);
    }

    @EventHandler
    public void onEvent(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!isInGame(p)) return;
        if (isSpectator(p)) event.setCancelled(true);
        if (SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).getGameState() != GameState.PLAYING) event.setCancelled(true);

    }

    @EventHandler
    public void onEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (!isInGame(p)) return;
        SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).removePlayer(p);
    }

    @EventHandler
    public void onEvent(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (!isInGame(p)) return;
        Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(p);
        game.killPlayer(p);
        game.getGameMap().getWorld().spawnParticle(Particle.CRIMSON_SPORE, p.getLocation(), 100);
        game.getGameMap().getWorld().playSound(p.getLocation(), Sound.AMBIENT_CRIMSON_FOREST_MOOD, 1, 1);
        p.teleport(game.getGameMap().getWorld().getSpawnLocation());

    }

    @EventHandler
    public void onEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!(isInGame(player))) return;
        if (isSpectator(player)) event.setCancelled(true);
        if (SkyWars.getInstance().getGameManager().getPlayerToGame().get(player).getGameState() != GameState.PLAYING) event.setCancelled(true);
    }

    @EventHandler
    public void onEvent(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (!(isInGame(p))) return;
        Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(p);
        if (p.getLocation().getY() < 0) {
            if(isSpectator(p)) p.teleport(game.getGameMap().getWorld().getSpawnLocation());
            else {
                if(game.getGameState() == GameState.PLAYING) p.setHealth(0);
                else p.teleport(new Location(game.getGameMap().getWorld(), 0, game.getGameMap().getWorld().getHighestBlockYAt(0, 0), 0));

            }
        }
    }

    private boolean isInGame(Player p) {
        return SkyWars.getInstance().getGameManager().getPlayerToGame().containsKey(p);
    }

    private boolean isSpectator(Player p) {
        return SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).getSpectators().contains(p);
    }

}

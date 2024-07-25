package me.secretlovers.skywars.listeners;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerData;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.GameState;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PlayerListeners implements Listener {


    @EventHandler
    public void onEvent(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        if(!isInGame(p)) return;
        if(isSpectator(p)) event.setCancelled(true);
        if(SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).getGameState() != GameState.PLAYING) event.setCancelled(true);
    }

    @EventHandler
    public void onEvent(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!isInGame(p)) return;
        if (isSpectator(p)) event.setCancelled(true);
        Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(p);
        if (game.getGameState() != GameState.PLAYING) event.setCancelled(true);
        if (p.getItemInHand().getType() == Material.WHITE_WOOL &&
                (game.getGameState() == GameState.UNKNOWN || game.getGameState() == GameState.WAITING) &&
                (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            p.performCommand("game team");
        }
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
        Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(p);
        if (isSpectator(p)) event.setCancelled(true);
        else if (game.getGameState() != GameState.PLAYING) event.setCancelled(true);
        else if (event.getBlock().getLocation().getY() > 150) event.setCancelled(true);
        else if (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST) event.setCancelled(true);
        else game.getPlayerStats().get(p).addBlockPlaced();
    }

    @EventHandler
    public void onEvent(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!isInGame(p)) return;
        if (isSpectator(p)) event.setCancelled(true);
        if (SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).getGameState() != GameState.PLAYING) event.setCancelled(true);
        if (event.getBlock().getType() == Material.CHEST) {
            Chest chest = (Chest) event.getBlock();
            if(!SkyWars.getInstance().getChestManager().isOpened(chest.getLocation())) SkyWars.getInstance().getChestManager().fill(chest.getInventory(), 1);
        } else if (event.getBlock().getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) event.getBlock();
            if(!SkyWars.getInstance().getChestManager().isOpened(chest.getLocation())) SkyWars.getInstance().getChestManager().fill(chest.getInventory(), 2);
        }

    }

    @EventHandler
    public void onEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        PlayerManager manager = SkyWars.getInstance().getPlayerManager();
        PlayerData data = PlayerManager.data.get(p);
        manager.savePlayer(data);
        PlayerManager.data.remove(p);

        if (!isInGame(p)) return;
        SkyWars.getInstance().getGameManager().getPlayerToGame().get(p).removePlayer(p);
    }

    @EventHandler
    public void onEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        PlayerManager manager = SkyWars.getInstance().getPlayerManager();
        manager.loadPlayerByNickname(p.getDisplayName(), result -> {
            if(result == null) {
                result = new PlayerData(p.getDisplayName());
                manager.savePlayer(result);
            } else {
            }
            PlayerManager.data.put(p, result);
        });;
    }

    @EventHandler
    public void onEvent(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (!isInGame(p)) return;
        Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(p);
        for(ItemStack itemStack : p.getInventory().getContents()) {
            if(itemStack == null) continue;
            p.getWorld().dropItem(p.getLocation().add(0, 1, 0), itemStack);
        }
        p.getInventory().clear();
        event.getDrops().removeIf(Objects::nonNull);
        p.setHealth(20);
        game.killPlayer(p);
        game.getArena().getWorlds().get(game.getMapName()).spawnParticle(Particle.CRIMSON_SPORE, p.getLocation(), 100);
        game.getArena().getWorlds().get(game.getMapName()).playSound(p.getLocation(), Sound.AMBIENT_CRIMSON_FOREST_MOOD, 1, 1);
        p.teleport(game.getArena().getWorlds().get(game.getMapName()).getSpawnLocation());
        Player killer = p.getKiller();
        if (killer != null) {
            game.getPlayerStats().get(p.getKiller()).addKill();
            PlayerManager.data.get(killer).setGold(PlayerManager.data.get(killer).getGold()+500);
            killer.sendMessage(ChatColor.GOLD + "+ 500 Gold");
        }
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
            if(isSpectator(p)) p.teleport(game.getArena().getWorlds().get(game.getMapName()).getSpawnLocation());
            else {
                if(game.getGameState() == GameState.PLAYING) p.setHealth(0);
                else p.teleport(new Location(game.getArena().getWorlds().get(game.getMapName()), 0, game.getArena().getWorlds().get(game.getMapName()).getHighestBlockYAt(0, 0), 0));

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

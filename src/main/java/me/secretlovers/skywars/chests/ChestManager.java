package me.secretlovers.skywars.chests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChestManager implements Listener {

    private ArrayList<Location> openedChests = new ArrayList<>();
    private ArrayList<SkyWarsItem> firstLevelDrop = new ArrayList<>();
    private ArrayList<SkyWarsItem> secondLevelDrop = new ArrayList<>();

    public ChestManager() {
        JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("items");
        for(Map.Entry<String, JsonElement> key : json.getAsJsonObject("first").entrySet()) {
            firstLevelDrop.add(new SkyWarsItem(key.getValue().getAsJsonObject()));
        }
        for(Map.Entry<String, JsonElement> key : json.getAsJsonObject("second").entrySet()) {
            secondLevelDrop.add(new SkyWarsItem(key.getValue().getAsJsonObject()));
        }
    }

    @EventHandler
    private void onEvent(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();
        //if(!SkyWars.getInstance().getGameManager().getPlayerToGame().containsKey(player)) return;
        //Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(player);
        if(holder instanceof Chest) {
            Chest chest = (Chest) holder;
            if(isOpened(chest.getLocation())) return;
            markAsOpen(chest.getLocation());
            if(chest.getBlock().getType() == Material.CHEST) fill(chest.getBlockInventory(), 1);
            else if(chest.getBlock().getType() == Material.TRAPPED_CHEST) fill(chest.getBlockInventory(), 2);
            //game.getPlayerStats().get(player).addChestOpen();
        }
        if(holder instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) holder;
            if (isOpened(chest.getLocation())) return;

            markAsOpen(chest.getLocation());
            if(chest.getLocation().getBlock().getType() == Material.CHEST) fill(chest.getInventory(), 1);
            else if(chest.getLocation().getBlock().getType() == Material.TRAPPED_CHEST) fill(chest.getInventory(), 2);
            //game.getPlayerStats().get(player).addChestOpen();
        }
    }

    public void fill(Inventory inventory, int chestLevel) {
        inventory.clear();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Set<SkyWarsItem> used = new HashSet<>();
        int maxDrop = chestLevel == 1 ? firstLevelDrop.size() : secondLevelDrop.size();
        if(maxDrop == 0) return;
        int itemFilled = 0;
        do {
            for (int slotIndex = 0; slotIndex < inventory.getSize(); slotIndex++) {
                SkyWarsItem randomItem = null;
                if(chestLevel == 1) randomItem = firstLevelDrop.get(random.nextInt(firstLevelDrop.size()));
                else if(chestLevel == 2) randomItem = secondLevelDrop.get(random.nextInt(secondLevelDrop.size()));

                if(used.contains(randomItem)) continue;

                if (randomItem.needToFill(random)) {
                    used.add(randomItem);
                    inventory.setItem(slotIndex, randomItem.getItemStack(random));
                    itemFilled++;
                }
            }
        } while (itemFilled <= inventory.getSize()/9+1 && itemFilled != maxDrop);
    }

    public void markAsOpen(Location location) {
        openedChests.add(location);
    }

    public boolean isOpened(Location location) {
        return openedChests.contains(location);
    }

    public void resetChests() {
        openedChests.clear();
    }

}

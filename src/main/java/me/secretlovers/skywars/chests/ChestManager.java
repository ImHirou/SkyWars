package me.secretlovers.skywars.chests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.Game;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChestManager implements Listener {

    private ArrayList<Location> openedChests = new ArrayList<>();
    private ArrayList<SkyWarsItem> itemsToDrop = new ArrayList<>();

    public ChestManager() {
        JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("items");
        for(Map.Entry<String, JsonElement> key : json.entrySet()) {
            itemsToDrop.add(new SkyWarsItem(key.getValue().getAsJsonObject()));
        }
    }

    @EventHandler
    private void onEvent(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof Chest) {
            Chest chest = (Chest) holder;
            if(isOpened(chest.getLocation())) return;

            markAsOpen(chest.getLocation());
            fill(chest.getBlockInventory());
        }
        if(holder instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) holder;
            if(isOpened(chest.getLocation())) return;

            markAsOpen(chest.getLocation());
            fill(chest.getInventory());
        }
    }

    public void fill(Inventory inventory) {
        inventory.clear();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Set<SkyWarsItem> used = new HashSet<>();

        for(int slotIndex = 0; slotIndex < inventory.getSize(); slotIndex++) {
            SkyWarsItem randomItem = null;
            for(int i = 0; i <= itemsToDrop.size(); i++) {
                randomItem = itemsToDrop.get(random.nextInt(itemsToDrop.size()));
                if(used.contains(randomItem)) continue;
                if(randomItem.needToFill(random)) {
                    used.add(randomItem);
                    inventory.setItem(slotIndex, randomItem.getItemStack(random));
                    break;
                }
            }


        }
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

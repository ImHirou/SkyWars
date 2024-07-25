package me.secretlovers.skywars.game.kits;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.chests.SkyWarsItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Kit {
    @Getter
    private static Map<String ,Kit> kits = new HashMap<>();

    private String name;
    private int cost;
    private Map<Integer, ItemStack> items = new HashMap<>();

    public Kit(JsonObject json) {
        name = json.get("name").getAsString();
        cost = json.get("cost").getAsInt();
        JsonObject jsonItems = json.getAsJsonObject("items");
        for(Map.Entry<String, JsonElement> entry : jsonItems.entrySet())
            items.put(Integer.parseInt(entry.getKey()), SkyWarsItem.JsonToItemStack(entry.getValue().getAsJsonObject()));
    }

    public void give(Player p) {
        for(Integer key : items.keySet())
            p.getInventory().setItem(key, items.get(key));
    }

    public static void initKits() {
        JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("kits");
        for(Map.Entry<String, JsonElement> entry : json.entrySet())
            kits.put(entry.getKey(), new Kit(entry.getValue().getAsJsonObject()));
    }

}

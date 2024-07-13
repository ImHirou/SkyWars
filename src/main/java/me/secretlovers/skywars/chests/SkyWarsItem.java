package me.secretlovers.skywars.chests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SkyWarsItem {

    private final Material material;
    private final String name;
    private final HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    private final double dropChance;
    private final int minAmount;
    private final int maxAmount;

    public SkyWarsItem(JsonObject json) {
            Material material;

        try {
            material = Material.valueOf(json.get("material").getAsString());
        } catch (Exception e) {
            material = Material.AIR;
        }
        this.material = material;

        name = json.get("name").getAsString();

        JsonObject enchantmentJson = json.getAsJsonObject("enchants");
        if(enchantmentJson != null) {
            System.out.println(enchantmentJson);
            for (Map.Entry<String, JsonElement> key : enchantmentJson.entrySet()) {
                System.out.println(key);
                Enchantment enchantment = Enchantment.getByKey(
                        NamespacedKey.minecraft(
                                key.getKey()
                        )
                );
                if(enchantment != null) {
                 int level = key.getValue().getAsInt();
                 enchantments.put(enchantment, level);
                }
            }
        }

        dropChance = json.get("chance").getAsDouble();
        minAmount = json.get("minAmount").getAsInt();
        maxAmount = json.get("maxAmount").getAsInt();

    }

    public boolean needToFill(Random random) {
        return random.nextDouble() < dropChance;
    }

    public ItemStack getItemStack(ThreadLocalRandom random) {

        int amount = random.nextInt(minAmount, maxAmount + 1);

        ItemStack itemStack = new ItemStack(material, amount);
        if(material == Material.AIR) return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if(name != null)  itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        itemMeta.setUnbreakable(true);

        if(!enchantments.isEmpty()) {
            for(Map.Entry<Enchantment, Integer> enchantmentEntry : enchantments.entrySet()) {
                int level = random.nextInt(0, enchantmentEntry.getValue() + 1);
                if(level == 0) continue;
                itemMeta.addEnchant(
                        enchantmentEntry.getKey(),
                        level,
                        true
                );
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}

package me.secretlovers.skywars.chests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SkyWarsItem {

    private final Material material;
    private final NBTTagCompound compound;
    private final double dropChance;
    private final int minAmount;
    private final int maxAmount;

    public SkyWarsItem(JsonObject json) {
        NBTTagCompound compound = null;
        Material material;

        try {
            material = Material.valueOf(json.get("material").getAsString());
        } catch (Exception e) {
            material = Material.AIR;
        }
        this.material = material;

        try {
            if(json.get("nbt") != null) compound = MojangsonParser.parse(json.get("nbt").getAsString());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        this.compound = compound;
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

        if(compound != null) {
            net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
            nmsItem.setTag(compound);
            itemStack = CraftItemStack.asBukkitCopy(nmsItem);
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        Set<Enchantment> removeEnch = new HashSet<>();
        for(Enchantment enchantment : itemMeta.getEnchants().keySet()) {
            int i = random.nextInt(itemMeta.getEnchants().get(enchantment)+1);
            if(i > 0) itemMeta.addEnchant(enchantment, i, true);
            else removeEnch.add(enchantment);
        }
        for(Enchantment ench : removeEnch) itemMeta.removeEnchant(ench);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static JsonObject ItemStackToJson(ItemStack itemStack) {
        JsonObject json = new JsonObject();
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if(nmsItem.getTag() != null) json.addProperty("nbt", nmsItem.getTag().toString());
        json.addProperty("material", itemStack.getType().toString());
        json.addProperty("amount", itemStack.getAmount());
        return json;
    }

    public static ItemStack JsonToItemStack(JsonObject json) {
        Material material = Material.getMaterial(json.get("material").getAsString());
        if(material == null) material = Material.AIR;
        int amount = json.get("amount").getAsInt();
        if (amount <= 0) amount = 1;
        ItemStack itemStack = new ItemStack(material, amount);
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        try {
            if(json.get("nbt") != null) nmsItem.setTag(MojangsonParser.parse(json.get("nbt").getAsString()));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        itemStack = CraftItemStack.asBukkitCopy(nmsItem);
        return itemStack;
    }


}

package me.secretlovers.skywars.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.chests.SkyWarsItem;
import me.secretlovers.skywars.utils.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminCommand implements CommandExecutor, TabCompleter {

    public AdminCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length < 1) return true;

        if (args[0].equals("goto")) {
            if (args.length < 2) return true;
            String mapName = args[1];
            File mapDirectory = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/maps/" + mapName);
            File newDirectory = new File(Bukkit.getWorldContainer(), mapDirectory.getName());
            if (!newDirectory.exists()) newDirectory.mkdirs();
            try {
                FileUtils.copyDirectory(mapDirectory, newDirectory);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Fail map load in GameMap from: " + mapDirectory.getName());
                e.printStackTrace(System.err);
                return false;
            }
            WorldCreator creator = new WorldCreator(newDirectory.getName());
            creator.createWorld();
            World world;

            world = Bukkit.getWorld(newDirectory.getName());

            if (world == null) return true;
            world.setAutoSave(false);
            player.teleport(world.getSpawnLocation());
        }

        if (args[0].equals("set")) {
            System.out.println("IS SET");
            if (args.length < 2) return true;
            if (args[1].equals("team")) {
                System.out.println("IS TEAM");
                if (args.length < 3) return true;
                JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("maps").getAsJsonObject(player.getWorld().getName()).getAsJsonObject("teams");
                System.out.println(args[2] + " ARGS2");
                System.out.println(json);
                if (!json.has(args[2])) return true;
                JsonObject jsonSpawnLoc = json.getAsJsonObject(args[2]).getAsJsonObject("spawnLocation");
                jsonSpawnLoc.addProperty("x", player.getLocation().getBlockX());
                jsonSpawnLoc.addProperty("y", player.getLocation().getBlockY());
                jsonSpawnLoc.addProperty("z", player.getLocation().getBlockZ());
                File file = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/config.json");
                JsonUtil.jsonToFile(SkyWars.getInstance().getJsonConfig(), file);
            }
        }

        if (args[0].equals("save")) {
            World world = player.getWorld();
            for (Player p : world.getPlayers())
                p.kickPlayer("Saving map " + world.getName());
            world.save();
            File mapDirectory = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/maps/" + player.getWorld().getName());
            try {
                FileUtils.copyDirectory(world.getWorldFolder(), mapDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            File uiddat = new File(mapDirectory.getAbsolutePath() + "/uid.dat");
            File sessionslock = new File(mapDirectory.getAbsolutePath() + "/session.lock");
            uiddat.delete();
            sessionslock.delete();
            world.getWorldFolder().delete();
        }

        if (args[0].equals("items")) {
            if (args.length < 3) return true;
            if (args[1].equals("add")) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack.getType() == Material.AIR) return false;
                double chance = Double.parseDouble(args[2]);
                String level = args[3];
                JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("items").getAsJsonObject(level);
                int prev = 1;
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (prev == Integer.parseInt(entry.getKey())) prev++;
                    else break;
                }
                JsonObject item = SkyWarsItem.ItemStackToJson(itemStack);
                item.remove("amount");

                item.addProperty("chance", chance);
                item.addProperty("minAmount", 1);
                item.addProperty("maxAmount", itemStack.getAmount());
                json.add(String.valueOf(prev), item);
                File file = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/config.json");
                JsonUtil.jsonToFile(SkyWars.getInstance().getJsonConfig(), file);
                player.sendMessage("Item successfully added");
            }
        }
        if (args[0].equals("kits")) {
            if(args.length < 4) return true;
            if(args[1].equals("add")) {
                String name = args[2];
                int cost = Integer.parseInt(args[3]);
                JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("kits");
                JsonObject kitJson = new JsonObject();
                kitJson.addProperty("name", name);
                kitJson.addProperty("cost", cost);
                kitJson.add("items", new JsonObject());
                int slot = 0;
                for(ItemStack itemStack : player.getInventory().getContents()) {
                    if(itemStack == null) {
                        slot++;
                        continue;
                    }
                    JsonObject itemJson = SkyWarsItem.ItemStackToJson(itemStack);
                    itemJson.addProperty("slot", slot);
                    kitJson.getAsJsonObject("items").add(String.valueOf(slot), itemJson);
                    slot++;
                }
                json.add(name, kitJson);
                File file = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/config.json");
                JsonUtil.jsonToFile(SkyWars.getInstance().getJsonConfig(), file);
                player.sendMessage("Kit " + name + " successfully added");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1) {
            return Arrays.asList(
                    "goto",
                    "save",
                    "set",
                    "items",
                    "kits");
        } else {
            return null;
        }
    }
}

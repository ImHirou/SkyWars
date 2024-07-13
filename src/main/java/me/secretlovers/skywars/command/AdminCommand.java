package me.secretlovers.skywars.command;

import com.google.gson.JsonObject;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.utils.JsonUtil;
import me.secretlovers.skywars.utils.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class AdminCommand implements CommandExecutor {

    public AdminCommand() {

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length < 1) return true;

        if (args[0].equals("goto")) {
            if(args.length < 2) return true;
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

            if(world == null) return true;
            world.setAutoSave(false);
            player.teleport(world.getSpawnLocation());
        }

        if(args[0].equals("set")) {
            System.out.println("IS SET");
            if(args.length < 2) return true;
            if(args[1].equals("team")) {
                System.out.println("IS TEAM");
                if(args.length < 3) return true;
                JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("maps").getAsJsonObject(player.getWorld().getName()).getAsJsonObject("teams");
                System.out.println(args[2] + " ARGS2");
                System.out.println(json);
                if(!json.has(args[2])) return true;
                JsonObject jsonSpawnLoc = json.getAsJsonObject(args[2]).getAsJsonObject("spawnLocation");
                jsonSpawnLoc.addProperty("x", player.getLocation().getBlockX());
                jsonSpawnLoc.addProperty("y", player.getLocation().getBlockY());
                jsonSpawnLoc.addProperty("z", player.getLocation().getBlockZ());
                File file = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/config.json");
                JsonUtil.jsonToFile(SkyWars.getInstance().getJsonConfig(), file);
            }
        }

        if(args[0].equals("save")) {
            World world = player.getWorld();
            for(Player p : world.getPlayers())
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

        if(args[0].equals("item")) {
            if(args.length < 3) return true;
            if(args[1].equals("add")) {
                ItemStack itemStack = player.getItemInHand();
                if(itemStack.getType() == Material.AIR) return false;
                double chance = Double.parseDouble(args[2]);
                JsonObject json = SkyWars.getInstance().getJsonConfig().getAsJsonObject("items");
                int items = json.entrySet().size()+1;
                JsonObject item = new JsonObject();
                ItemMeta itemMeta = itemStack.getItemMeta();
                item.addProperty("material", itemStack.getType().toString());
                item.addProperty("name", itemMeta.getDisplayName());
                if(!itemMeta.getEnchants().keySet().isEmpty()) {
                    JsonObject enchants = new JsonObject();
                    for (Enchantment enchantment : itemMeta.getEnchants().keySet())
                        enchants.addProperty(enchantment.getKey().getKey().toLowerCase(Locale.ROOT), itemMeta.getEnchants().get(enchantment));
                    item.add("enchants", enchants);
                }
                item.addProperty("chance", chance);
                item.addProperty("minAmount", 1);
                item.addProperty("maxAmount", itemStack.getAmount());
                json.add(String.valueOf(items), item);
                File file = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/config.json");
                JsonUtil.jsonToFile(SkyWars.getInstance().getJsonConfig(), file);
                player.sendMessage("Item successfully added");
            }
        }

        return true;
    }
}

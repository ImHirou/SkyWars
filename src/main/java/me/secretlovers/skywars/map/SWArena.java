package me.secretlovers.skywars.map;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.utils.WorldUtil;
import net.megacraft.multi.arena.IArena;
import org.bukkit.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SWArena{

    private UUID uuid;
    private Set<Player> players = new HashSet<>();
    private Map<String, World> worlds = new HashMap<>();

    public SWArena() {
        uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public Map<String, World> getWorlds() {
        return worlds;
    }

    public void addWorld(String s, World world) {
        worlds.put(s, world);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void addPlayers(List<Player> list) {
        players.addAll(list);
    }

    public void unload() {
        for(World world : worlds.values()) {
            for (Player p : world.getPlayers()) {
                SkyWars.getInstance().getLobby().teleportPlayer(p);
            }
            File folder = new File(Bukkit.getWorldContainer() + "/" + world.getName());

            Bukkit.unloadWorld(world, false);
            try {
                FileUtils.deleteDirectory(folder);
                if (folder.exists()) System.out.println("don't deleted");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
    }

    public void unloadWorld(String s) {
        for(Player p : worlds.get(s).getPlayers()) SkyWars.getInstance().getLobby().teleportPlayer(p);
        WorldUtil.unloadWorld(worlds.get(s));
        worlds.get(s).getWorldFolder();
    }

    public boolean isLoaded(String s) {
        return worlds.get(s) != null;
    }
    public void restoreFromSource(String s) {
        unloadWorld(s);
        loadWorld(new File(SkyWars.getInstance().getDataFolder().getAbsoluteFile() + "/maps/" + s), s);
    }

    public void loadWorld(File file, String name) {
        File activeWorldFolder = new File(
                Bukkit.getWorldContainer(),
                file.getName() + "_" + uuid
        );
        if (!activeWorldFolder.exists()) activeWorldFolder.mkdirs();
        try {
            FileUtils.copyDirectory(file, activeWorldFolder);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Fail map load from: " + file.getName());
            e.printStackTrace(System.err);
        }
        Bukkit.getScheduler().runTask(SkyWars.getInstance(), new Runnable() {
            @Override
            public void run() {

                WorldCreator creator = new WorldCreator(activeWorldFolder.getName());
                worlds.put(name, creator.createWorld());
                worlds.get(name).setKeepSpawnInMemory(false);
                worlds.get(name).setAutoSave(false);
                worlds.get(name).setDifficulty(Difficulty.HARD);

            }

        });
    }

}

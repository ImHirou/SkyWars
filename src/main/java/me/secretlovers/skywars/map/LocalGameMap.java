package me.secretlovers.skywars.map;

import me.secretlovers.skywars.SkyWars;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import me.secretlovers.skywars.utils.WorldUtil;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.FileUtil;

public class LocalGameMap implements GameMap{

    private final File sourceWorldFolder;
    private File activeWorldFolder;
    private World bukkitWorld;
    private final String worldName;

    public LocalGameMap(File worldFolder, String worldName, boolean loadOnInit) {
        this.sourceWorldFolder = new File(
                worldFolder,
                worldName
        );
        this.worldName = worldName;
        if (loadOnInit) {
            load();
        }
    }

    @Override
    public boolean load() {
        if (isLoaded()) return true;
        activeWorldFolder = new File(
                Bukkit.getWorldContainer(),
                sourceWorldFolder.getName() + "_active_" + System.currentTimeMillis()
        );
        if (!activeWorldFolder.exists()) activeWorldFolder.mkdirs();
        try {
            FileUtils.copyDirectory(sourceWorldFolder, activeWorldFolder);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Fail map load in GameMap from: " + sourceWorldFolder.getName());
            e.printStackTrace(System.err);
        }
        WorldCreator creator = new WorldCreator(activeWorldFolder.getName());
        creator.createWorld();

        bukkitWorld = Bukkit.getWorld(activeWorldFolder.getName());

        if (bukkitWorld != null) bukkitWorld.setAutoSave(false);

        return isLoaded();
    }

    @Override
    public void unload() {
            WorldUtil.unloadWorld(bukkitWorld);
            if (activeWorldFolder != null) {
                WorldUtil.deleteWorld(activeWorldFolder);
            }
            bukkitWorld = null;
            activeWorldFolder = null;
    }

    @Override
    public boolean restoreFromSource() {
            unload();
            return load();
    }

    @Override
    public boolean isLoaded() {
            return bukkitWorld != null;
    }

    @Override
    public World getWorld() {
            return bukkitWorld;
    }

    @Override
    public String getWorldName() {
            return worldName;
        }
}

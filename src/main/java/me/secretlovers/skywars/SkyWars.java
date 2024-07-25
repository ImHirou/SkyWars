package me.secretlovers.skywars;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samjakob.spigui.SpiGUI;
import io.vertx.core.Vertx;
import lombok.Getter;
import me.secretlovers.skywars.chests.ChestManager;
import me.secretlovers.skywars.command.AdminCommand;
import me.secretlovers.skywars.command.GameCommand;
import me.secretlovers.skywars.command.LobbyCommand;
import me.secretlovers.skywars.database.PlayerData;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.GameManager;
import me.secretlovers.skywars.game.kits.Kit;
import me.secretlovers.skywars.listeners.EntityListeners;
import me.secretlovers.skywars.listeners.PlayerListeners;
import me.secretlovers.skywars.listeners.WorldCreationListener;
import me.secretlovers.skywars.lobby.Lobby;
import me.secretlovers.skywars.map.SWArena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

@Getter
public final class SkyWars extends JavaPlugin {

    @Getter
    private static SkyWars instance;
    @Getter
    private static SpiGUI spiGUI;
    private JsonObject jsonConfig;
    private ChestManager chestManager;
    private GameManager gameManager;
    private PlayerManager playerManager;
    private ProtocolManager protocolManager;
    private Lobby lobby;

    @Override
    public void onEnable() {
        instance = this;
        spiGUI = new SpiGUI(this);
        saveDefaultConfig();

        loadConfig();
        Kit.initKits();

        chestManager = new ChestManager();
        gameManager = new GameManager();
        protocolManager = ProtocolLibrary.getProtocolManager();
        lobby = new Lobby();

        playerManager = new PlayerManager(Vertx.vertx());
        playerManager.loadPlayers();

        registerCommands();
        registerEvents();

    }

    @Override
    public void onDisable() {
        for(Game game : gameManager.getGames().values()) {
            game.getArena().unload();
        }
    }

    private void loadConfig() {
        String filePath = getDataFolder().getAbsolutePath() + "/config.json";

        try( FileReader reader = new FileReader(filePath) ) {
            JsonParser parser = new JsonParser();
            jsonConfig = parser.parse(reader).getAsJsonObject();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerCommands() {
        GameCommand game = new GameCommand();
        getCommand("game").setExecutor(game);
        getCommand("game").setTabCompleter(game);

        getCommand("admin").setExecutor(new AdminCommand());

        getCommand("lobby").setExecutor(new LobbyCommand());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(chestManager, this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new WorldCreationListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListeners(), this);
        getServer().getPluginManager().registerEvents(lobby, this);
    }

}

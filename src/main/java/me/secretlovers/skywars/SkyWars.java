package me.secretlovers.skywars;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samjakob.spigui.SpiGUI;
import lombok.Getter;
import me.secretlovers.skywars.chests.ChestManager;
import me.secretlovers.skywars.command.AdminCommand;
import me.secretlovers.skywars.command.GameCommand;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.GameManager;
import me.secretlovers.skywars.game.team.Team;
import me.secretlovers.skywars.listeners.PlayerListeners;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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

    @Override
    public void onEnable() {
        instance = this;
        spiGUI = new SpiGUI(this);
        saveDefaultConfig();

        loadConfig();


        JsonObject teamSection = SkyWars.getInstance().getJsonConfig().getAsJsonObject("maps").
                getAsJsonObject("test").getAsJsonObject("teams");

        System.out.println(teamSection);
        for(Map.Entry<String, JsonElement> element : teamSection.entrySet()) {
            System.out.println(element.getValue());
        }

        chestManager = new ChestManager();
        gameManager = new GameManager();

        getCommand("game").setExecutor(new GameCommand());
        getCommand("admin").setExecutor(new AdminCommand());
        getServer().getPluginManager().registerEvents(chestManager, this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);

    }

    @Override
    public void onDisable() {
        for(Game game : gameManager.getGames().values()) {
            game.getGameMap().unload();
        }
    }

    private void loadConfig() {
        String filePath = getDataFolder().getAbsolutePath() + "/config.json";

        try( FileReader reader = new FileReader(filePath) ) {
            JsonParser parser = new JsonParser();
            jsonConfig = parser.parse(reader).getAsJsonObject();

            System.out.println(jsonConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

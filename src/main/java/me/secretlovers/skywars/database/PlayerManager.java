package me.secretlovers.skywars.database;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    public static Map<Player, PlayerData> data = new HashMap<>();

    private final MongoClient mongoClient;

    public PlayerManager(Vertx vertx) {
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "test");

        mongoClient = MongoClient.createShared(vertx, mongoConfig);
    }

    public void savePlayer(PlayerData playerData) {
        if(playerData == null) {
            System.out.println("Player data is null");
            return;
        }
        JsonObject playerJson = new JsonObject().
                put("nickname", playerData.getNickname()).
                put("gold", playerData.getGold()).
                put("kills", playerData.getKills()).
                put("deaths", playerData.getDeaths()).
                put("chestsOpened", playerData.getChestsOpened()).
                put("blocksPlaced", playerData.getBlocksPlaced()).
                put("kits", playerData.getKits()).
                put("selectedKit", playerData.getSelectedKit());
        

        if(playerData.getId() == null) {
            String id = new ObjectId().toHexString();
            playerJson.put("_id", id);
            mongoClient.insert("players", playerJson, ar -> {
                if(ar.failed()) {
                    ar.cause().printStackTrace(System.err);
                }
            });
        } else {
            playerJson.put("_id", playerData.getId());
            mongoClient.replaceDocuments("players", new JsonObject().put("_id", playerData.getId()), playerJson, ar -> {
                if(ar.failed()) {
                    ar.cause().printStackTrace(System.err);
                }
            });
        }
    }

    public void loadPlayerByNickname(String nickname, Handler<PlayerData> resultHandler) {
        mongoClient.findOne("players", new JsonObject().put("nickname", nickname), null, ar -> {
            if(ar.succeeded()) {
                if(ar.result() != null) {
                    resultHandler.handle(new PlayerData(ar.result()));
                } else {
                    resultHandler.handle(null);
                }
            } else {
                ar.cause().printStackTrace(System.err);
                resultHandler.handle(null);
            }
        });
    }

    public void loadPlayers() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            this.loadPlayerByNickname(p.getDisplayName(), result -> {
                if(result == null) {
                    result = new PlayerData(p.getDisplayName());
                    savePlayer(result);
                }
                PlayerManager.data.put(p, result);
            });;
        }
    }

    public void savePlayers() {
        for(Player p : Bukkit.getOnlinePlayers())
            if(data.containsKey(p))
                savePlayer(PlayerManager.data.get(p));
    }

}

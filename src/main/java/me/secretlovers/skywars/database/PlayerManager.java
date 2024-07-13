package me.secretlovers.skywars.database;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.bson.types.ObjectId;

public class PlayerManager {

    private final MongoClient mongoClient;

    public PlayerManager(Vertx vertx) {
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "test");

        mongoClient = MongoClient.createShared(vertx, mongoConfig);
    }

    public void savePlayer(PlayerData playerData) {
        JsonObject playerJson = new JsonObject().
                put("nickname", playerData.getNickname()).
                put("kills", playerData.getKills()).
                put("deaths", playerData.getDeaths()).
                put("chestsOpened", playerData.getChestsOpened()).
                put("blocksPlaced", playerData.getBlocksPlaced()).
                put("classes", playerData.getClasses());

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

}

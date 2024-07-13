package me.secretlovers.skywars.database;

import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerData {

    final String id;
    final String nickname;
    int kills;
    int deaths;
    int chestsOpened;
    int blocksPlaced;
    List classes;
    private Player handle;


    public PlayerData(JsonObject json) {
        id           = json.getString("_id");
        nickname     = json.getString("nickname");
        kills        = json.getInteger("kills");
        deaths       = json.getInteger("deaths");
        chestsOpened = json.getInteger("chestsOpened");
        blocksPlaced = json.getInteger("blocksPlaced");
        classes      = json.getJsonArray("classes").getList();
        handle = Bukkit.getPlayerExact(nickname);
    }

    public PlayerData(String nickname) {  //New player data
        id              = null;
        this.nickname   = nickname;
        kills           = 0;
        deaths          = 0;
        chestsOpened    = 0;
        blocksPlaced    = 0;
        classes         = new ArrayList<>();
        handle = Bukkit.getPlayerExact(nickname);
    }

}

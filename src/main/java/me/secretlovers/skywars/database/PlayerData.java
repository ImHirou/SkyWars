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
    int gold;
    int kills;
    int deaths;
    int chestsOpened;
    int blocksPlaced;
    List kits;
    String selectedKit;
    private Player handle;


    public PlayerData(JsonObject json) {
        id           = json.getString("_id");
        nickname     = json.getString("nickname");
        gold         = json.getInteger("gold");
        kills        = json.getInteger("kills");
        deaths       = json.getInteger("deaths");
        chestsOpened = json.getInteger("chestsOpened");
        blocksPlaced = json.getInteger("blocksPlaced");
        kits         = json.getJsonArray("kits").getList();
        selectedKit  = json.getString("selectedKit");
        handle = Bukkit.getPlayerExact(nickname);
    }

    public PlayerData(String nickname) {  //New player data
        id              = null;
        this.nickname   = nickname;
        gold            = 0;
        kills           = 0;
        deaths          = 0;
        chestsOpened    = 0;
        blocksPlaced    = 0;
        kits            = new ArrayList<>();
        selectedKit     = "starter";
        handle = Bukkit.getPlayerExact(nickname);
    }

}

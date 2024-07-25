package me.secretlovers.skywars.utils;

import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;

@UtilityClass
public class LocationUtil {

    public Location fromJson(JsonObject object, World world) {
        return new Location(world,
                object.get("x").getAsDouble(),
                object.get("y").getAsDouble(),
                object.get("z").getAsDouble());
    }

    public boolean isEqual(Location first, Location second) {
        return first.getX() == second.getX() && first.getY() == second.getY() && first.getZ() == second.getZ();
    }

}

package me.secretlovers.skywars.utils;

import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

@UtilityClass
public class LocationUtil {

    public Location fromConfigurationSection(ConfigurationSection section, World world) {
        return new Location(world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"));
    }

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

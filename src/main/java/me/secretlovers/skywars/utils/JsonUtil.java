package me.secretlovers.skywars.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@UtilityClass
public class JsonUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public void jsonToFile(JsonObject json, File file) {
        if (json == null) throw new IllegalArgumentException("JsonObject cannot be null");
        if (file == null) throw new IllegalArgumentException("File cannot be null");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

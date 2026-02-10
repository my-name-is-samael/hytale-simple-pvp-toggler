package com.tontonsamael.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigManager {
    private final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    private final File configFile;

    private final Map<String, Boolean> config;

    public ConfigManager(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.configFile = new File(dataFolder, "simple_pvp_toggler.json");
        this.config = new HashMap<>();

        try {
            this.initConfig();
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to read configuration: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    public boolean isPvpEnabled(String worldName) {
        World world = Universe.get().getWorld(worldName);
        if(world == null){
            return false;
        } else if (!world.getWorldConfig().isPvpEnabled()) {
            // PvP forced to false :'(
            return false;
        }

        return Optional.ofNullable(this.config.get(worldName))
                .orElse(true); // default state
    }

    public void setPvpEnabled(String worldName, boolean newState) {
        this.config.put(worldName, newState);
        this.saveConfig();
    }

    private void initConfig() throws IOException {
        if (this.configFile.exists() && this.configFile.canRead()) {
            try (FileReader reader = new FileReader(this.configFile.getAbsolutePath())) {
                Type mapType = new TypeToken<Map<String, Boolean>>() {
                }.getType();
                Map<String, Boolean> savedConfig = this.gson.fromJson(reader, mapType);
                if (savedConfig != null) {
                    this.config.putAll(savedConfig);
                }
            } catch (IOException e) {
                LOGGER.atSevere().log("Failed to save configuration: " + e.getMessage());
                //e.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(this.configFile)) {
            this.gson.toJson(this.config, writer);
            LOGGER.atFinest().log("Configuration saved to " + this.configFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to save configuration: " + e.getMessage());
            //e.printStackTrace();
        }
    }
}

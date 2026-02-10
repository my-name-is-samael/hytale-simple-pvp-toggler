package com.tontonsamael;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tontonsamael.commands.PvpCommand;
import com.tontonsamael.config.ConfigManager;
import com.tontonsamael.event.PlayerJoinEvent;
import com.tontonsamael.systems.DamageEventListener;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SimplePvpToggler extends JavaPlugin {
    private static SimplePvpToggler INSTANCE;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ConfigManager configManager;
    private List<String> misconfiguredWorlds;

    public SimplePvpToggler(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
    }

    public void detectMisconfiguredWorlds() {
        this.misconfiguredWorlds = new ArrayList<>();
        Universe.get().getWorlds().forEach((worldName, world) -> {
            if (!isWorldBlacklisted(worldName) && !world.getWorldConfig().isPvpEnabled()) {
                this.misconfiguredWorlds.add(worldName);
            }
        });
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("SimplePvPToggler Plugin loading in...");
        this.configManager = new ConfigManager(new File("config"));

        this.getCommandRegistry().registerCommand(new PvpCommand(this.configManager));

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerJoinEvent::onPlayerReady);
        this.getEntityStoreRegistry().registerSystem(new DamageEventListener(this.configManager));
        LOGGER.atInfo().log("SimplePvPToggler Plugin loaded successfully !");
    }

    public static SimplePvpToggler get() {
        return INSTANCE;
    }

    public ConfigManager getConfig() {
        return this.configManager;
    }

    public boolean isWorldMisconfigured(String worldName) {
        if(this.misconfiguredWorlds == null){
            this.detectMisconfiguredWorlds();
        }
        return this.misconfiguredWorlds.contains(worldName);
    }

    public List<String> getMisconfiguredWorld() {
        if(this.misconfiguredWorlds == null){
            this.detectMisconfiguredWorlds();
        }
        return this.misconfiguredWorlds;
    }

    private static final List<String> WORLDS_BLACKLIST = List.of("forgotten_temple");
    public static boolean isWorldBlacklisted(String worldName) {
        return WORLDS_BLACKLIST.stream().anyMatch(s->worldName.toLowerCase().contains(s));
    }
}
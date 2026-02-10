package com.tontonsamael.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.SimplePvpToggler;
import com.tontonsamael.config.ConfigManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PvpCommand extends AbstractPlayerCommand {
    private final ConfigManager config;

    public PvpCommand(ConfigManager config) {
        super("pvp", "simplepvptoggler.command.show.description");
        this.config = config;
        this.setPermissionGroups("Adventure");
        this.requirePermission("pvp.show");
        this.addSubCommand(new PvpSetCommand(config));
    }

    @Override
    protected void execute(@NonNullDecl CommandContext context, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        Set<String> groups = PermissionsModule.get().getGroupsForUser(playerRef.getUuid());
        if (groups.contains("OP") ||
                PermissionsModule.get().hasPermission(playerRef.getUuid(), "pvp.set")) {
            List<Message> messages = new ArrayList<>();
            messages.add(Message.translation("simplepvptoggler.op-status").color(Color.YELLOW));
            Universe.get().getWorlds().forEach((worldName, w) -> {
                if (!SimplePvpToggler.isWorldBlacklisted(w.getName())) {
                    boolean pvpState = this.config.isPvpEnabled(worldName);
                    messages.add(Message.join(
                            Message.raw("\n  " + worldName + " - "),
                            Message.translation(pvpState ? "simplepvptoggler.status.on" : "simplepvptoggler.status.off")
                                    .color(pvpState ? Color.ORANGE : Color.GREEN)
                    ));
                }
            });
            context.sendMessage(Message.join(messages.toArray(Message[]::new)));
        } else if (!SimplePvpToggler.isWorldBlacklisted(world.getName())) {
            boolean pvpState = this.config.isPvpEnabled(world.getName());
            context.sendMessage(Message.join(
                    Message.translation("simplepvptoggler.status"),
                    Message.translation(pvpState ? "simplepvptoggler.status.on" : "simplepvptoggler.status.off")
                            .color(pvpState ? Color.ORANGE : Color.GREEN)
            ));
        }
    }
}

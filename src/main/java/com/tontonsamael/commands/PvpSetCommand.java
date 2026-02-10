package com.tontonsamael.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.SimplePvpToggler;
import com.tontonsamael.config.ConfigManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.awt.*;

public class PvpSetCommand extends AbstractPlayerCommand {
    private final ConfigManager config;

    @Nonnull
    private final RequiredArg<String> stateArg;
    @Nonnull
    private final OptionalArg<String> worldArg;

    public PvpSetCommand(ConfigManager config) {
        super("set", "simplepvptoggler.command.set.description");
        this.config = config;

        this.stateArg = this.withRequiredArg("state", "simplepvptoggler.command.set.arg.state", ArgTypes.STRING);
        this.worldArg = this.withOptionalArg("world", "simplepvptoggler.command.set.arg.world", ArgTypes.STRING);
        this.setPermissionGroups("OP");
        this.requirePermission("pvp.set");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext context, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        String stateArg = this.stateArg.get(context);
        if (!stateArg.equalsIgnoreCase("on") && !stateArg.equalsIgnoreCase("off")) {
            context.sendMessage(Message.translation("simplepvptoggler.command.set.arg.state.invalid")
                    .color(Color.RED));
            return;
        }
        boolean newState = stateArg.equalsIgnoreCase("on");

        World targetWorld;
        if (this.worldArg.provided(context)) {
            String worldArg = this.worldArg.get(context);
            targetWorld = Universe.get().getWorlds().get(worldArg);
            if (targetWorld == null) {
                context.sendMessage(Message.translation("simplepvptoggler.command.set.arg.world.invalid")
                        .param("worldName", worldArg)
                        .color(Color.RED));
                return;
            }
        } else {
            targetWorld = world;
        }

        if (SimplePvpToggler.isWorldBlacklisted(targetWorld.getName())) {
            context.sendMessage(Message.translation("simplepvptoggler.command.set.world-blacklisted")
                    .color(Color.ORANGE));
            return;
        } else if (SimplePvpToggler.get().isWorldMisconfigured(targetWorld.getName())) {
            context.sendMessage(Message.translation("simplepvptoggler.command.set.world-misconfigured")
                    .param("worldName", targetWorld.getName())
                    .color(Color.ORANGE));
            return;
        }

        boolean currWorldState = this.config.isPvpEnabled(targetWorld.getName());
        if (newState == currWorldState) {
            context.sendMessage(Message.join(
                    Message.translation("simplepvptoggler.command.set.already")
                            .param("worldName", targetWorld.getName()),
                    Message.translation(currWorldState ? "simplepvptoggler.status.on" : "simplepvptoggler.status.off")
                            .color(currWorldState ? Color.ORANGE : Color.GREEN)
            ));
            return;
        }

        this.config.setPvpEnabled(targetWorld.getName(), newState);
        context.sendMessage(Message.join(
                Message.translation("simplepvptoggler.command.set.changed")
                        .param("worldName", targetWorld.getName()),
                Message.translation(newState ? "simplepvptoggler.status.on" : "simplepvptoggler.status.off")
                        .color(newState ? Color.ORANGE : Color.GREEN)
        ));
        Universe.get().getPlayers().stream().filter(player -> player.getWorldUuid() != null &&
                        player.getWorldUuid().equals(targetWorld.getWorldConfig().getUuid()) &&
                        !player.getUuid().equals(playerRef.getUuid()))
                .forEach(player -> player.sendMessage(Message.join(
                        Message.translation("simplepvptoggler.status"),
                        Message.translation(newState ? "simplepvptoggler.status.on" : "simplepvptoggler.status.off")
                                .color(newState ? Color.ORANGE : Color.GREEN)
                )));
    }
}

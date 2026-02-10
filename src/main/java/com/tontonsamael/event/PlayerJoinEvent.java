package com.tontonsamael.event;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.tontonsamael.SimplePvpToggler;

import java.awt.*;
import java.util.List;


public class PlayerJoinEvent {
    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (world == null) {
            return;
        }
        if (!SimplePvpToggler.isWorldBlacklisted(world.getName())) {
            boolean pvpState = SimplePvpToggler.get().getConfig().isPvpEnabled(world.getName());
            player.sendMessage(Message.join(
                    Message.translation("simplepvptoggler.status"),
                    Message.translation(pvpState ? "simplepvptoggler.status.on" : "simplepvptoggler.status.off")
                            .color(pvpState ? Color.ORANGE : Color.GREEN)
            ));
        }

        PlayerRef pRef = event.getPlayerRef().getStore().getComponent(event.getPlayerRef(), PlayerRef.getComponentType());
        if (pRef != null && PermissionsModule.get().getGroupsForUser(pRef.getUuid())
                .contains("OP")) {
            List<String> worlds = SimplePvpToggler.get().getMisconfiguredWorld();
            if (!worlds.isEmpty()) {
                player.sendMessage(Message.join(
                        Message.translation("simplepvptoggler.join.misconfigured-worlds")
                                .color(Color.RED),
                        Message.raw(String.join(", ", worlds))
                                .color(Color.RED)
                ));
            }
        }
    }
}

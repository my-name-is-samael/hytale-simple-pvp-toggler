package com.tontonsamael.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.config.ConfigManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;

public class DamageEventListener extends EntityEventSystem<EntityStore, Damage> {
    private final ConfigManager config;

    public DamageEventListener(ConfigManager config) {
        super(Damage.class);
        this.config = config;
    }

    @Override
    public void handle(int entityId, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl Damage event) {
        if (event.isCancelled()) {
            return;
        }

        boolean isProjectileSource = event.getSource() instanceof Damage.ProjectileSource;
        boolean isEntitySource = event.getSource() instanceof Damage.EntitySource;
        if (!isProjectileSource && !isEntitySource) {
            // check the damage comes from another entity
            return;
        }

        final Ref<EntityStore> attackerRef = isEntitySource ? ((Damage.EntitySource) event.getSource()).getRef() : ((Damage.ProjectileSource) event.getSource()).getRef();
        Player attacker = store.getComponent(attackerRef, Player.getComponentType());
        if (attacker == null) {
            // the origin entity is not a player
            return;
        }

        final Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(entityId);
        Player victim = store.getComponent(victimRef, Player.getComponentType());
        if (victim == null) {
            // the victim entity is not a player
            return;
        }

        if (victim.getWorld() == null || attacker.getWorld() == null) {
            // invalid world
            return;
        } else if (!victim.getWorld().getName().equals(attacker.getWorld().getName())) {
            // world not matching
            return;
        }

        if (!this.config.isPvpEnabled(victim.getWorld().getName())) {
            // PVP is not enabled in this world
            event.setCancelled(true);
            commandBuffer.tryRemoveComponent(victimRef, KnockbackComponent.getComponentType());
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Nullable
    public SystemGroup getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }
}
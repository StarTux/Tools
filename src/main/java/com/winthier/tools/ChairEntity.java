package com.winthier.tools;

import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.custom.entity.TickableEntity;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.material.Stairs;
import org.spigotmc.event.entity.EntityDismountEvent;

@RequiredArgsConstructor
public final class ChairEntity implements CustomEntity, TickableEntity {
    public static final String CUSTOM_ID = "tools:chair";
    private final ToolsPlugin plugin;

    @Override
    public String getCustomId() {
        return CUSTOM_ID;
    }

    @Override
    public Entity spawnEntity(Location location) {
        ArmorStand entity = location.getWorld().spawn(location, ArmorStand.class, e -> {
                e.setVisible(false);
                e.setMarker(true);
                e.setSmall(true);
                e.setGravity(false);
            });
        return entity;
    }

    @Override
    public void entityWillUnload(EntityWatcher watcher) {
        watcher.getEntity().remove();
    }

    @Override
    public void entityWasDiscovered(EntityWatcher watcher) {
        if (watcher.getEntity().getPassengers().isEmpty()) {
            watcher.getEntity().remove();
        }
    }

    @Override
    public void onTick(EntityWatcher watcher) {
        if (watcher.getEntity().getPassengers().isEmpty()
            || !(watcher.getEntity().getLocation().getBlock().getState().getData() instanceof Stairs)) {
            watcher.getEntity().remove();
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event, EntityContext context) {
        if (context.getPosition() != EntityContext.Position.MOUNT) return;
        context.getEntity().remove();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event, EntityContext context) {
        context.getEntity().remove();
    }
}

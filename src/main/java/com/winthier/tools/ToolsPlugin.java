package com.winthier.tools;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.custom.event.CustomRegisterEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.plugin.java.JavaPlugin;

public class ToolsPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("salesman").setExecutor(new Salesman(this));
        saveResource("salesman.yml", false);
    }

    @EventHandler
    public void onCustomRegister(CustomRegisterEvent event) {
        reloadConfig();
        event.addItem(new TreeChopperItem(this));
        event.addItem(new AngelBlockItem(this));
        event.addItem(new SlimeFinderItem(this));
        event.addEntity(new ChairEntity(this));
        for (String id: getConfig().getStringList("EffectArmorItems")) {
            event.addItem(new EffectArmorItem(this, id));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasBlock()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (player.isSneaking()) return;
        Block block = event.getClickedBlock();
        MaterialData md = block.getState().getData();
        if (!(md instanceof Stairs)) return;
        Stairs stairs = (Stairs)md;
        if (stairs.isInverted()) return;
        if (block.getRelative(0, 1, 0).getType() != Material.AIR) return;
        if (!block.getRelative(0, -1, 0).getType().isSolid()) return;
        if (!player.isOnGround()) return;
        if (block.getY() != player.getLocation().getBlockY()) return;
        if (Math.abs(block.getX() - player.getLocation().getBlockX()) > 1) return;
        if (Math.abs(block.getZ() - player.getLocation().getBlockZ()) > 1) return;
        if (event.getItem() != null && event.getItem().getType().isBlock()) return;
        for (Entity e: block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)) {
            if (ChairEntity.CUSTOM_ID.equals(CustomPlugin.getInstance().getEntityManager().getCustomId(e))) return;
        }
        event.setCancelled(true);
        Location loc = block.getLocation().add(0.5, 0.35, 0.5);
        switch (stairs.getFacing()) {
        case SOUTH: loc.setYaw(0); break;
        case WEST: loc.setYaw(90); break;
        case NORTH: loc.setYaw(180); break;
        case EAST: loc.setYaw(270); break;
        default: break;
        }
        EntityWatcher watcher = CustomPlugin.getInstance().getEntityManager().spawnEntity(loc, ChairEntity.CUSTOM_ID);
        player.teleport(loc);
        watcher.getEntity().addPassenger(player);
        final Sound sound;
        switch (block.getType()) {
        case BRICK_STAIRS:
        case COBBLESTONE_STAIRS:
        case DARK_OAK_STAIRS:
        case NETHER_BRICK_STAIRS:
        case PURPUR_STAIRS:
        case QUARTZ_STAIRS:
        case RED_SANDSTONE_STAIRS:
        case SANDSTONE_STAIRS:
        case SMOOTH_STAIRS:
            sound = Sound.BLOCK_STONE_FALL;
            break;
        case JUNGLE_WOOD_STAIRS:
        case SPRUCE_WOOD_STAIRS:
        case ACACIA_STAIRS:
        case BIRCH_WOOD_STAIRS:
        case WOOD_STAIRS:
        default:
            sound = Sound.BLOCK_WOOD_FALL;
        }
        player.playSound(loc, sound, SoundCategory.PLAYERS, 1.0f, 1.25f);
    }
}

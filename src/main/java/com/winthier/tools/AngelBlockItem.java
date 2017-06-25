package com.winthier.tools;

import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.generic_events.GenericEventsPlugin;
import com.winthier.generic_events.ItemNameEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;

public final class AngelBlockItem implements CustomItem, UncraftableItem {
    private final ToolsPlugin plugin;
    private final ItemStack itemStack;
    private final ItemDescription itemDescription;

    AngelBlockItem(ToolsPlugin plugin) {
        this.plugin = plugin;
        this.itemStack = new ItemStack(Material.STAINED_GLASS);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        this.itemDescription = ItemDescription.of(plugin.getConfig().getConfigurationSection("angel_block"));
        itemDescription.apply(itemStack);
    }

    @Override
    public String getCustomId() {
        return "tools:angel_block";
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @EventHandler(ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event, ItemContext context) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        event.setCancelled(true);
        BlockIterator blockIter = new BlockIterator(context.getPlayer());
        Player player = context.getPlayer();
        double width = player.getWidth() * 0.5;
        double height = player.getHeight();
        Block min = player.getLocation().add(-width, 0, -width).getBlock();
        Block max = player.getLocation().add(width, height, width).getBlock();
        Block block = null;
        for (int i = 0; i < 5; i += 1) {
            if (!blockIter.hasNext()) return;
            Block aBlock = blockIter.next();
            if (aBlock.getType().isSolid()) return;
            if (aBlock.getX() >= min.getX() && aBlock.getX() <= max.getX()
                && aBlock.getZ() >= min.getZ() && aBlock.getZ() <= max.getZ()
                && aBlock.getY() >= min.getY() && aBlock.getY() <= max.getY()) {
                continue;
            } else {
                block = aBlock;
                break;
            }
        }
        if (block == null) return;
        if (block.getType() != Material.AIR) return;
        if (!GenericEventsPlugin.getInstance().playerCanBuild(player, block)) return;
        block.setType(Material.STAINED_GLASS);
        if (player.getGameMode() != GameMode.CREATIVE) {
            context.getItemStack().setAmount(context.getItemStack().getAmount() - 1);
        }
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0f, 2.0f);
        loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 16, 0.5f, 0.5f, 0.5f, 0.1f);
    }

    @EventHandler
    public void onItemName(ItemNameEvent event) {
        event.setItemName(itemDescription.getDisplayName());
    }
}

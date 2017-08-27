package com.winthier.tools;

import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.custom.util.Msg;
import com.winthier.generic_events.GenericEventsPlugin;
import com.winthier.generic_events.ItemNameEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class SlimeFinderItem implements CustomItem, UncraftableItem {
    public static final String CUSTOM_ID = "tools:slime_finder";
    private final ToolsPlugin plugin;
    private final ItemDescription itemDescription;
    private final ItemStack itemStack;

    SlimeFinderItem(ToolsPlugin plugin) {
        this.plugin = plugin;
        this.itemStack = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        this.itemDescription = ItemDescription.of(plugin.getConfig().getConfigurationSection("slime_finder"));
        itemDescription.apply(itemStack);
    }

    @Override
    public String getCustomId() {
        return CUSTOM_ID;
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @EventHandler
    public void onItemName(ItemNameEvent event, ItemContext context) {
        event.setItemName(itemDescription.getDisplayName());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event, ItemContext context) {
        if (!event.hasBlock()) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (!GenericEventsPlugin.getInstance().playerCanBuild(player, block)) return;
        Location loc = block.getRelative(event.getBlockFace()).getLocation().add(0.5, 0.5, 0.5);
        if (block.getChunk().isSlimeChunk()) {
            Msg.sendActionBar(player, "&a&lSlime Chunk");
            loc.getWorld().playSound(loc, Sound.ENTITY_SLIME_JUMP, SoundCategory.MASTER, 0.5f, 1.25f);
            loc.getWorld().spawnParticle(Particle.SLIME, loc, 8, 0.5f, 0.5f, 0.5f, 1.0f);
        } else {
            Msg.sendActionBar(player, "&cNo Slime Chunk");
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.MASTER, 0.5f, 2.0f);
            loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 4, 0.1f, 0.1f, 0.1f, 0.0f);
        }
    }
}

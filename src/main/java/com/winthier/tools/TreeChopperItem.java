package com.winthier.tools;

import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.custom.item.UpdatableItem;
import com.winthier.generic_events.GenericEventsPlugin;
import com.winthier.generic_events.ItemNameEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class TreeChopperItem implements CustomItem, UncraftableItem, UpdatableItem {
    private final ToolsPlugin plugin;
    private final String customId = "tools:tree_chopper";
    private final ItemStack itemStack;
    private final ItemDescription itemDescription;
    private final String displayName;
    private final BlockFace[] surroundingFaces = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    TreeChopperItem(ToolsPlugin plugin) {
        this.plugin = plugin;
        ItemStack item = new ItemStack(Material.IRON_AXE);
        this.displayName = plugin.getConfig().getString("tree_chopper.DisplayName");
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.RESET + displayName);
        item.setItemMeta(meta);
        ItemDescription desc = new ItemDescription();
        // TODO remove 3 lines
        desc.setCategory(plugin.getConfig().getString("tree_chopper.Category"));
        desc.setDescription(plugin.getConfig().getString("tree_chopper.Description"));
        desc.setUsage(plugin.getConfig().getString("tree_chopper.Usage"));
        desc.load(plugin.getConfig().getConfigurationSection("tree_chopper"));
        desc.apply(item);
        this.itemDescription = desc;
        this.itemStack = item;
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @Override
    public void updateItem(ItemStack item) {
        itemDescription.apply(item);
    }

    private boolean isLog(Block block) {
        switch (block.getType()) {
        case LOG:
        case LOG_2:
            return true;
        default:
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event, ItemContext context) {
        Block block = event.getBlock();
        if (!isLog(block)) return;
        if (block.getRelative(BlockFace.DOWN).getType().isSolid()) {
            for (BlockFace face: surroundingFaces) {
                if (isLog(block.getRelative(face))) return;
            }
            block = block.getRelative(BlockFace.UP);
        }
        LinkedList<Block> todo = new LinkedList<>();
        Set<Block> done = new HashSet<>();
        final LinkedList<Block> found = new LinkedList<>();
        if (!block.equals(event.getBlock())) todo.add(block);
        for (BlockFace face: surroundingFaces) todo.add(block.getRelative(face));
        ItemStack item = context.getItemStack();
        int max = item.getType().getMaxDurability() - item.getDurability();
        Player player = context.getPlayer();
        while (!todo.isEmpty() && found.size() < max) {
            Block todoBlock = todo.removeFirst();
            if (!isLog(todoBlock)) continue;
            if (!GenericEventsPlugin.getInstance().playerCanBuild(player, todoBlock)) continue;
            found.add(todoBlock);
            Block upBlock = todoBlock.getRelative(BlockFace.UP);
            if (!done.contains(upBlock)) {
                todo.add(upBlock);
                done.add(upBlock);
            }
            for (BlockFace face: surroundingFaces) {
                Block nborBlock = upBlock.getRelative(face);
                if (!done.contains(nborBlock)) {
                    todo.add(nborBlock);
                    done.add(nborBlock);
                }
            }
        }
        if (found.isEmpty()) return;
        new BukkitRunnable() {
            @Override public void run() {
                Block foundBlock = found.removeFirst();
                Location loc = foundBlock.getLocation().add(0.5, 0.5, 0.5);
                foundBlock.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 8, foundBlock.getState().getData());
                foundBlock.getWorld().playSound(loc, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
                foundBlock.breakNaturally();
                if (found.isEmpty()) cancel();
            }
        }.runTaskTimer(plugin, 2, 2);
        int newDurability = item.getDurability() + found.size();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (newDurability < item.getType().getMaxDurability()) {
            item.setDurability((short)newDurability);
        } else {
            item.setAmount(0);
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, found.size() * 10, 1));
    }

    @EventHandler
    public void onItemName(ItemNameEvent event) {
        event.setItemName(displayName);
    }
}

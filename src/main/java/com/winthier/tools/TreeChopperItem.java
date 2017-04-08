package com.winthier.tools;

import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.custom.item.UpdatableItem;
import com.winthier.generic_events.GenericEventsPlugin;
import com.winthier.generic_events.ItemNameEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
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
    private final Random random = new Random(System.currentTimeMillis());

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

    private boolean isLeaf(Block block) {
        switch (block.getType()) {
        case LEAVES:
        case LEAVES_2:
            return true;
        default:
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event, ItemContext context) {
        Block block = event.getBlock();
        if (!isLog(block)) return;
        for (BlockFace face: surroundingFaces) {
            if (isLog(block.getRelative(face))) return;
        }
        LinkedList<Block> todo = new LinkedList<>();
        Set<Block> done = new HashSet<>();
        done.add(block);
        final LinkedList<Block> found = new LinkedList<>();
        for (BlockFace face: surroundingFaces) todo.add(block.getRelative(face));
        block = block.getRelative(BlockFace.UP);
        todo.add(block);
        for (BlockFace face: surroundingFaces) todo.add(block.getRelative(face));
        ItemStack item = context.getItemStack();
        int max = item.getType().getMaxDurability() - item.getDurability();
        Player player = context.getPlayer();
        int damageAmount = 0;
        while (!todo.isEmpty() && damageAmount < max) {
            Block todoBlock = todo.removeFirst();
            if (done.contains(todoBlock)) continue;
            done.add(todoBlock);
            if (isLeaf(todoBlock)) {
                ArrayList<Block> leafNbors = new ArrayList<>(9 + 8 + 9);
                Block tmp = todoBlock;
                for (BlockFace face: surroundingFaces) leafNbors.add(tmp.getRelative(face));
                tmp = todoBlock.getRelative(BlockFace.UP);
                leafNbors.add(tmp);
                for (BlockFace face: surroundingFaces) leafNbors.add(tmp.getRelative(face));
                leafNbors.add(todoBlock.getRelative(BlockFace.DOWN));
                for (Block leafNbor: leafNbors) {
                    if (!done.contains(leafNbor)
                        && isLog(leafNbor)) {
                        todo.add(leafNbor);
                    }
                }
                continue;
            }
            if (!isLog(todoBlock)) continue;
            if (!GenericEventsPlugin.getInstance().playerCanBuild(player, todoBlock)) continue;
            found.add(todoBlock);
            if (random.nextInt(6) == 0) damageAmount += 1;
            Block upBlock = todoBlock.getRelative(BlockFace.UP);
            if (!done.contains(upBlock)) {
                todo.add(upBlock);
            }
            for (BlockFace face: surroundingFaces) {
                Block nborBlock = todoBlock.getRelative(face);
                if (!done.contains(nborBlock)) {
                    todo.add(nborBlock);
                }
            }
            for (BlockFace face: surroundingFaces) {
                Block nborBlock = upBlock.getRelative(face);
                if (!done.contains(nborBlock)) {
                    todo.add(nborBlock);
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
        int newDurability = item.getDurability() + damageAmount;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (newDurability < item.getType().getMaxDurability()) {
            item.setDurability((short)newDurability);
        } else {
            item.setAmount(0);
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, found.size() * 5, 1));
    }

    @EventHandler
    public void onItemName(ItemNameEvent event) {
        event.setItemName(displayName);
    }
}

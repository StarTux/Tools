package com.winthier.tools;

import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.TickableItem;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.generic_events.ItemNameEvent;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
public final class MiningHelmetItem implements CustomItem, UncraftableItem, TickableItem {
    private final ToolsPlugin plugin;
    private final String customId = "tools:mining_helmet";
    private final ItemDescription itemDescription;
    private final ItemStack itemStack;

    MiningHelmetItem(ToolsPlugin plugin) {
        this.plugin = plugin;
        this.itemDescription = ItemDescription.of(plugin.getConfig().getConfigurationSection("mining_helmet"));
        this.itemStack = plugin.getConfig().getItemStack("mining_helmet.ItemStack");
        itemDescription.apply(itemStack);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack item = itemStack.clone();
        item.setAmount(amount);
        return item;
    }

    @Override
    public void onTick(ItemContext itemContext, int ticks) {
        if (ticks % 20 != 0) return;
        if (itemContext.getPosition() != ItemContext.Position.HELMET) return;
        ItemStack item = itemContext.getItemStack();
        if (item.getDurability() >= item.getType().getMaxDurability()) return;
        Player player = itemContext.getPlayer();
        if (player == null) return;
        PotionEffect effect = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        if (effect != null && effect.getDuration() >= 20 * 11) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 15, 0, true, false), true);
        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setDurability((short)(item.getDurability() + 1));
        }
        if (effect == null) {
            Location eyeLocation = player.getEyeLocation();
            player.getWorld().playSound(eyeLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5f, 2.0f);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, eyeLocation, 8, 0.2, 0.2, 0.2, 0.0);
        }
    }

    @EventHandler
    public void onItemName(ItemNameEvent event, ItemContext context) {
        event.setItemName(itemDescription.getDisplayName());
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event, ItemContext context) {
        if (context.getItemStack().getDurability() + event.getDamage() > context.getItemStack().getType().getMaxDurability()) {
            event.setCancelled(true);
        }
    }
}

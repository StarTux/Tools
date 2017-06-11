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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
public final class EffectArmorItem implements CustomItem, UncraftableItem, TickableItem {
    private final ToolsPlugin plugin;
    private final String customId;
    private final ItemDescription itemDescription;
    private final ItemStack itemStack;
    private final PotionEffectType potionEffectType;
    private final int potionDuration, potionMaxDuration, potionAmplifier;
    private final ItemContext.Position itemPosition;

    EffectArmorItem(ToolsPlugin plugin, String id) {
        this.plugin = plugin;
        this.customId = "tools:" + id;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(id);
        this.itemDescription = ItemDescription.of(section.getConfigurationSection("ItemDescription"));
        this.itemStack = section.getItemStack("ItemStack");
        this.potionEffectType = PotionEffectType.getByName(section.getString("PotionEffect.Type"));
        this.potionDuration = section.getInt("PotionEffect.Duration");
        this.potionMaxDuration = section.getInt("PotionEffect.MaxDuration");
        this.potionAmplifier = section.getInt("PotionEffect.Amplifier");
        itemDescription.apply(itemStack);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        switch (itemStack.getType()) {
        case LEATHER_HELMET:
        case IRON_HELMET:
        case GOLD_HELMET:
        case DIAMOND_HELMET:
        case CHAINMAIL_HELMET:
            itemPosition = ItemContext.Position.HELMET;
            break;
        case LEATHER_CHESTPLATE:
        case IRON_CHESTPLATE:
        case GOLD_CHESTPLATE:
        case DIAMOND_CHESTPLATE:
        case CHAINMAIL_CHESTPLATE:
            itemPosition = ItemContext.Position.CHESTPLATE;
            break;
        case LEATHER_LEGGINGS:
        case IRON_LEGGINGS:
        case GOLD_LEGGINGS:
        case DIAMOND_LEGGINGS:
        case CHAINMAIL_LEGGINGS:
            itemPosition = ItemContext.Position.LEGGINGS;
            break;
        case LEATHER_BOOTS:
        case IRON_BOOTS:
        case GOLD_BOOTS:
        case DIAMOND_BOOTS:
        case CHAINMAIL_BOOTS:
            itemPosition = ItemContext.Position.BOOTS;
            break;
        default:
            throw new IllegalArgumentException("Do not know what to do with " + itemStack.getType());
        }
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
        if (itemContext.getPosition() != itemPosition) return;
        ItemStack item = itemContext.getItemStack();
        if (item.getDurability() >= item.getType().getMaxDurability()) return;
        Player player = itemContext.getPlayer();
        if (player == null) return;
        PotionEffect effect = player.getPotionEffect(potionEffectType);
        if (effect != null && effect.getDuration() >= 20 * potionMaxDuration) return;
        player.addPotionEffect(new PotionEffect(potionEffectType, 20 * potionDuration, potionAmplifier, true, false), true);
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

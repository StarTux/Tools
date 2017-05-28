package com.winthier.tools;

import com.winthier.custom.CustomPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

@RequiredArgsConstructor
public final class Salesman implements TabExecutor {
    private final ToolsPlugin plugin;
    private YamlConfiguration config;

    ConfigurationSection getConfig() {
        if (config == null) {
            config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "salesman.yml"));
        }
        return config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = args.length == 0 ? null : args[0].toLowerCase();
        if (cmd == null) {
            return false;
        } else if (cmd.equals("reload") && args.length == 1) {
            config = null;
            sender.sendMessage("Merchant recipes reloaded");
        } else if (cmd.equals("open") && args.length == 3) {
            String shopName = args[1];
            String playerName = args[2];
            ConfigurationSection section = getConfig().getConfigurationSection(shopName);
            if (section == null) {
                sender.sendMessage("Shop not found: " + shopName);
                return true;
            }
            Player player = plugin.getServer().getPlayerExact(playerName);
            if (player == null) {
                sender.sendMessage("Player not found: " + playerName);
                return true;
            }
            String title = section.getString("title", shopName);
            List<MerchantRecipe> recipes = new ArrayList<>();
            for (Map<?, ?> map: section.getMapList("recipes")) {
                ItemStack result = itemOf(map.get("result"));
                ItemStack input1 = itemOf(map.get("input1"));
                ItemStack input2 = itemOf(map.get("input2"));
                int maxUses = 9 * 64;
                Number num = (Number)map.get("max_uses");
                if (num != null) maxUses = num.intValue();
                MerchantRecipe recipe = new MerchantRecipe(result, maxUses);
                recipe.addIngredient(input1);
                recipe.addIngredient(input2);
                recipe.setExperienceReward(false);
                recipes.add(recipe);
            }
            Merchant merchant = plugin.getServer().createMerchant(title);
            merchant.setRecipes(recipes);
            player.openMerchant(merchant, false);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    ItemStack itemOf(Object o) {
        if (o == null) return new ItemStack(Material.AIR);
        if (o instanceof ItemStack) return (ItemStack)o;
        if (o instanceof String) return CustomPlugin.getInstance().getItemManager().spawnItemStack((String)o, 1);
        if (o instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection)o;
            return CustomPlugin.getInstance().getItemManager().spawnItemStack(section.getString("id"), section.getInt("amount"));
        }
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)o;
            if ("org.bukkit.inventory.ItemStack".equals(map.get("=="))) {
                return ItemStack.deserialize(map);
            } else {
                return CustomPlugin.getInstance().getItemManager().spawnItemStack((String)map.get("id"), ((Number)map.get("amount")).intValue());
            }
        }
        return new ItemStack(Material.AIR);
    }
}

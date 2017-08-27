package com.winthier.tools;

import com.winthier.custom.event.CustomRegisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        for (String id: getConfig().getStringList("EffectArmorItems")) {
            event.addItem(new EffectArmorItem(this, id));
        }
    }
}

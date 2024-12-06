package com.github.eltankesitoarceus.statister;

import com.github.eltankesitoarceus.statister.commands.Stat;
import com.github.eltankesitoarceus.statister.events.GeneralEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Statister extends JavaPlugin {

    Logger logger = getLogger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new GeneralEvents(), this);
        getCommand("playerstat").setExecutor(new Stat());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

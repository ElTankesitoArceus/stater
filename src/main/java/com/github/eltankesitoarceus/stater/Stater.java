package com.github.eltankesitoarceus.stater;

import com.github.eltankesitoarceus.stater.commands.Stat;
import com.github.eltankesitoarceus.stater.data.ConsoleColors;
import com.github.eltankesitoarceus.stater.data.database.DatabaseManager;
import com.github.eltankesitoarceus.stater.events.GeneralEvents;
import com.github.eltankesitoarceus.stater.webserver.api.APIServer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

public final class Stater extends JavaPlugin {

    public final Logger logger = getLogger();

    FileConfiguration config;
    private static Stater plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        getServer().getPluginManager().registerEvents(new GeneralEvents(), this);
        getCommand("playerstat").setExecutor(new Stat());
        logger.info(ConsoleColors.YELLOW_BRIGHT + "Initializing API server" + ConsoleColors.RESET);
        this.saveDefaultConfig();
        config = getConfig();
        try {
            DatabaseManager.getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        APIServer.startServer();
        logger.info(ConsoleColors.GREEN_BRIGHT + "API server initialized" + ConsoleColors.RESET);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public static Stater getPlugin() {
        return plugin;
    }
}

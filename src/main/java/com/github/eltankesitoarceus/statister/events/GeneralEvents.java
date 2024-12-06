package com.github.eltankesitoarceus.statister.events;

import com.github.eltankesitoarceus.statister.data.PlayerStats;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Logger;

public class GeneralEvents implements Listener {

    Logger logger = Logger.getLogger(String.valueOf(GeneralEvents.class));

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        logger.info("Player " + p.getName() + ": " + PlayerStats.getPlayerStat(p, Statistic.DEATHS));
    }
}

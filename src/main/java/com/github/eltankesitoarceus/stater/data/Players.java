package com.github.eltankesitoarceus.stater.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Players {

    public static String[] getAllPlayers() {
        List<OfflinePlayer> offline = Arrays.asList(Bukkit.getOfflinePlayers());
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        String[] res = new String[offline.size() + online.size()];
        int pos = 0;
        for (OfflinePlayer p : offline) {
            res[pos] = p.getName();
            pos++;
        }
        for (Player p : online) {
            res[pos] = p.getName();
            pos++;
        }
        return res;
    }
}

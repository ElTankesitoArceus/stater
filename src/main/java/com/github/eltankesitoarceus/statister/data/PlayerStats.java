package com.github.eltankesitoarceus.statister.data;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerStats {

    public static List<String> availableStats;

    static {
        loadAvailableStats();
    }

    static public int getPlayerStat(Player p,String stat) {
        return getPlayerStat(p, Statistic.valueOf(stat));
    }

    static public int getPlayerStat(Player p, Statistic s) {
        return p.getStatistic(s);
    }

    static void loadAvailableStats() {
        availableStats = new ArrayList<>(Statistic.values().length);
        for (Statistic s : Statistic.values()) {
            if (!s.isSubstatistic())
                availableStats.add(s.toString());
        }
    }
}

package com.github.eltankesitoarceus.stater.webserver.api;

import com.github.eltankesitoarceus.stater.data.PlayerStats;
import com.github.eltankesitoarceus.stater.data.Players;
import com.github.eltankesitoarceus.stater.data.database.DatabaseManager;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class APIServer {

    static Javalin app;

    static Logger logger = LoggerFactory.getLogger("Statister server");

    private static final String GET_PLAYER_STATS = "SELECT ";

    public static void startServer() {
        app = Javalin.create(config -> {
            config.jetty.defaultHost = "localhost";
            config.jetty.defaultPort = 25535;
            config.useVirtualThreads = true;
        });
        app.get("/stats", ctx -> {
            JSONObject jo = new JSONObject();
            jo.put("players", Players.getAllPlayers());
            jo.put("stats", PlayerStats.getAvailableStats());
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result(jo.toString());
        });
        app.get("/stats/player/{player}/{stat}", ctx -> {
            JSONObject jo = new JSONObject();
            String playerName = ctx.pathParam("player");
            jo.put("player", playerName);
            jo.put("value", PlayerStats.getPlayerStat(Bukkit.getPlayer(playerName), ctx.pathParam("stat")));
            ctx.contentType(ContentType.APPLICATION_JSON);
            ctx.result(jo.toString());
        });

        app.get("/stats/player/{player}", ctx -> {
            JSONObject jo = new JSONObject();
            String playerName = ctx.pathParam("player");
            jo.put("player", playerName);
            Player p = Bukkit.getPlayer(playerName);
            if (p == null) {
                try (Connection conn = DatabaseManager.getConnection()) {

                }
            }
            if (p == null) {
                ctx.status(404);
                ctx.result("The player %s could not be found".formatted(playerName));
                return;
            }
            Map<String, String> stats = new HashMap<>(PlayerStats.getAvailableStats().size());
            for (String st : PlayerStats.getAvailableStats()) {
                stats.put(st.toString(), String.valueOf(p.getStatistic(Statistic.valueOf(st))));
            }
            jo.put("stats", stats);
            ctx.result(jo.toString());
        });
        app.start();
    }
}

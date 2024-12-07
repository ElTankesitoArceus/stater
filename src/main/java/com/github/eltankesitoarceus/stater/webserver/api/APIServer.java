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

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class APIServer {

    static Javalin app;

    static Logger logger = LoggerFactory.getLogger("Stater API");

    private static final String GET_PLAYER_STATS = "SELECT s.NAME, s.VALUE FROM \"%1$sstats\" s inner join \"%1$splayers\" p on s.PLAYER_ID = p.ID WHERE p.NAME = ? ORDER BY s.NAME ASC";
    private static final String GET_PLAYER_STAT = "SELECT s.NAME, s.VALUE FROM \"%1$sstats\" s inner join \"%1$splayers\" p on s.PLAYER_ID = p.ID WHERE p.NAME = ? AND s.NAME = ? ORDER BY s.NAME ASC";
    private static final String GET_STAT = "SELECT p.NAME, s.VALUE FROM \"%1$sstats\" s inner join \"%1$splayers\" p on s.PLAYER_ID = p.ID WHERE s.NAME = ? AND p.NAME NOT IN (%2$s)";

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
            String stat = ctx.pathParam("stat");
            jo.put("player", playerName);
            Player p = Bukkit.getPlayer(playerName);
            Map<String, String> stats = new HashMap<>(PlayerStats.getAvailableStats().size());
            if (p == null) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(GET_PLAYER_STAT.formatted(DatabaseManager.getPrefix()))) {
                    ps.setString(1, playerName);
                    ps.setString(2, stat);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        stats.put(rs.getString(1), rs.getString(2));
                    } else {
                        ctx.status(404);
                        ctx.result("The statistic %s could not be found".formatted(stat));
                        return;
                    }
                    rs.close();
                }
            } else {
                for (String st : PlayerStats.getAvailableStats()) {
                    stats.put(st, String.valueOf(p.getStatistic(Statistic.valueOf(st))));
                }
            }
            if (stats.isEmpty()) {
                ctx.status(404);
                ctx.result("The player %s could not be found".formatted(playerName));
                return;
            }
            jo.put("stats", stats);
            ctx.result(jo.toString());
        });

        app.get("/stats/player/{player}", ctx -> {
            JSONObject jo = new JSONObject();
            String playerName = ctx.pathParam("player");
            jo.put("player", playerName);
            Player p = Bukkit.getPlayer(playerName);
            Map<String, String> stats = new HashMap<>(PlayerStats.getAvailableStats().size());
            if (p == null) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(GET_PLAYER_STATS.formatted(DatabaseManager.getPrefix()))) {
                    ps.setString(1, playerName);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        stats.put(rs.getString(1), rs.getString(2));
                    }
                    rs.close();
                }
            } else {
                for (String st : PlayerStats.getAvailableStats()) {
                    stats.put(st, String.valueOf(p.getStatistic(Statistic.valueOf(st))));
                }
            }
            if (stats.isEmpty()) {
                ctx.status(404);
                ctx.result("The player %s could not be found".formatted(playerName));
                return;
            }
            jo.put("stats", stats);
            ctx.result(jo.toString());
        });
        app.get("/stats/stat/{stat}", ctx -> {
            JSONObject jo = new JSONObject();
            String stat = ctx.pathParam("stat");
            jo.put("stat", stat);
            Map<String, String> stats = new HashMap<>();
            List<String> online = new LinkedList<>();
            Statistic s = Statistic.valueOf(stat);
            for (Player p : Bukkit.getOnlinePlayers()) {
                online.add(p.getName());
                stats.put(p.getName(), String.valueOf(p.getStatistic(s)));
            }
            try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement ps = conn.prepareStatement(GET_STAT.formatted(DatabaseManager.getPrefix(), String.join(", ", Collections.nCopies(online.size(), "?"))));) {
                ps.setString(1, stat);
                int cnt = 2;
                for (String name : online) {
                    ps.setString(cnt, name);
                    cnt++;
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    stats.put(rs.getString(1), rs.getString(2));
                }
                rs.close();
            }
            if (stats.isEmpty()) {
                ctx.status(404);
                ctx.result("The stat %s could not be found".formatted(stat));
                return;
            }
            jo.put("values", stats);
            ctx.result(jo.toString());
        });
        app.start();
    }
}

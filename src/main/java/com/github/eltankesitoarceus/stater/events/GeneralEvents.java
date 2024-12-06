package com.github.eltankesitoarceus.stater.events;

import com.github.eltankesitoarceus.stater.data.PlayerStats;
import com.github.eltankesitoarceus.stater.data.database.DatabaseManager;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class GeneralEvents implements Listener {

    Logger logger = Logger.getLogger(String.valueOf(GeneralEvents.class));

    private static final String CHECK_PLAYER_QUERY = "SELECT COUNT(*) FROM \"%splayers\" WHERE NAME = ?";
    private static final String MAX_ID = "SELECT MAX(ID) FROM \"%splayers\"";//This will NOT scale well on big servers. TODO find a non database specific way of implementing a default value/autoincrement in the database itself
    private static final String INSERT_PLAYER = "INSERT INTO \"%splayers\" (ID, NAME) VALUES (?,?)";
    private static final String INSERT_PLAYER_STATS = "INSERT INTO \"%sstats\" (PLAYER_ID, NAME, VALUE) VALUES (?,?,?)";
    private static final String UPDATE_PLATER_STAT = "UPDATE \"%1$sstats\" SET VALUE = ? WHERE PLAYER_ID = (SELECT ID FROM \"%1$splayers\" WHERE NAME = ?) AND NAME = ?";


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String prefix = DatabaseManager.getPrefix();
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(CHECK_PLAYER_QUERY.formatted(prefix));
            ps.setString(1, p.getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cnt = rs.getInt(1);
                ps.close();
                rs.close();
                if (cnt <= 0) {
                    ps = conn.prepareStatement(MAX_ID.formatted(prefix));
                    rs = ps.executeQuery();
                    int id = 1;
                    if (rs.next()) {
                        id = rs.getInt(1) + 1;
                    }
                    System.out.println("Cnt: " + id);
                    ps.close();
                    rs.close();
                    ps = conn.prepareStatement(INSERT_PLAYER.formatted(prefix));
                    ps.setInt(1, id);
                    ps.setString(2, p.getName());
                    ps.executeUpdate();
                    ps.close();
                    ps = conn.prepareStatement(INSERT_PLAYER_STATS.formatted(prefix));
                    for (String s : PlayerStats.getAvailableStats()) {
                        ps.setInt(1, id);
                        ps.setString(2, s);
                        ps.setString(3, "");
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            } else {
                throw new SQLException("A COUNT() query to the \"%splayers\" table returned an empty response".formatted(prefix));
            }
        } catch (SQLException ex) {
            logger.severe("SQL " + ex.getSQLState() + " error:" + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(UPDATE_PLATER_STAT.formatted(DatabaseManager.getPrefix()))) {
            Player p = e.getPlayer();
            for (String s : PlayerStats.getAvailableStats()) {
                ps.setString(1, String.valueOf(p.getStatistic(Statistic.valueOf(s))));
                ps.setString(2, p.getName());
                ps.setString(3, s);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

    }
}

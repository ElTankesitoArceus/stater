package com.github.eltankesitoarceus.stater.commands;

import com.github.eltankesitoarceus.stater.data.PlayerStats;
import com.github.eltankesitoarceus.stater.data.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Stat implements CommandExecutor, TabExecutor {

    public static final String message = "%s for %s: %s";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p;
        if (args.length < 2) {
            return false;
        }
        String statName;
        if (args.length > 2) {
            p = Bukkit.getPlayer(args[1]);
            statName = args[2];
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("In console, you need to provide a player name");
                return false;
            }
            p = (Player) sender;
            statName = args[1];
        }
        assert p != null;
        sender.sendMessage(message.formatted(p.getName(), statName, String.valueOf(PlayerStats.getPlayerStat(p, statName))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of(Players.getAllPlayers());
        }
        return PlayerStats.getAvailableStats();
    }
}


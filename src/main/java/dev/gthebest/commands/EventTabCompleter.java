package dev.gthebest.commands;

import dev.gthebest.ASnowball;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class EventTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String a, String[] args) {
        if (args.length == 1) return Arrays.asList("team", "start", "stop", "reload", "setspawn");
        if (args.length == 2 && args[0].equalsIgnoreCase("team")) {
            return new ArrayList<>(ASnowball.getInstance().getConfig().getConfigurationSection("teams").getKeys(false));
        }
        return Collections.emptyList();
    }
}
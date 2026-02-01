package dev.gthebest.placeholders;

import dev.gthebest.ASnowball;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

public class EventExpansion extends PlaceholderExpansion {
    private final ASnowball plugin;
    public EventExpansion(ASnowball plugin) { this.plugin = plugin; }

    @Override @NotNull public String getIdentifier() { return "asnowball"; }
    @Override @NotNull public String getAuthor() { return "GTheBest_"; }
    @Override @NotNull public String getVersion() { return "1.0"; }
    @Override public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("total_wins_")) {
            try {
                int rank = Integer.parseInt(params.replace("total_wins_", ""));
                List<Map.Entry<String, Integer>> lb = plugin.getData().getLeaderboard();
                if (lb.size() < rank) return "N/A";
                Map.Entry<String, Integer> entry = lb.get(rank - 1);
                return entry.getKey() + ": " + entry.getValue();
            } catch (Exception e) { return "0"; }
        }
        if (player == null) return "";
        if (params.equalsIgnoreCase("score")) return String.valueOf(plugin.getData().currentScores.getOrDefault(player.getUniqueId(), 0));
        return null;
    }
}
package dev.gthebest.listeners;

import dev.gthebest.ASnowball;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scoreboard.*;
import java.util.List;
import java.util.UUID;

public class GameListener implements Listener {
    private final ASnowball plugin;
    public GameListener(ASnowball plugin) { this.plugin = plugin; }

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(plugin.getData().color(plugin.getConfig().getString("gui.title")))) {
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player p)) return;
            if (e.getRawSlot() == 11) {
                plugin.getParticipants().add(p.getUniqueId());
                Bukkit.broadcastMessage(plugin.getData().color(plugin.getConfig().getString("messages.event-broadcast").replace("{player}", p.getName())));
                p.closeInventory();
            } else if (e.getRawSlot() == 15) {
                Bukkit.broadcastMessage(plugin.getData().color(plugin.getConfig().getString("messages.event-not-joined").replace("{player}", p.getName())));
                p.closeInventory();
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!plugin.isEventActive()) return;
        if (e.getDamager() instanceof Snowball s && e.getEntity() instanceof Player v) {
            if (s.getShooter() instanceof Player sh) {
                if (plugin.getParticipants().contains(v.getUniqueId()) && plugin.getParticipants().contains(sh.getUniqueId())) {
                    String t = plugin.getData().playerTeams.get(v.getUniqueId());
                    v.teleport(plugin.getData().getLoc("team." + t));
                    plugin.getData().currentScores.put(sh.getUniqueId(), plugin.getData().currentScores.getOrDefault(sh.getUniqueId(), 0) + 1);
                    updateBoards();
                    e.setCancelled(true);
                }
            }
        }
    }

    private void updateBoards() {
        for (UUID uuid : plugin.getParticipants()) {
            Player pl = Bukkit.getPlayer(uuid);
            if (pl == null) continue;
            Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective o = b.registerNewObjective("asnow", "dummy", plugin.getData().color(plugin.getConfig().getString("scoreboard.title")));
            o.setDisplaySlot(DisplaySlot.SIDEBAR);
            List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
            for (int i = 0; i < lines.size(); i++) {
                String tKey = plugin.getData().playerTeams.getOrDefault(pl.getUniqueId(), "none");
                String line = plugin.getData().color(lines.get(i)
                        .replace("%player_name%", pl.getName())
                        .replace("%score%", String.valueOf(plugin.getData().currentScores.getOrDefault(pl.getUniqueId(), 0)))
                        .replace("%team_name%", plugin.getConfig().getString("teams." + tKey + ".display", tKey))
                        .replace("%time%", String.valueOf(plugin.getData().timeLeft))
                        .replace("%team_color%", plugin.getConfig().getString("teams." + tKey + ".color", "&f")));
                o.getScore(line).setScore(lines.size() - i);
            }
            pl.setScoreboard(b);
        }
    }
}
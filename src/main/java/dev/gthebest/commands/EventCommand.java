package dev.gthebest.commands;

import dev.gthebest.ASnowball;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class EventCommand implements CommandExecutor {
    private final ASnowball plugin;
    private BukkitRunnable gameTask;

    public EventCommand(ASnowball plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("aevent.admin")) return true;

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "setspawn" -> {
                    plugin.getData().setLoc("main", p.getLocation());
                    p.sendMessage(plugin.getData().color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.set-main-spawn")));
                }
                case "team" -> {
                    if (args.length == 3) {
                        plugin.getData().setLoc("team." + args[1].toLowerCase(), p.getLocation());
                        p.sendMessage(plugin.getData().color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.set-team-spawn").replace("{team}", args[1])));
                    }
                }
                case "start" -> {
                    openJoinGUI();
                    startCountdown();
                }
                case "stop" -> stopEvent(true);
                case "reload" -> {
                    plugin.reloadPlugin();
                    p.sendMessage(plugin.getData().color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.reload")));
                }
            }
            return true;
        }
        return false;
    }

    private void openJoinGUI() {
        Inventory inv = Bukkit.createInventory(null, 27, plugin.getData().color(plugin.getConfig().getString("gui.title")));

        ItemStack join = new ItemStack(Material.LIME_WOOL);
        ItemMeta jm = join.getItemMeta();
        jm.setDisplayName(plugin.getData().color(plugin.getConfig().getString("gui.join-item.name")));
        jm.setLore(plugin.getConfig().getStringList("gui.join-item.lore").stream().map(plugin.getData()::color).toList());
        join.setItemMeta(jm);

        ItemStack leave = new ItemStack(Material.RED_WOOL);
        ItemMeta lm = leave.getItemMeta();
        lm.setDisplayName(plugin.getData().color(plugin.getConfig().getString("gui.leave-item.name")));
        lm.setLore(plugin.getConfig().getStringList("gui.leave-item.lore").stream().map(plugin.getData()::color).toList());
        leave.setItemMeta(lm);

        inv.setItem(11, join);
        inv.setItem(15, leave);
        Bukkit.getOnlinePlayers().forEach(pl -> pl.openInventory(inv));
    }

    private void startCountdown() {
        new BukkitRunnable() {
            int c = plugin.getConfig().getInt("settings.countdown");
            public void run() {
                if (c > 0) {
                    Bukkit.getOnlinePlayers().forEach(pl -> {
                        pl.sendTitle(plugin.getData().color("&e" + c), "", 5, 10, 5);
                        plugin.getData().playSound(pl, "BLOCK_NOTE_BLOCK_PLING", "UI_BUTTON_CLICK");
                    });
                } else {
                    plugin.setEventActive(true);
                    assignTeams();
                    runGameLoop();
                    cancel();
                }
                c--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void runGameLoop() {
        gameTask = new BukkitRunnable() {
            public void run() {
                if (plugin.getData().timeLeft > 0 && plugin.isEventActive()) {
                    String bar = plugin.getData().color("&fRemaining: &b" + plugin.getData().timeLeft + "s");
                    plugin.getParticipants().stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                            .forEach(pl -> pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar)));
                    plugin.getData().timeLeft--;
                } else {
                    stopEvent(false);
                    cancel();
                }
            }
        };
        gameTask.runTaskTimer(plugin, 0, 20);
    }

    public void stopEvent(boolean manual) {
        plugin.setEventActive(false);
        if (gameTask != null) gameTask.cancel();

        Player w = plugin.getParticipants().stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                .max(Comparator.comparingInt(pl -> plugin.getData().currentScores.getOrDefault(pl.getUniqueId(), 0))).orElse(null);

        if (w != null && !manual) {
            plugin.getData().addWin(w.getUniqueId());
            Bukkit.broadcastMessage(plugin.getData().color(plugin.getConfig().getString("messages.winner-msg").replace("{player}", w.getName()).replace("{score}", String.valueOf(plugin.getData().currentScores.getOrDefault(w.getUniqueId(), 0)))));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), plugin.getConfig().getString("settings.winner-command").replace("%player%", w.getName()));
        }

        Location main = plugin.getData().getLoc("main");
        plugin.getParticipants().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(pl -> {
            if (main != null) pl.teleport(main);
            pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        });

        plugin.getParticipants().clear();
        plugin.getData().currentScores.clear();
        plugin.getData().timeLeft = plugin.getConfig().getInt("settings.game-duration");
    }

    private void assignTeams() {
        List<String> teams = new ArrayList<>(plugin.getConfig().getConfigurationSection("teams").getKeys(false));
        if (teams.isEmpty()) return;
        int i = 0;
        for (UUID uuid : plugin.getParticipants()) {
            Player pl = Bukkit.getPlayer(uuid);
            if (pl == null) continue;
            String t = teams.get(i % teams.size());
            plugin.getData().playerTeams.put(uuid, t);
            Location loc = plugin.getData().getLoc("team." + t);
            if (loc != null) pl.teleport(loc);
            i++;
        }
    }
}
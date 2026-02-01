package dev.gthebest.data;

import dev.gthebest.ASnowball;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataManager {
    private final ASnowball plugin;
    private File configFile;
    private FileConfiguration data;
    public final Map<UUID, Integer> currentScores = new HashMap<>();
    public final Map<UUID, String> playerTeams = new HashMap<>();
    public int timeLeft;

    public DataManager(ASnowball plugin) {
        this.plugin = plugin;
        this.timeLeft = plugin.getConfig().getInt("settings.game-duration");
        reloadData();
    }

    public void reloadData() {
        configFile = new File(plugin.getDataFolder(), "data.yml");
        if (!configFile.exists()) { try { configFile.createNewFile(); } catch (IOException ignored) {} }
        data = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveData() { try { data.save(configFile); } catch (IOException ignored) {} }

    public void addWin(UUID uuid) {
        int wins = data.getInt("stats." + uuid.toString() + ".wins", 0);
        data.set("stats." + uuid.toString() + ".wins", wins + 1);
        data.set("stats." + uuid.toString() + ".name", Bukkit.getOfflinePlayer(uuid).getName());
        saveData();
    }

    public List<Map.Entry<String, Integer>> getLeaderboard() {
        Map<String, Integer> lb = new HashMap<>();
        if (data.getConfigurationSection("stats") == null) return new ArrayList<>();
        for (String key : data.getConfigurationSection("stats").getKeys(false)) {
            lb.put(data.getString("stats." + key + ".name"), data.getInt("stats." + key + ".wins"));
        }
        return lb.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).collect(Collectors.toList());
    }

    public String color(String msg) {
        if (msg == null) return "";
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            String color = msg.substring(matcher.start(), matcher.end());
            msg = msg.replace(color, ChatColor.of(color.substring(1)).toString());
            matcher = pattern.matcher(msg);
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void setLoc(String path, Location loc) {
        data.set("locations." + path + ".world", loc.getWorld().getName());
        data.set("locations." + path + ".x", loc.getX());
        data.set("locations." + path + ".y", loc.getY());
        data.set("locations." + path + ".z", loc.getZ());
        data.set("locations." + path + ".yaw", (float) loc.getYaw());
        data.set("locations." + path + ".pitch", (float) loc.getPitch());
        saveData();
    }

    public Location getLoc(String path) {
        if (!data.contains("locations." + path)) return null;
        String p = "locations." + path;
        return new Location(Bukkit.getWorld(data.getString(p + ".world")),
                data.getDouble(p + ".x"), data.getDouble(p + ".y"), data.getDouble(p + ".z"),
                (float) data.getDouble(p + ".yaw"), (float) data.getDouble(p + ".pitch"));
    }

    public void playSound(Player p, String m, String l) {
        try { p.playSound(p.getLocation(), Sound.valueOf(m), 1f, 1f); } catch (Exception e) { p.playSound(p.getLocation(), Sound.valueOf(l), 1f, 1f); }
    }
}
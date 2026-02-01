package dev.gthebest;

import dev.gthebest.commands.EventCommand;
import dev.gthebest.commands.EventTabCompleter;
import dev.gthebest.data.DataManager;
import dev.gthebest.listeners.GameListener;
import dev.gthebest.placeholders.EventExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ASnowball extends JavaPlugin {
    private static ASnowball instance;
    private DataManager dataManager;
    private boolean eventActive = false;
    private final Set<UUID> participants = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.dataManager = new DataManager(this);

        getCommand("aevent").setExecutor(new EventCommand(this));
        getCommand("aevent").setTabCompleter(new EventTabCompleter());
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EventExpansion(this).register();
        }
    }

    public static ASnowball getInstance() { return instance; }
    public DataManager getData() { return dataManager; }
    public boolean isEventActive() { return eventActive; }
    public void setEventActive(boolean active) { this.eventActive = active; }
    public Set<UUID> getParticipants() { return participants; }
    public void reloadPlugin() { reloadConfig(); this.dataManager.reloadData(); }
}
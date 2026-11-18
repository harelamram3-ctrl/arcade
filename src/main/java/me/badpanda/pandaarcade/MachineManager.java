package me.badpanda.pandaarcade;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MachineManager {

    private final PandaArcade plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, String> machines = new HashMap<>();

    private static final Set<String> VALID_TYPES = Set.of(
            "menu", "shooter", "pacman", "snake", "mole", "mario", "airhockey", "ttt", "ships"
    );

    public MachineManager(PandaArcade plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "machines.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public static boolean isValidGameType(String type) {
        return VALID_TYPES.contains(type.toLowerCase());
    }

    private String locToKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void setMachine(Location loc, String gameType) {
        String key = locToKey(loc);
        machines.put(key, gameType.toLowerCase());
        config.set("machines." + key, gameType.toLowerCase());
        save();
    }

    public void removeMachine(Location loc) {
        String key = locToKey(loc);
        machines.remove(key);
        config.set("machines." + key, null);
        save();
    }

    public boolean isMachine(Location loc) {
        return machines.containsKey(locToKey(loc));
    }

    public String getMachineGame(Location loc) {
        return machines.get(locToKey(loc));
    }

    private void load() {
        ConfigurationSection sec = config.getConfigurationSection("machines");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            machines.put(key, sec.getString(key));
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

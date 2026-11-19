package me.badpanda.pandaarcade;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MachineManager {

    private final PandaArcade plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, String> machines = new HashMap<>();
    private final NamespacedKey barrierGameKey;

    private static final Set<String> VALID_TYPES = Set.of(
            "menu", "shooter", "pacman", "snake", "mole", "mario", "flappy",
            "airhockey", "ttt", "ships", "connect4",
            "sumo", "spleef", "duel", "tnttag"
    );

    public MachineManager(PandaArcade plugin) {
        this.plugin = plugin;
        this.barrierGameKey = new NamespacedKey(plugin, "barrier_game_type");
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

    public ItemStack createBarrierWand(String gameType) {
        String clean = gameType.toLowerCase();
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lבלוק בריאר כניסה: §e" + clean.toUpperCase());
            meta.setLore(List.of(
                    "§7הנח בלוק זה בעולם כדי ליצור כניסה למשחק!",
                    "§7שחקנים שילחצו עליו ייכנסו מיד ל-§f" + clean,
                    "§8הסר בלוק (Shift + שבירה): §c/arcade delmachine"
            ));
            meta.getPersistentDataContainer().set(barrierGameKey, PersistentDataType.STRING, clean);
            item.setItemMeta(meta);
        }
        return item;
    }

    public String getGameTypeFromWand(ItemStack item) {
        if (item == null || item.getType() != Material.BARRIER || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(barrierGameKey, PersistentDataType.STRING);
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

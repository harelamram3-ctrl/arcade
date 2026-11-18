package il.arcade;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScoreManager {
    private final ArcadePlugin plugin;
    private final File file;
    private final FileConfiguration cfg;

    public ScoreManager(ArcadePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "scores.yml");
        plugin.getDataFolder().mkdirs();
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    /** Returns true if this is a new personal best. */
    public boolean submit(String game, Player p, int score) {
        String path = game + "." + p.getUniqueId();
        int old = cfg.getInt(path + ".best", -1);
        if (score > old) {
            cfg.set(path + ".best", score);
            cfg.set(path + ".name", p.getName());
            save();
            return true;
        }
        return false;
    }

    public int best(String game, Player p) {
        return Math.max(0, cfg.getInt(game + "." + p.getUniqueId() + ".best", 0));
    }

    public List<Map.Entry<String, Integer>> top(String game, int n) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        ConfigurationSection section = cfg.getConfigurationSection(game);
        if (section == null) return list;
        for (String key : section.getKeys(false)) {
            list.add(new AbstractMap.SimpleEntry<>(
                    section.getString(key + ".name", "?"), section.getInt(key + ".best")));
        }
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return list.size() > n ? list.subList(0, n) : list;
    }

    private void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save scores.yml: " + e.getMessage());
        }
    }
}

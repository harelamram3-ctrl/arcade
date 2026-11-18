package me.badpanda.pandaarcade;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final PandaArcade plugin;
    private final File file;
    private FileConfiguration config;

    public LeaderboardManager(PandaArcade plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "leaderboards.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void updateScore(String gameId, String playerName, int score) {
        int currentHigh = config.getInt(gameId + "." + playerName, 0);
        if (score > currentHigh) {
            config.set(gameId + "." + playerName, score);
            save();
        }
    }

    public void addWin(String gameId, String playerName) {
        int currentWins = config.getInt(gameId + "." + playerName, 0);
        config.set(gameId + "." + playerName, currentWins + 1);
        save();
    }

    public int getHighScore(String gameId, String playerName) {
        return config.getInt(gameId + "." + playerName, 0);
    }

    public List<Map.Entry<String, Integer>> getTopScores(String gameId, int limit) {
        ConfigurationSection section = config.getConfigurationSection(gameId);
        if (section == null) return Collections.emptyList();
        Map<String, Integer> scores = new HashMap<>();
        for (String key : section.getKeys(false)) {
            scores.put(key, config.getInt(gameId + "." + key));
        }
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

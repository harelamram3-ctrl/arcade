package me.badpanda.pandaarcade.games;

import me.badpanda.pandaarcade.ArcadeGame;
import me.badpanda.pandaarcade.ArcadeGUI;
import me.badpanda.pandaarcade.PandaArcade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceShooterGame extends ArcadeGame {

    private int shipCol = 4;
    private final List<Integer> lasers = new ArrayList<>();
    private final List<Integer> enemies = new ArrayList<>();
    private final Random random = new Random();
    private int tickCount = 0;

    public SpaceShooterGame(Player player) {
        super(player, "🚀 Space Shooter | יריות חלל", 54, "shooter");
    }

    @Override
    public void start() {
        player.openInventory(inventory);
        render();
        this.task = Bukkit.getScheduler().runTaskTimer(PandaArcade.getInstance(), this::tick, 5L, 5L);
    }

    private void tick() {
        if (isOver) return;
        tickCount++;

        List<Integer> nextLasers = new ArrayList<>();
        for (int pos : lasers) {
            int next = pos - 9;
            if (next >= 0) {
                if (enemies.contains(next)) {
                    enemies.remove((Integer) next);
                    score += 10;
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
                } else {
                    nextLasers.add(next);
                }
            }
        }
        lasers.clear();
        lasers.addAll(nextLasers);

        if (tickCount % 3 == 0) {
            List<Integer> nextEnemies = new ArrayList<>();
            for (int pos : enemies) {
                int next = pos + 9;
                if (next >= 45) {
                    gameOver("חללית אויב חדרה להגנות!");
                    return;
                }
                if (lasers.contains(next)) {
                    lasers.remove((Integer) next);
                    score += 10;
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
                } else {
                    nextEnemies.add(next);
                }
            }
            enemies.clear();
            enemies.addAll(nextEnemies);

            if (random.nextInt(100) < 65) {
                enemies.add(random.nextInt(9));
            }
        }

        if (enemies.contains(36 + shipCol)) {
            gameOver("התנגשת בחללית אויב!");
            return;
        }

        render();
    }

    @Override
    public void handleClick(int slot) {
        if (isOver) return;
        if (slot == 47 && shipCol > 0) {
            shipCol--;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
        } else if (slot == 51 && shipCol < 8) {
            shipCol++;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
        } else if (slot == 49) {
            int laserStart = 27 + shipCol;
            if (enemies.contains(laserStart)) {
                enemies.remove((Integer) laserStart);
                score += 10;
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
            } else {
                lasers.add(laserStart);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 2.0f);
            }
        }
        render();
    }

    private void render() {
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        for (int enemy : enemies) {
            inventory.setItem(enemy, ArcadeGUI.createItem(Material.TNT, "§c§lחללית אויב", List.of()));
        }
        for (int laser : lasers) {
            inventory.setItem(laser, ArcadeGUI.createItem(Material.END_ROD, "§b§lלייזר", List.of()));
        }
        inventory.setItem(36 + shipCol, ArcadeGUI.createItem(Material.BEACON, "§a§lהחללית שלך §7(ניקוד: " + score + ")", List.of()));

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(47, ArcadeGUI.createItem(Material.ARROW, "§e§l⬅ שמאלה", List.of()));
        inventory.setItem(49, ArcadeGUI.createItem(Material.FIRE_CHARGE, "§c§l🔥 ירי לייזר!", List.of("§7ניקוד נוכחי: §a" + score)));
        inventory.setItem(51, ArcadeGUI.createItem(Material.ARROW, "§e§lימינה ➡", List.of()));
    }
}

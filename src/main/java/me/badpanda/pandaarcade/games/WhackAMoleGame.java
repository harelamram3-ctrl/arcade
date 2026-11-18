package me.badpanda.pandaarcade.games;

import me.badpanda.pandaarcade.ArcadeGame;
import me.badpanda.pandaarcade.ArcadeGUI;
import me.badpanda.pandaarcade.PandaArcade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class WhackAMoleGame extends ArcadeGame {

    // 0 = בור ריק, 1 = חפרפרת רגילה (+15), 2 = חפרפרת זהב (+40), 3 = פצצת TNT (-25)
    private final int[] board = new int[45];
    private final Random random = new Random();
    private int ticksRemaining = 60; // 30 שניות משחק (כל טיק 10L = 0.5 שנייה)

    public WhackAMoleGame(Player player) {
        super(player, "🔨 Whack-A-Mole | הכה בחפרפרת", 54, "mole");
    }

    @Override
    public void start() {
        player.openInventory(inventory);
        spawnTargets();
        render();
        this.task = Bukkit.getScheduler().runTaskTimer(PandaArcade.getInstance(), this::tick, 10L, 10L);
    }

    private void tick() {
        if (isOver) return;
        ticksRemaining--;
        if (ticksRemaining <= 0) {
            gameOver("הזמן נגמר! כל הכבוד!");
            return;
        }

        // ניקוי אקראי והופעת מטרות חדשות
        if (ticksRemaining % 2 == 0) {
            Arrays.fill(board, 0);
            spawnTargets();
        }
        render();
    }

    private void spawnTargets() {
        int count = 3 + random.nextInt(3); // 3 עד 5 מטרות בו-זמנית
        for (int i = 0; i < count; i++) {
            int slot = random.nextInt(45);
            int roll = random.nextInt(100);
            if (roll < 60) {
                board[slot] = 1; // חפרפרת רגילה
            } else if (roll < 80) {
                board[slot] = 2; // חפרפרת זהב
            } else {
                board[slot] = 3; // פצצה
            }
        }
    }

    @Override
    public void handleClick(int slot) {
        if (isOver || slot < 0 || slot >= 45) return;

        int target = board[slot];
        if (target == 1) {
            board[slot] = 0;
            score += 15;
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 1.4f);
        } else if (target == 2) {
            board[slot] = 0;
            score += 40;
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.8f);
        } else if (target == 3) {
            board[slot] = 0;
            score = Math.max(0, score - 25);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);
        }
        render();
    }

    private void render() {
        for (int i = 0; i < 45; i++) {
            if (board[i] == 1) {
                inventory.setItem(i, ArcadeGUI.createItem(Material.BROWN_CONCRETE, "§6§lחפרפרת! (+15)", List.of("§7לחץ מהר כדי להכות בה!")));
            } else if (board[i] == 2) {
                inventory.setItem(i, ArcadeGUI.createItem(Material.GOLD_BLOCK, "§e§l✨ חפרפרת זהב! (+40)", List.of("§7בונוס מהיר!")));
            } else if (board[i] == 3) {
                inventory.setItem(i, ArcadeGUI.createItem(Material.TNT, "§c§l💣 פצצה! (-25)", List.of("§cאל תלחץ על זה!")));
            } else {
                inventory.setItem(i, ArcadeGUI.createItem(Material.COARSE_DIRT, "§8בור ריק", List.of()));
            }
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(48, ArcadeGUI.createItem(Material.CLOCK, "§eזמן שנותר: §f" + (ticksRemaining / 2) + " שנ'", List.of()));
        inventory.setItem(50, ArcadeGUI.createItem(Material.EMERALD, "§aניקוד: §f" + score, List.of()));
    }
}

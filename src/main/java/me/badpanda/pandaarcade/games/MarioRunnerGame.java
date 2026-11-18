package me.badpanda.pandaarcade.games;

import me.badpanda.pandaarcade.ArcadeGame;
import me.badpanda.pandaarcade.ArcadeGUI;
import me.badpanda.pandaarcade.PandaArcade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class MarioRunnerGame extends ArcadeGame {

    private int marioRow = 3;
    private int jumpTicks = 0;
    private final int marioCol = 1;

    private final int[][] grid = new int[5][9];
    private final Random random = new Random();
    private int stepCount = 0;

    public MarioRunnerGame(Player player) {
        super(player, "🍄 Super Mario Run | מריו ראנר", 54, "mario");
    }

    @Override
    public void start() {
        player.openInventory(inventory);
        render();
        this.task = Bukkit.getScheduler().runTaskTimer(PandaArcade.getInstance(), this::tick, 5L, 5L);
    }

    private void tick() {
        if (isOver) return;
        stepCount++;
        score += 2;

        if (jumpTicks > 0) {
            jumpTicks--;
            if (jumpTicks >= 2) {
                marioRow = 1;
            } else {
                marioRow = 2;
            }
        } else {
            marioRow = 3;
        }

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                grid[r][c] = grid[r][c + 1];
            }
            grid[r][8] = 0;
        }

        if (stepCount % 3 == 0) {
            int roll = random.nextInt(100);
            if (roll < 45) {
                grid[3][8] = 1;
                if (random.nextBoolean()) {
                    grid[2][8] = 1;
                }
            } else if (roll < 75) {
                grid[2][8] = 2;
                grid[1][8] = 2;
            } else {
                grid[1][8] = 3;
            }
        }

        int cell = grid[marioRow][marioCol];
        if (cell == 1) {
            gameOver("נתקעת בצינור ירוק!");
            return;
        } else if (cell == 2) {
            grid[marioRow][marioCol] = 0;
            score += 20;
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 2.0f);
        } else if (cell == 3) {
            grid[marioRow][marioCol] = 0;
            score += 50;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.8f);
        }

        render();
    }

    @Override
    public void handleClick(int slot) {
        if (isOver) return;
        if (marioRow == 3) {
            jumpTicks = 4;
            marioRow = 2;
            player.playSound(player.getLocation(), Sound.ENTITY_RABBIT_JUMP, 0.8f, 1.2f);
            render();
        }
    }

    private void render() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 9; c++) {
                int slot = r * 9 + c;
                int val = grid[r][c];
                if (val == 1) {
                    inventory.setItem(slot, ArcadeGUI.createItem(Material.LIME_TERRACOTTA, "§2§lצינור ירוק", List.of("§cקפוץ מעליו!")));
                } else if (val == 2) {
                    inventory.setItem(slot, ArcadeGUI.createItem(Material.SUNFLOWER, "§e§lמטבע זהב (+20)", List.of()));
                } else if (val == 3) {
                    inventory.setItem(slot, ArcadeGUI.createItem(Material.GOLD_BLOCK, "§6§lבלוק הפתעה [?] (+50)", List.of("§eקפוץ כדי לשבור!")));
                } else {
                    inventory.setItem(slot, ArcadeGUI.createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", List.of()));
                }
            }
        }

        for (int c = 0; c < 9; c++) {
            inventory.setItem(36 + c, ArcadeGUI.createItem(Material.GRASS_BLOCK, "§aאדמת הממלכה", List.of()));
        }

        int marioSlot = marioRow * 9 + marioCol;
        inventory.setItem(marioSlot, ArcadeGUI.createItem(Material.RED_CONCRETE, "§c§lמריו §7(ניקוד: " + score + ")", List.of()));

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(49, ArcadeGUI.createItem(Material.FEATHER, "§a§l⬆ קפיצה! (לחץ כאן)", List.of("§7ניקוד נוכחי: §e" + score)));
    }
}

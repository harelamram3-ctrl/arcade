package me.badpanda.pandaarcade.games;

import me.badpanda.pandaarcade.ArcadeGame;
import me.badpanda.pandaarcade.ArcadeGUI;
import me.badpanda.pandaarcade.PandaArcade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SnakeGame extends ArcadeGame {

    private final LinkedList<Integer> snake = new LinkedList<>();
    private int direction = 1; // 1 ימינה, -1 שמאלה, -9 למעלה, 9 למטה
    private int applePos;
    private final Random random = new Random();

    public SnakeGame(Player player) {
        super(player, "🐍 Snake | סנייק", 54, "snake");
        snake.add(20);
        snake.add(19);
        spawnApple();
    }

    @Override
    public void start() {
        player.openInventory(inventory);
        render();
        this.task = Bukkit.getScheduler().runTaskTimer(PandaArcade.getInstance(), this::tick, 6L, 6L);
    }

    private void spawnApple() {
        do {
            applePos = random.nextInt(45);
        } while (snake.contains(applePos));
    }

    private void tick() {
        if (isOver) return;
        int head = snake.getFirst();
        int next = head + direction;

        // בדיקת גבולות מסך (0-44)
        if (next < 0 || next >= 45 || (Math.abs(direction) == 1 && head / 9 != next / 9)) {
            gameOver("התנגשת בקיר!");
            return;
        }
        if (snake.contains(next)) {
            gameOver("נשכת את הזנב של עצמך!");
            return;
        }

        snake.addFirst(next);
        if (next == applePos) {
            score += 15;
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1.3f);
            spawnApple();
        } else {
            snake.removeLast();
        }

        render();
    }

    @Override
    public void handleClick(int slot) {
        if (slot == 46 && direction != 9) direction = -9;
        else if (slot == 48 && direction != 1) direction = -1;
        else if (slot == 50 && direction != -1) direction = 1;
        else if (slot == 52 && direction != -9) direction = 9;
    }

    private void render() {
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }

        inventory.setItem(applePos, ArcadeGUI.createItem(Material.APPLE, "§c§lתפוח (+15)", List.of()));

        for (int i = 0; i < snake.size(); i++) {
            int pos = snake.get(i);
            if (i == 0) {
                inventory.setItem(pos, ArcadeGUI.createItem(Material.EMERALD_BLOCK, "§a§lראש הנחש", List.of()));
            } else {
                inventory.setItem(pos, ArcadeGUI.createItem(Material.LIME_CONCRETE, "§2גוף הנחש", List.of()));
            }
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(46, ArcadeGUI.createItem(Material.ARROW, "§e§l⬆ למעלה", List.of()));
        inventory.setItem(48, ArcadeGUI.createItem(Material.ARROW, "§e§l⬅ שמאלה", List.of()));
        inventory.setItem(49, ArcadeGUI.createItem(Material.GOLD_INGOT, "§6ניקוד: §f" + score, List.of()));
        inventory.setItem(50, ArcadeGUI.createItem(Material.ARROW, "§e§lימינה ➡", List.of()));
        inventory.setItem(52, ArcadeGUI.createItem(Material.ARROW, "§e§l⬇ למטה", List.of()));
    }
}

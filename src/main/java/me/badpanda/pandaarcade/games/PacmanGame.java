package me.badpanda.pandaarcade.games;

import me.badpanda.pandaarcade.ArcadeGame;
import me.badpanda.pandaarcade.ArcadeGUI;
import me.badpanda.pandaarcade.PandaArcade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class PacmanGame extends ArcadeGame {

    // 0 = ריק, 1 = קיר, 2 = נקודה, 3 = כדור כוח
    private final int[] map = {
            1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 3, 2, 2, 1, 2, 2, 3, 1,
            1, 2, 1, 2, 2, 2, 1, 2, 1,
            1, 2, 2, 2, 1, 2, 2, 2, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    private int pacPos = 22; // מרכז המבוך
    private int direction = 0; // -1 שמאלה, 1 ימינה, -9 למעלה, 9 למטה
    private final List<Integer> ghosts = new ArrayList<>(List.of(10, 16));
    private int powerTicks = 0;
    private final Random random = new Random();

    public PacmanGame(Player player) {
        super(player, "🟡 Pac-Man | פקמן", 54, "pacman");
        map[pacPos] = 0;
    }

    @Override
    public void start() {
        player.openInventory(inventory);
        render();
        this.task = Bukkit.getScheduler().runTaskTimer(PandaArcade.getInstance(), this::tick, 8L, 8L);
    }

    private void tick() {
        if (isOver) return;

        if (powerTicks > 0) powerTicks--;

        // תזוזת פקמן
        if (direction != 0) {
            int next = pacPos + direction;
            if (isValidMove(pacPos, next)) {
                pacPos = next;
                if (map[pacPos] == 2) {
                    map[pacPos] = 0;
                    score += 10;
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.8f);
                } else if (map[pacPos] == 3) {
                    map[pacPos] = 0;
                    score += 50;
                    powerTicks = 15;
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
                }
            }
        }

        // בדיקת ניצחון (נגמרו הנקודות)
        boolean dotsLeft = false;
        for (int cell : map) {
            if (cell == 2 || cell == 3) {
                dotsLeft = true;
                break;
            }
        }
        if (!dotsLeft) {
            score += 200;
            gameOver("ניצחת! אספת את כל הנקודות במבוך!");
            return;
        }

        // תזוזת רוחות
        int[] dirs = {-1, 1, -9, 9};
        for (int i = 0; i < ghosts.size(); i++) {
            int gPos = ghosts.get(i);
            if (gPos == pacPos) {
                if (handleGhostCollision(i)) return;
            }
            List<Integer> validDirs = new ArrayList<>();
            for (int d : dirs) {
                if (isValidMove(gPos, gPos + d)) validDirs.add(gPos + d);
            }
            if (!validDirs.isEmpty()) {
                gPos = validDirs.get(random.nextInt(validDirs.size()));
                ghosts.set(i, gPos);
            }
            if (gPos == pacPos) {
                if (handleGhostCollision(i)) return;
            }
        }

        render();
    }

    private boolean handleGhostCollision(int ghostIndex) {
        if (powerTicks > 0) {
            score += 100;
            ghosts.set(ghostIndex, 13); // החזרת הרוח למרכז
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.7f, 1.8f);
            return false;
        } else {
            gameOver("נתפסת על ידי רוח רפאים!");
            return true;
        }
    }

    private boolean isValidMove(int from, int to) {
        if (to < 0 || to >= 45) return false;
        if (Math.abs(from - to) == 1 && (from / 9 != to / 9)) return false;
        return map[to] != 1;
    }

    @Override
    public void handleClick(int slot) {
        if (slot == 46) direction = -9; // למעלה
        else if (slot == 48) direction = -1; // שמאלה
        else if (slot == 50) direction = 1; // ימינה
        else if (slot == 52) direction = 9; // למטה
    }

    private void render() {
        for (int i = 0; i < 45; i++) {
            if (map[i] == 1) {
                inventory.setItem(i, ArcadeGUI.createItem(Material.BLUE_STAINED_GLASS_PANE, "§9קיר", List.of()));
            } else if (map[i] == 2) {
                inventory.setItem(i, ArcadeGUI.createItem(Material.GOLD_NUGGET, "§eנקודה (+10)", List.of()));
            } else if (map[i] == 3) {
                inventory.setItem(i, ArcadeGUI.createItem(Material.GOLDEN_APPLE, "§6§lכדור כוח!", List.of()));
            } else {
                inventory.setItem(i, ArcadeGUI.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
            }
        }

        // רוחות
        for (int g : ghosts) {
            if (powerTicks > 0) {
                inventory.setItem(g, ArcadeGUI.createItem(Material.LIGHT_BLUE_DYE, "§b§lרוח פגיעה! (אכול אותה)", List.of()));
            } else {
                inventory.setItem(g, ArcadeGUI.createItem(Material.REDSTONE_BLOCK, "§c§lרוח רפאים", List.of()));
            }
        }

        // פקמן
        inventory.setItem(pacPos, ArcadeGUI.createItem(Material.YELLOW_CONCRETE, "§e§lפקמן §7(ניקוד: " + score + ")", List.of()));

        // כפתורי שליטה בשורה התחתונה
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(46, ArcadeGUI.createItem(Material.ARROW, "§e§l⬆ למעלה", List.of()));
        inventory.setItem(48, ArcadeGUI.createItem(Material.ARROW, "§e§l⬅ שמאלה", List.of()));
        inventory.setItem(49, ArcadeGUI.createItem(Material.CLOCK, "§6ניקוד: §f" + score, List.of(powerTicks > 0 ? "§bמצב כוח פעיל!" : "§7אסוף נקודות")));
        inventory.setItem(50, ArcadeGUI.createItem(Material.ARROW, "§e§lימינה ➡", List.of()));
        inventory.setItem(52, ArcadeGUI.createItem(Material.ARROW, "§e§l⬇ למטה", List.of()));
    }
}

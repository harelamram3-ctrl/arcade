package me.badpanda.pandaarcade.multiplayer;

import me.badpanda.pandaarcade.ArcadeGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class ConnectFourGame extends MultiplayerGame {

    private final Inventory inv;
    // לוח 6 שורות (0..5) על 7 עמודות (0..6, ממוקם בעמודות 1..7 של ה-GUI)
    private final int[][] grid = new int[6][7];
    private boolean p1Turn = true;

    public ConnectFourGame(Player p1, Player p2) {
        super(p1, p2, "connect4");
        this.inv = Bukkit.createInventory(null, 54, Component.text("🔴🟡 4 בשורה 1v1"));
    }

    @Override
    public boolean isGameInventory(Inventory inventory) {
        return inventory.equals(inv);
    }

    @Override
    public void start() {
        render();
        p1.openInventory(inv);
        p2.openInventory(inv);
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (isOver) return;
        if (p1Turn && !player.equals(p1)) return;
        if (!p1Turn && !player.equals(p2)) return;

        int col = (slot % 9) - 1;
        if (col < 0 || col > 6) return;

        // מציאת השורה הפנויה הנמוכה ביותר בעמודה
        int targetRow = -1;
        for (int r = 5; r >= 0; r--) {
            if (grid[r][col] == 0) {
                targetRow = r;
                break;
            }
        }
        if (targetRow == -1) return; // עמודה מלאה

        int piece = p1Turn ? 1 : 2;
        grid[targetRow][col] = piece;

        p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, p1Turn ? 1.5f : 1.0f);
        p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, p1Turn ? 1.5f : 1.0f);

        if (checkWin(piece)) {
            render();
            Player winner = p1Turn ? p1 : p2;
            endMatch(winner, winner.getName() + " יצר רצף של 4 וניצח!");
            return;
        }

        if (isFull()) {
            render();
            endMatch(null, "תיקו! הלוח התמלא.");
            return;
        }

        p1Turn = !p1Turn;
        render();
    }

    private boolean checkWin(int p) {
        // אופקי
        for (int r = 0; r < 6; r++)
            for (int c = 0; c < 4; c++)
                if (grid[r][c] == p && grid[r][c+1] == p && grid[r][c+2] == p && grid[r][c+3] == p) return true;
        // אנכי
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 7; c++)
                if (grid[r][c] == p && grid[r+1][c] == p && grid[r+2][c] == p && grid[r+3][c] == p) return true;
        // אלכסון יורד
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 4; c++)
                if (grid[r][c] == p && grid[r+1][c+1] == p && grid[r+2][c+2] == p && grid[r+3][c+3] == p) return true;
        // אלכסון עולה
        for (int r = 3; r < 6; r++)
            for (int c = 0; c < 4; c++)
                if (grid[r][c] == p && grid[r-1][c+1] == p && grid[r-2][c+2] == p && grid[r-3][c+3] == p) return true;
        return false;
    }

    private boolean isFull() {
        for (int c = 0; c < 7; c++) if (grid[0][c] == 0) return false;
        return true;
    }

    private void render() {
        for (int r = 0; r < 6; r++) {
            inv.setItem(r * 9, ArcadeGUI.createItem(Material.BLUE_STAINED_GLASS_PANE, " ", List.of()));
            inv.setItem(r * 9 + 8, ArcadeGUI.createItem(Material.BLUE_STAINED_GLASS_PANE, " ", List.of()));
            for (int c = 0; c < 7; c++) {
                int slot = r * 9 + (c + 1);
                int val = grid[r][c];
                if (val == 0) {
                    inv.setItem(slot, ArcadeGUI.createItem(Material.WHITE_STAINED_GLASS_PANE, "§7עמודה " + (c + 1), List.of("§eלחץ כדי להפיל אסימון!")));
                } else if (val == 1) {
                    inv.setItem(slot, ArcadeGUI.createItem(Material.RED_CONCRETE, "§c§lאדום (" + p1.getName() + ")", List.of()));
                } else {
                    inv.setItem(slot, ArcadeGUI.createItem(Material.YELLOW_CONCRETE, "§e§lצהוב (" + p2.getName() + ")", List.of()));
                }
            }
        }
        Player current = p1Turn ? p1 : p2;
        inv.setItem(0, ArcadeGUI.createItem(Material.CLOCK, "§6תור: §e" + current.getName(), List.of("§cאדום: " + p1.getName(), "§eצהוב: " + p2.getName())));
    }
}

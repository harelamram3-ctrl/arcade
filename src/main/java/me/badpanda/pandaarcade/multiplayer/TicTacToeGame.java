package me.badpanda.pandaarcade.multiplayer;

import me.badpanda.pandaarcade.ArcadeGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class TicTacToeGame extends MultiplayerGame {

    private final Inventory inv;
    private final int[] board = new int[9]; // 0 = ריק, 1 = X (p1), 2 = O (p2)
    private boolean p1Turn = true;
    private final int[] boardSlots = {12, 13, 14, 21, 22, 23, 30, 31, 32};

    public TicTacToeGame(Player p1, Player p2) {
        super(p1, p2, "ttt");
        this.inv = Bukkit.createInventory(null, 45, Component.text("❌⭕ איקס עיגול 1v1"));
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

        int index = -1;
        for (int i = 0; i < boardSlots.length; i++) {
            if (boardSlots[i] == slot) {
                index = i;
                break;
            }
        }
        if (index == -1 || board[index] != 0) return;

        board[index] = p1Turn ? 1 : 2;
        p1.playSound(p1.getLocation(), Sound.UI_BUTTON_CLICK, 1f, p1Turn ? 1.5f : 1.0f);
        p2.playSound(p2.getLocation(), Sound.UI_BUTTON_CLICK, 1f, p1Turn ? 1.5f : 1.0f);

        int winner = checkWinner();
        if (winner == 1) {
            render();
            endMatch(p1, p1.getName() + " (X) ניצח באיקס עיגול!");
            return;
        } else if (winner == 2) {
            render();
            endMatch(p2, p2.getName() + " (O) ניצח באיקס עיגול!");
            return;
        } else if (isBoardFull()) {
            render();
            endMatch(null, "תיקו! הלוח התמלא.");
            return;
        }

        p1Turn = !p1Turn;
        render();
    }

    private int checkWinner() {
        int[][] lines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] l : lines) {
            if (board[l[0]] != 0 && board[l[0]] == board[l[1]] && board[l[1]] == board[l[2]]) {
                return board[l[0]];
            }
        }
        return 0;
    }

    private boolean isBoardFull() {
        for (int cell : board) {
            if (cell == 0) return false;
        }
        return true;
    }

    private void render() {
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, ArcadeGUI.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }

        for (int i = 0; i < 9; i++) {
            int s = boardSlots[i];
            if (board[i] == 0) {
                inv.setItem(s, ArcadeGUI.createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§7משבצת פנויה", List.of("§eלחץ כדי לסמן!")));
            } else if (board[i] == 1) {
                inv.setItem(s, ArcadeGUI.createItem(Material.RED_CONCRETE, "§c§lX (" + p1.getName() + ")", List.of()));
            } else {
                inv.setItem(s, ArcadeGUI.createItem(Material.BLUE_CONCRETE, "§b§lO (" + p2.getName() + ")", List.of()));
            }
        }

        Player current = p1Turn ? p1 : p2;
        inv.setItem(4, ArcadeGUI.createItem(Material.CLOCK, "§6תורו של: §e" + current.getName(),
                List.of("§cX: §f" + p1.getName(), "§bO: §f" + p2.getName())));
    }
}

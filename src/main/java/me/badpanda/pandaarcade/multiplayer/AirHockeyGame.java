package me.badpanda.pandaarcade.multiplayer;

import me.badpanda.pandaarcade.ArcadeGUI;
import me.badpanda.pandaarcade.PandaArcade;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Random;

public class AirHockeyGame extends MultiplayerGame {

    private final Inventory inv1;
    private final Inventory inv2;

    // לוח 5 שורות (0..4) על 9 עמודות (0..8). עמודה 0 = שער של P1, עמודה 8 = שער של P2
    private int p1Row = 2; // שחקן 1 שולט במחבט בעמודה 1
    private int p2Row = 2; // שחקן 2 שולט במחבט בעמודה 7

    private int puckRow = 2;
    private int puckCol = 4;
    private int dRow = 1;
    private int dCol = 1;

    private int score1 = 0;
    private int score2 = 0;
    private final Random random = new Random();

    public AirHockeyGame(Player p1, Player p2) {
        super(p1, p2, "airhockey");
        this.inv1 = Bukkit.createInventory(null, 54, Component.text("🏒 הוקי אוויר | אתה כחול (שמאל)"));
        this.inv2 = Bukkit.createInventory(null, 54, Component.text("🏒 הוקי אוויר | אתה אדום (ימין)"));
    }

    @Override
    public boolean isGameInventory(Inventory inv) {
        return inv.equals(inv1) || inv.equals(inv2);
    }

    @Override
    public void start() {
        p1.openInventory(inv1);
        p2.openInventory(inv2);
        resetPuck(random.nextBoolean() ? 1 : -1);
        render();
        this.task = Bukkit.getScheduler().runTaskTimer(PandaArcade.getInstance(), this::tick, 4L, 4L);
    }

    private void resetPuck(int dirCol) {
        puckRow = 2;
        puckCol = 4;
        dCol = dirCol;
        dRow = random.nextBoolean() ? 1 : -1;
    }

    private void tick() {
        if (isOver) return;

        int nextRow = puckRow + dRow;
        int nextCol = puckCol + dCol;

        // פגיעה בקיר עליון או תחתון
        if (nextRow < 0 || nextRow > 4) {
            dRow = -dRow;
            nextRow = puckRow + dRow;
            playBoth(Sound.BLOCK_NOTE_BLOCK_HAT, 1.5f);
        }

        // בדיקת פגיעה במחבט של שחקן 1 (עמודה 1)
        if (nextCol == 1 && Math.abs(nextRow - p1Row) <= 1) {
            dCol = 1;
            dRow = Integer.compare(nextRow, p1Row);
            if (dRow == 0) dRow = random.nextBoolean() ? 1 : -1;
            nextCol = 2;
            playBoth(Sound.ENTITY_ITEM_PICKUP, 1.2f);
        }
        // בדיקת פגיעה במחבט של שחקן 2 (עמודה 7)
        else if (nextCol == 7 && Math.abs(nextRow - p2Row) <= 1) {
            dCol = -1;
            dRow = Integer.compare(nextRow, p2Row);
            if (dRow == 0) dRow = random.nextBoolean() ? 1 : -1;
            nextCol = 6;
            playBoth(Sound.ENTITY_ITEM_PICKUP, 1.2f);
        }

        // בדיקת גול!
        if (nextCol <= 0) {
            score2++;
            playBoth(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.2f);
            if (score2 >= 5) {
                endMatch(p2, p2.getName() + " ניצח 5-" + score1 + " בהוקי אוויר!");
                return;
            }
            resetPuck(1);
        } else if (nextCol >= 8) {
            score1++;
            playBoth(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.2f);
            if (score1 >= 5) {
                endMatch(p1, p1.getName() + " ניצח 5-" + score2 + " בהוקי אוויר!");
                return;
            }
            resetPuck(-1);
        } else {
            puckRow = nextRow;
            puckCol = nextCol;
        }

        render();
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (isOver) return;
        if (player.equals(p1)) {
            if (slot == 47 && p1Row > 0) p1Row--;
            else if (slot == 51 && p1Row < 4) p1Row++;
        } else if (player.equals(p2)) {
            if (slot == 47 && p2Row > 0) p2Row--;
            else if (slot == 51 && p2Row < 4) p2Row++;
        }
        render();
    }

    private void playBoth(Sound sound, float pitch) {
        p1.playSound(p1.getLocation(), sound, 0.7f, pitch);
        p2.playSound(p2.getLocation(), sound, 0.7f, pitch);
    }

    private void render() {
        renderFor(inv1, true);
        renderFor(inv2, false);
    }

    private void renderFor(Inventory inv, boolean isFirst) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                int slot = r * 9 + c;
                if (c == 0) {
                    inv.setItem(slot, ArcadeGUI.createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§bשער כחול (" + p1.getName() + ")", List.of()));
                } else if (c == 8) {
                    inv.setItem(slot, ArcadeGUI.createItem(Material.RED_STAINED_GLASS_PANE, "§cשער אדום (" + p2.getName() + ")", List.of()));
                } else if (c == 4) {
                    inv.setItem(slot, ArcadeGUI.createItem(Material.WHITE_STAINED_GLASS_PANE, "§7קו אמצע", List.of()));
                } else {
                    inv.setItem(slot, ArcadeGUI.createItem(Material.ICE, " ", List.of()));
                }
            }
        }

        // מחבט 1 (כחול)
        inv.setItem(p1Row * 9 + 1, ArcadeGUI.createItem(Material.BLUE_CONCRETE, "§b§lמחבט של " + p1.getName(), List.of()));
        // מחבט 2 (אדום)
        inv.setItem(p2Row * 9 + 7, ArcadeGUI.createItem(Material.RED_CONCRETE, "§c§lמחבט של " + p2.getName(), List.of()));
        // דיסקית
        inv.setItem(puckRow * 9 + puckCol, ArcadeGUI.createItem(Material.MAGMA_CREAM, "§6§lדיסקית הוקי", List.of()));

        // שורת שליטה
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, ArcadeGUI.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inv.setItem(47, ArcadeGUI.createItem(Material.ARROW, "§e§l⬆ הזז מחבט למעלה", List.of()));
        inv.setItem(49, ArcadeGUI.createItem(Material.NETHER_STAR,
                "§b" + p1.getName() + ": §f" + score1 + " §7| §c" + p2.getName() + ": §f" + score2,
                List.of("§7ראשון ל-5 גולים מנצח!")));
        inv.setItem(51, ArcadeGUI.createItem(Material.ARROW, "§e§l⬇ הזז מחבט למטה", List.of()));
    }
}

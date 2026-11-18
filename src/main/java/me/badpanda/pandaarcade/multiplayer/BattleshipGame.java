package me.badpanda.pandaarcade.multiplayer;

import me.badpanda.pandaarcade.ArcadeGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BattleshipGame extends MultiplayerGame {

    private final Inventory inv1;
    private final Inventory inv2;

    // 25 משבצות (5x5 במרכז המסך)
    private final int[] gridSlots = {
            2, 3, 4, 5, 6,
            11, 12, 13, 14, 15,
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33,
            38, 39, 40, 41, 42
    };

    private final Set<Integer> p1Ships = new HashSet<>();
    private final Set<Integer> p2Ships = new HashSet<>();
    private final Set<Integer> p1Shots = new HashSet<>(); // יריות של P1 על P2
    private final Set<Integer> p2Shots = new HashSet<>(); // יריות של P2 על P1

    private boolean p1Turn = true;
    private int p1Hits = 0;
    private int p2Hits = 0;

    public BattleshipGame(Player p1, Player p2) {
        super(p1, p2, "ships");
        this.inv1 = Bukkit.createInventory(null, 54, Component.text("🚢 קרב צוללות | " + p1.getName()));
        this.inv2 = Bukkit.createInventory(null, 54, Component.text("🚢 קרב צוללות | " + p2.getName()));
        placeRandomShips(p1Ships);
        placeRandomShips(p2Ships);
    }

    private void placeRandomShips(Set<Integer> ships) {
        Random r = new Random();
        while (ships.size() < 5) {
            ships.add(r.nextInt(25));
        }
    }

    @Override
    public boolean isGameInventory(Inventory inv) {
        return inv.equals(inv1) || inv.equals(inv2);
    }

    @Override
    public void start() {
        render();
        p1.openInventory(inv1);
        p2.openInventory(inv2);
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (isOver) return;
        if (p1Turn && !player.equals(p1)) return;
        if (!p1Turn && !player.equals(p2)) return;

        int idx = -1;
        for (int i = 0; i < gridSlots.length; i++) {
            if (gridSlots[i] == slot) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return;

        if (p1Turn) {
            if (p1Shots.contains(idx)) return;
            p1Shots.add(idx);
            if (p2Ships.contains(idx)) {
                p1Hits++;
                playBoth(Sound.ENTITY_GENERIC_EXPLODE, 1.3f);
                if (p1Hits >= 5) {
                    render();
                    endMatch(p1, p1.getName() + " הטביע את כל 5 הצוללות של " + p2.getName() + "!");
                    return;
                }
            } else {
                playBoth(Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.2f);
            }
        } else {
            if (p2Shots.contains(idx)) return;
            p2Shots.add(idx);
            if (p1Ships.contains(idx)) {
                p2Hits++;
                playBoth(Sound.ENTITY_GENERIC_EXPLODE, 1.3f);
                if (p2Hits >= 5) {
                    render();
                    endMatch(p2, p2.getName() + " הטביע את כל 5 הצוללות של " + p1.getName() + "!");
                    return;
                }
            } else {
                playBoth(Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.2f);
            }
        }

        p1Turn = !p1Turn;
        render();
    }

    private void playBoth(Sound s, float pitch) {
        p1.playSound(p1.getLocation(), s, 0.8f, pitch);
        p2.playSound(p2.getLocation(), s, 0.8f, pitch);
    }

    private void render() {
        renderBoard(inv1, p1Shots, p2Ships, p1Turn, p1Hits, p2Hits);
        renderBoard(inv2, p2Shots, p1Ships, !p1Turn, p2Hits, p1Hits);
    }

    private void renderBoard(Inventory inv, Set<Integer> myShots, Set<Integer> enemyShips, boolean myTurn, int myHits, int enemyHits) {
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, ArcadeGUI.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }

        for (int i = 0; i < 25; i++) {
            int s = gridSlots[i];
            if (!myShots.contains(i)) {
                inv.setItem(s, ArcadeGUI.createItem(Material.BLUE_STAINED_GLASS_PANE, "§9מימי האוקיינוס",
                        List.of(myTurn ? "§eלחץ כדי לשגר טיל!" : "§7המתן לתור היריב...")));
            } else if (enemyShips.contains(i)) {
                inv.setItem(s, ArcadeGUI.createItem(Material.TNT, "§c§l💥 פגיעה ישירה! צוללת הוטבעה", List.of()));
            } else {
                inv.setItem(s, ArcadeGUI.createItem(Material.WHITE_STAINED_GLASS_PANE, "§7החטאה (מים ריקים)", List.of()));
            }
        }

        inv.setItem(49, ArcadeGUI.createItem(Material.COMPASS,
                myTurn ? "§a§lתורך לתקוף!" : "§c§lתור היריב...",
                List.of("§eצוללות שהטבעת: §a" + myHits + "/5",
                        "§eצוללות שהיריב הטביע לך: §c" + enemyHits + "/5")));
    }
}

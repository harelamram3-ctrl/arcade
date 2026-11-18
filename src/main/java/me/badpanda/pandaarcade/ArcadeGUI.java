package me.badpanda.pandaarcade;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArcadeGUI {

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("🕹 PANDAVERSE ARCADE", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        fillBackground(inv);

        LeaderboardManager lb = PandaArcade.getInstance().getLeaderboardManager();

        inv.setItem(11, createItem(Material.FIRE_CHARGE, "§c§lSpace Shooter (יריות חלל)",
                List.of("§7השמד חלליות אויב והתחמק מאש!",
                        "§7שליטה: קליק שמאלי לתזוזה וירי.",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("shooter", player.getName()),
                        "§a▶ לחץ כדי לשחק!")));

        inv.setItem(13, createItem(Material.YELLOW_CONCRETE, "§e§lPac-Man (פקמן)",
                List.of("§7אסוף את כל הנקודות וברח מהרוחות!",
                        "§7אכול כדור כוח כדי לצוד את הרוחות.",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("pacman", player.getName()),
                        "§a▶ לחץ כדי לשחק!")));

        inv.setItem(15, createItem(Material.LIME_CONCRETE, "§a§lSnake (סנייק)",
                List.of("§7אכול תפוחים וגדל מבלי להתנגש בעצמך!",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("snake", player.getName()),
                        "§a▶ לחץ כדי לשחק!")));

        inv.setItem(22, createItem(Material.NETHER_STAR, "§6§lטבלת שיאים (LEADERBOARD)",
                List.of("§7צפה ב-5 השחקנים הטובים ביותר בכל משחק!")));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        player.openInventory(inv);
    }

    public static void openLeaderboardMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("🏆 ARCADE LEADERBOARD", NamedTextColor.GOLD, TextDecoration.BOLD));
        fillBackground(inv);

        inv.setItem(11, createLeaderboardItem(Material.FIRE_CHARGE, "§c§lSpace Shooter - טופ 5", "shooter"));
        inv.setItem(13, createLeaderboardItem(Material.YELLOW_CONCRETE, "§e§lPac-Man - טופ 5", "pacman"));
        inv.setItem(15, createLeaderboardItem(Material.LIME_CONCRETE, "§a§lSnake - טופ 5", "snake"));

        inv.setItem(22, createItem(Material.BARRIER, "§cחזרה לתפריט הראשי", List.of()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        player.openInventory(inv);
    }

    private static ItemStack createLeaderboardItem(Material mat, String title, String gameId) {
        List<Map.Entry<String, Integer>> top = PandaArcade.getInstance().getLeaderboardManager().getTopScores(gameId, 5);
        List<String> lore = new ArrayList<>();
        lore.add("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        if (top.isEmpty()) {
            lore.add("§7אין עדיין שיאים רשומים.");
        } else {
            String[] medals = {"§6#1 ", "§f#2 ", "§c#3 ", "§7#4 ", "§7#5 "};
            for (int i = 0; i < top.size(); i++) {
                Map.Entry<String, Integer> entry = top.get(i);
                lore.add(medals[i] + "§e" + entry.getKey() + " §8- §a" + entry.getValue() + " נק'");
            }
        }
        lore.add("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        return createItem(mat, title, lore);
    }

    public static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void fillBackground(Inventory inv) {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }
    }
}

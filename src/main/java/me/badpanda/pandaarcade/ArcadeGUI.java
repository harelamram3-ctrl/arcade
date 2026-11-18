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
        Inventory inv = Bukkit.createInventory(null, 45, Component.text("🕹 PANDAVERSE ARCADE", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        fillBackground(inv);

        LeaderboardManager lb = PandaArcade.getInstance().getLeaderboardManager();

        inv.setItem(11, createItem(Material.FIRE_CHARGE, "§c§lSpace Shooter (יריות חלל)",
                List.of("§7השמד גלי חלליות אויב ובוסים!",
                        "§7כולל שדרוגי נשק ולייזר כפול.",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("shooter", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(12, createItem(Material.YELLOW_CONCRETE, "§e§lPac-Man (פקמן)",
                List.of("§7אסוף נקודות במבוך וברח מהרוחות!",
                        "§7אכול כדור כוח כדי לצוד את הרוחות.",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("pacman", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(13, createItem(Material.LIME_CONCRETE, "§a§lSnake (סנייק)",
                List.of("§7אכול תפוחים רגילים ותפוחי זהב!",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("snake", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(14, createItem(Material.MACE, "§6§lWhack-A-Mole (הכה בחפרפרת)",
                List.of("§7לחץ מהר על החפרפרות לפני שייעלמו!",
                        "§cזהירות מפצצות TNT!",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("mole", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(15, createItem(Material.RED_CONCRETE, "§c§lSuper Mario Run (מריו ראנר)",
                List.of("§7קפוץ מעל צינורות, אסוף מטבעות",
                        "§7ושבור בלוקי שאלה [?]!",
                        "",
                        "§eהשיא שלך: §f" + lb.getHighScore("mario", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        // שורה תחתונה - מולטיפלייר, לידרבורד ועזרה
        inv.setItem(29, createItem(Material.DIAMOND_SWORD, "§b§l⚔ משחקי 1v1 וזוגות (MULTIPLAYER)",
                List.of("§7שחק ראש בראש מול שחקן אחר בשרת!",
                        "§8• §fהוקי אוויר (Air Hockey)",
                        "§8• §fאיקס עיגול (Tic-Tac-Toe)",
                        "§8• §fקרב צוללות (Battleship)",
                        "",
                        "§b▶ לחץ לבחירת משחק 1v1!")));

        inv.setItem(31, createItem(Material.NETHER_STAR, "§6§l🏆 טבלת שיאים (LEADERBOARD)",
                List.of("§7צפה במובילי השיאים בכל המשחקים",
                        "§7ובאלופי הניצחונות של 1v1!")));

        inv.setItem(33, createItem(Material.BARRIER, "§d§l🧱 הגדרת מכונות בעולם",
                List.of("§7איך לחבר מכונת ארקייד לבלוק Barrier?",
                        "§e/arcade setmachine <משחק>",
                        "§7לחץ לפרטים מלאים!")));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        player.openInventory(inv);
    }

    public static void openMultiplayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("⚔ משחקי 1V1 וזוגות", NamedTextColor.AQUA, TextDecoration.BOLD));
        fillBackground(inv);

        LeaderboardManager lb = PandaArcade.getInstance().getLeaderboardManager();

        inv.setItem(11, createItem(Material.PACKED_ICE, "§b§l🏒 הוקי אוויר (Air Hockey 1v1)",
                List.of("§7משחק ארקייד מהיר בזמן אמת!",
                        "§7הזז את המחבט שלך למעלה ולמטה",
                        "§7והבקע 5 שערים ליריב כדי לנצח!",
                        "",
                        "§eהניצחונות שלך: §f" + lb.getHighScore("airhockey", player.getName()),
                        "§a▶ לחץ להצטרפות לתור!")));

        inv.setItem(13, createItem(Material.NETHER_STAR, "§e§l❌⭕ איקס עיגול (Tic-Tac-Toe 1v1)",
                List.of("§7קרב מוחות קלאסי 3x3 בתורות.",
                        "§7צור רצף של 3 כדי לנצח!",
                        "",
                        "§eהניצחונות שלך: §f" + lb.getHighScore("ttt", player.getName()),
                        "§a▶ לחץ להצטרפות לתור!")));

        inv.setItem(15, createItem(Material.SPYGLASS, "§9§l🚢 קרב צוללות (Battleship 1v1)",
                List.of("§7מצא והטבע את 5 הצוללות של היריב",
                        "§7לפני שהוא מטביע את שלך!",
                        "",
                        "§eהניצחונות שלך: §f" + lb.getHighScore("ships", player.getName()),
                        "§a▶ לחץ להצטרפות לתור!")));

        inv.setItem(22, createItem(Material.BARRIER, "§cחזרה לתפריט הראשי", List.of()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.3f);
        player.openInventory(inv);
    }

    public static void openLeaderboardMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, Component.text("🏆 ARCADE LEADERBOARD", NamedTextColor.GOLD, TextDecoration.BOLD));
        fillBackground(inv);

        // שורה 1: משחקי סולו
        inv.setItem(11, createLeaderboardItem(Material.FIRE_CHARGE, "§c§lSpace Shooter - טופ 5", "shooter", "נק'"));
        inv.setItem(12, createLeaderboardItem(Material.YELLOW_CONCRETE, "§e§lPac-Man - טופ 5", "pacman", "נק'"));
        inv.setItem(13, createLeaderboardItem(Material.LIME_CONCRETE, "§a§lSnake - טופ 5", "snake", "נק'"));
        inv.setItem(14, createLeaderboardItem(Material.MACE, "§6§lהכה בחפרפרת - טופ 5", "mole", "נק'"));
        inv.setItem(15, createLeaderboardItem(Material.RED_CONCRETE, "§c§lמריו ראנר - טופ 5", "mario", "נק'"));

        // שורה 2: אלופי 1v1
        inv.setItem(29, createLeaderboardItem(Material.PACKED_ICE, "§b§lהוקי אוויר 1v1 - אלופים", "airhockey", "ניצחונות"));
        inv.setItem(31, createLeaderboardItem(Material.NETHER_STAR, "§e§lאיקס עיגול 1v1 - אלופים", "ttt", "ניצחונות"));
        inv.setItem(33, createLeaderboardItem(Material.SPYGLASS, "§9§lקרב צוללות 1v1 - אלופים", "ships", "ניצחונות"));

        inv.setItem(40, createItem(Material.BARRIER, "§cחזרה לתפריט הראשי", List.of()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        player.openInventory(inv);
    }

    public static void openSetupHelpMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("🧱 הגדרת מכונות ארקייד", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        fillBackground(inv);

        inv.setItem(13, createItem(Material.BARRIER, "§d§lמדריך חיבור בלוק Barrier למכונה",
                List.of("§71. הנח בלוק §fBarrier §7בתוך מודל מכונת הארקייד שלך.",
                        "§72. הסתכל על ה-Barrier והקלד:",
                        "§e   /arcade setmachine <סוג>",
                        "",
                        "§6סוגים זמינים:",
                        "§fmenu §7(תפריט ראשי), §fshooter, pacman, snake,",
                        "§fmole, mario, airhockey, ttt, ships",
                        "",
                        "§7למחיקה: §c/arcade delmachine")));

        inv.setItem(22, createItem(Material.ARROW, "§cחזרה לתפריט הראשי", List.of()));
        player.openInventory(inv);
    }

    private static ItemStack createLeaderboardItem(Material mat, String title, String gameId, String suffix) {
        List<Map.Entry<String, Integer>> top = PandaArcade.getInstance().getLeaderboardManager().getTopScores(gameId, 5);
        List<String> lore = new ArrayList<>();
        lore.add("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        if (top.isEmpty()) {
            lore.add("§7אין עדיין שיאים רשומים.");
        } else {
            String[] medals = {"§6#1 ", "§f#2 ", "§c#3 ", "§7#4 ", "§7#5 "};
            for (int i = 0; i < top.size(); i++) {
                Map.Entry<String, Integer> entry = top.get(i);
                lore.add(medals[i] + "§e" + entry.getKey() + " §8- §a" + entry.getValue() + " " + suffix);
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

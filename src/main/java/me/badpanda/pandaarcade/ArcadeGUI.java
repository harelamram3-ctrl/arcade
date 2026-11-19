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

        inv.setItem(10, createItem(Material.FIRE_CHARGE, "§c§lSpace Shooter (יריות חלל)",
                List.of("§7השמד גלי חלליות אויב ואסוף שדרוגים!",
                        "§eהשיא שלך: §f" + lb.getHighScore("shooter", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(11, createItem(Material.YELLOW_CONCRETE, "§e§lPac-Man (פקמן)",
                List.of("§7אסוף נקודות במבוך וברח מהרוחות!",
                        "§eהשיא שלך: §f" + lb.getHighScore("pacman", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(12, createItem(Material.LIME_CONCRETE, "§a§lSnake (סנייק)",
                List.of("§7אכול תפוחים רגילים ותפוחי זהב!",
                        "§eהשיא שלך: §f" + lb.getHighScore("snake", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(13, createItem(Material.MACE, "§6§lWhack-A-Mole (הכה בחפרפרת)",
                List.of("§7לחץ מהר על החפרפרות והיזהר מ-TNT!",
                        "§eהשיא שלך: §f" + lb.getHighScore("mole", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(14, createItem(Material.RED_CONCRETE, "§c§lSuper Mario Run (מריו ראנר)",
                List.of("§7קפוץ מעל צינורות ושבור בלוקי שאלה [?]!",
                        "§eהשיא שלך: §f" + lb.getHighScore("mario", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(15, createItem(Material.FEATHER, "§e§lFlappy Panda (פלאפי פנדה)",
                List.of("§7עוף בין עמודי הבמבוק מבלי להתנגש!",
                        "§eהשיא שלך: §f" + lb.getHighScore("flappy", player.getName()),
                        "§a▶ לחץ למשחק סולו!")));

        inv.setItem(16, createItem(Material.ENDER_PEARL, "§d§l🌍 שיגור לעולם הארקייד (MV)",
                List.of("§7השתגר ישירות לאולם המכונות",
                        "§7ולזירות הקרב בעולם Multiverse!",
                        "§d▶ לחץ לשיגור!")));

        inv.setItem(29, createItem(Material.DIAMOND_SWORD, "§b§l⚔ משחקי 1v1 (מכונות + זירות בעולם!)",
                List.of("§7שחק ראש בראש מול שחקן אחר:",
                        "§8• §fמכונות: הוקי אוויר, 4 בשורה, צוללות, X-O",
                        "§8• §eזירות MV: סומו, ספליף, דו-קרב, פצצה!",
                        "",
                        "§b▶ לחץ לבחירת קרב 1v1!")));

        inv.setItem(31, createItem(Material.NETHER_STAR, "§6§l🏆 טבלת שיאים (LEADERBOARD)",
                List.of("§7צפה במובילי השיאים בכל המשחקים",
                        "§7ובאלופי הניצחונות של ה-1v1 והזירות!")));

        inv.setItem(33, createItem(Material.BARRIER, "§d§l🧱 הגדרת עולם MV ומכונות",
                List.of("§7פקודות לאדמינים לבניית מפות אוטומטית",
                        "§7והגדרת מכונות על בלוקי Barrier!")));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        player.openInventory(inv);
    }

    public static void openMultiplayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, Component.text("⚔ משחקי 1V1 וזירות בעולם MV", NamedTextColor.AQUA, TextDecoration.BOLD));
        fillBackground(inv);

        LeaderboardManager lb = PandaArcade.getInstance().getLeaderboardManager();

        // שורה 1: מכונות GUI 1v1
        inv.setItem(10, createItem(Material.PACKED_ICE, "§b§l🏒 הוקי אוויר (מכונה 1v1)",
                List.of("§7משחק ארקייד מהיר בזמן אמת!",
                        "§eניצחונות: §f" + lb.getHighScore("airhockey", player.getName()),
                        "§a▶ לחץ להצטרפות!")));

        inv.setItem(12, createItem(Material.NETHER_STAR, "§e§l❌⭕ איקס עיגול (מכונה 1v1)",
                List.of("§7קרב מוחות קלאסי 3x3.",
                        "§eניצחונות: §f" + lb.getHighScore("ttt", player.getName()),
                        "§a▶ לחץ להצטרפות!")));

        inv.setItem(14, createItem(Material.SPYGLASS, "§9§l🚢 קרב צוללות (מכונה 1v1)",
                List.of("§7הטבע את 5 הצוללות של היריב!",
                        "§eניצחונות: §f" + lb.getHighScore("ships", player.getName()),
                        "§a▶ לחץ להצטרפות!")));

        inv.setItem(16, createItem(Material.RED_TERRACOTTA, "§c§l🔴🟡 4 בשורה (מכונה 1v1)",
                List.of("§7הפל אסימונים וצור רצף של 4!",
                        "§eניצחונות: §f" + lb.getHighScore("connect4", player.getName()),
                        "§a▶ לחץ להצטרפות!")));

        // שורה 2: זירות קרב פיזיות בעולם MV!
        inv.setItem(28, createItem(Material.SLIME_BALL, "§a§l🥋 סומו 1v1 (זירה בעולם MV)",
                List.of("§7הדוף את היריב מחוץ לפלטפורמה!",
                        "§eניצחונות: §f" + lb.getHighScore("sumo", player.getName()),
                        "§a▶ לחץ לשיגור לזירה!")));

        inv.setItem(30, createItem(Material.DIAMOND_SHOVEL, "§b§l❄️ ספליף 1v1 (זירה בעולם MV)",
                List.of("§7חפור את השלג מתחת לרגלי היריב!",
                        "§eניצחונות: §f" + lb.getHighScore("spleef", player.getName()),
                        "§a▶ לחץ לשיגור לזירה!")));

        inv.setItem(32, createItem(Material.IRON_SWORD, "§c§l⚔️ דו-קרב אבירים (זירה בעולם MV)",
                List.of("§7קרב PvP מלא עם חרב, קשת ותפוחי זהב!",
                        "§eניצחונות: §f" + lb.getHighScore("duel", player.getName()),
                        "§a▶ לחץ לשיגור לזירה!")));

        inv.setItem(34, createItem(Material.TNT, "§4§l💣 פצצה מתקתקת (זירה בעולם MV)",
                List.of("§7העבר את ה-TNT ליריב לפני שיתפוצץ!",
                        "§eניצחונות: §f" + lb.getHighScore("tnttag", player.getName()),
                        "§a▶ לחץ לשיגור לזירה!")));

        inv.setItem(40, createItem(Material.BARRIER, "§cחזרה לתפריט הראשי", List.of()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.3f);
        player.openInventory(inv);
    }

    public static void openLeaderboardMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("🏆 ARCADE LEADERBOARD", NamedTextColor.GOLD, TextDecoration.BOLD));
        fillBackground(inv);

        // שורה 1: סולו
        inv.setItem(10, createLeaderboardItem(Material.FIRE_CHARGE, "§c§lSpace Shooter", "shooter", "נק'"));
        inv.setItem(11, createLeaderboardItem(Material.YELLOW_CONCRETE, "§e§lPac-Man", "pacman", "נק'"));
        inv.setItem(12, createLeaderboardItem(Material.LIME_CONCRETE, "§a§lSnake", "snake", "נק'"));
        inv.setItem(13, createLeaderboardItem(Material.MACE, "§6§lהכה בחפרפרת", "mole", "נק'"));
        inv.setItem(14, createLeaderboardItem(Material.RED_CONCRETE, "§c§lמריו ראנר", "mario", "נק'"));
        inv.setItem(15, createLeaderboardItem(Material.FEATHER, "§e§lFlappy Panda", "flappy", "נק'"));

        // שורה 2: מכונות 1v1
        inv.setItem(20, createLeaderboardItem(Material.PACKED_ICE, "§b§lהוקי אוויר 1v1", "airhockey", "ניצחונות"));
        inv.setItem(21, createLeaderboardItem(Material.NETHER_STAR, "§e§lאיקס עיגול 1v1", "ttt", "ניצחונות"));
        inv.setItem(22, createLeaderboardItem(Material.SPYGLASS, "§9§lקרב צוללות 1v1", "ships", "ניצחונות"));
        inv.setItem(23, createLeaderboardItem(Material.RED_TERRACOTTA, "§c§l4 בשורה 1v1", "connect4", "ניצחונות"));

        // שורה 3: אלופי זירות עולם MV
        inv.setItem(29, createLeaderboardItem(Material.SLIME_BALL, "§a§lאלופי סומו MV", "sumo", "ניצחונות"));
        inv.setItem(30, createLeaderboardItem(Material.DIAMOND_SHOVEL, "§b§lאלופי ספליף MV", "spleef", "ניצחונות"));
        inv.setItem(31, createLeaderboardItem(Material.IRON_SWORD, "§c§lאלופי דו-קרב MV", "duel", "ניצחונות"));
        inv.setItem(32, createLeaderboardItem(Material.TNT, "§4§lאלופי פצצה MV", "tnttag", "ניצחונות"));

        inv.setItem(49, createItem(Material.BARRIER, "§cחזרה לתפריט הראשי", List.of()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        player.openInventory(inv);
    }

    public static void openSetupHelpMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("🧱 הגדרת עולם MV ומכונות", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        fillBackground(inv);

        inv.setItem(11, createItem(Material.GRASS_BLOCK, "§a§l1. הגדרת עולם Multiverse ולובי",
                List.of("§7עמוד בעולם ה-MV שלך והקלד:",
                        "§e/arcade setlobby §7- שומר את ספאון הארקייד",
                        "",
                        "§6בניית מפות 1v1 אוטומטית בעולם MV:",
                        "§e/arcade buildmap sumo",
                        "§e/arcade buildmap spleef",
                        "§e/arcade buildmap duel",
                        "§e/arcade buildmap tnttag")));

        inv.setItem(15, createItem(Material.BARRIER, "§d§l2. חיבור מכונות לבלוק Barrier",
                List.of("§7הסתכל על בלוק Barrier והקלד:",
                        "§e/arcade setmachine <סוג>",
                        "",
                        "§7סוגים: §fmenu, shooter, pacman, snake,",
                        "§fmole, mario, flappy, airhockey, ttt,",
                        "§fships, connect4, sumo, spleef, duel, tnttag")));

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

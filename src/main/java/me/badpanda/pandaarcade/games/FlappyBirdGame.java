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

public class FlappyBirdGame extends ArcadeGame {

    private int birdRow = 2;
    private final int birdCol = 2;
    private final int[] pipeGapRow = new int[9]; // -1 = אין צינור בעמודה
    private final Random random = new Random();
    private int step = 0;

    public FlappyBirdGame(Player player) {
        super(player, "🐼 Flappy Panda | פלאפי פנדה", 54, "flappy");
        for (int i = 0; i < 9; i++) pipeGapRow[i] = -1;
    }

    @Override
    public void start() {
        player.openInventory(inventory);
        render();
        this.task = Bukkit.getScheduler().runTaskTimer(PandaArcade.getInstance(), this::tick, 6L, 6L);
    }

    private void tick() {
        if (isOver) return;
        step++;

        // כבידה: הציפור יורדת שורה אחת למטה
        birdRow++;
        if (birdRow > 4) {
            gameOver("נפלת לקרקע!");
            return;
        }

        // הזזת צינורות שמאלה
        for (int c = 0; c < 8; c++) {
            pipeGapRow[c] = pipeGapRow[c + 1];
        }
        if (step % 3 == 0) {
            pipeGapRow[8] = 1 + random.nextInt(3); // פתח בשורה 1, 2 או 3
        } else {
            pipeGapRow[8] = -1;
        }

        // בדיקת התנגשות בעמודה של הציפור
        int gap = pipeGapRow[birdCol];
        if (gap != -1) {
            if (birdRow != gap && birdRow != gap - 1) {
                gameOver("התנגשת בעמוד במבוק!");
                return;
            } else {
                score += 10;
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.6f);
            }
        }

        render();
    }

    @Override
    public void handleClick(int slot) {
        if (isOver) return;
        if (birdRow > 0) {
            birdRow--;
            player.playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 0.8f, 1.4f);
            render();
        }
    }

    private void render() {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                int slot = r * 9 + c;
                int gap = pipeGapRow[c];
                if (gap != -1 && r != gap && r != gap - 1) {
                    inventory.setItem(slot, ArcadeGUI.createItem(Material.BAMBOO_BLOCK, "§2§lעמוד במבוק", List.of("§cאל תתנגש!")));
                } else {
                    inventory.setItem(slot, ArcadeGUI.createItem(Material.CYAN_STAINED_GLASS_PANE, " ", List.of()));
                }
            }
        }

        inventory.setItem(birdRow * 9 + birdCol, ArcadeGUI.createItem(Material.PANDA_SPAWN_EGG, "§f§lפלאפי פנדה §7(ניקוד: " + score + ")", List.of()));

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, ArcadeGUI.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(49, ArcadeGUI.createItem(Material.FEATHER, "§e§l⬆ נפנף בכנפיים! (לחץ לעלות)", List.of("§7ניקוד נוכחי: §a" + score)));
    }
}

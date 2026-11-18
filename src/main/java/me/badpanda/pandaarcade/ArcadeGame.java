package me.badpanda.pandaarcade;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

public abstract class ArcadeGame {
    protected final Player player;
    protected final Inventory inventory;
    protected final String gameId;
    protected int score = 0;
    protected BukkitTask task;
    protected boolean isOver = false;

    public ArcadeGame(Player player, String title, int size, String gameId) {
        this.player = player;
        this.gameId = gameId;
        this.inventory = Bukkit.createInventory(null, size, Component.text(title));
    }

    public Inventory getInventory() {
        return inventory;
    }

    public abstract void start();
    public abstract void handleClick(int slot);

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    protected void gameOver(String reason) {
        if (isOver) return;
        isOver = true;
        stop();
        PandaArcade.getInstance().getLeaderboardManager().updateScore(gameId, player.getName(), score);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.7f, 1.2f);
        player.sendMessage(Component.text("=================================", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("המשחק הסתיים! ", NamedTextColor.RED)
                .append(Component.text(reason, NamedTextColor.GRAY)));
        player.sendMessage(Component.text("ניקוד סופי: ", NamedTextColor.YELLOW)
                .append(Component.text(score, NamedTextColor.GREEN)));
        player.sendMessage(Component.text("=================================", NamedTextColor.DARK_GRAY));
        Bukkit.getScheduler().runTaskLater(PandaArcade.getInstance(), () -> {
            if (player.isOnline()) ArcadeGUI.openMainMenu(player);
        }, 40L);
    }
}

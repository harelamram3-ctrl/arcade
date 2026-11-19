package me.badpanda.pandaarcade.multiplayer;

import me.badpanda.pandaarcade.ArcadeGUI;
import me.badpanda.pandaarcade.PandaArcade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

public abstract class MultiplayerGame {
    protected final Player p1;
    protected final Player p2;
    protected final String gameId;
    protected BukkitTask task;
    protected boolean isOver = false;

    public MultiplayerGame(Player p1, Player p2, String gameId) {
        this.p1 = p1;
        this.p2 = p2;
        this.gameId = gameId;
    }

    public abstract void start();
    public abstract void handleClick(Player player, int slot);
    public abstract boolean isGameInventory(Inventory inv);

    public void forceStop() {
        isOver = true;
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        PandaArcade.getInstance().unregisterMultiGame(p1, p2);
    }

    public void handleDisconnect(Player quitter) {
        if (isOver) return;
        Player winner = quitter.equals(p1) ? p2 : p1;
        endMatch(winner, quitter.getName() + " יצא מהמשחק!");
    }

    protected void endMatch(Player winner, String reason) {
        if (isOver) return;
        forceStop();

        if (winner != null) {
            PandaArcade.getInstance().getLeaderboardManager().addWin(gameId, winner.getName());
        }

        for (Player p : new Player[]{p1, p2}) {
            if (p != null && p.isOnline()) {
                p.playSound(p.getLocation(), p.equals(winner) ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_VILLAGER_NO, 1f, 1.1f);
                p.sendMessage(Component.text("=================================", NamedTextColor.DARK_GRAY));
                p.sendMessage(Component.text("סיום קרב 1v1! ", NamedTextColor.GOLD)
                        .append(Component.text(reason, NamedTextColor.GRAY)));
                if (winner != null) {
                    p.sendMessage(Component.text("🏆 המנצח: ", NamedTextColor.YELLOW)
                            .append(Component.text(winner.getName(), NamedTextColor.GREEN)));
                }
                p.sendMessage(Component.text("=================================", NamedTextColor.DARK_GRAY));

                Bukkit.getScheduler().runTaskLater(PandaArcade.getInstance(), () -> {
                    if (p.isOnline()) ArcadeGUI.openMultiplayerMenu(p);
                }, 40L);
            }
        }
    }
}

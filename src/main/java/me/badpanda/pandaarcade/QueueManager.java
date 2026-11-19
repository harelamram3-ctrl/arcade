package me.badpanda.pandaarcade;

import me.badpanda.pandaarcade.multiplayer.AirHockeyGame;
import me.badpanda.pandaarcade.multiplayer.BattleshipGame;
import me.badpanda.pandaarcade.multiplayer.ConnectFourGame;
import me.badpanda.pandaarcade.multiplayer.TicTacToeGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QueueManager {

    private final PandaArcade plugin;
    private final Map<String, UUID> waitingPlayers = new HashMap<>();

    public QueueManager(PandaArcade plugin) {
        this.plugin = plugin;
    }

    public void joinQueue(Player player, String gameId, String displayTitle) {
        removePlayerFromQueues(player);

        UUID opponentId = waitingPlayers.get(gameId);
        if (opponentId != null && !opponentId.equals(player.getUniqueId())) {
            Player opponent = Bukkit.getPlayer(opponentId);
            if (opponent != null && opponent.isOnline()) {
                waitingPlayers.remove(gameId);
                startMatch(gameId, opponent, player);
                return;
            }
        }

        waitingPlayers.put(gameId, player.getUniqueId());
        Inventory waitInv = Bukkit.createInventory(null, 27, Component.text("⏳ ממתין ליריב - " + displayTitle));
        for (int i = 0; i < 27; i++) {
            waitInv.setItem(i, ArcadeGUI.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        waitInv.setItem(13, ArcadeGUI.createItem(Material.CLOCK, "§e§lמחפש שחקן שני...",
                List.of("§7משחק: §f" + displayTitle,
                        "§7ברגע ששחקן נוסף ילחץ על המכונה",
                        "§7או יבחר במשחק, הקרב יתחיל מיד!")));
        waitInv.setItem(22, ArcadeGUI.createItem(Material.BARRIER, "§cביטול ויציאה מהתור", List.of()));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
        player.openInventory(waitInv);

        Bukkit.broadcast(Component.text("🕹 [Arcade] ", NamedTextColor.DARK_PURPLE)
                .append(Component.text(player.getName(), NamedTextColor.GOLD))
                .append(Component.text(" ממתין ליריב למשחק ", NamedTextColor.GRAY))
                .append(Component.text(displayTitle, NamedTextColor.AQUA))
                .append(Component.text("! הקלידו /arcade להצטרפות.", NamedTextColor.GRAY)));
    }

    public void removePlayerFromQueues(Player player) {
        waitingPlayers.entrySet().removeIf(entry -> entry.getValue().equals(player.getUniqueId()));
    }

    private void startMatch(String gameId, Player p1, Player p2) {
        p1.playSound(p1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
        p2.playSound(p2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);

        switch (gameId) {
            case "airhockey" -> plugin.registerMultiGame(p1, p2, new AirHockeyGame(p1, p2));
            case "ttt" -> plugin.registerMultiGame(p1, p2, new TicTacToeGame(p1, p2));
            case "ships" -> plugin.registerMultiGame(p1, p2, new BattleshipGame(p1, p2));
            case "connect4" -> plugin.registerMultiGame(p1, p2, new ConnectFourGame(p1, p2));
            case "sumo", "spleef", "duel", "tnttag" -> plugin.getWorldArenaManager().startWorldMatch(gameId, p1, p2);
        }
    }
}

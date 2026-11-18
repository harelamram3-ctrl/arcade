package me.badpanda.pandaarcade;

import me.badpanda.pandaarcade.games.PacmanGame;
import me.badpanda.pandaarcade.games.SnakeGame;
import me.badpanda.pandaarcade.games.SpaceShooterGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PandaArcade extends JavaPlugin implements CommandExecutor, Listener {

    private static PandaArcade instance;
    private LeaderboardManager leaderboardManager;
    private final Map<UUID, ArcadeGame> activeGames = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.leaderboardManager = new LeaderboardManager(this);
        if (getCommand("arcade") != null) {
            getCommand("arcade").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        activeGames.values().forEach(ArcadeGame::stop);
        activeGames.clear();
        leaderboardManager.save();
    }

    public static PandaArcade getInstance() {
        return instance;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public void startGame(Player player, ArcadeGame game) {
        stopGame(player);
        activeGames.put(player.getUniqueId(), game);
        game.start();
    }

    public void stopGame(Player player) {
        ArcadeGame game = activeGames.remove(player.getUniqueId());
        if (game != null) {
            game.stop();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("פקודה זו מיועדת לשחקנים בלבד.", NamedTextColor.RED));
            return true;
        }
        ArcadeGUI.openMainMenu(player);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // טיפול במשחק פעיל
        ArcadeGame activeGame = activeGames.get(player.getUniqueId());
        if (activeGame != null && event.getView().getTopInventory().equals(activeGame.getInventory())) {
            event.setCancelled(true);
            activeGame.handleClick(event.getRawSlot());
            return;
        }

        // טיפול בתפריט הראשי או לידרבורד
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.contains("ARCADE") || title.contains("LEADERBOARD")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (title.contains("ARCADE")) {
                if (slot == 11) startGame(player, new SpaceShooterGame(player));
                else if (slot == 13) startGame(player, new PacmanGame(player));
                else if (slot == 15) startGame(player, new SnakeGame(player));
                else if (slot == 22) ArcadeGUI.openLeaderboardMenu(player);
            } else if (title.contains("LEADERBOARD")) {
                if (slot == 22) ArcadeGUI.openMainMenu(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            ArcadeGame game = activeGames.get(player.getUniqueId());
            if (game != null && event.getInventory().equals(game.getInventory())) {
                stopGame(player);
            }
        }
    }
}

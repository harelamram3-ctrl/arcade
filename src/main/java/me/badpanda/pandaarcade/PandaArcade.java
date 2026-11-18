package me.badpanda.pandaarcade;

import me.badpanda.pandaarcade.games.*;
import me.badpanda.pandaarcade.multiplayer.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PandaArcade extends JavaPlugin implements CommandExecutor, Listener {

    private static PandaArcade instance;
    private LeaderboardManager leaderboardManager;
    private MachineManager machineManager;
    private QueueManager queueManager;

    private final Map<UUID, ArcadeGame> activeSoloGames = new HashMap<>();
    private final Map<UUID, MultiplayerGame> activeMultiGames = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.leaderboardManager = new LeaderboardManager(this);
        this.machineManager = new MachineManager(this);
        this.queueManager = new QueueManager(this);

        if (getCommand("arcade") != null) {
            getCommand("arcade").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        activeSoloGames.values().forEach(ArcadeGame::stop);
        activeSoloGames.clear();
        activeMultiGames.values().forEach(MultiplayerGame::forceStop);
        activeMultiGames.clear();
        leaderboardManager.save();
        machineManager.save();
    }

    public static PandaArcade getInstance() {
        return instance;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public MachineManager getMachineManager() {
        return machineManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public void startSoloGame(Player player, ArcadeGame game) {
        stopAllForPlayer(player);
        activeSoloGames.put(player.getUniqueId(), game);
        game.start();
    }

    public void registerMultiGame(Player p1, Player p2, MultiplayerGame game) {
        stopAllForPlayer(p1);
        stopAllForPlayer(p2);
        activeMultiGames.put(p1.getUniqueId(), game);
        activeMultiGames.put(p2.getUniqueId(), game);
        game.start();
    }

    public void unregisterMultiGame(Player p1, Player p2) {
        if (p1 != null) activeMultiGames.remove(p1.getUniqueId());
        if (p2 != null) activeMultiGames.remove(p2.getUniqueId());
    }

    public void stopAllForPlayer(Player player) {
        queueManager.removePlayerFromQueues(player);
        ArcadeGame solo = activeSoloGames.remove(player.getUniqueId());
        if (solo != null) solo.stop();

        MultiplayerGame multi = activeMultiGames.remove(player.getUniqueId());
        if (multi != null) {
            multi.handleDisconnect(player);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("פקודה זו מיועדת לשחקנים בלבד.", NamedTextColor.RED));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("setmachine")) {
            if (!player.hasPermission("pandaarcade.admin")) {
                player.sendMessage(Component.text("אין לך הרשאה לפקודה זו.", NamedTextColor.RED));
                return true;
            }
            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType() != Material.BARRIER) {
                player.sendMessage(Component.text("עליך להסתכל על בלוק Barrier (מחסום) בטווח של 5 בלוקים!", NamedTextColor.RED));
                return true;
            }
            String gameType = args[1].toLowerCase();
            if (!MachineManager.isValidGameType(gameType)) {
                player.sendMessage(Component.text("סוג משחק לא חוקי! אפשרויות: menu, shooter, pacman, snake, mole, mario, airhockey, ttt, ships", NamedTextColor.YELLOW));
                return true;
            }
            machineManager.setMachine(target.getLocation(), gameType);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
            player.sendMessage(Component.text("✔ מכונת ארקייד הוגדרה בהצלחה על ה-Barrier למשחק: ", NamedTextColor.GREEN)
                    .append(Component.text(gameType.toUpperCase(), NamedTextColor.GOLD)));
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("delmachine")) {
            if (!player.hasPermission("pandaarcade.admin")) return true;
            Block target = player.getTargetBlockExact(5);
            if (target != null && machineManager.isMachine(target.getLocation())) {
                machineManager.removeMachine(target.getLocation());
                player.sendMessage(Component.text("✔ מכונת הארקייד הוסרה מבלוק זה.", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("לא נמצאה מכונת ארקייד בבלוק שעליו אתה מסתכל.", NamedTextColor.RED));
            }
            return true;
        }

        ArcadeGUI.openMainMenu(player);
        return true;
    }

    @EventHandler
    public void onBarrierInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BARRIER) return;

        String gameType = machineManager.getMachineGame(block.getLocation());
        if (gameType == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        launchById(player, gameType);
    }

    @EventHandler
    public void onBarrierBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BARRIER && machineManager.isMachine(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("pandaarcade.admin")) {
                event.setCancelled(true);
                return;
            }
            machineManager.removeMachine(event.getBlock().getLocation());
            event.getPlayer().sendMessage(Component.text("מכונת הארקייד נמחקה.", NamedTextColor.YELLOW));
        }
    }

    public void launchById(Player player, String id) {
        switch (id.toLowerCase()) {
            case "shooter" -> startSoloGame(player, new SpaceShooterGame(player));
            case "pacman" -> startSoloGame(player, new PacmanGame(player));
            case "snake" -> startSoloGame(player, new SnakeGame(player));
            case "mole" -> startSoloGame(player, new WhackAMoleGame(player));
            case "mario" -> startSoloGame(player, new MarioRunnerGame(player));
            case "airhockey" -> queueManager.joinQueue(player, "airhockey", "🏒 הוקי אוויר (1v1)");
            case "ttt" -> queueManager.joinQueue(player, "ttt", "❌⭕ איקס עיגול (1v1)");
            case "ships" -> queueManager.joinQueue(player, "ships", "🚢 קרב צוללות (1v1)");
            default -> ArcadeGUI.openMainMenu(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ArcadeGame solo = activeSoloGames.get(player.getUniqueId());
        if (solo != null && event.getView().getTopInventory().equals(solo.getInventory())) {
            event.setCancelled(true);
            solo.handleClick(event.getRawSlot());
            return;
        }

        MultiplayerGame multi = activeMultiGames.get(player.getUniqueId());
        if (multi != null && multi.isGameInventory(event.getView().getTopInventory())) {
            event.setCancelled(true);
            multi.handleClick(player, event.getRawSlot());
            return;
        }

        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.contains("ARCADE") || title.contains("LEADERBOARD") || title.contains("1V1") || title.contains("ממתין ליריב")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (title.contains("ממתין ליריב")) {
                if (slot == 22) {
                    queueManager.removePlayerFromQueues(player);
                    player.closeInventory();
                    player.sendMessage(Component.text("יצאת מתור ההמתנה.", NamedTextColor.YELLOW));
                }
                return;
            }

            if (title.contains("PANDAVERSE ARCADE")) {
                if (slot == 11) launchById(player, "shooter");
                else if (slot == 12) launchById(player, "pacman");
                else if (slot == 13) launchById(player, "snake");
                else if (slot == 14) launchById(player, "mole");
                else if (slot == 15) launchById(player, "mario");
                else if (slot == 29) ArcadeGUI.openMultiplayerMenu(player);
                else if (slot == 31) ArcadeGUI.openLeaderboardMenu(player);
                else if (slot == 33) ArcadeGUI.openSetupHelpMenu(player);
            } else if (title.contains("1V1")) {
                if (slot == 11) launchById(player, "airhockey");
                else if (slot == 13) launchById(player, "ttt");
                else if (slot == 15) launchById(player, "ships");
                else if (slot == 22) ArcadeGUI.openMainMenu(player);
            } else if (title.contains("LEADERBOARD") || title.contains("מכונות")) {
                if (slot == 40 || slot == 22) ArcadeGUI.openMainMenu(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            ArcadeGame solo = activeSoloGames.remove(player.getUniqueId());
            if (solo != null && event.getInventory().equals(solo.getInventory())) {
                solo.stop();
            }

            MultiplayerGame multi = activeMultiGames.get(player.getUniqueId());
            if (multi != null && multi.isGameInventory(event.getInventory())) {
                activeMultiGames.remove(player.getUniqueId());
                multi.handleDisconnect(player);
            }

            String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());
            if (title.contains("ממתין ליריב")) {
                queueManager.removePlayerFromQueues(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopAllForPlayer(event.getPlayer());
    }
}

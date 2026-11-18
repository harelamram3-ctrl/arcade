package il.arcade;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class ArcadePlugin extends JavaPlugin {
    private ScoreManager scores;
    private ArcadeMenu menu;

    @Override
    public void onEnable() {
        scores = new ScoreManager(this);
        menu = new ArcadeMenu(this);
        getServer().getPluginManager().registerEvents(new ArcadeListener(this), this);
        getLogger().info("Arcade enabled.");
    }

    public ScoreManager scores() { return scores; }

    public ArcadeMenu menu() { return menu; }

    public void sendTop(CommandSender to, String game) {
        List<Map.Entry<String, Integer>> top = scores.top(game, 5);
        to.sendMessage(Items.text("&6&l=== Top 5: " + game + " ==="));
        if (top.isEmpty()) {
            to.sendMessage(Items.text("&7No scores yet."));
            return;
        }
        int i = 1;
        for (Map.Entry<String, Integer> e : top) {
            to.sendMessage(Items.text("&e" + i++ + ". &f" + e.getKey() + " &7- &a" + e.getValue()));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("top")) {
            String game = args.length >= 2 ? args[1].toLowerCase() : "whack";
            if (!List.of("whack", "memory", "reaction").contains(game)) {
                sender.sendMessage(Items.text("&cUsage: /arcade top <whack|memory|reaction>"));
                return true;
            }
            sendTop(sender, game);
            return true;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        menu.open(p);
        return true;
    }
}

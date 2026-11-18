package il.arcade;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ArcadeMenu {
    private final ArcadePlugin plugin;

    public ArcadeMenu(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    public static class Holder implements InventoryHolder {
        private Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public void open(Player p) {
        Holder holder = new Holder();
        Inventory inv = Bukkit.createInventory(holder, 27, Items.text("&6&l★ ARCADE ★"));
        holder.inv = inv;

        for (int i = 0; i < 27; i++) inv.setItem(i, Items.make(Material.PURPLE_STAINED_GLASS_PANE, " "));
        inv.setItem(11, Items.make(Material.ZOMBIE_HEAD, "&2&lWhack-a-Zombie",
                "&7Click zombies, avoid TNT!", "&7Your best: &a" + plugin.scores().best("whack", p)));
        inv.setItem(13, Items.make(Material.ENDER_EYE, "&5&lMemory Match",
                "&7Find all the pairs.", "&7Your best: &a" + plugin.scores().best("memory", p)));
        inv.setItem(15, Items.make(Material.CLOCK, "&e&lReaction Test",
                "&7Click when it turns green.", "&7Your best: &a" + plugin.scores().best("reaction", p)));
        inv.setItem(22, Items.make(Material.GOLD_INGOT, "&6&lLeaderboards", "&7Click to see the top players in chat"));
        p.openInventory(inv);
    }

    public void click(Player p, int slot) {
        Game game = switch (slot) {
            case 11 -> new WhackAMole(plugin, p);
            case 13 -> new MemoryMatch(plugin, p);
            case 15 -> new ReactionGame(plugin, p);
            default -> null;
        };
        if (game != null) {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            game.start();
        } else if (slot == 22) {
            p.closeInventory();
            for (String id : new String[]{"whack", "memory", "reaction"}) plugin.sendTop(p, id);
        }
    }
}

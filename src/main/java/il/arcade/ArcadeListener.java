package il.arcade;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class ArcadeListener implements Listener {
    private final ArcadePlugin plugin;

    public ArcadeListener(ArcadePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean ours(InventoryHolder h) {
        return h instanceof Game || h instanceof ArcadeMenu.Holder;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (!ours(holder)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (holder instanceof Game g) {
            g.onClick(e.getSlot());
        } else {
            plugin.menu().click(p, e.getSlot());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (ours(e.getView().getTopInventory().getHolder())) e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof Game g) g.stop();
    }
}

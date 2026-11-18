package il.arcade;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public abstract class Game implements InventoryHolder {
    protected final ArcadePlugin plugin;
    protected final Player player;
    protected Inventory inv;
    protected boolean finished;
    private final List<BukkitTask> tasks = new ArrayList<>();

    protected Game(ArcadePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public abstract String id();

    public abstract String displayName();

    public abstract void start();

    public abstract void onClick(int slot);

    protected void open(int size) {
        inv = Bukkit.createInventory(this, size, Items.text(displayName()));
        player.openInventory(inv);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    protected BukkitTask later(long ticks, Runnable r) {
        BukkitTask t = Bukkit.getScheduler().runTaskLater(plugin, r, ticks);
        tasks.add(t);
        return t;
    }

    protected BukkitTask repeat(long period, Runnable r) {
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, r, period, period);
        tasks.add(t);
        return t;
    }

    protected void sound(Sound s, float pitch) {
        player.playSound(player.getLocation(), s, 1f, pitch);
    }

    public void stop() {
        for (BukkitTask t : tasks) t.cancel();
        tasks.clear();
    }

    protected void finish(int score) {
        if (finished) return;
        finished = true;
        stop();
        boolean record = plugin.scores().submit(id(), player, score);
        player.sendMessage(Items.text("&6[Arcade] &e" + displayName() + "&7 - score: &a" + score
                + (record ? " &d&l(NEW RECORD!)" : " &7(best: " + plugin.scores().best(id(), player) + ")")));
        sound(record ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_PLAYER_LEVELUP, 1f);
        later(50, () -> {
            if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == this) {
                plugin.menu().open(player);
            }
        });
    }
}

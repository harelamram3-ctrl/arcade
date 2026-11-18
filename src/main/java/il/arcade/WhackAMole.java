package il.arcade;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/** Click the zombies, avoid the TNT. 30 seconds. */
public class WhackAMole extends Game {
    private static final int DURATION = 30 * 20;
    private static final int STEP = 4;

    private final Random rnd = new Random();
    private final Map<Integer, Integer> expiry = new HashMap<>();
    private final Set<Integer> bombs = new HashSet<>();
    private int tick;
    private int score;

    public WhackAMole(ArcadePlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override public String id() { return "whack"; }

    @Override public String displayName() { return "&2&lWhack-a-Zombie"; }

    private ItemStack bg() {
        return Items.make(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    @Override
    public void start() {
        open(54);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg());
        repeat(STEP, this::step);
    }

    private void step() {
        tick += STEP;
        if (tick >= DURATION) {
            finish(Math.max(0, score));
            return;
        }
        expiry.entrySet().removeIf(e -> {
            if (e.getValue() <= tick) {
                inv.setItem(e.getKey(), bg());
                bombs.remove(e.getKey());
                return true;
            }
            return false;
        });
        if (rnd.nextInt(100) < 40) {
            int slot = rnd.nextInt(54);
            if (!expiry.containsKey(slot)) {
                boolean bomb = rnd.nextInt(100) < 20;
                int life = 30 - (12 * tick / DURATION); // gets faster
                expiry.put(slot, tick + life);
                if (bomb) {
                    bombs.add(slot);
                    inv.setItem(slot, Items.make(Material.TNT, "&c&lDON'T CLICK!"));
                } else {
                    inv.setItem(slot, Items.make(Material.ZOMBIE_HEAD, "&a&lWHACK!"));
                }
            }
        }
        int left = (DURATION - tick) / 20;
        player.sendActionBar(Component.text("Score: " + score + "   Time: " + left + "s"));
    }

    @Override
    public void onClick(int slot) {
        if (finished || expiry.remove(slot) == null) return;
        boolean bomb = bombs.remove(slot);
        inv.setItem(slot, bg());
        if (bomb) {
            score -= 25;
            sound(Sound.ENTITY_GENERIC_EXPLODE, 1f);
        } else {
            score += 10;
            sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2f);
        }
    }
}

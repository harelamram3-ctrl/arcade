package il.arcade;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

/** Find all 8 pairs. Fewer moves and less time = higher score. */
public class MemoryMatch extends Game {
    private static final Material[] FACES = {
            Material.DIAMOND, Material.EMERALD, Material.GOLDEN_APPLE, Material.TNT,
            Material.CAKE, Material.COMPASS, Material.BOW, Material.ENDER_PEARL
    };

    private final Map<Integer, Material> cards = new HashMap<>();
    private final Set<Integer> matched = new HashSet<>();
    private int first = -1, second = -1;
    private boolean locked;
    private int moves, pairs;
    private long startMs;

    public MemoryMatch(ArcadePlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override public String id() { return "memory"; }

    @Override public String displayName() { return "&5&lMemory Match"; }

    private void hide(int slot) {
        inv.setItem(slot, Items.make(Material.ORANGE_STAINED_GLASS_PANE, "&6?"));
    }

    private void reveal(int slot) {
        Material m = cards.get(slot);
        inv.setItem(slot, Items.make(m, "&f" + m.name().toLowerCase().replace('_', ' ')));
    }

    @Override
    public void start() {
        open(54);
        for (int i = 0; i < 54; i++) inv.setItem(i, Items.make(Material.BLACK_STAINED_GLASS_PANE, " "));

        List<Material> deck = new ArrayList<>();
        for (Material m : FACES) { deck.add(m); deck.add(m); }
        Collections.shuffle(deck);

        int idx = 0;
        for (int row = 1; row <= 4; row++) {
            for (int col = 2; col <= 5; col++) {
                int slot = row * 9 + col;
                cards.put(slot, deck.get(idx++));
                hide(slot);
            }
        }
        startMs = System.currentTimeMillis();
    }

    @Override
    public void onClick(int slot) {
        if (finished || locked || !cards.containsKey(slot) || matched.contains(slot) || slot == first) return;
        reveal(slot);
        sound(Sound.UI_BUTTON_CLICK, 1.4f);

        if (first == -1) {
            first = slot;
            return;
        }
        second = slot;
        moves++;

        if (cards.get(first) == cards.get(second)) {
            matched.add(first);
            matched.add(second);
            pairs++;
            first = second = -1;
            sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f);
            if (pairs == FACES.length) {
                int seconds = (int) ((System.currentTimeMillis() - startMs) / 1000);
                finish(Math.max(10, 300 - moves * 8 - seconds * 2));
            }
        } else {
            locked = true;
            later(20, () -> {
                hide(first);
                hide(second);
                first = second = -1;
                locked = false;
            });
        }
    }
}

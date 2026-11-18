package il.arcade;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;

/** 5 rounds: wait for green, then click as fast as you can. */
public class ReactionGame extends Game {
    private static final int ROUNDS = 5;
    private static final int INTERMISSION = 0, RED = 1, GREEN = 2;

    private final Random rnd = new Random();
    private int state = INTERMISSION;
    private int round;
    private int total;
    private long greenAt;

    public ReactionGame(ArcadePlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override public String id() { return "reaction"; }

    @Override public String displayName() { return "&e&lReaction Test"; }

    private void fill(Material m, String name) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, Items.make(m, name));
    }

    @Override
    public void start() {
        open(27);
        beginRound();
    }

    private void beginRound() {
        state = INTERMISSION;
        fill(Material.YELLOW_STAINED_GLASS_PANE, "&eGet ready... (" + (round + 1) + "/" + ROUNDS + ")");
        later(30, () -> {
            state = RED;
            fill(Material.RED_STAINED_GLASS_PANE, "&cWait for green...");
            later(30 + rnd.nextInt(70), () -> {
                state = GREEN;
                fill(Material.LIME_STAINED_GLASS_PANE, "&a&lCLICK NOW!");
                greenAt = System.nanoTime();
                sound(Sound.BLOCK_NOTE_BLOCK_PLING, 2f);
            });
        });
    }

    private void nextRound() {
        stop(); // cancel any pending timers
        round++;
        if (round >= ROUNDS) {
            finish(total);
        } else {
            beginRound();
        }
    }

    @Override
    public void onClick(int slot) {
        if (finished || state == INTERMISSION) return;
        if (state == RED) {
            player.sendMessage(Items.text("&6[Arcade] &cToo early! 0 points this round."));
            sound(Sound.ENTITY_VILLAGER_NO, 1f);
            nextRound();
            return;
        }
        long ms = (System.nanoTime() - greenAt) / 1_000_000;
        int pts = (int) Math.max(0, (1000 - ms) / 10);
        total += pts;
        player.sendMessage(Items.text("&6[Arcade] &7Round " + (round + 1) + ": &a" + ms + "ms &7(+" + pts + ")"));
        sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f);
        nextRound();
    }
}

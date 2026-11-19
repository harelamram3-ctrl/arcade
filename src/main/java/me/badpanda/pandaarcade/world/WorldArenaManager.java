package me.badpanda.pandaarcade.world;

import me.badpanda.pandaarcade.PandaArcade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class WorldArenaManager implements Listener {

    private final PandaArcade plugin;
    private final File file;
    private FileConfiguration config;

    private static final Set<String> ARENA_TYPES = Set.of("sumo", "spleef", "duel", "tnttag");

    // מעקב אחרי משחקי זירה פעילים בעולם MV
    private final Map<UUID, ArenaMatch> playerMatches = new HashMap<>();
    private final List<ArenaMatch> activeMatches = new ArrayList<>();

    public WorldArenaManager(PandaArcade plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public static boolean isValidArena(String type) {
        return ARENA_TYPES.contains(type.toLowerCase());
    }

    public void setArcadeWorld(String worldName) {
        config.set("arcade-world", worldName);
        save();
    }

    public String getArcadeWorldName() {
        return config.getString("arcade-world", "arcade");
    }

    public World getArcadeWorld() {
        String name = getArcadeWorldName();
        World w = Bukkit.getWorld(name);
        if (w == null) {
            // אם Multiverse טוען את העולם או שצריך לטעון אותו
            w = Bukkit.createWorld(new WorldCreator(name));
        }
        return w;
    }

    public void setLobbySpawn(Location loc) {
        config.set("arcade-world", loc.getWorld().getName());
        config.set("lobby", loc);
        save();
    }

    public Location getLobbySpawn() {
        Location loc = config.getLocation("lobby");
        if (loc != null && loc.getWorld() != null) return loc;
        World w = getArcadeWorld();
        return w != null ? w.getSpawnLocation() : Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    public void setArenaSpawn(String arenaType, int spot, Location loc) {
        config.set("arenas." + arenaType + ".spawn" + spot, loc);
        save();
    }

    public Location getArenaSpawn(String arenaType, int spot) {
        return config.getLocation("arenas." + arenaType + ".spawn" + spot);
    }

    public void teleportToArcadeLobby(Player player) {
        Location lobby = getLobbySpawn();
        player.teleport(lobby);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
        player.sendMessage(Component.text("🕹 ברוך הבא לעולם הארקייד של PANDAVERSE!", NamedTextColor.LIGHT_PURPLE));
    }

    /**
     * בונה אוטומטית זירה מוכנה בעולם MV ומגדיר את נקודות השיגור שלה!
     */
    public void buildPresetArena(Player admin, String type) {
        Location center = admin.getLocation().getBlock().getLocation();
        World world = center.getWorld();
        setArcadeWorld(world.getName());

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // ניקוי חלל באוויר מעל הזירה
        for (int x = -12; x <= 12; x++) {
            for (int y = -2; y <= 10; y++) {
                for (int z = -12; z <= 12; z++) {
                    world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR);
                }
            }
        }

        switch (type) {
            case "sumo" -> {
                // פלטפורמת סומו עגולה מ-Quartz ו-Red/Blue Concrete
                for (int x = -6; x <= 6; x++) {
                    for (int z = -6; z <= 6; z++) {
                        if (x * x + z * z <= 36) {
                            Material mat = (x * x + z * z >= 25) ? Material.RED_CONCRETE : Material.SMOOTH_QUARTZ;
                            world.getBlockAt(cx + x, cy - 1, cz + z).setType(mat);
                        }
                    }
                }
            }
            case "spleef" -> {
                // רצפת שלג עם קירות זכוכית מסביב
                for (int x = -8; x <= 8; x++) {
                    for (int z = -8; z <= 8; z++) {
                        world.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.SNOW_BLOCK);
                        if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                            for (int h = 0; h <= 3; h++) {
                                world.getBlockAt(cx + x, cy + h, cz + z).setType(Material.LIGHT_BLUE_STAINED_GLASS);
                            }
                        }
                    }
                }
            }
            case "duel", "tnttag" -> {
                // זירת קרב מוקפת חומה עם מחסות
                for (int x = -10; x <= 10; x++) {
                    for (int z = -10; z <= 10; z++) {
                        Material floor = ((x + z) % 2 == 0) ? Material.STONE_BRICKS : Material.POLISHED_ANDESITE;
                        world.getBlockAt(cx + x, cy - 1, cz + z).setType(floor);
                        if (Math.abs(x) == 10 || Math.abs(z) == 10) {
                            for (int h = 0; h <= 4; h++) {
                                world.getBlockAt(cx + x, cy + h, cz + z).setType(Material.DEEPSLATE_BRICKS);
                            }
                        }
                    }
                }
                // עמודי מחסה במרכז
                for (int h = 0; h <= 2; h++) {
                    world.getBlockAt(cx - 4, cy + h, cz).setType(Material.CHISELED_STONE_BRICKS);
                    world.getBlockAt(cx + 4, cy + h, cz).setType(Material.CHISELED_STONE_BRICKS);
                }
            }
        }

        Location s1 = new Location(world, cx - 4.5, cy, cz + 0.5, -90f, 0f);
        Location s2 = new Location(world, cx + 5.5, cy, cz + 0.5, 90f, 0f);
        setArenaSpawn(type, 1, s1);
        setArenaSpawn(type, 2, s2);

        admin.teleport(new Location(world, cx + 0.5, cy + 5, cz + 0.5));
        admin.playSound(admin.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        admin.sendMessage(Component.text("✔ המפה עבור " + type.toUpperCase() + " נבנתה אוטומטית בעולם " + world.getName() + " ונקודות השיגור הוגדרו!", NamedTextColor.GREEN));
    }

    public boolean startWorldMatch(String type, Player p1, Player p2) {
        Location s1 = getArenaSpawn(type, 1);
        Location s2 = getArenaSpawn(type, 2);

        if (s1 == null || s2 == null || s1.getWorld() == null) {
            p1.sendMessage(Component.text("✖ זירת " + type + " עדיין לא הוגדרה בעולם! אדמין צריך להריץ /arcade buildmap " + type, NamedTextColor.RED));
            p2.sendMessage(Component.text("✖ זירת " + type + " עדיין לא הוגדרה בעולם! אדמין צריך להריץ /arcade buildmap " + type, NamedTextColor.RED));
            return false;
        }

        // איפוס רצפת שלג אם זה Spleef
        if (type.equals("spleef")) {
            restoreSpleefFloor(s1, s2);
        }

        ArenaMatch match = new ArenaMatch(type, p1, p2, s1, s2);
        playerMatches.put(p1.getUniqueId(), match);
        playerMatches.put(p2.getUniqueId(), match);
        activeMatches.add(match);
        match.begin();
        return true;
    }

    private void restoreSpleefFloor(Location s1, Location s2) {
        World w = s1.getWorld();
        int minX = Math.min(s1.getBlockX(), s2.getBlockX()) - 4;
        int maxX = Math.max(s1.getBlockX(), s2.getBlockX()) + 4;
        int minZ = Math.min(s1.getBlockZ(), s2.getBlockZ()) - 8;
        int maxZ = Math.max(s1.getBlockZ(), s2.getBlockZ()) + 8;
        int y = s1.getBlockY() - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Block b = w.getBlockAt(x, y, z);
                if (b.getType() == Material.AIR) {
                    b.setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    public void finishWorldMatch(ArenaMatch match, Player winner, String reason) {
        if (match.ended) return;
        match.ended = true;
        if (match.task != null) match.task.cancel();

        activeMatches.remove(match);
        playerMatches.remove(match.p1.getUniqueId());
        playerMatches.remove(match.p2.getUniqueId());

        if (winner != null) {
            plugin.getLeaderboardManager().addWin(match.type, winner.getName());
        }

        Location lobby = getLobbySpawn();
        for (Player p : new Player[]{match.p1, match.p2}) {
            if (p != null && p.isOnline()) {
                p.getInventory().clear();
                p.setHealth(20.0);
                p.setFoodLevel(20);
                p.setFireTicks(0);
                p.teleport(lobby);
                p.playSound(p.getLocation(), p.equals(winner) ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_VILLAGER_NO, 1f, 1.1f);
                p.sendMessage(Component.text("=================================", NamedTextColor.DARK_GRAY));
                p.sendMessage(Component.text("סיום קרב בזירה! ", NamedTextColor.GOLD).append(Component.text(reason, NamedTextColor.GRAY)));
                if (winner != null) {
                    p.sendMessage(Component.text("🏆 המנצח: ", NamedTextColor.YELLOW).append(Component.text(winner.getName(), NamedTextColor.GREEN)));
                }
                p.sendMessage(Component.text("=================================", NamedTextColor.DARK_GRAY));
            }
        }
    }

    public void handlePlayerQuit(Player player) {
        ArenaMatch match = playerMatches.get(player.getUniqueId());
        if (match != null) {
            Player winner = match.p1.equals(player) ? match.p2 : match.p1;
            finishWorldMatch(match, winner, player.getName() + " יצא באמצע הקרב!");
        }
    }

    public void cleanupAllMatches() {
        for (ArenaMatch m : new ArrayList<>(activeMatches)) {
            finishWorldMatch(m, null, "השרת נסגר.");
        }
    }

    // --- מאזיני אירועים בזירות בעולם MV ---

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        ArenaMatch match = playerMatches.get(p.getUniqueId());
        if (match == null || match.ended) return;

        // נפילה מתחת לגובה הזירה (ב-Sumo או Spleef)
        if (p.getLocation().getY() < match.minY - 3) {
            Player winner = match.p1.equals(p) ? match.p2 : match.p1;
            finishWorldMatch(match, winner, p.getName() + " נפל מחוץ לזירה!");
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        ArenaMatch match = playerMatches.get(victim.getUniqueId());
        if (match == null) return;

        if (match.type.equals("sumo") || match.type.equals("spleef") || match.type.equals("tnttag")) {
            event.setDamage(0.0); // ללא נזק חיים, רק הדף!
        } else if (match.type.equals("duel")) {
            if (victim.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true);
                Player winner = match.p1.equals(victim) ? match.p2 : match.p1;
                finishWorldMatch(match, winner, winner.getName() + " חיסל את " + victim.getName() + " בדו-קרב!");
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) return;
        ArenaMatch match = playerMatches.get(victim.getUniqueId());
        if (match == null) return;

        if (match.type.equals("tnttag")) {
            // העברת הפצצה!
            if (attacker.equals(match.tntHolder)) {
                match.setTntHolder(victim);
                victim.playSound(victim.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1.5f);
                attacker.sendMessage(Component.text("✔ העברת את הפצצה אל " + victim.getName() + "!", NamedTextColor.GREEN));
                victim.sendMessage(Component.text("💣 קיבלת את הפצצה! הכה ב-" + attacker.getName() + " כדי להעביר אותה!", NamedTextColor.RED));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        ArenaMatch match = playerMatches.get(p.getUniqueId());
        if (match == null) return;

        if (match.type.equals("spleef") && event.getBlock().getType() == Material.SNOW_BLOCK) {
            event.setDropItems(false);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (playerMatches.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player p && playerMatches.containsKey(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // מחלקה פנימית לניהול משחק זירה פעיל בעולם MV
    public class ArenaMatch {
        final String type;
        final Player p1;
        final Player p2;
        final Location s1;
        final Location s2;
        final double minY;
        boolean ended = false;
        BukkitTask task;
        Player tntHolder;
        int timer = 30;

        public ArenaMatch(String type, Player p1, Player p2, Location s1, Location s2) {
            this.type = type;
            this.p1 = p1;
            this.p2 = p2;
            this.s1 = s1;
            this.s2 = s2;
            this.minY = Math.min(s1.getY(), s2.getY());
        }

        public void begin() {
            preparePlayer(p1, s1);
            preparePlayer(p2, s2);

            switch (type) {
                case "sumo" -> {
                    ItemStack stick = new ItemStack(Material.STICK);
                    ItemMeta meta = stick.getItemMeta();
                    meta.setDisplayName("§6§lמקל סומו");
                    meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                    stick.setItemMeta(meta);
                    p1.getInventory().setItem(0, stick);
                    p2.getInventory().setItem(0, stick);
                }
                case "spleef" -> {
                    ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
                    ItemMeta meta = shovel.getItemMeta();
                    meta.setDisplayName("§b§lאת ספליף");
                    meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
                    shovel.setItemMeta(meta);
                    p1.getInventory().setItem(0, shovel);
                    p2.getInventory().setItem(0, shovel);
                }
                case "duel" -> {
                    equipDuelKit(p1);
                    equipDuelKit(p2);
                }
                case "tnttag" -> {
                    setTntHolder(new Random().nextBoolean() ? p1 : p2);
                    startTntTimer();
                }
            }

            Title title = Title.title(
                    Component.text("⚔ הקרב התחיל! ⚔", NamedTextColor.GOLD),
                    Component.text(p1.getName() + " נגד " + p2.getName(), NamedTextColor.YELLOW),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(300))
            );
            p1.showTitle(title);
            p2.showTitle(title);
        }

        private void preparePlayer(Player p, Location loc) {
            p.closeInventory();
            p.getInventory().clear();
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.teleport(loc);
            p.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.5f);
        }

        private void equipDuelKit(Player p) {
            p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
            p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
            p.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
            p.getInventory().setItem(1, new ItemStack(Material.GOLDEN_APPLE, 2));
            p.getInventory().setItem(2, new ItemStack(Material.BOW));
            p.getInventory().setItem(8, new ItemStack(Material.ARROW, 12));
        }

        public void setTntHolder(Player holder) {
            this.tntHolder = holder;
            Player other = holder.equals(p1) ? p2 : p1;

            holder.getInventory().clear();
            holder.getInventory().setHelmet(new ItemStack(Material.TNT));
            holder.getInventory().setItem(0, new ItemStack(Material.TNT));
            holder.setGlowing(true);

            other.getInventory().clear();
            other.getInventory().setHelmet(null);
            other.setGlowing(false);
        }

        private void startTntTimer() {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (ended) {
                        cancel();
                        return;
                    }
                    timer--;
                    p1.sendActionBar(Component.text("💣 הפצצה אצל: " + tntHolder.getName() + " | פיצוץ בעוד: " + timer + " שנ'", NamedTextColor.RED));
                    p2.sendActionBar(Component.text("💣 הפצצה אצל: " + tntHolder.getName() + " | פיצוץ בעוד: " + timer + " שנ'", NamedTextColor.RED));

                    if (timer <= 0) {
                        tntHolder.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, tntHolder.getLocation(), 1);
                        tntHolder.playSound(tntHolder.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
                        Player winner = tntHolder.equals(p1) ? p2 : p1;
                        finishWorldMatch(ArenaMatch.this, winner, "הפצצה התפוצצה על " + tntHolder.getName() + "!");
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
    }
}

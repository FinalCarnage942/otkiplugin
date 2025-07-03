package carnage.otkiplugin.classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Inferno implements Listener {
    private static JavaPlugin plugin;
    private static final Random random = new Random();

    // Store cooldowns per player per ability
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_FLARE_BLITZ = 10000;
    private static final long COOLDOWN_SPINZITU = 15000;
    private static final long COOLDOWN_RING_OF_FIRE = 20000;

    // Passive tracking
    private static final Map<UUID, Integer> fireStreakTicks = new HashMap<>(); // Track consecutive fire ticks
    private static final Map<UUID, Double> currentSpeedBonus = new HashMap<>(); // Current speed bonus
    private static final Map<UUID, Long> lastWaterDamage = new HashMap<>(); // Track water damage timing

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        startPassiveEffectsTask();
    }

    private static void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasInfernoClass(player)) {
                        handlePassiveEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private static boolean hasInfernoClass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        if (item.getItemMeta() == null) return false;

        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return false;

        return displayName.toString().contains("Inferno");
    }

    private static void handlePassiveEffects(Player player) {
        UUID playerId = player.getUniqueId();

        // Handle Healing Flame - Regeneration in lava
        if (player.getLocation().getBlock().getType() == Material.LAVA) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false));
        }

        // Handle Infernal Flow - Speed bonus while on fire
        if (player.getFireTicks() > 0) {
            int currentStreak = fireStreakTicks.getOrDefault(playerId, 0) + 1;
            fireStreakTicks.put(playerId, currentStreak);

            // Calculate speed bonus (0.02% per second, max 1.30%)
            double speedBonus = Math.min(currentStreak * 0.0002, 0.013); // 0.02% = 0.0002, 1.30% = 0.013
            currentSpeedBonus.put(playerId, speedBonus);

            // Apply speed effect (convert percentage to amplifier levels)
            int amplifier = (int) Math.floor(speedBonus * 100); // Convert to reasonable amplifier
            if (amplifier > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, Math.min(amplifier, 4), false, false));
            }
        } else {
            // Reset fire streak when not on fire
            if (fireStreakTicks.containsKey(playerId)) {
                fireStreakTicks.remove(playerId);
                currentSpeedBonus.remove(playerId);
            }
        }

        // Handle Burning Wrath - Attack speed when on fire
        if (player.getFireTicks() > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 30, 0, false, false));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!hasInfernoClass(player)) return;

        // Born n' Raised - Immunity to lava/fire damage
        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.setCancelled(true);
            return;
        }

        // Wqtr - Water damage
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            handleWaterDamage(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hasInfernoClass(player)) return;

        // Check if player is in water
        Material blockType = player.getLocation().getBlock().getType();
        if (blockType == Material.WATER) {
            handleWaterDamage(player);
        }
    }

    private void handleWaterDamage(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastDamage = lastWaterDamage.getOrDefault(playerId, 0L);

        // Water damage at lava damage tick rate (roughly every 10 ticks/0.5 seconds)
        if (currentTime - lastDamage >= 500) {
            player.damage(2.0); // 1 heart = 2.0 damage
            player.sendActionBar(Component.text("Water burns you!", NamedTextColor.RED));
            lastWaterDamage.put(playerId, currentTime);

            // Visual effect
            player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 10, 0.5, 1, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.5f);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ItemStack item = potion.getItem();

        if (item.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();

            // Check if it's a water bottle
            if (meta.getBasePotionData().getType() == PotionType.WATER) {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (hasInfernoClass(player)) {
                            // Cancel normal splash effect and apply custom damage
                            event.setIntensity(entity, 0);

                            // Damage equivalent to Instant Damage 1 potion (6 damage = 3 hearts)
                            player.damage(6.0);
                            player.sendActionBar(Component.text("Splash water burns you!", NamedTextColor.RED));

                            // Visual effect
                            player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 15, 1, 1, 1, 0.1);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.2f);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Fire Within - When a player hits you, 10% chance to light them on fire
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            if (hasInfernoClass(victim) && random.nextDouble() < 0.10) { // 10% chance
                attacker.setFireTicks(40); // 2 seconds of fire (40 ticks)
                attacker.sendActionBar(Component.text("You've been burned by infernal power!", NamedTextColor.GOLD));

                // Visual effect
                attacker.getWorld().spawnParticle(Particle.FLAME, attacker.getLocation(), 20, 0.5, 1, 0.5, 0.1);
                attacker.playSound(attacker.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.2f);
            }
        }

        // Fire Aspect - 5% chance to light whatever you hit on fire
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player attacker = (Player) event.getDamager();
            LivingEntity victim = (LivingEntity) event.getEntity();

            if (hasInfernoClass(attacker) && random.nextDouble() < 0.05) { // 5% chance
                victim.setFireTicks(60); // 3 seconds of fire (60 ticks)
                attacker.sendActionBar(Component.text("Your attack ignites the target!", NamedTextColor.GOLD));

                // Visual effect
                victim.getWorld().spawnParticle(Particle.FLAME, victim.getLocation(), 15, 0.5, 1, 0.5, 0.1);
                victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.0f);
            }
        }
    }

    // Method to get cooldown map for player or create new
    private static Map<String, Long> getCooldownMap(Player player) {
        return abilityCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
    }

    private static long getCooldown(Player player, String ability) {
        Map<String, Long> map = getCooldownMap(player);
        return map.getOrDefault(ability.toLowerCase(), 0L);
    }

    private static void setCooldown(Player player, String ability, long cooldownMs) {
        Map<String, Long> map = getCooldownMap(player);
        map.put(ability.toLowerCase(), System.currentTimeMillis() + cooldownMs);
    }

    public static double getCooldownRemaining(Player player, String ability) {
        long end = getCooldown(player, ability);
        long now = System.currentTimeMillis();
        return (end > now) ? (end - now) / 1000.0 : 0;
    }

    // Example: When an ability is used, set cooldown
    public static void useAbility(Player player, String ability) {
        switch (ability.toLowerCase()) {
            case "flare blitz" -> {
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Flare Blitz is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_FLARE_BLITZ);
                flareBlitz(player);
            }
            case "spinzitu" -> {
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Spinzitu is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_SPINZITU);
                spinzitu(player);
            }
            case "ring of fire" -> {
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Ring of Fire is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_RING_OF_FIRE);
                ringOfFire(player);
            }
            default -> {
                player.sendActionBar(Component.text("Unknown ability.", NamedTextColor.RED));
                return;
            }
        }
        // ActionBar will be automatically updated by ActionBarManager
    }

    // Ability implementations
    public static void flareBlitz(Player player) {
        player.sendActionBar(Component.text("You used Flare Blitz!", NamedTextColor.RED));

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 50, 1, 1, 1, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

        // Launch player forward with fire trail
        Vector direction = player.getLocation().getDirection().multiply(2);
        direction.setY(0.5); // Add some upward momentum
        player.setVelocity(direction);

        // Fire trail effect and damage
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 20 || !player.isOnline()) { // Run for 1 second
                    cancel();
                    return;
                }

                Location loc = player.getLocation();
                player.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.5, 0.5, 0.5, 0.05);

                // Damage nearby enemies
                for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                    if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                        LivingEntity living = (LivingEntity) entity;
                        living.damage(4.0, player);
                        living.setFireTicks(60); // 3 seconds of fire
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void spinzitu(Player player) {
        player.sendActionBar(Component.text("You used Spinzitu!", NamedTextColor.RED));

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 100, 2, 1, 2, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 0.8f);

        // Spinning fire attack
        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;
            @Override
            public void run() {
                if (ticks >= 40 || !player.isOnline()) { // Run for 2 seconds
                    cancel();
                    return;
                }

                Location center = player.getLocation();

                // Create spinning fire effect
                for (int i = 0; i < 8; i++) {
                    double radians = Math.toRadians(angle + (i * 45));
                    double x = center.getX() + Math.cos(radians) * 3;
                    double z = center.getZ() + Math.sin(radians) * 3;
                    Location fireLocation = new Location(center.getWorld(), x, center.getY() + 1, z);

                    player.getWorld().spawnParticle(Particle.FLAME, fireLocation, 5, 0.1, 0.1, 0.1, 0.02);

                    // Damage entities at fire locations
                    for (Entity entity : fireLocation.getWorld().getNearbyEntities(fireLocation, 1, 1, 1)) {
                        if (entity instanceof LivingEntity && !(entity instanceof Player) && entity != player) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(2.0, player);
                            living.setFireTicks(40);
                        }
                    }
                }

                angle += 15; // Spin speed
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void ringOfFire(Player player) {
        player.sendActionBar(Component.text("You used Ring of Fire!", NamedTextColor.RED));

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 200, 4, 1, 4, 0.3);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 0.5f);

        Location center = player.getLocation();

        // Create expanding ring of fire
        new BukkitRunnable() {
            int ticks = 0;
            double radius = 1;
            @Override
            public void run() {
                if (ticks >= 60 || !player.isOnline()) { // Run for 3 seconds
                    cancel();
                    return;
                }

                // Create fire ring
                for (int i = 0; i < 32; i++) {
                    double angle = Math.toRadians(i * 11.25); // 360/32 = 11.25 degrees
                    double x = center.getX() + Math.cos(angle) * radius;
                    double z = center.getZ() + Math.sin(angle) * radius;
                    Location fireLocation = new Location(center.getWorld(), x, center.getY(), z);

                    // Spawn fire particles
                    player.getWorld().spawnParticle(Particle.FLAME, fireLocation, 3, 0.1, 0.5, 0.1, 0.02);

                    // Set blocks on fire temporarily
                    if (fireLocation.getBlock().getType() == Material.AIR ||
                            fireLocation.getBlock().getType() == Material.SHORT_GRASS ||
                            fireLocation.getBlock().getType() == Material.TALL_GRASS) {

                        // Create temporary fire effect without actually setting blocks
                        player.getWorld().spawnParticle(Particle.FLAME, fireLocation.add(0, 1, 0), 5, 0.1, 0.3, 0.1, 0.05);
                    }

                    // Damage entities in the ring
                    for (Entity entity : fireLocation.getWorld().getNearbyEntities(fireLocation, 1.5, 2, 1.5)) {
                        if (entity instanceof LivingEntity && !(entity instanceof Player) && entity != player) {
                            LivingEntity living = (LivingEntity) entity;
                            living.damage(3.0, player);
                            living.setFireTicks(80);
                            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                        }
                    }
                }

                if (radius < 6) {
                    radius += 0.1; // Expand the ring
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
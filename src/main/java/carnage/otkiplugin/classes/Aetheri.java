package carnage.otkiplugin.classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Aetheri implements Listener {
    private static JavaPlugin plugin;

    // Store cooldowns per player per ability
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_SUPERCRIT = 30000; // 30 seconds
    private static final long COOLDOWN_SOAR = 60000; // 60 seconds
    private static final long COOLDOWN_MIND_OVER_MATTER = 45000; // 45 seconds
    private static final long COOLDOWN_WARRIORS_LEAP = 20000; // 20 seconds
    private static final long COOLDOWN_DOUBLE_JUMP = 3000; // 3 seconds

    // Passive tracking
    private static final Map<UUID, Long> lastDoubleJump = new HashMap<>();
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>(); // Track ground state for double jump
    private static final Map<UUID, Integer> bowShotCount = new HashMap<>();
    private static final Map<UUID, Boolean> supercritCharged = new HashMap<>();
    private static final Map<UUID, Long> supercritChargeStart = new HashMap<>();
    private static final Map<UUID, Boolean> supercritActive = new HashMap<>();
    private static final Map<UUID, Integer> soarActivations = new HashMap<>();
    private static final Map<UUID, Boolean> soarActive = new HashMap<>();
    private static final Map<UUID, Long> soarStartTime = new HashMap<>();
    private static final Map<UUID, Long> mindOverMatterEnd = new HashMap<>();
    private static final Map<UUID, Double> warriorsLeapStartY = new HashMap<>(); // Track Warriors Leap specifically

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        startPassiveEffectsTask();
    }

    private static void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasAetheriClass(player)) {
                        handlePassiveEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private static boolean hasAetheriClass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.FEATHER) return false;
        if (item.getItemMeta() == null) return false;

        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return false;

        return displayName.toString().contains("Aetheri");
    }

    private static void handlePassiveEffects(Player player) {
        UUID playerId = player.getUniqueId();

        // Handle Frail - 9 hearts max
        if (player.getAttribute(Attribute.MAX_HEALTH).getValue() != 18.0) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(18.0);
        }

        // Handle Aerial Strength - Y=100+ bonus
        if (player.getLocation().getY() >= 100) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, false, false));
            // Attack damage bonus handled in damage event
        }

        // Handle Claustrophobia - block overhead debuff
        Location overhead = player.getLocation().add(0, 2, 0);
        if (overhead.getBlock().getType() != Material.AIR) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0, false, false));
            // Damage reduction handled in damage event
        }

        // Handle Supercrit charging
        if (supercritChargeStart.containsKey(playerId) && player.isSneaking()) {
            long chargeTime = System.currentTimeMillis() - supercritChargeStart.get(playerId);
            if (chargeTime >= 6000 && !supercritCharged.getOrDefault(playerId, false)) { // 6 seconds
                supercritCharged.put(playerId, true);
                supercritChargeStart.remove(playerId);
                player.sendActionBar(Component.text("Supercrit Charged!", NamedTextColor.GOLD));
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 1, 1, 1, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);
            }
        }

        // Handle Soar duration
        if (soarActive.getOrDefault(playerId, false)) {
            long soarTime = System.currentTimeMillis() - soarStartTime.get(playerId);
            if (soarTime >= 20000) { // 20 seconds max
                deactivateSoar(player);
            } else {
                // Visual trail
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 2, 0.2, 0.2, 0.2, 0.01);
            }
        }

        // Handle Mind over Matter duration
        Long mindEnd = mindOverMatterEnd.get(playerId);
        if (mindEnd != null && System.currentTimeMillis() > mindEnd) {
            mindOverMatterEnd.remove(playerId);
            player.sendActionBar(Component.text("Mind over Matter ended!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 0.8f, 1.0f);
            // Red and white particles
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 10, 0.5, 1, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 1, 0.5, 0.1);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!hasAetheriClass(player)) return;

        // Strong Ankles - No Fall Damage
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);

            // Handle Warriors Leap landing damage
            UUID playerId = player.getUniqueId();
            if (warriorsLeapStartY.containsKey(playerId)) {
                double fallDistance = warriorsLeapStartY.get(playerId) - player.getLocation().getY();
                if (fallDistance > 0) { // Any fall distance triggers it
                    // Calculate mace-like damage based on fall distance
                    double effectiveFallDistance = Math.min(fallDistance, 10); // Max 10 blocks worth
                    // Mace damage formula: base damage + (fall distance * multiplier)
                    double damage = 6.0 + (effectiveFallDistance * 1.5); // Base 6 damage + 1.5 per block fallen
                    warriorsLeapLanding(player, damage);
                }
                warriorsLeapStartY.remove(playerId);
            }
            return;
        }

        // Claustrophobia damage reduction
        Location overhead = player.getLocation().add(0, 2, 0);
        if (overhead.getBlock().getType() != Material.AIR) {
            event.setDamage(Math.max(0, event.getDamage() - 1.0)); // -1 damage
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle attacker bonuses
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (!hasAetheriClass(attacker)) return;

            UUID attackerId = attacker.getUniqueId();
            double damage = event.getDamage();

            // Aerial Strength bonus damage at Y=100+
            if (attacker.getLocation().getY() >= 100) {
                damage += 1.0;
            }

            // Mind over Matter bonus damage
            if (mindOverMatterEnd.containsKey(attackerId) &&
                    System.currentTimeMillis() < mindOverMatterEnd.get(attackerId)) {
                damage += 2.0;
            }

            // Supercrit damage
            if (supercritActive.getOrDefault(attackerId, false)) {
                damage *= 3.0;
                supercritActive.put(attackerId, false);
                attacker.sendActionBar(Component.text("Supercrit activated!", NamedTextColor.GOLD));
                attacker.getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation(), 30, 1, 1, 1, 0.2);
            }

            // Focus Crit - 1.25x damage on crits
            // Note: Detecting crits in Bukkit is complex, this is a simplified version
            if (attacker.getFallDistance() > 0 && !attacker.isOnGround()) {
                damage *= 1.25;
            }

            event.setDamage(damage);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hasAetheriClass(player)) return;

        UUID playerId = player.getUniqueId();

        // Track ground state for double jump detection
        boolean currentlyOnGround = player.isOnGround();
        boolean previouslyOnGround = wasOnGround.getOrDefault(playerId, true);
        wasOnGround.put(playerId, currentlyOnGround);

        // Handle double jump - only trigger when jumping from ground or after landing
        if (event.getTo() != null && event.getFrom() != null) {
            // Player just left the ground (jumped)
            if (previouslyOnGround && !currentlyOnGround && player.getVelocity().getY() > 0.1) {
                // This is the first jump, allow double jump next
                // Don't trigger double jump here
            }
            // Player is in air and trying to jump again (double jump detection)
            else if (!currentlyOnGround && !previouslyOnGround &&
                    player.getVelocity().getY() < 0.1 && player.getVelocity().getY() > -0.3) {

                // Check if soar is active - if so, don't allow double jump
                if (soarActive.getOrDefault(playerId, false)) {
                    return;
                }

                long currentTime = System.currentTimeMillis();
                long lastJump = lastDoubleJump.getOrDefault(playerId, 0L);

                if (currentTime - lastJump >= COOLDOWN_DOUBLE_JUMP) {
                    // Check if player pressed jump (this is simplified - you might need a more sophisticated method)
                    // For now, we'll use velocity patterns to detect jump attempts
                    performDoubleJump(player);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();

        if (!hasAetheriClass(player)) return;

        // Handle Gale Storm - every 5th bow shot doesn't need charge
        if (event.getEntity() instanceof org.bukkit.entity.Arrow) {
            UUID playerId = player.getUniqueId();
            int shotCount = bowShotCount.getOrDefault(playerId, 0) + 1;
            bowShotCount.put(playerId, shotCount);

            if (shotCount >= 5) {
                bowShotCount.put(playerId, 0);
                // Give the arrow extra velocity as if fully charged
                event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(1.5));
                player.sendActionBar(Component.text("Gale Storm!", NamedTextColor.AQUA));
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!hasAetheriClass(player)) return;

        UUID playerId = player.getUniqueId();

        // Start Supercrit charging
        if (event.isSneaking() && !supercritCharged.getOrDefault(playerId, false)) {
            supercritChargeStart.put(playerId, System.currentTimeMillis());
        } else if (!event.isSneaking()) {
            supercritChargeStart.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!hasAetheriClass(player)) return;

        // Handle wind charge agility boost
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.WIND_CHARGE) {
            // This will be handled by the wind charge's natural mechanics
            // but we can add a visual effect
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        Vector velocity = player.getVelocity();
                        player.setVelocity(velocity.multiply(1.5)); // 1.5x further
                        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 1, 1, 1, 0.1);
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    private static void performDoubleJump(Player player) {
        UUID playerId = player.getUniqueId();
        lastDoubleJump.put(playerId, System.currentTimeMillis());

        Vector velocity = player.getVelocity();
        velocity.setY(0.8); // 2 blocks high jump
        player.setVelocity(velocity);

        // Visual effect
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.5, 0.2, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.8f, 1.5f);
    }

    private static void warriorsLeapLanding(Player player, double damage) {
        Location center = player.getLocation();

        // 3 block radius AoE damage
        for (Entity entity : center.getWorld().getNearbyEntities(center, 3, 3, 3)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;
                living.damage(damage, player);
                living.setVelocity(living.getLocation().subtract(center).toVector().normalize().multiply(1.5));
            }
        }

        // Visual effect - 3 block circle of white particles
        for (int i = 0; i < 32; i++) {
            double angle = Math.toRadians(i * 11.25);
            double x = center.getX() + Math.cos(angle) * 3;
            double z = center.getZ() + Math.sin(angle) * 3;
            Location particleLoc = new Location(center.getWorld(), x, center.getY() + 0.1, z);
            center.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 5, 0.1, 0.1, 0.1, 0.05);
        }

        player.playSound(center, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.0f, 0.8f);
        player.sendActionBar(Component.text("Warriors Leap Impact!", NamedTextColor.GOLD));
    }

    private static void deactivateSoar(Player player) {
        UUID playerId = player.getUniqueId();
        soarActive.put(playerId, false);
        soarStartTime.remove(playerId);

        // Remove elytra
        player.getInventory().setChestplate(null);
        player.sendActionBar(Component.text("Soar ended!", NamedTextColor.GRAY));
    }

    // Cooldown management methods (same pattern as Inferno)
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

    public static void useAbility(Player player, String ability) {
        switch (ability.toLowerCase()) {
            case "supercrit" -> {
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Supercrit is on cooldown!", NamedTextColor.RED));
                    return;
                }
                supercrit(player);
            }
            case "soar" -> {
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Soar is on cooldown!", NamedTextColor.RED));
                    return;
                }
                soar(player);
            }
            case "mind over matter" -> {
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Mind over Matter is on cooldown!", NamedTextColor.RED));
                    return;
                }
                mindOverMatter(player);
            }
            case "warriors leap" -> {
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Warriors Leap is on cooldown!", NamedTextColor.RED));
                    return;
                }
                warriorsLeap(player);
            }
            default -> {
                player.sendActionBar(Component.text("Unknown ability.", NamedTextColor.RED));
                return;
            }
        }
    }

    // Ability implementations
    public static void supercrit(Player player) {
        UUID playerId = player.getUniqueId();

        if (!supercritCharged.getOrDefault(playerId, false)) {
            player.sendActionBar(Component.text("Supercrit not charged! Crouch for 6 seconds.", NamedTextColor.RED));
            return;
        }

        setCooldown(player, "supercrit", COOLDOWN_SUPERCRIT);
        supercritCharged.put(playerId, false);
        supercritActive.put(playerId, true);

        // Launch player 50 blocks high
        Vector velocity = player.getVelocity();
        velocity.setY(3.5); // Roughly 50 blocks
        player.setVelocity(velocity);

        // Visual effects
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 50, 1, 1, 1, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);

        // Particle trail
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100 || !player.isOnline() || player.isOnGround()) {
                    cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        player.sendActionBar(Component.text("Supercrit activated! Next hit deals 3x damage!", NamedTextColor.GOLD));
    }

    public static void soar(Player player) {
        UUID playerId = player.getUniqueId();

        if (soarActive.getOrDefault(playerId, false)) {
            // Toggle off
            deactivateSoar(player);
            return;
        }

        int activations = soarActivations.getOrDefault(playerId, 0);
        if (activations >= 6) {
            setCooldown(player, "soar", COOLDOWN_SOAR);
            soarActivations.put(playerId, 0);
            player.sendActionBar(Component.text("Soar on cooldown!", NamedTextColor.RED));
            return;
        }

        soarActivations.put(playerId, activations + 1);
        soarActive.put(playerId, true);
        soarStartTime.put(playerId, System.currentTimeMillis());

        // Give elytra
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        player.getInventory().setChestplate(elytra);

        player.sendActionBar(Component.text("Soar activated! (" + soarActivations.get(playerId) + "/6)", NamedTextColor.AQUA));
        player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1.0f, 1.2f);
    }

    public static void mindOverMatter(Player player) {
        setCooldown(player, "mind over matter", COOLDOWN_MIND_OVER_MATTER);

        UUID playerId = player.getUniqueId();
        mindOverMatterEnd.put(playerId, System.currentTimeMillis() + 15000); // 15 seconds

        // Sacrifice 2 hearts (4 health)
        double currentHealth = player.getHealth();
        player.setHealth(Math.max(1, currentHealth - 4));

        // Visual effects
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 15, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.5, 1, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 0.8f, 1.5f);

        player.sendActionBar(Component.text("Mind over Matter! +2 attack damage for 15s", NamedTextColor.RED));
    }

    public static void warriorsLeap(Player player) {
        setCooldown(player, "warriors leap", COOLDOWN_WARRIORS_LEAP);

        UUID playerId = player.getUniqueId();
        // Store the Y position when Warriors Leap is activated
        warriorsLeapStartY.put(playerId, player.getLocation().getY());

        // Launch player 3 blocks high
        Vector velocity = player.getVelocity();
        velocity.setY(1.2); // 3 blocks high
        player.setVelocity(velocity);

        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.0f);
        player.sendActionBar(Component.text("Warriors Leap!", NamedTextColor.GOLD));
    }

    // Method to check if player is currently charging supercrit
    public static boolean isSupercritCharging(Player player) {
        UUID playerId = player.getUniqueId();
        return supercritChargeStart.containsKey(playerId) && player.isSneaking();
    }

    // Method to get supercrit charge progress (0.0 to 1.0)
    public static double getSupercritChargeProgress(Player player) {
        UUID playerId = player.getUniqueId();
        if (!supercritChargeStart.containsKey(playerId)) {
            return 0.0;
        }

        long chargeTime = System.currentTimeMillis() - supercritChargeStart.get(playerId);
        double progress = Math.min(chargeTime / 6000.0, 1.0); // 6 seconds to fully charge
        return progress;
    }

    // Method to check if supercrit is fully charged
    public static boolean isSupercritCharged(Player player) {
        UUID playerId = player.getUniqueId();
        return supercritCharged.getOrDefault(playerId, false);
    }

    // Method to get current soar activations used
    public static int getSoarActivations(Player player) {
        UUID playerId = player.getUniqueId();
        return soarActivations.getOrDefault(playerId, 0);
    }

    // Method to check if soar is currently active
    public static boolean isSoarActive(Player player) {
        UUID playerId = player.getUniqueId();
        return soarActive.getOrDefault(playerId, false);
    }

    // Method to get remaining soar time in seconds
    public static double getSoarTimeRemaining(Player player) {
        UUID playerId = player.getUniqueId();
        if (!soarActive.getOrDefault(playerId, false) || !soarStartTime.containsKey(playerId)) {
            return 0.0;
        }

        long elapsed = System.currentTimeMillis() - soarStartTime.get(playerId);
        long remaining = 20000 - elapsed; // 20 seconds total duration
        return Math.max(remaining / 1000.0, 0.0);
    }

}
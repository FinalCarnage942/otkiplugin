package carnage.otkiplugin.classes;

import carnage.otkiplugin.items.HydronItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Hydron implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_RIPTIDE_RUSH = 30000; // 30 seconds
    private static final long COOLDOWN_WATER_GUN = 45000; // 45 seconds
    private static final long COOLDOWN_WHIRLPOOL = 60000; // 60 seconds
    private static final long COOLDOWN_BUBBLE_FLURRY = 50000; // 50 seconds

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        startPassiveEffectsTask();
    }

    private static void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasHydronClass(player)) {
                        handlePassiveEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private static boolean hasHydronClass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NAUTILUS_SHELL) return false;
        if (item.getItemMeta() == null) return false;
        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return false;
        return displayName.toString().contains("Hydron");
    }

    private static void handlePassiveEffects(Player player) {
        UUID playerId = player.getUniqueId();

        // Dolphine - Speed 3 in water
        if (player.getLocation().getBlock().isLiquid()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 2, false, false));
        }

        // Amphibious - Breathe Underwater Infinitely
        player.setRemainingAir(300);

        // Refresher - In rain get permanent speed
        if (player.getWorld().hasStorm() && player.getWorld().isThundering()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
        }

        // Aquatic Haste - In water/rain get haste
        if (player.getLocation().getBlock().isLiquid() || (player.getWorld().hasStorm() && player.getWorld().isThundering())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, false, false));
        }

        // Aquatic - Permanent Slowness 1 when not in water
        if (!player.getLocation().getBlock().isLiquid()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (!hasHydronClass(attacker)) return;

            // Hydrefinery - Tridents deal 3+ Damage
            if (attacker.getInventory().getItemInMainHand().getType() == Material.TRIDENT) {
                double damage = event.getDamage() + 3.0;
                event.setDamage(damage);
            }

            // Hydreforgery - Trident deal another extra 3 damage in water or while it rains
            if ((attacker.getLocation().getBlock().isLiquid() || (attacker.getWorld().hasStorm() && attacker.getWorld().isThundering()))
                    && attacker.getInventory().getItemInMainHand().getType() == Material.TRIDENT) {
                double damage = event.getDamage() + 3.0;
                event.setDamage(damage);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!hasHydronClass(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // Cycle abilities logic here
            } else {
                String abilityName = HydronItem.getAbilityNameFromPlayer(player);
                useAbility(player, abilityName);
            }
        }
    }

    public static void useAbility(Player player, String ability) {
        switch (ability.toLowerCase()) {
            case "riptide rush":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Riptide Rush is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_RIPTIDE_RUSH);
                riptideRush(player);
                break;
            case "water gun":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Water Gun is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_WATER_GUN);
                waterGun(player);
                break;
            case "whirlpool":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Whirlpool is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_WHIRLPOOL);
                whirlpool(player);
                break;
            case "bubble flurry":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Bubble Flurry is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_BUBBLE_FLURRY);
                bubbleFlurry(player);
                break;
            default:
                player.sendActionBar(Component.text("Unknown ability.", NamedTextColor.RED));
        }
    }

    private static void riptideRush(Player player) {
        if (player.getLocation().getBlock().isLiquid()) {
            Vector direction = player.getLocation().getDirection().multiply(2);
            player.setVelocity(direction);
            player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 50, 1, 1, 1, 0.1);
        }
    }

    private static void waterGun(Player player) {
        Location location = player.getLocation();
        Vector direction = location.getDirection();
        for (int i = 0; i < 5; i++) {
            Location particleLoc = location.clone().add(direction.clone().multiply(i));
            player.getWorld().spawnParticle(Particle.SPLASH, particleLoc, 20, 0.5, 0.5, 0.5, 0.1);
            for (Entity entity : particleLoc.getWorld().getNearbyEntities(particleLoc, 2, 2, 2)) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(1.3, player);
                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                }
            }
        }
    }

    private static void whirlpool(Player player) {
        if (player.getLocation().getBlock().isLiquid()) {
            for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 30, 30, 30)) {
                if (entity instanceof Boat) {
                    entity.setVelocity(new Vector(0, 0, 0));
                } else if (entity instanceof Dolphin) {
                    Vector direction = player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                    entity.setVelocity(direction);
                }
            }
        } else {
            waterGun(player);
        }
    }

    private static void bubbleFlurry(Player player) {
        Location center = player.getLocation();
        for (int i = 0; i < 7; i++) {
            double angle = Math.toRadians(i * (360.0 / 7));
            double x = center.getX() + Math.cos(angle) * 7;
            double z = center.getZ() + Math.sin(angle) * 7;
            Location bubbleLoc = new Location(center.getWorld(), x, center.getY(), z);
            player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, bubbleLoc, 10, 0.5, 0.5, 0.5, 0.1);
            for (Entity entity : bubbleLoc.getWorld().getNearbyEntities(bubbleLoc, 1, 1, 1)) {
                if (entity instanceof LivingEntity && entity != player) {
                    Vector direction = center.toVector().subtract(entity.getLocation().toVector()).normalize();
                    entity.setVelocity(direction.multiply(0.5).setY(0.5));
                    ((LivingEntity) entity).damage(1.0, player);
                }
            }
        }
    }

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
}

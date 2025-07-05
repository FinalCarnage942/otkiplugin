package carnage.otkiplugin.classes;

import carnage.otkiplugin.items.EtherioItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Etherio implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_VOID_PEARL = 30000; // 30 seconds
    private static final long COOLDOWN_ETHEREAL_PLANE = 45000; // 45 seconds
    private static final long COOLDOWN_DARK_PULSE = 60000; // 60 seconds
    private static final long COOLDOWN_ECHO_STEP = 40000; // 40 seconds

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        startPassiveEffectsTask();
    }


    private static void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasEtherioClass(player)) {
                        handlePassiveEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private static boolean hasEtherioClass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.ENDER_PEARL) return false;
        if (item.getItemMeta() == null) return false;
        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return false;
        return displayName.toString().contains("Etherio");
    }

    private static void handlePassiveEffects(Player player) {
        // Shadow Cloak - In Light Level 0 get Invisibility and +20% speed
        if (player.getLocation().getBlock().getLightLevel() == 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
        }

        // Void Sight - All players within 15 blocks have glowing to you
        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
            if (entity instanceof Player) {
                ((Player) entity).setGlowing(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!hasEtherioClass(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            String abilityName = EtherioItem.getAbilityNameFromPlayer(player);
            useAbility(player, abilityName);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (!hasEtherioClass(attacker)) return;

            // Dark Empowerment - Deal 1.3x dmg to all entities with darkness
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) event.getEntity();
                if (entity.hasPotionEffect(PotionEffectType.DARKNESS)) {
                    event.setDamage(event.getDamage() * 1.3);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hasEtherioClass(player)) return;

        // Phaser - Basically an automatic chorus fruit at 2 hearts or below
        if (player.getHealth() <= 4) {
            player.setVelocity(new Vector(0, 1, 0));
        }
    }

    public static void useAbility(Player player, String ability) {
        switch (ability.toLowerCase()) {
            case "void pearl":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Void Pearl is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_VOID_PEARL);
                voidPearl(player);
                break;
            case "ethereal plane":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Ethereal Plane is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_ETHEREAL_PLANE);
                etherealPlane(player);
                break;
            case "dark pulse":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Dark Pulse is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_DARK_PULSE);
                darkPulse(player);
                break;
            case "echo step":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Echo Step is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_ECHO_STEP);
                echoStep(player);
                break;
            default:
                player.sendActionBar(Component.text("Unknown ability.", NamedTextColor.RED));
        }
    }

    private static void voidPearl(Player player) {
        // Logic for Void Pearl ability
        player.launchProjectile(org.bukkit.entity.EnderPearl.class);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 10, 0.1, 0.1, 0.1, 0.01);
    }

    private static void etherealPlane(Player player) {
        // Logic for Ethereal Plane ability
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.1, 0.1, 0.1, 0.01);
    }

    private static void darkPulse(Player player) {
        // Logic for Dark Pulse ability
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1, false, false));
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1, false, false));
            }
        }
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, player.getLocation(), 100, 1, 1, 1, 0.01);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.0f, 1.0f);
    }

    private static void echoStep(Player player) {
        // Logic for Echo Step ability
        Entity target = getTargetEntity(player);
        if (target != null) {
            Location behindTarget = target.getLocation().subtract(target.getLocation().getDirection().multiply(2));
            player.teleport(behindTarget);
            player.getWorld().spawnParticle(Particle.SCULK_SOUL, player.getLocation(), 10, 0.1, 0.1, 0.1, 0.01);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0f, 1.0f);
        }
    }

    private static Entity getTargetEntity(Player player) {
        List<Entity> nearby = player.getNearbyEntities(10, 10, 10);
        for (int i = 0; i < 100; i++) {
            Location point = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(i * 0.25));
            for (Entity e : nearby) {
                if (e.getBoundingBox().contains(point.toVector()) && e != player) {
                    return e;
                }
            }
        }
        return null;
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

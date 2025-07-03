package carnage.otkiplugin.classes;

import carnage.otkiplugin.items.DraculoxItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Draculox implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_VAPORIZE = 45000; // 45 seconds
    private static final long COOLDOWN_BLOODSUCK = 30000; // 30 seconds
    private static final long COOLDOWN_BLOOD_DEMONS = 50000; // 50 seconds
    private static final long COOLDOWN_BLOODPACT = 60000; // 60 seconds

    private static final Map<UUID, Integer> bloodlustStacks = new HashMap<>();
    private static final Map<UUID, Integer> hitCount = new HashMap<>();

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        startPassiveEffectsTask();
    }

    private static void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasDraculoxClass(player)) {
                        handlePassiveEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private static boolean hasDraculoxClass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_POWDER) return false;
        if (item.getItemMeta() == null) return false;
        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return false;
        return displayName.toString().contains("Draculox");
    }

    private static void handlePassiveEffects(Player player) {
        UUID playerId = player.getUniqueId();

        // Bloodmoon - +10% speed and +0.5 dmg at night
        if (player.getWorld().getTime() >= 13000 && player.getWorld().getTime() <= 23000) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
            player.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(player.getAttribute(Attribute.ATTACK_DAMAGE).getValue() + 0.5);
        }

        // Dark Resilience - Below 3 hearts, gain 20% damage reduction, and all cooldowns reset
        if (player.getHealth() <= 6) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, false, false));
            resetCooldowns(player);
        }
    }

    private static void resetCooldowns(Player player) {
        abilityCooldowns.remove(player.getUniqueId());
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (!hasDraculoxClass(attacker)) return;

            // Bloody Frenzy - Every 3 hits to victim heals half a heart
            int hits = hitCount.getOrDefault(attacker.getUniqueId(), 0) + 1;
            hitCount.put(attacker.getUniqueId(), hits);
            if (hits >= 3) {
                attacker.setHealth(Math.min(attacker.getHealth() + 1, attacker.getAttribute(Attribute.MAX_HEALTH).getValue()));
                hitCount.put(attacker.getUniqueId(), 0);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!hasDraculoxClass(player)) return;

        // Bloodline - Keep 30% randomly of your inventory upon death
        Random random = new Random();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && random.nextDouble() > 0.3) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.getInventory().remove(item);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (victim.getKiller() != null && hasDraculoxClass(victim.getKiller())) {
                Player killer = victim.getKiller();

                // Bloodlust - When you kill a player, gain +2 dmg and +20% speed for 45 seconds (stackable)
                int stacks = bloodlustStacks.getOrDefault(killer.getUniqueId(), 0) + 1;
                bloodlustStacks.put(killer.getUniqueId(), stacks);
                killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 900, stacks - 1, false, false));
                killer.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(killer.getAttribute(Attribute.ATTACK_DAMAGE).getValue() + 2);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        bloodlustStacks.put(killer.getUniqueId(), stacks - 1);
                        if (bloodlustStacks.get(killer.getUniqueId()) == 0) {
                            bloodlustStacks.remove(killer.getUniqueId());
                        }
                    }
                }.runTaskLater(plugin, 900L); // 45 seconds
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!hasDraculoxClass(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // Cycle abilities logic here
            } else {
                String abilityName = DraculoxItem.getAbilityNameFromPlayer(player);
                useAbility(player, abilityName);
            }
        }
    }

    public static void useAbility(Player player, String ability) {
        switch (ability.toLowerCase()) {
            case "vaporize":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Vaporize is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_VAPORIZE);
                vaporize(player);
                break;
            case "bloodsuck":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Bloodsuck is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_BLOODSUCK);
                bloodsuck(player);
                break;
            case "blood demons":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Blood Demons is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_BLOOD_DEMONS);
                bloodDemons(player);
                break;
            case "bloodpact":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Bloodpact is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_BLOODPACT);
                bloodpact(player);
                break;
            default:
                player.sendActionBar(Component.text("Unknown ability.", NamedTextColor.RED));
        }
    }

    private static void vaporize(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 160, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 160, 0, false, false));
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 100, 1, 1, 1, 0.1);
    }

    private static void bloodsuck(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        Location targetLocation = eyeLocation.clone().add(direction.multiply(10));

        player.getWorld().spawnParticle(Particle.CRIMSON_SPORE, eyeLocation, 10, direction.getX(), direction.getY(), direction.getZ(), 1, new Particle.DustOptions(Color.RED, 1));
        player.getWorld().spawnParticle(Particle.HEART, targetLocation, 10, direction.getX(), direction.getY(), direction.getZ(), 1);

        for (Entity entity : player.getWorld().getNearbyEntities(targetLocation, 2, 2, 2)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(2, player);
                player.setHealth(Math.min(player.getHealth() + 4, player.getAttribute(Attribute.MAX_HEALTH).getValue()));
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
    }

    private static void bloodDemons(Player player) {
        for (int i = 0; i < 5; i++) {
            player.getWorld().spawnEntity(player.getLocation(), org.bukkit.entity.EntityType.VEX);
        }
        player.getWorld().spawnParticle(Particle.CRIMSON_SPORE, player.getLocation(), 100, 1, 1, 1, 1, new Particle.DustOptions(Color.MAROON, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
    }

    private static void bloodpact(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0, false, false));

        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0, false, false));
            }
        }.runTaskLater(plugin, 200L); // 10 seconds
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

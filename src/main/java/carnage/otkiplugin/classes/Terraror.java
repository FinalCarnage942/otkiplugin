package carnage.otkiplugin.classes;

import carnage.otkiplugin.items.TerrarorItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Terraror implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_DUST_STORM = 40000; // 40 seconds
    private static final long COOLDOWN_REINFORCEMENT = 30000; // 30 seconds
    private static final long COOLDOWN_BONEY_SMASH = 35000; // 35 seconds
    private static final long COOLDOWN_METEOR_TRAPS = 50000; // 50 seconds

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        startPassiveEffectsTask();
    }

    private static void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasTerrarorClass(player)) {
                        handlePassiveEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private static boolean hasTerrarorClass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BONE) return false;
        if (item.getItemMeta() == null) return false;
        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return false;
        return displayName.toString().contains("Terraror");
    }

    private static void handlePassiveEffects(Player player) {
        // Natural Protection - +3 hearts
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(26.0);

        // Fossilized - For every stack of bones in your inventory, gain half an armor bar
        int boneStacks = countBoneStacks(player);
        double armorBonus = boneStacks * 2.0; // Half an armor bar per stack
        player.getAttribute(Attribute.ARMOR).setBaseValue(armorBonus);

        // Suspicious Grace - Can craft suspicious sand and gravel, and when you stand on it, you get 2+ hearts and regen 1, and +20% speed
        if (isStandingOnSuspiciousBlocks(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 40, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
        }

        // Heavyweight - 15% Slower with .1 slower attack speed
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false));

        // Low Stamina - Permanent hunger
        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0, false, false));
    }

    private static int countBoneStacks(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BONE) {
                count += item.getAmount() / 64;
            }
        }
        return count;
    }

    private static boolean isStandingOnSuspiciousBlocks(Player player) {
        Location loc = player.getLocation().subtract(0, 1, 0);
        Material blockType = loc.getBlock().getType();
        return blockType == Material.SUSPICIOUS_SAND || blockType == Material.SUSPICIOUS_GRAVEL;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (!hasTerrarorClass(attacker)) return;

            // Claws - +1 Attack Damage
            event.setDamage(event.getDamage() + 1);

            // Ironskin - Take 15% less kb from all hits
            Vector velocity = event.getEntity().getVelocity();
            event.getEntity().setVelocity(velocity.multiply(0.85));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!hasTerrarorClass(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // Cycle abilities logic here
            } else {
                String abilityName = TerrarorItem.getAbilityNameFromPlayer(player);
                useAbility(player, abilityName);
            }
        }
    }

    public static void useAbility(Player player, String ability) {
        switch (ability.toLowerCase()) {
            case "dust storm":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Dust Storm is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_DUST_STORM);
                dustStorm(player);
                break;
            case "reinforcement":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Reinforcement is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_REINFORCEMENT);
                reinforcement(player);
                break;
            case "boney smash":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Boney Smash is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_BONEY_SMASH);
                boneySmash(player);
                break;
            case "meteor traps":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Meteor Traps is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_METEOR_TRAPS);
                meteorTraps(player);
                break;
            default:
                player.sendActionBar(Component.text("Unknown ability.", NamedTextColor.RED));
        }
    }

    private static void dustStorm(Player player) {
        Location center = player.getLocation();
        for (Entity entity : center.getWorld().getNearbyEntities(center, 10, 10, 10)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector direction = center.toVector().subtract(entity.getLocation().toVector()).normalize();
                entity.setVelocity(direction.multiply(0.5).setY(1));
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks >= 100) { // 10 seconds
                            cancel();
                            return;
                        }
                        ((LivingEntity) entity).damage(1, player);
                        ticks += 10; // Every 10 ticks
                    }
                }.runTaskTimer(plugin, 0L, 10L);
            }
        }
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, center, 100, 5, 5, 5, 0.1);
    }

    private static void reinforcement(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 140, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 140, 2, false, false));
        player.getWorld().spawnParticle(Particle.CRIMSON_SPORE, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
    }

    private static void boneySmash(Player player) {
        Location center = player.getLocation();
        for (Entity entity : center.getWorld().getNearbyEntities(center, 5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).damage(8, player);
                entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.BONE, 1));
            }
        }
        player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, center, 100, 1, 1, 1, 0.1, Material.BONE_BLOCK.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
    }

    private static void meteorTraps(Player player) {
        Location center = player.getLocation();
        for (Entity entity : center.getWorld().getNearbyEntities(center, 12, 12, 12)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector direction = center.toVector().subtract(entity.getLocation().toVector()).normalize();
                entity.setVelocity(direction.multiply(0.5).setY(1));
                entity.getLocation().getBlock().setType(Material.COBWEB);
                ((LivingEntity) entity).damage(8, player);
            }
        }
        player.getWorld().spawnParticle(Particle.FLAME, center, 200, 5, 5, 5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
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

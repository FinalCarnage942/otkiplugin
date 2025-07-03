package carnage.otkiplugin.classes;

import carnage.otkiplugin.items.CavernonItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
import java.util.Random;
import java.util.UUID;

public class Cavernon implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();
    private static final long COOLDOWN_PITCH = 30000; // 30 seconds
    private static final long COOLDOWN_GROUND_SLAM = 20000; // 20 seconds
    private static final long COOLDOWN_EARTHQUAKE = 40000; // 40 seconds
    private static final long COOLDOWN_STONE_SLIDE = 20000; // 20 seconds

    private static final Map<UUID, Integer> stoneskipCount = new HashMap<>();
    private static final Map<UUID, Long> lastStoneskip = new HashMap<>();
    private static final Map<UUID, Boolean> stoneSlideActive = new HashMap<>();
    private static final Map<UUID, Long> stoneSlideStartTime = new HashMap<>();

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        startPassiveEffectsTask();
    }

    private static void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (hasCavernonClass(player)) {
                        handlePassiveEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private static boolean hasCavernonClass(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.STONE_PICKAXE) return false;
        if (item.getItemMeta() == null) return false;
        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return false;
        return displayName.toString().contains("Cavernon");
    }

    private static void handlePassiveEffects(Player player) {
        UUID playerId = player.getUniqueId();

        // Rocky Skin - Gain Resistance 2 when 3 Hearts or Below
        if (player.getHealth() <= 6) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1, false, false));
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1, Material.STONE.createBlockData());
        }

        // Cavernous Strength - 10% Faster and 5% Stronger when Below Y=-20
        if (player.getLocation().getY() < -20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0, false, false));
        }

        // Heavyweight - Permanent 10% less speed
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!hasCavernonClass(player)) return;

        // Natures Luck - 10% Chance for Ores and Minerals to Drop Double When mined for the first time
        Random random = new Random();
        if (random.nextDouble() < 0.10) {
            event.setDropItems(true);
            event.getBlock().getDrops().forEach(itemStack -> {
                itemStack.setAmount(itemStack.getAmount() * 2);
                player.getWorld().dropItemNaturally(event.getBlock().getLocation(), itemStack);
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hasCavernonClass(player)) return;

        // Stoneskip - Leap on water twice before sinking
        if (player.getLocation().getBlock().isLiquid() && player.getLocation().getBlock().getType() == Material.WATER) {
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            long lastSkip = lastStoneskip.getOrDefault(playerId, 0L);
            int skipCount = stoneskipCount.getOrDefault(playerId, 0);

            if (currentTime - lastSkip >= 1000 && skipCount < 2) { // 1 second cooldown between skips
                Vector velocity = player.getVelocity();
                velocity.setY(0.8); // Leap effect
                player.setVelocity(velocity);
                player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
                stoneskipCount.put(playerId, skipCount + 1);
                lastStoneskip.put(playerId, currentTime);
            }
        } else {
            stoneskipCount.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!hasCavernonClass(player)) return;

        // Sinking Stone - You sink much faster and move much slower in water
        if (player.getLocation().getBlock().isLiquid()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, false, false));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!hasCavernonClass(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // Cycle abilities logic here
            } else {
                String abilityName = CavernonItem.getAbilityNameFromPlayer(player);
                useAbility(player, abilityName);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!hasCavernonClass(player)) return;

        if (!event.isSneaking() && stoneSlideActive.getOrDefault(player.getUniqueId(), false)) {
            deactivateStoneSlide(player);
        }
    }

    public static void useAbility(Player player, String ability) {
        switch (ability.toLowerCase()) {
            case "pitch":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Pitch is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_PITCH);
                pitch(player);
                break;
            case "ground slam":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Ground Slam is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_GROUND_SLAM);
                groundSlam(player);
                break;
            case "earthquake":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Earthquake is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_EARTHQUAKE);
                earthquake(player);
                break;
            case "stone slide":
                if (getCooldownRemaining(player, ability) > 0) {
                    player.sendActionBar(Component.text("Stone Slide is on cooldown!", NamedTextColor.RED));
                    return;
                }
                setCooldown(player, ability, COOLDOWN_STONE_SLIDE);
                stoneSlide(player);
                break;
            default:
                player.sendActionBar(Component.text("Unknown ability.", NamedTextColor.RED));
        }
    }

    private static void pitch(Player player) {
        Location blockLocation = player.getLocation().subtract(0, 1, 0);
        if (blockLocation.getBlock().getType() != Material.AIR) {
            Material blockType = blockLocation.getBlock().getType();
            blockLocation.getBlock().setType(Material.AIR);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, blockLocation, 50, 0.5, 0.5, 0.5, 0.1, blockType.createBlockData());

            Vector direction = player.getLocation().getDirection().multiply(2);
            ItemStack thrownBlock = new ItemStack(blockType);
            player.getWorld().dropItem(player.getEyeLocation(), thrownBlock).setVelocity(direction);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        }
    }

    private static void groundSlam(Player player) {
        for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
            if (entity instanceof LivingEntity && entity != player) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 2, false, false));
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 4, false, false));
        player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 100, 1, 1, 1, 0.1, Material.STONE.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }

    private static void earthquake(Player player) {
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(-2);
                entity.setVelocity(direction);
            }
        }
        for (int i = 1; i <= 3; i++) {
            for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 8) {
                double x = player.getLocation().getX() + Math.cos(angle) * i;
                double z = player.getLocation().getZ() + Math.sin(angle) * i;
                Location particleLoc = new Location(player.getWorld(), x, player.getLocation().getY() + 1, z);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, particleLoc, 20, 0.5, 0.5, 0.5, 0.1, Material.STONE.createBlockData());
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
    }

    private static void stoneSlide(Player player) {
        UUID playerId = player.getUniqueId();
        if (stoneSlideActive.getOrDefault(playerId, false)) {
            deactivateStoneSlide(player);
            return;
        }
        stoneSlideActive.put(playerId, true);
        stoneSlideStartTime.put(playerId, System.currentTimeMillis());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!stoneSlideActive.getOrDefault(playerId, false) || !player.isOnline()) {
                    cancel();
                    return;
                }
                Vector direction = player.getLocation().getDirection().multiply(2);
                player.setVelocity(direction);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1, Material.STONE.createBlockData());
            }
        }.runTaskTimer(plugin, 0L, 5L); // Run every 5 ticks

        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - stoneSlideStartTime.get(playerId) >= 8000) { // 8 seconds
                    deactivateStoneSlide(player);
                }
            }
        }.runTaskLater(plugin, 160L); // Check after 8 seconds
    }

    private static void deactivateStoneSlide(Player player) {
        UUID playerId = player.getUniqueId();
        stoneSlideActive.put(playerId, false);
        stoneSlideStartTime.remove(playerId);
        player.sendActionBar(Component.text("Stone Slide ended!", NamedTextColor.GRAY));
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

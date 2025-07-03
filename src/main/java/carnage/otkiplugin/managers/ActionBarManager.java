package carnage.otkiplugin.managers;

import carnage.otkiplugin.classes.Aetheri;
import carnage.otkiplugin.classes.Inferno;
import carnage.otkiplugin.items.AetheriItem;
import carnage.otkiplugin.items.InfernoItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarManager {

    private static ActionBarManager instance;
    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> playersWithActionBar = new HashMap<>();
    private BukkitRunnable actionBarTask;

    public ActionBarManager(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        startActionBarTask();
    }

    public static ActionBarManager getInstance() {
        return instance;
    }

    private void startActionBarTask() {
        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updatePlayerActionBar(player);
                }
            }
        };
        // Run every 5 ticks (4 times per second) for smooth updates
        actionBarTask.runTaskTimer(plugin, 0L, 5L);
    }

    private void updatePlayerActionBar(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        // Check if player is holding an Inferno item
        if (isInfernoItem(mainHand)) {
            String abilityName = InfernoItem.getAbilityNameFromPlayer(player);
            double cooldown = Inferno.getCooldownRemaining(player, abilityName);
            String actionBarText = formatActionBar(abilityName, cooldown);
            player.sendActionBar(actionBarText);
            playersWithActionBar.put(player.getUniqueId(), true);
        }
        // Check if player is holding an Aetheri item
        else if (isAetheriItem(mainHand)) {
            String abilityName = AetheriItem.getAbilityNameFromPlayer(player);
            double cooldown = Aetheri.getCooldownRemaining(player, abilityName);
            String actionBarText = formatAetheriActionBar(player, abilityName, cooldown);
            player.sendActionBar(actionBarText);
            playersWithActionBar.put(player.getUniqueId(), true);
        }
        else {
            // Clear action bar if player was previously showing it
            if (playersWithActionBar.getOrDefault(player.getUniqueId(), false)) {
                player.sendActionBar("");
                playersWithActionBar.put(player.getUniqueId(), false);
            }
        }
    }

    private boolean isInfernoItem(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        if (item.getItemMeta() == null) return false;
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equalsIgnoreCase("Inferno");
    }

    private boolean isAetheriItem(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER) return false;
        if (item.getItemMeta() == null) return false;
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equalsIgnoreCase("Aetheri");
    }

    private String formatActionBar(String abilityName, double cooldown) {
        String cooldownText;
        ChatColor cooldownColor;
        if (cooldown > 0) {
            cooldownText = String.format("%.1fs", cooldown);
            cooldownColor = ChatColor.RED;
        } else {
            cooldownText = "Ready";
            cooldownColor = ChatColor.GREEN;
        }
        return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + cooldownColor + cooldownText;
    }

    private String formatAetheriActionBar(Player player, String abilityName, double cooldown) {
        UUID playerId = player.getUniqueId();

        if (abilityName.equalsIgnoreCase("supercrit")) {
            if (cooldown > 0) {
                // On cooldown
                return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.RED + String.format("%.1fs", cooldown);
            } else {
                // Check if currently charging
                if (Aetheri.isSupercritCharging(player)) {
                    double chargeProgress = Aetheri.getSupercritChargeProgress(player);
                    int chargePercentage = (int) (chargeProgress * 100);

                    if (chargeProgress >= 1.0) {
                        // Fully charged
                        return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.GREEN + "Charged!";
                    } else {
                        // Charging in progress
                        return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.YELLOW + "Charging " + chargePercentage + "%";
                    }
                } else if (Aetheri.isSupercritCharged(player)) {
                    // Charged and ready to use
                    return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.GREEN + "Charged!";
                } else {
                    // Not charging or not charged
                    return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.GRAY + "Hold Sneak to Charge";
                }
            }
        }
        else if (abilityName.equalsIgnoreCase("soar")) {
            if (cooldown > 0) {
                // On cooldown
                return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.RED + String.format("%.1fs", cooldown);
            } else {
                // Show activations remaining
                int activations = Aetheri.getSoarActivations(player);
                int remainingActivations = 6 - activations;

                if (remainingActivations > 0) {
                    if (Aetheri.isSoarActive(player)) {
                        // Currently soaring
                        double soarTimeRemaining = Aetheri.getSoarTimeRemaining(player);
                        return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.AQUA + "Active " +
                                String.format("%.1fs", soarTimeRemaining) + ChatColor.WHITE + " | " + ChatColor.YELLOW +
                                "Activations: " + activations + "/6";
                    } else {
                        // Ready to use
                        return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.GREEN + "Ready" +
                                ChatColor.WHITE + " | " + ChatColor.YELLOW + "Activations: " + activations + "/6";
                    }
                } else {
                    // All activations used and  will go on cooldown
                    return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.GOLD + "Last Use Available";
                }
            }
        }
        // Default formatting for other abilities
        else {
            String cooldownText;
            ChatColor cooldownColor;
            if (cooldown > 0) {
                cooldownText = String.format("%.1fs", cooldown);
                cooldownColor = ChatColor.RED;
            } else {
                cooldownText = "Ready";
                cooldownColor = ChatColor.GREEN;
            }
            return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + cooldownColor + cooldownText;
        }
    }

    public void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        // Clear all action bars
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (playersWithActionBar.getOrDefault(player.getUniqueId(), false)) {
                player.sendActionBar("");
            }
        }
        playersWithActionBar.clear();
    }

    // Method to temporarily show a message (like ability switch notification)
    public void showTemporaryMessage(Player player, String message, int durationTicks) {
        UUID playerId = player.getUniqueId();
        // Show the temporary message
        player.sendActionBar(message);
        // Schedule to resume normal action bar after the duration
        new BukkitRunnable() {
            @Override
            public void run() {
                // Only resume if player is still online and holding a class item
                if (player.isOnline() && (isInfernoItem(player.getInventory().getItemInMainHand()) ||
                        isAetheriItem(player.getInventory().getItemInMainHand()))) {
                    updatePlayerActionBar(player);
                }
            }
        }.runTaskLater(plugin, durationTicks);
    }
}
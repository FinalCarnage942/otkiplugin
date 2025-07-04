package carnage.otkiplugin.managers;

import carnage.otkiplugin.classes.*;
import carnage.otkiplugin.items.*;
import net.kyori.adventure.text.Component;
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
        actionBarTask.runTaskTimer(plugin, 0L, 5L);
    }

    private void updatePlayerActionBar(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (isInfernoItem(mainHand)) {
            String abilityName = InfernoItem.getAbilityNameFromPlayer(player);
            double cooldown = Inferno.getCooldownRemaining(player, abilityName);
            player.sendActionBar(formatActionBar(abilityName, cooldown));
        } else if (isAetheriItem(mainHand)) {
            String abilityName = AetheriItem.getAbilityNameFromPlayer(player);
            if (abilityName.equals("Soar")) {
                int activations = Aetheri.getSoarActivations(player);
                player.sendActionBar(formatActionBar(abilityName, 0, activations));
            } else {
                double cooldown = Aetheri.getCooldownRemaining(player, abilityName);
                player.sendActionBar(formatActionBar(abilityName, cooldown));
            }
        } else if (isHydronItem(mainHand)) {
            String abilityName = HydronItem.getAbilityNameFromPlayer(player);
            double cooldown = Hydron.getCooldownRemaining(player, abilityName);
            player.sendActionBar(formatActionBar(abilityName, cooldown));
        } else if (isCavernonItem(mainHand)) {
            String abilityName = CavernonItem.getAbilityNameFromPlayer(player);
            double cooldown = Cavernon.getCooldownRemaining(player, abilityName);
            player.sendActionBar(formatActionBar(abilityName, cooldown));
        } else if (isTerrarorItem(mainHand)) {
            String abilityName = TerrarorItem.getAbilityNameFromPlayer(player);
            double cooldown = Terraror.getCooldownRemaining(player, abilityName);
            player.sendActionBar(formatActionBar(abilityName, cooldown));
        } else if (isDraculoxItem(mainHand)) {
            String abilityName = DraculoxItem.getAbilityNameFromPlayer(player);
            double cooldown = Draculox.getCooldownRemaining(player, abilityName);
            player.sendActionBar(formatActionBar(abilityName, cooldown));
        } else {
            player.sendActionBar("");
        }
    }

    private boolean isInfernoItem(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD || item.getItemMeta() == null) return false;
        return item.getItemMeta().hasDisplayName() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Inferno");
    }

    private boolean isAetheriItem(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER || item.getItemMeta() == null) return false;
        return item.getItemMeta().hasDisplayName() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Aetheri");
    }

    private boolean isHydronItem(ItemStack item) {
        if (item == null || item.getType() != Material.NAUTILUS_SHELL || item.getItemMeta() == null) return false;
        return item.getItemMeta().hasDisplayName() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Hydron");
    }

    private boolean isCavernonItem(ItemStack item) {
        if (item == null || item.getType() != Material.STONE_PICKAXE || item.getItemMeta() == null) return false;
        return item.getItemMeta().hasDisplayName() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Cavernon");
    }

    private boolean isTerrarorItem(ItemStack item) {
        if (item == null || item.getType() != Material.BONE || item.getItemMeta() == null) return false;
        return item.getItemMeta().hasDisplayName() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Terraror");
    }

    private boolean isDraculoxItem(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_POWDER || item.getItemMeta() == null) return false;
        return item.getItemMeta().hasDisplayName() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Draculox");
    }

    private String formatActionBar(String abilityName, double cooldown) {
        String cooldownText = cooldown > 0 ? String.format("%.1fs", cooldown) : "Ready";
        ChatColor cooldownColor = cooldown > 0 ? ChatColor.RED : ChatColor.GREEN;
        return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + cooldownColor + cooldownText;
    }

    private String formatActionBar(String abilityName, double cooldown, int activations) {
        if (abilityName.equals("Soar")) {
            return ChatColor.GOLD + abilityName + ChatColor.WHITE + " : " + ChatColor.AQUA + activations + "/6 activations";
        } else {
            return formatActionBar(abilityName, cooldown);
        }
    }

    public void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendActionBar("");
        }
    }
}

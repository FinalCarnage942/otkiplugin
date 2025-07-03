package carnage.otkiplugin.items;

import carnage.otkiplugin.classes.Inferno;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class InfernoItem {
    private static NamespacedKey ABILITY_KEY;

    public static void init(JavaPlugin plugin) {
        ABILITY_KEY = new NamespacedKey(plugin, "inferno_ability");
    }

    public static ItemStack createInfernoItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Inferno");

        // Set default ability 1 (Flare Blitz) in persistent data
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, 1);

        // Lore shows abilities and current selected
        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Abilities:",
                ChatColor.RED + "1. Flare Blitz",
                ChatColor.RED + "2. Spinzitu",
                ChatColor.RED + "3. Ring of Fire",
                ChatColor.GREEN + "Current Ability: " + getAbilityName(1),
                ChatColor.GRAY + "Right-click to use ability",
                ChatColor.GRAY + "Shift + Right-click to cycle abilities"
        ));

        item.setItemMeta(meta);
        return item;
    }

    public static void cycleAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int current = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        int next = (current % 3) + 1;
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, next);

        // Update lore current ability line (5th line, index 4)
        if (meta.hasLore()) {
            var lore = meta.getLore();
            if (lore != null && lore.size() >= 5) {
                lore.set(4, ChatColor.GREEN + "Current Ability: " + getAbilityName(next));
                meta.setLore(lore);
            }
        }

        item.setItemMeta(meta);
    }

    public static void useAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        String abilityName = getAbilityNameStripped(ability);

        // Use the Inferno class's useAbility method which handles cooldowns
        Inferno.useAbility(player, abilityName);
    }

    public static String getAbilityName(int ability) {
        return switch (ability) {
            case 1 -> ChatColor.RED + "Flare Blitz";
            case 2 -> ChatColor.RED + "Spinzitu";
            case 3 -> ChatColor.RED + "Ring of Fire";
            default -> ChatColor.RED + "Unknown";
        };
    }

    public static String getAbilityNameStripped(int ability) {
        return switch (ability) {
            case 1 -> "Flare Blitz";
            case 2 -> "Spinzitu";
            case 3 -> "Ring of Fire";
            default -> "Unknown";
        };
    }

    // Helper to get current ability from player item
    public static String getAbilityNameFromPlayer(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_ROD) return "None";

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "None";

        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        return getAbilityNameStripped(ability);
    }
}
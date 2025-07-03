package carnage.otkiplugin.items;

import carnage.otkiplugin.classes.Hydron;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class HydronItem {

    private static NamespacedKey ABILITY_KEY;

    public static void init(JavaPlugin plugin) {
        ABILITY_KEY = new NamespacedKey(plugin, "hydron_ability");
    }

    public static ItemStack createHydronItem() {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Hydron");
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Abilities:",
                ChatColor.BLUE + "1. Riptide Rush",
                ChatColor.BLUE + "2. Water Gun",
                ChatColor.BLUE + "3. Whirlpool",
                ChatColor.BLUE + "4. Bubble Flurry",
                ChatColor.GREEN + "Current Ability: " + getAbilityName(1),
                ChatColor.GRAY + "Right-click to use ability",
                ChatColor.GRAY + "Shift + Right-click to cycle abilities"
        ));
        item.setItemMeta(meta);
        return item;
    }

    public static void cycleAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NAUTILUS_SHELL) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int current = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        int next = (current % 4) + 1;
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, next);
        if (meta.hasLore()) {
            var lore = meta.getLore();
            if (lore != null && lore.size() >= 6) {
                lore.set(4, ChatColor.GREEN + "Current Ability: " + getAbilityName(next));
                meta.setLore(lore);
            }
        }
        item.setItemMeta(meta);
    }

    public static void useAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NAUTILUS_SHELL) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        String abilityName = getAbilityNameStripped(ability);
        Hydron.useAbility(player, abilityName);
    }

    public static String getAbilityName(int ability) {
        return switch (ability) {
            case 1 -> ChatColor.BLUE + "Riptide Rush";
            case 2 -> ChatColor.BLUE + "Water Gun";
            case 3 -> ChatColor.BLUE + "Whirlpool";
            case 4 -> ChatColor.BLUE + "Bubble Flurry";
            default -> ChatColor.BLUE + "Unknown";
        };
    }

    public static String getAbilityNameStripped(int ability) {
        return switch (ability) {
            case 1 -> "Riptide Rush";
            case 2 -> "Water Gun";
            case 3 -> "Whirlpool";
            case 4 -> "Bubble Flurry";
            default -> "Unknown";
        };
    }

    public static String getAbilityNameFromPlayer(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NAUTILUS_SHELL) return "None";
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "None";
        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        return getAbilityNameStripped(ability);
    }
}

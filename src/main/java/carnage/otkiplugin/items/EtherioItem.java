package carnage.otkiplugin.items;

import carnage.otkiplugin.classes.Etherio;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class EtherioItem {

    private static NamespacedKey ABILITY_KEY;

    public static void init(JavaPlugin plugin) {
        ABILITY_KEY = new NamespacedKey(plugin, "etherio_ability");
    }

    public static ItemStack createEtherioItem() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Etherio");
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, 1);

        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Abilities:",
                ChatColor.DARK_PURPLE + "1. Void Pearl",
                ChatColor.DARK_PURPLE + "2. Ethereal Plane",
                ChatColor.DARK_PURPLE + "3. Dark Pulse",
                ChatColor.DARK_PURPLE + "4. Echo Step",
                ChatColor.GREEN + "Current Ability: " + getAbilityName(1),
                ChatColor.GRAY + "Right-click to use ability",
                ChatColor.GRAY + "Shift + Right-click to cycle abilities"
        ));

        item.setItemMeta(meta);
        return item;
    }

    public static void cycleAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.ENDER_PEARL) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int current = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        int next = (current % 4) + 1;
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, next);

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 6) {
                lore.set(5, ChatColor.GREEN + "Current Ability: " + getAbilityName(next));
                meta.setLore(lore);
            }
        }

        item.setItemMeta(meta);
    }

    public static void useAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.ENDER_PEARL) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        String abilityName = getAbilityNameStripped(ability);
        Etherio.useAbility(player, abilityName);
    }

    public static String getAbilityName(int ability) {
        return switch (ability) {
            case 1 -> ChatColor.DARK_PURPLE + "Void Pearl";
            case 2 -> ChatColor.DARK_PURPLE + "Ethereal Plane";
            case 3 -> ChatColor.DARK_PURPLE + "Dark Pulse";
            case 4 -> ChatColor.DARK_PURPLE + "Echo Step";
            default -> "Unknown";
        };
    }

    public static String getAbilityNameStripped(int ability) {
        return switch (ability) {
            case 1 -> "Void Pearl";
            case 2 -> "Ethereal Plane";
            case 3 -> "Dark Pulse";
            case 4 -> "Echo Step";
            default -> "Unknown";
        };
    }

    public static String getAbilityNameFromPlayer(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.ENDER_PEARL) return "None";
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "None";
        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        return getAbilityNameStripped(ability);
    }
}

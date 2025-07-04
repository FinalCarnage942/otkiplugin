package carnage.otkiplugin.items;

import carnage.otkiplugin.classes.Draculox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class DraculoxItem {

    private static NamespacedKey ABILITY_KEY;

    public static void init(JavaPlugin plugin) {
        ABILITY_KEY = new NamespacedKey(plugin, "draculox_ability");
    }

    public static ItemStack createDraculoxItem() {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(org.bukkit.ChatColor.DARK_RED + "Draculox"));
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Abilities:",
                ChatColor.DARK_RED + "1. Vaporize",
                ChatColor.DARK_RED + "2. Bloodsuck",
                ChatColor.DARK_RED + "3. Blood Demons",
                ChatColor.DARK_RED + "4. Bloodpact",
                ChatColor.GREEN + "Current Ability: " + getAbilityName(1),
                ChatColor.GRAY + "Right-click to use ability",
                ChatColor.GRAY + "Shift + Right-click to cycle abilities"
        ));
        item.setItemMeta(meta);
        return item;
    }


    public static void cycleAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_POWDER) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int current = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        int next = (current % 4) + 1;
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, next);

        // Update the lore to reflect the current ability
        if (meta.hasLore()) {
            var lore = meta.getLore();
            if (lore != null && lore.size() > 6) {
                lore.set(5, org.bukkit.ChatColor.GREEN + "Current Ability: " + getAbilityName(next));
                meta.setLore(lore);
            }
        }
        item.setItemMeta(meta);
    }


    public static void useAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_POWDER) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        String abilityName = getAbilityNameStripped(ability);
        Draculox.useAbility(player, abilityName);
    }

    public static String getAbilityName(int ability) {
        return switch (ability) {
            case 1 -> ChatColor.DARK_RED +"Vaporize";
            case 2 -> ChatColor.DARK_RED +"Bloodsuck";
            case 3 -> ChatColor.DARK_RED +"Blood Demons";
            case 4 -> ChatColor.DARK_RED +"Bloodpact";
            default -> "Unknown";
        };
    }

    public static String getAbilityNameStripped(int ability) {
        return switch (ability) {
            case 1 -> "Vaporize";
            case 2 -> "Bloodsuck";
            case 3 -> "Blood Demons";
            case 4 -> "Bloodpact";
            default -> "Unknown";
        };
    }

    public static String getAbilityNameFromPlayer(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BLAZE_POWDER) return "None";
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "None";
        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        return getAbilityNameStripped(ability);
    }
}

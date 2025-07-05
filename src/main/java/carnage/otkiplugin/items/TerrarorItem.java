package carnage.otkiplugin.items;

import carnage.otkiplugin.classes.Terraror;
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

public class TerrarorItem {

    private static NamespacedKey ABILITY_KEY;

    public static void init(JavaPlugin plugin) {
        ABILITY_KEY = new NamespacedKey(plugin, "terraror_ability");
    }

    public static ItemStack createTerrarorItem() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Terraror");
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Abilities:",
                ChatColor.DARK_RED + "1. Dust Storm",
                ChatColor.DARK_RED + "2. Reinforcement",
                ChatColor.DARK_RED + "3. Boney Smash",
                ChatColor.DARK_RED + "4. Meteor Traps",
                ChatColor.GREEN + "Current Ability: " + getAbilityName(1),
                ChatColor.GRAY + "Right-click to use ability",
                ChatColor.GRAY + "Shift + Right-click to cycle abilities"
        ));
        item.setItemMeta(meta);
        return item;
    }


    public static void cycleAbility(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BONE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int current = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        int next = (current % 4) + 1;
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, next);

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
        if (item == null || item.getType() != Material.BONE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        String abilityName = getAbilityNameStripped(ability);
        Terraror.useAbility(player, abilityName);
    }

    public static String getAbilityName(int ability) {
        return switch (ability) {
            case 1 -> ChatColor.DARK_RED + "Dust Storm";
            case 2 -> ChatColor.DARK_RED +"Reinforcement";
            case 3 -> ChatColor.DARK_RED +"Boney Smash";
            case 4 -> ChatColor.DARK_RED +"Meteor Traps";
            default -> "Unknown";
        };
    }

    public static String getAbilityNameStripped(int ability) {
        return switch (ability) {
            case 1 -> "Dust Storm";
            case 2 -> "Reinforcement";
            case 3 -> "Boney Smash";
            case 4 -> "Meteor Traps";
            default -> "Unknown";
        };
    }

    public static String getAbilityNameFromPlayer(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BONE) return "None";
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "None";
        int ability = meta.getPersistentDataContainer().getOrDefault(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        return getAbilityNameStripped(ability);
    }
}

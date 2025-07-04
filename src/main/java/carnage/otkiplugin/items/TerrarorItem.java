package carnage.otkiplugin.items;

import carnage.otkiplugin.classes.Terraror;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
        meta.displayName(Component.text("Terraror").color(TextColor.color(0xAA0000)));
        meta.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.INTEGER, 1);
        meta.lore(Arrays.asList(
                Component.text("Abilities:").color(TextColor.color(0xFFAA00)),
                Component.text("1. Dust Storm").color(TextColor.color(0xAA0000)),
                Component.text("2. Reinforcement").color(TextColor.color(0xAA0000)),
                Component.text("3. Boney Smash").color(TextColor.color(0xAA0000)),
                Component.text("4. Meteor Traps").color(TextColor.color(0xAA0000)),
                Component.text("Current Ability: Dust Storm").color(TextColor.color(0x00FF00)),
                Component.text("Right-click to use ability").color(TextColor.color(0xAAAAAA)),
                Component.text("Shift + Right-click to cycle abilities").color(TextColor.color(0xAAAAAA))
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
            var lore = meta.lore();
            if (lore != null && lore.size() >= 5) {
                lore.set(4, Component.text("Current Ability: " + getAbilityName(next)).color(TextColor.color(0x00FF00)));
                meta.lore(lore);
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
            case 1 -> "Dust Storm";
            case 2 -> "Reinforcement";
            case 3 -> "Boney Smash";
            case 4 -> "Meteor Traps";
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

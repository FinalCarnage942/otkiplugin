package carnage.otkiplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClassGUI {

    public static final String GUI_TITLE = ChatColor.DARK_RED + "Choose Your Class";

    public static void openClassGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Border
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, createBorderItem());
            }
        }

        // Inferno class item at slot 10
        gui.setItem(10, createInfernoClassItem());

        // Aetheri class item at slot 12
        gui.setItem(12, createAetheriClassItem());

        player.openInventory(gui);
    }

    private static ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createInfernoClassItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Inferno");
        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Abilities:",
                ChatColor.RED + " - Flare Blitz",
                ChatColor.RED + " - Spinzitu",
                ChatColor.RED + " - Ring of Fire",
                ChatColor.GOLD + "Passives:",
                ChatColor.RED + " - Born 'n Raised: Immune to Lava/Fire.",
                ChatColor.RED + " - Healing Flame: Regenerate in lava.",
                ChatColor.RED + " - Fire Within, Fire Aspect, Inferno Flow, Burning Wrath",
                ChatColor.GOLD + "Nerfs:",
                ChatColor.RED + " - Wqr: Water hurts.",
                ChatColor.RED + " - SplashWqr: Splash water deals damage."
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createAetheriClassItem() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Aetheri");
        meta.setLore(Arrays.asList(
                ChatColor.GOLD + "Abilities:",
                ChatColor.AQUA + " - Supercrit",
                ChatColor.AQUA + " - Soar",
                ChatColor.AQUA + " - Mind over Matter",
                ChatColor.AQUA + " - Warriors Leap",
                ChatColor.GOLD + "Passives:",
                ChatColor.AQUA + " - Strong Ankles: No Fall Damage.",
                ChatColor.AQUA + " - Aerial Strength: Y=100+ you get +10% movement speed and +1 damage.",
                ChatColor.AQUA + " - Double Jump: Double Jump, goes 2 blocks high, 3 seconds cooldown.",
                ChatColor.AQUA + " - Focus Crit: Crits do 1.25x Damage.",
                ChatColor.AQUA + " - Gale Storm: Every 5 Bow shots, one does not need charge.",
                ChatColor.AQUA + " - Agility: Wind charges send the player 1.5x further.",
                ChatColor.GOLD + "Nerfs:",
                ChatColor.AQUA + " - Frail: 9 hearts max.",
                ChatColor.AQUA + " - Claustrophobia: If there is a block directly over your head, -1 dmg and -15% speed."
        ));
        item.setItemMeta(meta);
        return item;
    }
}

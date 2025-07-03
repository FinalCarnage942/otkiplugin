package carnage.otkiplugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClassGUI {

    public static final Component GUI_TITLE = Component.text("Choose Your Class").color(TextColor.color(0xAA0000));

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
        // Hydron class item at slot 14
        gui.setItem(14, createHydronClassItem());
        // Cavernon class item at slot 16
        gui.setItem(16, createCavernonClassItem());
        // Terraror class item at slot 20
        gui.setItem(20, createTerrarorClassItem());
        // Draculox class item at slot 22
        gui.setItem(22, createDraculoxClassItem());

        player.openInventory(gui);
    }

    private static ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" ").color(TextColor.color(0xFFFFFF)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createInfernoClassItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Inferno").color(TextColor.color(0xFF0000)));
        meta.lore(Arrays.asList(
                Component.text("Abilities:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Flare Blitz").color(TextColor.color(0xFF0000)),
                Component.text(" - Spinzitu").color(TextColor.color(0xFF0000)),
                Component.text(" - Ring of Fire").color(TextColor.color(0xFF0000)),
                Component.text("Passives:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Born 'n Raised: Immune to Lava/Fire.").color(TextColor.color(0xFF0000)),
                Component.text(" - Healing Flame: Regenerate in lava.").color(TextColor.color(0xFF0000)),
                Component.text(" - Fire Within, Fire Aspect, Inferno Flow, Burning Wrath").color(TextColor.color(0xFF0000)),
                Component.text("Nerfs:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Wqr: Water hurts.").color(TextColor.color(0xFF0000)),
                Component.text(" - SplashWqr: Splash water deals damage.").color(TextColor.color(0xFF0000))
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createAetheriClassItem() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Aetheri").color(TextColor.color(0x00FFFF)));
        meta.lore(Arrays.asList(
                Component.text("Abilities:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Supercrit").color(TextColor.color(0x00FFFF)),
                Component.text(" - Soar").color(TextColor.color(0x00FFFF)),
                Component.text(" - Mind over Matter").color(TextColor.color(0x00FFFF)),
                Component.text(" - Warriors Leap").color(TextColor.color(0x00FFFF)),
                Component.text("Passives:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Strong Ankles: No Fall Damage.").color(TextColor.color(0x00FFFF)),
                Component.text(" - Aerial Strength: Y=100+ you get +10% movement speed and +1 damage.").color(TextColor.color(0x00FFFF)),
                Component.text(" - Double Jump: Double Jump, goes 2 blocks high, 3 seconds cooldown.").color(TextColor.color(0x00FFFF)),
                Component.text(" - Focus Crit: Crits do 1.25x Damage.").color(TextColor.color(0x00FFFF)),
                Component.text(" - Gale Storm: Every 5 Bow shots, one does not need charge.").color(TextColor.color(0x00FFFF)),
                Component.text(" - Agility: Wind charges send the player 1.5x further.").color(TextColor.color(0x00FFFF)),
                Component.text("Nerfs:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Frail: 9 hearts max.").color(TextColor.color(0x00FFFF)),
                Component.text(" - Claustrophobia: If there is a block directly over your head, -1 dmg and -15% speed.").color(TextColor.color(0x00FFFF))
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createHydronClassItem() {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Hydron").color(TextColor.color(0x0000FF)));
        meta.lore(Arrays.asList(
                Component.text("Abilities:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Riptide Rush").color(TextColor.color(0x0000FF)),
                Component.text(" - Water Gun").color(TextColor.color(0x0000FF)),
                Component.text(" - Whirlpool").color(TextColor.color(0x0000FF)),
                Component.text(" - Bubble Flurry").color(TextColor.color(0x0000FF)),
                Component.text("Passives:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Dolphine: Speed 3 in water.").color(TextColor.color(0x0000FF)),
                Component.text(" - Amphibious: Breathe Underwater Infinitely.").color(TextColor.color(0x0000FF)),
                Component.text(" - Refresher: In rain get permanent speed.").color(TextColor.color(0x0000FF)),
                Component.text(" - Hydrefinery: Tridents deal 3+ Damage.").color(TextColor.color(0x0000FF)),
                Component.text(" - Hydreforgery: Trident deal another extra 3 damage in water or while it rains.").color(TextColor.color(0x0000FF)),
                Component.text(" - Aquatic Haste: In water/rain get haste.").color(TextColor.color(0x0000FF)),
                Component.text("Nerfs:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Aquatic: Permanent Slowness 1 when not in water.").color(TextColor.color(0x0000FF)),
                Component.text(" - Cold Blooded: Take 1.2x dmg in the nether, and 1.5x dmg in lava.").color(TextColor.color(0x0000FF))
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createCavernonClassItem() {
        ItemStack item = new ItemStack(Material.STONE_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Cavernon").color(TextColor.color(0x555555)));
        meta.lore(Arrays.asList(
                Component.text("Abilities:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Pitch").color(TextColor.color(0x555555)),
                Component.text(" - Ground Slam").color(TextColor.color(0x555555)),
                Component.text(" - Earthquake").color(TextColor.color(0x555555)),
                Component.text(" - Stone Slide").color(TextColor.color(0x555555)),
                Component.text("Passives:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Natures Luck: 10% Chance for Ores and Minerals to Drop Double When mined for the first time.").color(TextColor.color(0x555555)),
                Component.text(" - Stoneskip: Leap on water twice before sinking.").color(TextColor.color(0x555555)),
                Component.text(" - Craftsmanship: All Armor worn has 10% more durability.").color(TextColor.color(0x555555)),
                Component.text(" - Blacksmith: Auto Smelt Ores.").color(TextColor.color(0x555555)),
                Component.text(" - Rocky Skin: Gain Resistance 2 when 3 Hearts or Below.").color(TextColor.color(0x555555)),
                Component.text(" - Cavernous Strength: 10% Faster and 5% Stronger when Below Y=-20.").color(TextColor.color(0x555555)),
                Component.text("Nerfs:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Heavyweight: Permanent 10% less speed.").color(TextColor.color(0x555555)),
                Component.text(" - Sinking Stone: You sink much faster and move much slower in water.").color(TextColor.color(0x555555))
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createTerrarorClassItem() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Terraror").color(TextColor.color(0xAA0000)));
        meta.lore(Arrays.asList(
                Component.text("Abilities:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Dust Storm").color(TextColor.color(0xAA0000)),
                Component.text(" - Reinforcement").color(TextColor.color(0xAA0000)),
                Component.text(" - Boney Smash").color(TextColor.color(0xAA0000)),
                Component.text(" - Meteor Traps").color(TextColor.color(0xAA0000)),
                Component.text("Passives:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Claws: +1 Attack Damage.").color(TextColor.color(0xAA0000)),
                Component.text(" - Natural Protection: +3 hearts.").color(TextColor.color(0xAA0000)),
                Component.text(" - Fossilized: For every stack of bones in your inventory, gain half an armor bar.").color(TextColor.color(0xAA0000)),
                Component.text(" - Past Nutrients: Bones Function of Golden Apples.").color(TextColor.color(0xAA0000)),
                Component.text(" - Ironskin: Take 15% less kb from all hits gaining 5% per level.").color(TextColor.color(0xAA0000)),
                Component.text(" - Suspicious Grace: Can craft suspicious sand and gravel, and when you stand on it, you get 2+ hearts and regen 1, and +20% speed.").color(TextColor.color(0xAA0000)),
                Component.text("Nerfs:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Low Stamina: Permanent hunger.").color(TextColor.color(0xAA0000)),
                Component.text(" - Heavyweight: 15% Slower with .1 slower attack speed.").color(TextColor.color(0xAA0000))
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createDraculoxClassItem() {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Draculox").color(TextColor.color(0x8B0000)));
        meta.lore(Arrays.asList(
                Component.text("Abilities:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Vaporize").color(TextColor.color(0x8B0000)),
                Component.text(" - Bloodsuck").color(TextColor.color(0x8B0000)),
                Component.text(" - Blood Demons").color(TextColor.color(0x8B0000)),
                Component.text(" - Bloodpact").color(TextColor.color(0x8B0000)),
                Component.text("Passives:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Bloodmoon: +10% speed and +0.5 dmg at night.").color(TextColor.color(0x8B0000)),
                Component.text(" - Bloodline: Keep 30% randomly of your inventory upon death.").color(TextColor.color(0x8B0000)),
                Component.text(" - Foreshadow: When a nearby player hit 3 hearts or below, a bell sound plays.").color(TextColor.color(0x8B0000)),
                Component.text(" - Bloodlust: When you kill a player, gain +2 dmg and +20% speed for 45 seconds (stackable).").color(TextColor.color(0x8B0000)),
                Component.text(" - Bloody Frenzy: Every 3 hits to victim heals half a heart.").color(TextColor.color(0x8B0000)),
                Component.text(" - Dark Resilience: Below 3 hearts, gain 20% damage reduction, and all cooldowns reset.").color(TextColor.color(0x8B0000)),
                Component.text("Nerfs:").color(TextColor.color(0xFFAA00)),
                Component.text(" - Holy Water: Splash Water bottles hurt like instant dmg 1.").color(TextColor.color(0x8B0000)),
                Component.text(" - Silver Bullet: Arrows do 1.5x damage to you.").color(TextColor.color(0x8B0000))
        ));
        item.setItemMeta(meta);
        return item;
    }
}

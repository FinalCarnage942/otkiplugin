package carnage.otkiplugin.listeners.classitem;

import carnage.otkiplugin.items.AetheriItem;
import carnage.otkiplugin.managers.ActionBarManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class AetheriItemListener implements Listener {

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        // Only handle main hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is holding a feather
        if (item == null || item.getType() != org.bukkit.Material.FEATHER) return;

        // Check if it's the Aetheri item
        if (item.getItemMeta() == null ||
                !ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("Aetheri")) {
            return;
        }

        Action action = event.getAction();

        // Shift + Right-click to cycle abilities
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
            event.setCancelled(true);
            AetheriItem.cycleAbility(player);
            String abilityName = AetheriItem.getAbilityNameFromPlayer(player);

            // Show temporary message for 2 seconds (40 ticks)
            String message = ChatColor.GREEN + "Switched to ability: " + ChatColor.AQUA + abilityName;
            ActionBarManager.getInstance().showTemporaryMessage(player, message, 40);
            return;
        }

        // Right-click to use ability
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            AetheriItem.useAbility(player);
        }
    }
}

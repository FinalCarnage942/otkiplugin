package carnage.otkiplugin.listeners.classitem;

import carnage.otkiplugin.items.TerrarorItem;
import carnage.otkiplugin.managers.ActionBarManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class TerrarorItemListener implements Listener {

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.BONE) return;

        if (item.getItemMeta() == null ||
                !ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("Terraror")) {
            return;
        }

        Action action = event.getAction();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
            event.setCancelled(true);
            TerrarorItem.cycleAbility(player);
            return;
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            TerrarorItem.useAbility(player);
        }
    }
}

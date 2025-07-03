package carnage.otkiplugin.listeners.classitem;

import carnage.otkiplugin.items.DraculoxItem;
import carnage.otkiplugin.managers.ActionBarManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class DraculoxItemListener implements Listener {

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.BLAZE_POWDER) return;

        if (item.getItemMeta() == null ||
                !item.getItemMeta().displayName().equals(Component.text("Draculox").color(TextColor.color(0x8B0000)))) {
            return;
        }

        Action action = event.getAction();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
            event.setCancelled(true);
            DraculoxItem.cycleAbility(player);
            return;
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            DraculoxItem.useAbility(player);
        }
    }
}

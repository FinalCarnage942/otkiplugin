package carnage.otkiplugin.listeners;

import carnage.otkiplugin.items.InfernoItem;
import carnage.otkiplugin.items.AetheriItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClassGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Choose Your Class")) {
            return;
        }

        event.setCancelled(true); // Disable ALL interaction inside GUI

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (itemName.equalsIgnoreCase("Inferno")) {
            player.getInventory().addItem(InfernoItem.createInfernoItem());
            player.sendMessage(ChatColor.GREEN + "You selected the Inferno class!");
            player.closeInventory();
        } else if (itemName.equalsIgnoreCase("Aetheri")) {
            player.getInventory().addItem(AetheriItem.createAetheriItem());
            player.sendMessage(ChatColor.GREEN + "You selected the Aetheri class!");
            player.closeInventory();
        }
    }
}

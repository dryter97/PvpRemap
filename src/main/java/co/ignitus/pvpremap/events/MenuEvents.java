package co.ignitus.pvpremap.events;

import co.ignitus.pvpremap.entities.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuEvents implements Listener {

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Menu) {
            event.setCancelled(true);
            ((Menu) holder).onClick(event);
        }
    }

    @EventHandler
    private void onOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Menu)
            ((Menu) holder).onOpen((Player) event.getPlayer());
    }

    @EventHandler
    private void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Menu)
            ((Menu) holder).onClose((Player) event.getPlayer());
    }
}

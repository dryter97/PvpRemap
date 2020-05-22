package co.ignitus.pvpremap.entities;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface Menu extends InventoryHolder {

    default void onClick(final Player player, final int slot, final ClickType type) {
    }

    default void onClick(final InventoryClickEvent event) {
        this.onClick((Player) event.getWhoClicked(), event.getSlot(), event.getClick());
    }

    default void onOpen(final Player player) {

    }

    default void onClose(final Player player) {
    }
}

package co.ignitus.pvpremap.entities;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static co.ignitus.pvpremap.util.ItemUtil.doesItemFromConfigExist;
import static co.ignitus.pvpremap.util.ItemUtil.getItemFromConfig;
import static co.ignitus.pvpremap.util.MessageUtil.format;

public class VoteMenu implements Menu {

    final private PvpRemap plugin = PvpRemap.getInstance();
    final private FileConfiguration config;
    final String path = "vote-menu.";

    final private Group group;
    final private Inventory inventory;

    public VoteMenu(Group group) {
        this.group = group;
        this.config = plugin.getConfig();
        String name = format(config.getString(path + "name", "&6Vote Menu"));
        int size = config.getInt(path + "size", 54);
        this.inventory = Bukkit.createInventory(this, size, name);

        if (config.getBoolean(path + "fill-inventory")) {
            if (doesItemFromConfigExist(path + "fill-item")) {
                ItemStack fillItem = getItemFromConfig(config, path + "fill-item");
                for (int i = 0; i < inventory.getSize(); i++)
                    inventory.setItem(i, fillItem);
            }
        }

        if (doesItemFromConfigExist(path + "remove-item")) {
            int removeItemSlot = config.getInt(path + "remove-item.slot");
            inventory.setItem(removeItemSlot, getItemFromConfig(config, path + "remove-item"));
        }

        int index = -1;
        for (Map map : group.getMaps()) {
            index++;
            int voteAmount = group.getVotes(map);
            String votes = Integer.toString(voteAmount);
            if (map.getItem() == null) {
                ItemStack itemStack = getItemFromConfig(config, path + "map-item",
                        "%mapname%", map.getName(),
                        "%votes%", votes);
                itemStack.setAmount(Math.min(Math.max(1, voteAmount), itemStack.getMaxStackSize()));
                inventory.setItem(index, itemStack);
                continue;
            }
            ItemStack itemStack = map.getItem().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();
            String[] replace = new String[]{"%votes%", votes};
            if (itemMeta.getLore() == null || itemMeta.getLore().isEmpty())
                itemMeta.setLore(format(config.getStringList(path + "map-item.lore"), replace));
            else
                itemMeta.setLore(format(itemMeta.getLore(), replace));
            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(Math.min(Math.max(1, voteAmount), itemStack.getMaxStackSize()));
            inventory.setItem(index, itemStack);
        }
    }

    @Override
    public void onClick(Player player, int slot, ClickType type) {
        int removeItemSlot = config.getInt(path + "remove-item.slot");
        if (slot == removeItemSlot) {
            player.closeInventory();
            if (group.removeVote(player.getUniqueId()))
                player.sendMessage(MessageUtil.getMessage("vote.removed"));
            else
                player.sendMessage(MessageUtil.getMessage("vote.no-vote"));
            return;
        }
        if (slot >= group.getMaps().size() || slot < 0)
            return;
        Map map = group.getMaps().get(slot);
        group.addVote(player, map);
        player.closeInventory();
        player.sendMessage(MessageUtil.getMessage("vote.added",
                "%mapname%", map.getName()));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

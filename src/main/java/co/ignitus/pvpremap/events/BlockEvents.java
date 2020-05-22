package co.ignitus.pvpremap.events;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import static org.bukkit.Bukkit.getServer;

public class BlockEvents implements Listener {

    private final PvpRemap pvpRemap = PvpRemap.getInstance();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (GroupUtil.getGroup(player) == null)
            return;
        final Block block = event.getBlock();
        final FileConfiguration config = pvpRemap.getConfig();
        if (!config.getBoolean("minerals.enabled", true))
            return;
        if (config.getStringList("minerals.whitelist").stream()
                .noneMatch(item -> block.getType().name().equals(item) || block.getType().toString().equalsIgnoreCase(item))) {
            if (!player.hasPermission("pvpremap.admin.breakbypass")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(MessageUtil.getMessage("anti.break"));
            }
            return;
        }
        if (player.hasPermission("pvpremap.admin.breakbypass") && GroupUtil.getGroup(player) == null)
            return;
        Material material = block.getType();
        getServer().getScheduler().runTaskLater(pvpRemap, () -> block.setType(material), (long) config.getInt("minerals.delay") * 20);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (GroupUtil.getGroup(player) == null)
            return;
        if (!player.hasPermission("pvpremap.admin.placebypass")) {
            event.setCancelled(true);
            player.sendMessage(MessageUtil.getMessage("anti.place.no-permission"));
            return;
        }
        event.setCancelled(true);
        player.sendMessage(MessageUtil.getMessage("anti.place.has-permission"));
    }

}

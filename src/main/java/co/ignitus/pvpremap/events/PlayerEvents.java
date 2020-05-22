package co.ignitus.pvpremap.events;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.tasks.PlayerLoadTask;
import co.ignitus.pvpremap.tasks.PlayerUnloadTask;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

    private final PvpRemap pvpRemap = PvpRemap.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        new PlayerLoadTask(event.getPlayer()).runTask(pvpRemap);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Group group = GroupUtil.getGroup(player.getUniqueId());
        if (group != null) {
            group.removePlayer(player.getUniqueId());
            PlayerUtil.revertInventory(player);
        }
        new PlayerUnloadTask(event.getPlayer()).runTask(pvpRemap);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("pvpremap.admin.dropbypass") || GroupUtil.getGroup(player.getUniqueId()) == null)
            return;
        if (!GroupUtil.isSafeZone(player.getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (pvpRemap.getConfig().getBoolean("prevent.drop"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (event.getFoodLevel() > player.getFoodLevel())
            return;
        if (GroupUtil.isSafeZone(player.getLocation()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFall(EntityDamageEvent event) {
        if (event.isCancelled())
            return;
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        if (GroupUtil.isSafeZone(player.getLocation()))
            event.setCancelled(true);
    }

}

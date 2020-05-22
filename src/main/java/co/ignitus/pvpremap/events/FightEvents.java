package co.ignitus.pvpremap.events;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.PlayerData;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class FightEvents implements Listener {

    final PvpRemap pvpRemap = PvpRemap.getInstance();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer == null)
            return;
        if (player.getUniqueId().equals(killer.getUniqueId()))
            return;
        final FileConfiguration config = pvpRemap.getConfig();
        int constant = config.getInt("elo.formula.constant", 32);
        PlayerData playerData = PlayerUtil.getPlayerData(player);
        PlayerData killerData = PlayerUtil.getPlayerData(killer);

        double playerElo = playerData.getElo();
        double killerElo = killerData.getElo();
        double denominator = Math.pow(10, playerElo / 400) + Math.pow(10, killerElo / 400);
        double playerCalculations = Math.pow(10, playerElo / 400) / denominator;
        double killerCalculations = Math.pow(10, killerElo / 400) / denominator;
        double newPlayerElo = playerElo + constant * (0 - playerCalculations);
        double newKillerElo = killerElo + constant * (1 - killerCalculations);

        playerData.setElo((int) newPlayerElo);
        killerData.setElo((int) newKillerElo);
        playerData.setKillStreak(0);
        killerData.setKillStreak(killerData.getKillStreak() + 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        Group group = GroupUtil.getGroup(player);
        if (group == null)
            return;
        Map currentMap = group.getCurrentMap();
        if (currentMap != null && currentMap.getSpawnPoint() != null)
            event.setRespawnLocation(currentMap.getSpawnPoint());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        if (GroupUtil.isSafeZone(event.getEntity().getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (GroupUtil.isSafeZone(event.getDamager().getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() == null)
                return;
            if (!(projectile.getShooter() instanceof Entity))
                return;
            Entity shooter = (Entity) projectile.getShooter();
            if (GroupUtil.isSafeZone(shooter.getLocation()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotion(PotionSplashEvent event) {
        if (GroupUtil.isSafeZone(event.getEntity().getLocation()))
            event.setCancelled(true);
    }
}

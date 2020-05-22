package co.ignitus.pvpremap.tasks;

import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.util.GroupUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class AutoWarpTask extends BukkitRunnable {

    private static AtomicLong lastRun = new AtomicLong(System.currentTimeMillis());

    @Override
    public void run() {
        lastRun.set(System.currentTimeMillis());
        GroupUtil.getGroups()
                .forEach(group -> {
                    Map map = group.getVotedMap();
                    if (map.getSpawnPoint() != null)
                        group.getPlayers().stream()
                                .map(Bukkit::getPlayer)
                                .filter(Objects::nonNull)
                                .forEach(player -> player.teleport(map.getSpawnPoint()));
                    group.resetVotes();
                });
    }

    public static long getLastRun() {
        return lastRun.get();
    }
}

package co.ignitus.pvpremap.tasks;

import co.ignitus.pvpremap.PvpRemap;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.UUID;

public class IntervalUpdateTask extends BukkitRunnable {

    private static PvpRemap pvpRemap = PvpRemap.getInstance();
    @Getter
    public static LinkedHashMap<UUID, Integer> topSavedElo = new LinkedHashMap<>(pvpRemap.getDataSource().getTopSavedElo());

    @Override
    public void run() {
        topSavedElo = new LinkedHashMap<>(pvpRemap.getDataSource().getTopSavedElo());
    }

}

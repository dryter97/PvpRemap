package co.ignitus.pvpremap.tasks;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class IntervalSaveTask extends BukkitRunnable {

    final static PvpRemap pvpRemap = PvpRemap.getInstance();

    @Override
    public void run() {
        pvpRemap.getDataSource().updateAllData(PlayerUtil.getPlayerDataList());
    }
}

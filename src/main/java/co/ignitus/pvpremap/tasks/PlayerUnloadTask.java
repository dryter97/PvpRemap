package co.ignitus.pvpremap.tasks;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.PlayerData;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerUnloadTask extends BukkitRunnable {

    private final PvpRemap pvpRemap = PvpRemap.getInstance();
    private final Player player;

    public PlayerUnloadTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        PlayerData playerData = PlayerUtil.getPlayerData(player);
        pvpRemap.getDataSource().updatePlayerData(playerData);
        PlayerUtil.removePlayerData(playerData);
    }
}

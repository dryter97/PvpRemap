package co.ignitus.pvpremap.tasks;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerLoadTask extends BukkitRunnable {

    private final PvpRemap pvpRemap = PvpRemap.getInstance();
    private final Player player;

    public PlayerLoadTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        PlayerUtil.addPlayerData(pvpRemap.getDataSource().getPlayerData(player.getUniqueId()));

        final FileConfiguration config = pvpRemap.getConfig();
        if (!config.getBoolean("auto-join.enabled", false))
            return;
        Group group = GroupUtil.getGroup(config.getString("auto-join.group"));
        if (group != null)
            group.addPlayer(player);
    }
}

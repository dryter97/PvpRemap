package co.ignitus.pvpremap.tasks;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.PlayerData;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.OtherUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class KillTimerTask extends BukkitRunnable {

    final PvpRemap pvpRemap = PvpRemap.getInstance();

    @Override
    public void run() {
        final FileConfiguration config = pvpRemap.getConfig();
        int minimum = config.getInt("killstreak.bossbar.minimum", 3);
        double base = config.getDouble("killstreak.formula.base", 60);
        double increment = config.getDouble("killstreak.formula.increment", 0.5);
        Bukkit.getOnlinePlayers()
                .forEach(player -> {
                    final PlayerData playerData = PlayerUtil.getPlayerData(player);
                    final int killStreak = playerData.getKillStreak();
                    if (killStreak < minimum)
                        return;
                    long secondsPassed = OtherUtil.getSecondsPassed(playerData.getLastKill());
                    double totalTime = base - (increment * killStreak);
                    if (secondsPassed < 0)
                        return;
                    final BossBar bossBar = playerData.getBossBar();
                    if (secondsPassed >= totalTime) {
                        playerData.setKillStreak(0);
                        bossBar.removePlayer(player);
                        return;
                    }
                    int timeLeft = (int) (totalTime - secondsPassed);
                    bossBar.setTitle(MessageUtil.format(config.getString("killstreak.bossbar.title"),
                            "%time%", Integer.toString(timeLeft),
                            "%killstreak%", Integer.toString(killStreak)));
                    bossBar.setProgress((totalTime - secondsPassed) / totalTime);
                    if (!bossBar.isVisible())
                        bossBar.setVisible(true);
                    if (!bossBar.getPlayers().contains(player))
                        bossBar.addPlayer(player);
                });
    }
}

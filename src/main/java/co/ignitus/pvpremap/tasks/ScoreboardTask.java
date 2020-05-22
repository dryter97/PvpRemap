package co.ignitus.pvpremap.tasks;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ScoreboardTask extends BukkitRunnable {

    final private PvpRemap pvpRemap = PvpRemap.getInstance();

    @Override
    public void run() {
        FileConfiguration config = pvpRemap.getConfig();
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> GroupUtil.getGroup(player) != null)
                .forEach(player -> {
                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                    Objective obj = scoreboard.registerNewObjective("pvpremap", "pvpremap", MessageUtil.format(config.getString("scoreboard.title", "&6Scoreboard")));
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                    List<String> lines = config.getStringList("scoreboard.lines");
                    int counter = lines.size();
                    for (String line : lines) {
                        obj.getScore(MessageUtil.format(PlaceholderAPI.setPlaceholders(player, line))).setScore(counter);
                        counter--;
                    }
                    player.setScoreboard(scoreboard);
                });
    }
}

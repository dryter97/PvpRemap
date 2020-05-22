package co.ignitus.pvpremap.entities;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.files.RewardsFile;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.OtherUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class PlayerData {

    private final static PvpRemap pvpRemap = PvpRemap.getInstance();
    private final static RewardsFile rewardsFile = PvpRemap.getInstance().getRewardsFile();

    private final UUID uuid;

    private int elo;
    private SavedInventory savedInventory;

    private Location previousLocation;

    private List<String> claimedRewards = new ArrayList<>();

    private int killStreak = 0;
    private long lastKill = 0;

    private BossBar bossBar;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.savedInventory = new SavedInventory(null, null, null, null, null, null);
        this.elo = pvpRemap.getConfig().getInt("elo.default", 120);
        setupBossBar();
    }

    public PlayerData(UUID uuid, int elo, SavedInventory savedInventory, String[] claimedRewards) {
        this.uuid = uuid;
        this.elo = elo;
        this.savedInventory = savedInventory;
        this.claimedRewards.addAll(Arrays.stream(claimedRewards).collect(Collectors.toList()));
        setupBossBar();
    }

    private void setupBossBar() {
        final String path = "killstreak.bossbar.";
        final FileConfiguration config = pvpRemap.getConfig();
        this.bossBar = Bukkit.createBossBar(MessageUtil.format(config.getString(path + "title")),
                BarColor.valueOf(config.getString(path + "color").toUpperCase()),
                BarStyle.valueOf(config.getString(path + "style").toUpperCase()));
        bossBar.removeAll();
        bossBar.setVisible(false);
        bossBar.setProgress(0);
        Player player = Bukkit.getPlayer(uuid);
        if (!bossBar.getPlayers().contains(player))
            bossBar.addPlayer(player);
    }

    public void setElo(int newElo) {
        int oldElo = this.elo;
        if (newElo == oldElo)
            return;
        this.elo = Math.max(10, Math.min(newElo, pvpRemap.getConfig().getInt("elo.max", 12000)));
        if (oldElo <= 0)
            return;
        final FileConfiguration rewardsConfig = rewardsFile.getFileConfiguration();
        ConfigurationSection section = rewardsConfig.getConfigurationSection("elo");
        if (section == null)
            return;
        section.getKeys(false).stream()
                .mapToInt(reward -> {
                    try {
                        return Integer.parseInt(reward);
                    } catch (NumberFormatException nfe) {
                        return -1;
                    }
                }).filter(reward -> reward > 0)
                .filter(reward -> new IntRange(oldElo, newElo).containsInteger(reward))
                .forEach(reward -> {
                    if (claimedRewards.contains(Integer.toString(reward)))
                        return;
                    ConsoleCommandSender commandSender = Bukkit.getConsoleSender();
                    String playerName = OtherUtil.getName(this.uuid);
                    if (newElo > oldElo)
                        rewardsConfig.getStringList("elo." + reward + ".positive")
                                .forEach(command ->
                                        Bukkit.dispatchCommand(commandSender, command
                                                .replace("%player%", playerName)));
                    else
                        rewardsConfig.getStringList("elo." + reward + ".negative")
                                .forEach(command ->
                                        Bukkit.dispatchCommand(commandSender, command
                                                .replace("%player%", playerName)));
                    if (rewardsConfig.getBoolean("elo." + reward + ".once"))
                        claimedRewards.add(Integer.toString(reward));
                });
    }

    public void setKillStreak(int newKillStreak) {
        int oldKillStreak = this.killStreak;
        if (oldKillStreak == newKillStreak)
            return;
        this.killStreak = newKillStreak;
        this.lastKill = System.currentTimeMillis();

        final FileConfiguration config = pvpRemap.getConfig();
        final FileConfiguration rewardsConfig = rewardsFile.getFileConfiguration();
        final int killMinimum = config.getInt("killstreak.bossbar.minimum", 3);

        bossBar.setVisible(killStreak >= killMinimum);

        ConsoleCommandSender commandSender = Bukkit.getConsoleSender();
        String playerName = OtherUtil.getName(this.uuid);

        if (oldKillStreak >= killMinimum && newKillStreak <= 0) {
            rewardsConfig.getStringList("killstreak.end").forEach(command ->
                    Bukkit.dispatchCommand(commandSender, command
                            .replace("%player%", playerName)
                            .replace("%killstreak%", Integer.toString(oldKillStreak))));
            return;
        }
        rewardsConfig.getStringList("killstreak.level." + newKillStreak).forEach(command ->
                Bukkit.dispatchCommand(commandSender, command
                        .replace("%player%", playerName)
                        .replace("%killstreak%", Integer.toString(newKillStreak))));
    }
}

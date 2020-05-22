package co.ignitus.pvpremap;

import co.ignitus.pvpremap.commands.PvpRemapCMD;
import co.ignitus.pvpremap.data.DataSource;
import co.ignitus.pvpremap.data.Flatfile;
import co.ignitus.pvpremap.data.MySQL;
import co.ignitus.pvpremap.events.BlockEvents;
import co.ignitus.pvpremap.events.FightEvents;
import co.ignitus.pvpremap.events.MenuEvents;
import co.ignitus.pvpremap.events.PlayerEvents;
import co.ignitus.pvpremap.files.MessagesFile;
import co.ignitus.pvpremap.files.RewardsFile;
import co.ignitus.pvpremap.tasks.*;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.PlaceholderUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class PvpRemap extends JavaPlugin {

    private CommandSender cs = Bukkit.getConsoleSender();

    @Getter
    private static PvpRemap instance;

    private Economy economy;

    private MessagesFile messagesFile;
    private RewardsFile rewardsFile;

    private DataSource dataSource;

    @Override
    public void onEnable() {
        instance = this;
        cs.sendMessage(ChatColor.GREEN + ChatColor.STRIKETHROUGH.toString() + "---------------------------");
        cs.sendMessage(ChatColor.GREEN + "  Enabling PvpRemap");
        cs.sendMessage(ChatColor.GREEN + " Developed by Ignitus Co.");
        cs.sendMessage(ChatColor.GREEN + ChatColor.STRIKETHROUGH.toString() + "---------------------------");

        loadFiles();

        if (getConfig().getBoolean("mysql.enabled"))
            dataSource = new MySQL();
        else
            dataSource = new Flatfile();

        if (!dataSource.connect()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        if (!checkDependencies())
            return;

        new PvpRemapCMD(this);
        getServer().getPluginManager().registerEvents(new BlockEvents(), this);
        getServer().getPluginManager().registerEvents(new FightEvents(), this);
        getServer().getPluginManager().registerEvents(new MenuEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        getServer().getOnlinePlayers().forEach(player -> new PlayerLoadTask(player).runTask(this));
        startTasks();
    }

    @Override
    public void onDisable() {
        cs.sendMessage(ChatColor.RED + ChatColor.STRIKETHROUGH.toString() + "---------------------------");
        cs.sendMessage(ChatColor.RED + "   Disabling PvpRemap");
        cs.sendMessage(ChatColor.RED + "  Developed by Ignitus Co.");
        cs.sendMessage(ChatColor.RED + ChatColor.STRIKETHROUGH.toString() + "---------------------------");
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (GroupUtil.getGroup(player.getUniqueId()) != null)
                PlayerUtil.revertInventory(player);
        });
        getDataSource().updateAllData(PlayerUtil.getPlayerDataList());
        getDataSource().disconnect();
    }

    private void loadFiles() {
        saveDefaultConfig();
        messagesFile = new MessagesFile();
        rewardsFile = new RewardsFile();
    }

    private boolean checkDependencies() {
        if (!setupEconomy()) {
            cs.sendMessage(ChatColor.RED + "[PvpRemap] Vault not found - Disabling Plugin");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        cs.sendMessage(MessageUtil.format("&2[PvpRemap] Hooked into Vault"));
        if (!getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            cs.sendMessage(ChatColor.RED + "[PvpRemap] WorldEdit not found - Disabling plugin");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        cs.sendMessage(MessageUtil.format("&2[PvpRemap] Hooked into WorldEdit"));
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            cs.sendMessage(ChatColor.RED + "[PvpRemap] PlaceholderAPI not found - Disabling plugin");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        new PlaceholderUtil().register();
        cs.sendMessage(MessageUtil.format("&2[PvpRemap] Hooked into PlaceholderAPI"));

        return true;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    private void startTasks() {
        long frequency = minutesToTicks(getConfig().getInt("autowarp.frequency"));
        long saveInterval = minutesToTicks(getConfig().getInt("intervals.save"));
        long updateInterval = minutesToTicks(getConfig().getInt("intervals.update"));
        new AutoWarpTask().runTaskTimer(this, frequency, frequency);
        new IntervalSaveTask().runTaskTimer(this, saveInterval, saveInterval);
        new IntervalUpdateTask().runTaskTimer(this, updateInterval, updateInterval);
        if (getConfig().getBoolean("scoreboard.enabled"))
            new ScoreboardTask().runTaskTimer(this, 0L, 20L);
        new KillTimerTask().runTaskTimer(this, 20L, 20L);
    }

    private long minutesToTicks(int minutes) {
        return (long) minutes * 60 * 20;
    }
}

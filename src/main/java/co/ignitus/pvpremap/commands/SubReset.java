package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.PlayerData;
import co.ignitus.pvpremap.entities.SavedInventory;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SubReset implements SubCommand {

    private final PvpRemap pvpRemap = PvpRemap.getInstance();

    @Override
    public boolean consoleUse() {
        return true;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getUsage() {
        return "(player)";
    }

    @Override
    public String getPermission() {
        return "pvpremap.admin.reset";
    }

    @Override
    public String getDescription() {
        return "Reset a player's elo & inventory.";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommandByPlayer(Player player, String[] args) {
        onCommand(player, args);
    }

    @Override
    public void onCommandByConsole(ConsoleCommandSender sender, String[] args) {
        onCommand(sender, args);
    }

    private void onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(MessageUtil.getMessage("commands.insufficient-arguments",
                    "%command%", "/pvpremap reset",
                    "%usage%", getUsage()));
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(MessageUtil.getMessage("commands.reset.invalid-player"));
            return;
        }
        PlayerUtil.revertInventory(player);
        Group group = GroupUtil.getGroup(player);
        if (group != null)
            group.removePlayer(player.getUniqueId());
        FileConfiguration config = pvpRemap.getConfig();
        PlayerData playerData = PlayerUtil.getPlayerData(player);
        playerData.setElo(config.getInt("elo.default", 120));
        SavedInventory savedInventory = playerData.getSavedInventory();
        savedInventory.setGroupArmorContents(null);
        savedInventory.setGroupStorageContents(null);
        sender.sendMessage(MessageUtil.getMessage("commands.reset.success",
                "%player%", player.getName()));
        if (group != null)
            group.addPlayer(player);
    }
}

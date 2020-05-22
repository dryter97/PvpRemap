package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SubDelete implements SubCommand {

    private PvpRemap pvpRemap = PvpRemap.getInstance();

    @Override
    public boolean consoleUse() {
        return true;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getUsage() {
        return "(name)";
    }

    @Override
    public String getPermission() {
        return "pvpremap.admin.delete";
    }

    @Override
    public String getDescription() {
        return "Delete a map.";
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
            sender.sendMessage(MessageUtil.getMessage("commands.delete.insufficient-arguments",
                    "%command%", "/pvpremap delete",
                    "%usage%", getUsage()));
            return;
        }
        String mapName = args[0];
        if (!pvpRemap.getDataSource().mapExists(mapName)) {
            sender.sendMessage(MessageUtil.getMessage("commands.delete.invalid-map"));
            return;
        }

        pvpRemap.getDataSource().deleteMap(mapName);
        GroupUtil.removeMap(mapName);
        sender.sendMessage(MessageUtil.getMessage("commands.delete.success"));
    }
}

package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import org.bukkit.entity.Player;

public class SubJoin implements SubCommand {

    @Override
    public boolean consoleUse() {
        return false;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getUsage() {
        return "(groupname)";
    }

    @Override
    public String getPermission() {
        return "pvpremap.join";
    }

    @Override
    public String getDescription() {
        return "Join a group.";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommandByPlayer(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(MessageUtil.getMessage("commands.insufficient-arguments",
                    "%command%", "/pvpremap join",
                    "%usage%", getUsage()));
            return;
        }
        Group group = GroupUtil.getGroup(args[0]);
        if (group == null) {
            player.sendMessage(MessageUtil.getMessage("commands.join.invalid-group"));
            return;
        }
        if (!player.isOp() && !player.hasPermission("pvpremap.join.*") && !player.hasPermission("pvpremap.join." + group.getName())) {
            player.sendMessage(MessageUtil.getMessage("commands.join.no-permission",
                    "%group%", group.getName()));
            return;
        }
        Group currentGroup = GroupUtil.getGroup(player);
        if (group == currentGroup) {
            player.sendMessage(MessageUtil.getMessage("commands.join.already-joined"));
            return;
        }
        group.addPlayer(player);
        player.sendMessage(MessageUtil.getMessage("commands.join.success",
                "%group%", group.getName()));
    }
}

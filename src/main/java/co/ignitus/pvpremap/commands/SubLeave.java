package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SubLeave implements SubCommand {

    @Override
    public boolean consoleUse() {
        return false;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public String getPermission() {
        return "pvpremap.leave";
    }

    @Override
    public String getDescription() {
        return "Leave your current group.";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommandByPlayer(Player player, String[] args) {
        if (args.length == 1) {
            if (!player.hasPermission("pvpremap.leave.other")) {
                player.sendMessage(MessageUtil.getMessage("commands.leave.other.no-permission"));
                return;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(MessageUtil.getMessage("commands.leave.other.invalid-player"));
                return;
            }
            Group group = GroupUtil.getGroup(target);
            if (group == null) {
                player.sendMessage(MessageUtil.getMessage("commands.leave.other.no-group"));
                return;
            }
            group.removePlayer(target.getUniqueId());
            player.sendMessage(MessageUtil.getMessage("commands.leave.other.success",
                    "%player%", target.getName(),
                    "%group%", group.getName()));
            return;
        }
        Group group = GroupUtil.getGroup(player);
        if (group == null) {
            player.sendMessage(MessageUtil.getMessage("commands.leave.no-group"));
            return;
        }
        PlayerUtil.revertInventory(player);
        group.removePlayer(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        player.sendMessage(MessageUtil.getMessage("commands.leave.success"));
    }
}

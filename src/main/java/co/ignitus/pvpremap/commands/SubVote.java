package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.entities.VoteMenu;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import org.bukkit.entity.Player;

public class SubVote implements SubCommand {

    @Override
    public boolean consoleUse() {
        return false;
    }

    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Vote for the next map.";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommandByPlayer(Player player, String[] args) {
        Group group = GroupUtil.getGroup(player);
        if(group == null) {
            player.sendMessage(MessageUtil.getMessage("commands.vote.no-group"));
            return;
        }
        player.openInventory(new VoteMenu(group).getInventory());
    }
}

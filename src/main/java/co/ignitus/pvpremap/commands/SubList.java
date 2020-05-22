package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SubList implements SubCommand {

    @Override
    public boolean consoleUse() {
        return true;
    }

    @Override
    public String getName() {
        return "list";
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
        return "List all the available groups";
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
        List<String> messages = new ArrayList<>();
        messages.add(MessageUtil.getMessage("commands.list.header"));
        GroupUtil.getGroups().forEach(group -> messages.add(MessageUtil.getMessage("commands.list.format",
                "%name%", group.getName())));
        sender.sendMessage(String.join("\n", messages));
    }
}

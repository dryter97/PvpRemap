package co.ignitus.pvpremap.entities;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Command implements CommandExecutor, TabCompleter {

    private List<SubCommand> subCommands = new ArrayList<>();

    private String name;

    public Command(PvpRemap main, String name) {
        this.name = name;
        main.getCommand(name).setExecutor(this);
        main.getCommand(name).setTabCompleter(this);
    }

    public Command registerSubCommand(SubCommand subCommand) {
        this.subCommands.add(subCommand);
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 0) {
            onUse(sender);
            return true;
        }

        SubCommand sub = subCommands.stream().filter(s -> {
            if (s.getName().equalsIgnoreCase(args[0])) {
                return true;
            }
            return Arrays.asList(s.getAliases()).contains(args[0].toLowerCase());
        }).findFirst().orElse(null);

        if (sub == null) {
            sender.sendMessage(MessageUtil.getMessage("commands.invalid-subargument",
                    "%argument%", args[0]));
            return true;
        }

        if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return true;
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        if (sender instanceof Player) {
            sub.onCommandByPlayer((Player) sender, newArgs);
            return true;
        }
        sub.onCommandByConsole((ConsoleCommandSender) sender, newArgs);
        return true;
    }

    protected void onUse(CommandSender sender) {
        sender.sendMessage(MessageUtil.format("&7&l--- &r&2" + StringUtils.capitalize(name) + " &bSubCommands &7&l---"));
        subCommands.forEach(sub -> {
            sender.sendMessage(MessageUtil.format("&2/" + name + " " + sub.getName() + (sub.getUsage() == null || sub.getUsage().length() == 0 ? "" : " &b" + sub.getUsage()) + " &e&l- &7" + sub.getDescription()));
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return subCommands.stream().filter(sub -> {
                if (sub.getPermission() == null) return true;
                return sender.hasPermission(sub.getPermission());
            })
                    .filter(sub -> sub.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                    .map(SubCommand::getName).collect(Collectors.toList());
        }
        return null;
    }
}

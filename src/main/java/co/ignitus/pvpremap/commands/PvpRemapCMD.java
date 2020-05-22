package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Command;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.util.GroupUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PvpRemapCMD extends Command {

    public PvpRemapCMD(PvpRemap main) {
        super(main, "pvpremap");
        registerSubCommand(new SubCreate())
                .registerSubCommand(new SubDelete())
                .registerSubCommand(new SubJoin())
                .registerSubCommand(new SubLeave())
                .registerSubCommand(new SubSetup())
                .registerSubCommand(new SubSpawn())
                .registerSubCommand(new SubReset())
                .registerSubCommand(new SubList())
                .registerSubCommand(new SubVote())
                .registerSubCommand(new SubElo());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 1)
            return super.onTabComplete(sender, cmd, label, args);
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "join":
                    return GroupUtil.getGroups().stream().map(Group::getName).collect(Collectors.toList());
                case "setup":
                case "delete":
                    return GroupUtil.getGroups().stream().flatMap(group -> group.getMaps().stream()).map(Map::getName).collect(Collectors.toList());
                case "elo":
                    return Arrays.asList("give", "take", "set", "reset");
                case "create":
                case "leave":
                case "list":
                case "vote":
                    return new ArrayList<>();
                default:
                    return null;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setup"))
                return Arrays.asList("spawn", "safezone", "item");
            if (args[0].equalsIgnoreCase("elo"))
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1]))
                        .collect(Collectors.toList());
            return null;
        }
        return null;
    }
}

package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.PlayerData;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SubElo implements SubCommand {

    private final static PvpRemap pvpRemap = PvpRemap.getInstance();

    @Override
    public boolean consoleUse() {
        return true;
    }

    @Override
    public String getName() {
        return "elo";
    }

    @Override
    public String getUsage() {
        return "(give/take/set/reset) (player) [amount]";
    }

    @Override
    public String getPermission() {
        return "pvpremap.admin.elo";
    }

    @Override
    public String getDescription() {
        return "Modify a player's elo";
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
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.getMessage("commands.insufficient-arguments",
                    "%command%", "/pvpremap elo",
                    "%usage%", getUsage()));
            return;
        }
        List<String> possibleSubCommands = Arrays.asList("give", "take", "set", "reset");
        if (!possibleSubCommands.contains(args[0].toLowerCase())) {
            sender.sendMessage(MessageUtil.getMessage("commands.elo.invalid-argument"));
            return;
        }
        final Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(MessageUtil.getMessage("commands.elo.invalid-player"));
            return;
        }
        final FileConfiguration config = pvpRemap.getConfig();
        final PlayerData playerData = PlayerUtil.getPlayerData(player);
        if (args[0].equalsIgnoreCase("reset")) {
            playerData.setElo(config.getInt("elo.default", 120));
            sender.sendMessage(MessageUtil.getMessage("commands.elo.reset",
                    "%player%", player.getName()));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.getMessage("commands.insufficient-arguments",
                    "%command%", "/pvpremap elo",
                    "%usage%", getUsage()));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException nfe) {
            sender.sendMessage(MessageUtil.getMessage("commands.elo.invalid-number"));
            return;
        }
        int currentElo = playerData.getElo();
        switch (args[0].toLowerCase()) {
            case "give":
                playerData.setElo(currentElo + amount);
                sender.sendMessage(MessageUtil.getMessage("commands.elo.give",
                        "%player%", player.getName(),
                        "%elo%", Integer.toString(playerData.getElo())));
                return;
            case "take":
                playerData.setElo(currentElo - amount);
                sender.sendMessage(MessageUtil.getMessage("commands.elo.take",
                        "%player%", player.getName(),
                        "%elo%", Integer.toString(playerData.getElo())));
                return;
            case "set":
                playerData.setElo(amount);
                sender.sendMessage(MessageUtil.getMessage("commands.elo.set",
                        "%player%", player.getName(),
                        "%elo%", Integer.toString(playerData.getElo())));
        }
    }
}

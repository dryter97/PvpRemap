package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SubSpawn implements SubCommand {

    @Override
    public boolean consoleUse() {
        return false;
    }

    @Override
    public String getName() {
        return "spawn";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public String getPermission() {
        return "pvpremap.spawn";
    }

    @Override
    public String getDescription() {
        return "Teleport to the current map spawn.";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommandByPlayer(Player player, String[] args) {
        final Player target;
        if (args.length == 1 && player.hasPermission("pvpremap.spawn.other")) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(MessageUtil.getMessage("commands.spawn.other.invalid-player"));
                return;
            }
        } else {
            target = player;
        }
        String path = "commands.spawn." + (target.equals(player) ? "" : "other.");
        String[] replace = new String[]{"%player%", target.getName()};
        Group group = GroupUtil.getGroup(target);
        if (group == null) {
            player.sendMessage(MessageUtil.getMessage(path + "no-group", replace));
            return;
        }
        Map currentMap = group.getCurrentMap();
        if (currentMap == null) {
            player.sendMessage(MessageUtil.getMessage(path + "no-map", replace));
            return;
        }
        if (currentMap.getSpawnPoint() == null) {
            player.sendMessage(MessageUtil.getMessage(path + "no-spawn", replace));
            return;
        }
        target.teleport(currentMap.getSpawnPoint());
        player.sendMessage(MessageUtil.getMessage(path + "success", replace));
    }
}

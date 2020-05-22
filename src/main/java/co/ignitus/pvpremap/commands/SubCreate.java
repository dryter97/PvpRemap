package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import com.sk89q.worldedit.WorldEdit;
import org.bukkit.entity.Player;

import java.util.Collections;

public class SubCreate implements SubCommand {

    private PvpRemap pvpRemap = PvpRemap.getInstance();
    private WorldEdit worldEdit = WorldEdit.getInstance();

    @Override
    public boolean consoleUse() {
        return false;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getUsage() {
        return "(name) (group)";
    }

    @Override
    public String getPermission() {
        return "pvpremap.admin.create";
    }

    @Override
    public String getDescription() {
        return "Create a new map.";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommandByPlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtil.getMessage("commands.insufficient-arguments",
                    "%command%", "/pvpremap create",
                    "%usage%", getUsage()));
            return;
        }
        String name = args[0];
        if (pvpRemap.getDataSource().mapExists(name)) {
            player.sendMessage(MessageUtil.getMessage("commands.create.map-exists"));
            return;
        }
        String groupName = args[1];

        Map map = new Map(name, groupName, null);
        pvpRemap.getDataSource().createMap(map);
        Group group = GroupUtil.getGroup(groupName);
        if (group != null)
            group.addMap(map);
        else
            GroupUtil.addGroup(new Group(groupName, Collections.singletonList(map)));
        player.sendMessage(MessageUtil.getMessage("commands.create.map-created",
                "%name%", name,
                "%group%", groupName));
    }
}

package co.ignitus.pvpremap.commands;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Cuboid;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.SubCommand;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.MessageUtil;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SubSetup implements SubCommand {

    private PvpRemap pvpRemap = PvpRemap.getInstance();
    private WorldEdit worldEdit = WorldEdit.getInstance();

    @Override
    public boolean consoleUse() {
        return false;
    }

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public String getUsage() {
        return "(mapname) (spawn/safezone/item)";
    }

    @Override
    public String getPermission() {
        return "pvpremap.admin.setup";
    }

    @Override
    public String getDescription() {
        return "Modify a maps information.";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommandByPlayer(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(MessageUtil.getMessage("commands.insufficient-arguments",
                    "%command%", "/pvpremap setup",
                    "%usage%", getUsage()));
            return;
        }
        String mapName = args[0];
        if (!pvpRemap.getDataSource().mapExists(mapName)) {
            player.sendMessage(MessageUtil.getMessage("commands.setup.invalid-map"));
            return;
        }
        Map map = GroupUtil.getMap(mapName);
        String subArgument = args[1];
        if (subArgument.equalsIgnoreCase("item")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                player.sendMessage(MessageUtil.getMessage("commands.setup.invalid-item"));
                return;
            }
            pvpRemap.getDataSource().setItem(mapName, item);
            map.setItem(item);
            player.sendMessage(MessageUtil.getMessage("commands.setup.item-set"));
            return;
        }
        if (subArgument.equalsIgnoreCase("spawn")) {
            pvpRemap.getDataSource().setSpawnPoint(mapName, player.getLocation());
            map.setSpawnPoint(player.getLocation());
            player.sendMessage(MessageUtil.getMessage("commands.setup.spawn-set"));
            return;
        }
        if (subArgument.equalsIgnoreCase("safezone")) {
            LocalSession session = worldEdit.getSessionManager().get(new BukkitPlayer(player));
            if (session == null) {
                player.sendMessage(MessageUtil.getMessage("commands.setup.invalid-selection"));
                return;
            }
            World world = player.getWorld();
            Region selection;
            try {
                selection = session.getSelection(new BukkitWorld(world));
            } catch (IncompleteRegionException e) {
                player.sendMessage(MessageUtil.getMessage("commands.setup.invalid-selection"));
                return;
            }
            BlockVector3 minimumPoint = selection.getMinimumPoint();
            BlockVector3 maximumPoint = selection.getMaximumPoint();

            Location minPoint = new Location(world, minimumPoint.getX(), minimumPoint.getY(), minimumPoint.getZ());
            Location maxPoint = new Location(world, maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ());
            Cuboid safeZone = new Cuboid(minPoint, maxPoint);
            pvpRemap.getDataSource().setSafeZone(mapName, safeZone);
            map.setSafeZone(safeZone);
            player.sendMessage(MessageUtil.getMessage("commands.setup.safezone-set"));
            return;
        }
        player.sendMessage(MessageUtil.getMessage("commands.setup.invalid-argument"));
    }
}

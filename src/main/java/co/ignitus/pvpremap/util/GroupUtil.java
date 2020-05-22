package co.ignitus.pvpremap.util;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupUtil {

    private static PvpRemap pvpRemap = PvpRemap.getInstance();

    private static List<Group> groups = pvpRemap.getDataSource().getGroups();

    public static Group getGroup(String name) {
        return groups.stream().filter(group -> group.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Group getGroup(Player player) {
        return getGroup(player.getUniqueId());
    }

    public static Group getGroup(UUID uuid) {
        return groups.stream().filter(group -> group.hasPlayer(uuid)).findFirst().orElse(null);
    }

    public static void addGroup(Group group) {
        groups.add(group);
    }

    public static Map getMap(String mapName) {
        return groups.stream().flatMap(group -> group.getMaps().stream())
                .filter(map -> map.getName().equalsIgnoreCase(mapName))
                .findFirst().orElse(null);
    }

    public static void removeMap(String mapName) {
        groups.stream().flatMap(group -> group.getMaps().stream())
                .filter(map -> map.getName().equalsIgnoreCase(mapName))
                .findFirst()
                .ifPresent(map -> {
                    Group group = getGroup(map.getGroup());
                    if (group == null)
                        return;
                    group.removeMap(map);
                    if (group.getMaps().size() == 0)
                        groups.remove(group);
                });
    }

    public static List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    public static boolean isSafeZone(Location location) {
        return GroupUtil.getGroups().stream().flatMap(group -> group.getMaps().stream())
                .filter(map -> map.getSafeZone() != null)
                .anyMatch(map -> map.getSafeZone().isIn(location));
    }

}

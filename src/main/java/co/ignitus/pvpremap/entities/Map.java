package co.ignitus.pvpremap.entities;

import co.ignitus.pvpremap.util.OtherUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class Map {

    private String name, group;
    private Location spawnPoint;
    private Cuboid safeZone;
    private ItemStack item;

    public Map(String name, String group, Location spawnPoint) {
        this.name = name;
        this.group = group;
        this.spawnPoint = spawnPoint;
        this.safeZone = null;
    }

    public Map(String name, String group, Location spawnPoint, String safeZone1, String safeZone2, ItemStack item) {
        this.name = name;
        this.group = group;
        this.spawnPoint = spawnPoint;
        this.item = item;
        if (safeZone1 != null && safeZone2 != null)
            this.safeZone = new Cuboid(OtherUtil.stringToLocation(safeZone1), OtherUtil.stringToLocation(safeZone2));
        else
            this.safeZone = null;
    }
}

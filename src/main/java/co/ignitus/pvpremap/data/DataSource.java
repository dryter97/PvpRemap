package co.ignitus.pvpremap.data;

import co.ignitus.pvpremap.entities.Cuboid;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.PlayerData;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public interface DataSource {

    boolean connect();

    void disconnect();

    void createMap(Map map);

    void setSpawnPoint(String name, Location spawnPoint);

    void setSafeZone(String name, Cuboid spawn);

    void setItem(String name, ItemStack item);

    boolean mapExists(String name);

    void deleteMap(String name);

    Map getMap(String name);

    ArrayList<Map> getMaps(String group);

    ArrayList<Group> getGroups();

    PlayerData getPlayerData(UUID uuid);

    void updatePlayerData(PlayerData playerData);

    void updateAllData(List<PlayerData> dataList);

    int getEloRank(UUID uuid);

    LinkedHashMap<UUID, Integer> getTopSavedElo();
}

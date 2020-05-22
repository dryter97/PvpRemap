package co.ignitus.pvpremap.data;

import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.*;
import co.ignitus.pvpremap.files.FileManager;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.OtherUtil;
import co.ignitus.pvpremap.util.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Flatfile extends FileManager implements DataSource {

    public Flatfile() {
        super("data.yml");
    }

    public boolean connect() {
        this.reloadConfig();
        Bukkit.getConsoleSender().sendMessage(MessageUtil.format("&2[PvpRemap] Currently using flatfile storage. (data.yml)."));
        return true;
    }

    @Override
    public void disconnect() {
        this.saveFileConfiguration();
    }

    @Override
    public void createMap(Map map) {
        final FileConfiguration config = getFileConfiguration();
        String path = "maps." + map.getName().toLowerCase() + ".";
        config.set(path + "group", map.getGroup().toLowerCase());
        config.set(path + "spawnPoint", null);
        config.set(path + "safeZone1", null);
        config.set(path + "safeZone2", null);
        config.set(path + "item", null);
        saveFileConfiguration();
    }

    @Override
    public void setSpawnPoint(String name, Location spawnPoint) {
        getFileConfiguration().set("maps." + name.toLowerCase() + ".spawnPoint", OtherUtil.locationToString(spawnPoint));
        saveFileConfiguration();
    }

    @Override
    public void setSafeZone(String name, Cuboid spawn) {
        final FileConfiguration config = getFileConfiguration();
        String path = "maps." + name.toLowerCase() + ".";
        config.set(path + "safeZone1", OtherUtil.locationToString(spawn.getPoint1()));
        config.set(path + "safeZone2", OtherUtil.locationToString(spawn.getPoint2()));
        saveFileConfiguration();
    }

    @Override
    public void setItem(String name, ItemStack item) {
        final FileConfiguration config = getFileConfiguration();
        String path = "maps." + name.toLowerCase() + ".";
        config.set(path + "item", SerializationUtils.serializeItemStack(item));
        saveFileConfiguration();
    }

    @Override
    public boolean mapExists(String name) {
        return getFileConfiguration().contains("maps." + name.toLowerCase());
    }

    @Override
    public void deleteMap(String name) {
        getFileConfiguration().set("maps." + name.toLowerCase(), null);
        saveFileConfiguration();
    }

    @Override
    public Map getMap(String name) {
        if (!mapExists(name)) return null;
        final FileConfiguration config = getFileConfiguration();
        String path = "maps." + name.toLowerCase() + ".";
        String group = config.getString(path + "group");
        String spawnPoint = config.getString(path + "spawnPoint");
        String safeZone1 = config.getString(path + "safeZone1");
        String safeZone2 = config.getString(path + "safeZone2");
        final String item = config.getString(path + "item", null);
        ItemStack itemStack;
        if (item == null)
            itemStack = null;
        else
            itemStack = SerializationUtils.deserializeItemStack(item);
        return new Map(name, group, OtherUtil.stringToLocation(spawnPoint), safeZone1, safeZone2, itemStack);
    }

    @Override
    public ArrayList<Map> getMaps(String group) {
        final FileConfiguration config = getFileConfiguration();
        ArrayList<Map> maps = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("maps");
        if (section == null)
            return maps;
        section.getKeys(false).stream()
                .filter(mapName -> config.getString("maps." + mapName + ".group").equalsIgnoreCase(group))
                .forEach(mapName -> {
                    final String path = "maps." + mapName + ".";
                    final String spawnPoint = config.getString(path + "spawnPoint");
                    final String safeZone1 = config.getString(path + "safeZone1");
                    final String safeZone2 = config.getString(path + "safeZone2");
                    final String item = config.getString(path + "item", null);
                    ItemStack itemStack;
                    if (item == null)
                        itemStack = null;
                    else
                        itemStack = SerializationUtils.deserializeItemStack(item);
                    maps.add(new Map(mapName, group, OtherUtil.stringToLocation(spawnPoint), safeZone1, safeZone2, itemStack));
                });
        return maps;
    }

    @Override
    public ArrayList<Group> getGroups() {
        final FileConfiguration config = getFileConfiguration();
        final ArrayList<Group> groups = new ArrayList<>();
        final ConfigurationSection section = config.getConfigurationSection("maps");
        if (section == null)
            return groups;
        section.getKeys(false).stream()
                .map(mapName -> config.getString("maps." + mapName + ".group"))
                .distinct()
                .forEach(group -> groups.add(new Group(group, getMaps(group))));
        return groups;
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        final FileConfiguration config = getFileConfiguration();
        if (!getFileConfiguration().contains("players." + uuid.toString()))
            return new PlayerData(uuid);
        final String path = "players." + uuid.toString() + ".";
        final int elo = config.getInt(path + "elo", 120);
        String rewardsString = config.getString(path + "claimedRewards", null);
        String[] claimedRewards = new String[]{};
        if (rewardsString != null)
            claimedRewards = rewardsString.split(",");
        final String groupStorageContents = config.getString(path + "groupStorageContents", null);
        final String groupArmorContents = config.getString(path + "groupArmorContents", null);
        final String groupExtraContents = config.getString(path + "groupExtraContents", null);
        final String savedStorageContents = config.getString(path + "savedStorageContents", null);
        final String savedArmorContents = config.getString(path + "savedArmorContents", null);
        final String savedExtraContents = config.getString(path + "savedExtraContents", null);
        final SavedInventory savedInventory = new SavedInventory(groupStorageContents, groupArmorContents, groupExtraContents, savedStorageContents, savedArmorContents, savedExtraContents);
        return new PlayerData(uuid, elo, savedInventory, claimedRewards);
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        final FileConfiguration config = getFileConfiguration();
        final String path = "players." + playerData.getUuid().toString() + ".";
        final SavedInventory savedInventory = playerData.getSavedInventory();
        config.set(path + "elo", playerData.getElo());
        config.set(path + "claimedRewards", String.join(",", playerData.getClaimedRewards()));
        config.set(path + "groupStorageContents", savedInventory.serializeGroupStorageContents());
        config.set(path + "groupArmorContents", savedInventory.serializeGroupArmorContents());
        config.set(path + "groupExtraContents", savedInventory.serializeGroupExtraContents());
        config.set(path + "savedStorageContents", savedInventory.serializeSavedStorageContents());
        config.set(path + "savedArmorContents", savedInventory.serializeSavedArmorContents());
        config.set(path + "savedExtraContents", savedInventory.serializeSavedExtraContents());
        saveFileConfiguration();
    }

    @Override
    public void updateAllData(List<PlayerData> dataList) {
        dataList.forEach(this::updatePlayerData);
    }

    @Override
    public int getEloRank(UUID uuid) {
        final FileConfiguration config = getFileConfiguration();
        final ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null)
            return -1;
        List<String> players = section.getKeys(false).stream()
                .sorted(Comparator.comparingInt(playerData -> config.getInt("players." + playerData + ".elo")).reversed())
                .collect(Collectors.toList());
        return players.indexOf(uuid.toString()) + 1;
    }

    @Override
    public LinkedHashMap<UUID, Integer> getTopSavedElo() {
        LinkedHashMap<UUID, Integer> topElo = new LinkedHashMap<>();
        final FileConfiguration config = getFileConfiguration();
        final ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null)
            return topElo;
        section.getKeys(false).stream()
                .sorted(Comparator.comparingInt(player -> config.getInt("players." + player + ".elo")).reversed())
                .forEach(player ->
                        topElo.put(UUID.fromString(player), config.getInt("players." + player + ".elo")));
        return topElo;
    }
}

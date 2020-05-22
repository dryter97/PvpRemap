package co.ignitus.pvpremap.util;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.PlayerData;
import co.ignitus.pvpremap.entities.SavedInventory;
import co.ignitus.pvpremap.tasks.IntervalUpdateTask;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerUtil {

    private static HashMap<UUID, PlayerData> playerDataList = new HashMap<>();

    public static void addPlayerData(PlayerData playerData) {
        playerDataList.put(playerData.getUuid(), playerData);
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public static PlayerData getPlayerData(UUID uuid) {
        PlayerData playerData = playerDataList.get(uuid);
        if (playerData != null)
            return playerData;
        playerData = PvpRemap.getInstance().getDataSource().getPlayerData(uuid);
        addPlayerData(playerData);
        return playerData;
    }

    public static void removePlayerData(PlayerData playerData) {
        playerDataList.remove(playerData.getUuid());
    }

    public static ArrayList<PlayerData> getPlayerDataList() {
        return new ArrayList<>(playerDataList.values());
    }

    public static void revertInventory(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        if (playerData.getPreviousLocation() != null)
            player.teleport(playerData.getPreviousLocation());
        SavedInventory savedInventory = playerData.getSavedInventory();
        if (savedInventory == null)
            return;
        savedInventory.setGroupStorageContents(player.getInventory().getStorageContents());
        savedInventory.setGroupArmorContents(player.getInventory().getArmorContents());
        savedInventory.setGroupExtraContents(player.getInventory().getExtraContents());
        if (savedInventory.getSavedArmorContents() != null)
            player.getInventory().setArmorContents(savedInventory.getSavedArmorContents());
        if (savedInventory.getSavedStorageContents() != null)
            player.getInventory().setStorageContents(savedInventory.getSavedStorageContents());
        if (savedInventory.getSavedExtraContents() != null)
            player.getInventory().setExtraContents(savedInventory.getSavedExtraContents());
    }

    public static LinkedHashMap<UUID, Integer> getTopElo() {
        LinkedHashMap<UUID, Integer> topTen = new LinkedHashMap<>(IntervalUpdateTask.getTopSavedElo());
        getPlayerDataList()
                .forEach(playerData -> topTen.put(playerData.getUuid(), playerData.getElo()));
        return topTen.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public static int getPlayerIndex(UUID uuid) {
        LinkedList<UUID> indexedList = new LinkedList<>(getTopElo().keySet());
        int index = indexedList.indexOf(uuid);
        if (index < 0)
            return index;
        return index + 1;
    }

    public static Map.Entry<UUID, Integer> getTopEloEntry(int index) {
        List<Map.Entry<UUID, Integer>> indexedList = new ArrayList<>(getTopElo().entrySet());
        if (index >= indexedList.size())
            return null;
        return indexedList.get(index);
    }
}

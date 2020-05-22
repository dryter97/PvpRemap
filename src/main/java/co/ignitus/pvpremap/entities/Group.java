package co.ignitus.pvpremap.entities;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.util.GroupUtil;
import co.ignitus.pvpremap.util.OtherUtil;
import co.ignitus.pvpremap.util.PlayerUtil;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Group {

    private final static PvpRemap pvpRemap = PvpRemap.getInstance();

    private String name;
    private List<Map> maps = new ArrayList<>();
    private List<UUID> players = new ArrayList<>();
    private HashMap<Map, List<UUID>> votes = new HashMap<>();
    private int currentMap;

    public Group(String name, List<Map> maps) {
        this.name = name;
        this.maps.addAll(maps);
        this.currentMap = new Random().nextInt(maps.size());
    }

    public void addMap(Map map) {
        maps.add(map);
    }

    public void removeMap(Map map) {
        maps.remove(map);
    }

    public void addPlayer(Player player) {
        PlayerData playerData = PlayerUtil.getPlayerData(player.getUniqueId());
        playerData.setPreviousLocation(player.getLocation());
        Map currentMap = getCurrentMap();
        if (currentMap != null && currentMap.getSpawnPoint() != null)
            player.teleport(currentMap.getSpawnPoint());
        Group currentGroup = GroupUtil.getGroup(player.getUniqueId());
        SavedInventory savedInventory = playerData.getSavedInventory();
        if (currentGroup == null) {
            savedInventory.setSavedArmorContents(player.getInventory().getArmorContents());
            savedInventory.setSavedStorageContents(player.getInventory().getStorageContents());
            savedInventory.setSavedExtraContents(player.getInventory().getExtraContents());
            ItemStack[] armorContents = savedInventory.getGroupArmorContents();
            ItemStack[] storageContents = savedInventory.getGroupStorageContents();
            if (armorContents == null || storageContents == null) {
                final FileConfiguration config = pvpRemap.getConfig();
                ItemStack[] items = Iterables.toArray(config.getConfigurationSection("default.inventory")
                        .getKeys(false).stream()
                        .map(item ->
                                OtherUtil.getItemFromConfig(config, "default.inventory." + item)
                        ).collect(Collectors.toList()), ItemStack.class);
                player.getInventory().setStorageContents(items);
                player.getInventory().setArmorContents(new ItemStack[]{});
                player.getInventory().setExtraContents(new ItemStack[]{});
            } else {
                player.getInventory().setArmorContents(savedInventory.getGroupArmorContents());
                player.getInventory().setStorageContents(savedInventory.getGroupStorageContents());
                player.getInventory().setExtraContents(savedInventory.getGroupExtraContents());
            }
        } else {
            currentGroup.removePlayer(player.getUniqueId());
            savedInventory.setGroupArmorContents(player.getInventory().getArmorContents());
            savedInventory.setGroupStorageContents(player.getInventory().getStorageContents());
            savedInventory.setGroupExtraContents(player.getInventory().getExtraContents());
        }
        players.add(player.getUniqueId());
    }

    public boolean hasPlayer(UUID uuid) {
        return players.contains(uuid);
    }

    public ArrayList<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    public void removePlayer(UUID uuid) {
        removeVote(uuid);
        players.remove(uuid);
    }

    public Map getCurrentMap() {
        if (maps.isEmpty())
            return null;
        return maps.get(currentMap);
    }

    public Map getVotedMap() {
        Map topMap = votes.keySet().stream().max(Comparator.comparing(this::getVotes)).orElse(null);
        if (topMap == null) {
            Map selectedMap = maps.get(new Random().nextInt(maps.size()));
            currentMap = maps.indexOf(selectedMap);
            return selectedMap;
        }
        int voteAmount = getVotes(topMap);
        Map selectedMap = votes.keySet().stream().filter(map -> getVotes(map) == voteAmount)
                .findAny().orElse(topMap);
        currentMap = maps.indexOf(selectedMap);
        return selectedMap;
    }

    public Map getPlayerVote(UUID uuid) {
        for (Map map : votes.keySet()) {
            List<UUID> players = votes.get(map);
            if (players.contains(uuid))
                return map;
        }
        return null;
        // Unnecessarily complex way of doing above.
        /*java.util.Map.Entry<Map, List<Player>> entry = votes.entrySet().stream().filter(mapEntry ->
                mapEntry.getValue().contains(player)
        ).findFirst().orElse(null);
        if (entry == null)
            return null;
        return entry.getKey(); */
    }

    public void addVote(Player player, Map map) {
        this.removeVote(player.getUniqueId());
        if (votes.containsKey(map)) {
            votes.get(map).add(player.getUniqueId());
            return;
        }
        votes.put(map, new ArrayList<>(Collections.singletonList(player.getUniqueId())));
    }

    public boolean removeVote(UUID uuid) {
        Map votedMap = getPlayerVote(uuid);
        if (votedMap == null)
            return false;
        votes.get(votedMap).remove(uuid);
        return true;
    }

    public int getVotes(Map map) {
        if (votes.containsKey(map))
            return votes.get(map).stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .mapToInt(this::getVoteValue).sum();
        return 0;
    }

    private int getVoteValue(Player player) {
        return player.getEffectivePermissions().stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(permission -> permission.startsWith("pvpremap.vote."))
                .mapToInt(permission -> {
                    try {
                        return Integer.parseInt(permission.replace("pvpremap.vote.", ""));
                    } catch (NumberFormatException ex) {
                        return 1;
                    }
                }).max().orElse(1);
    }

    public void resetVotes() {
        this.votes = new HashMap<>();
    }
}

package co.ignitus.pvpremap.util;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.Group;
import co.ignitus.pvpremap.entities.Map;
import co.ignitus.pvpremap.entities.PlayerData;
import co.ignitus.pvpremap.tasks.AutoWarpTask;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlaceholderUtil extends PlaceholderExpansion {

    final private PvpRemap pvpRemap = PvpRemap.getInstance();

    @Override
    public boolean persist() {
        return false;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return pvpRemap.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "pvpremap";
    }

    @Override
    public String getVersion() {
        return pvpRemap.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equalsIgnoreCase("elo_level")) {
            return Integer.toString(PlayerUtil.getPlayerData(player).getElo());
        }
        if (identifier.equalsIgnoreCase("elo_rank")) {
            return Integer.toString(PlayerUtil.getPlayerIndex(player.getUniqueId()));
        }
        if(identifier.equalsIgnoreCase("killstreak")) {
            return Integer.toString(PlayerUtil.getPlayerData(player).getKillStreak());
        }
        if (identifier.equalsIgnoreCase("map_cooldown")) {
            long lastRun = AutoWarpTask.getLastRun();
            long totalTime = TimeUnit.MINUTES.toSeconds(pvpRemap.getConfig().getInt("autowarp.frequency", 20));
            return OtherUtil.formatSeconds((int) (totalTime - OtherUtil.getSecondsPassed(lastRun)));
        }
        if (identifier.equalsIgnoreCase("current_map")) {
            Group group = GroupUtil.getGroup(player);
            if (group == null)
                return "";
            Map currentMap = group.getCurrentMap();
            if (currentMap == null)
                return "";
            return currentMap.getName();
        }
        if (identifier.startsWith("leaderboard")) {
            String[] args = identifier.split("_");
            if (args.length != 3)
                return "";
            int rank;
            try {
                rank = Integer.parseInt(args[1]) - 1;
            } catch (NumberFormatException nfe) {
                return "";
            }
            Entry<UUID, Integer> entry = PlayerUtil.getTopEloEntry(rank);
            if (entry == null)
                return "";
            if (args[2].equals("name"))
                return OtherUtil.getName(entry.getKey());
            if (args[2].equals("elo"))
                return Integer.toString(entry.getValue());
        }
        return null;
    }

}

package co.ignitus.pvpremap.data;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.entities.*;
import co.ignitus.pvpremap.util.MessageUtil;
import co.ignitus.pvpremap.util.OtherUtil;
import co.ignitus.pvpremap.util.SerializationUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ALL")
public class MySQL implements DataSource {

    @Getter
    final private HikariDataSource dataSource;

    public MySQL() {
        this.dataSource = setupDataSource();
    }

    private HikariDataSource setupDataSource() {
        final FileConfiguration config = PvpRemap.getInstance().getConfig();
        final HikariDataSource dataSource = new HikariDataSource();
        final String host = config.getString("mysql.host");
        final int port = config.getInt("mysql.port");
        final String database = config.getString("mysql.database");
        dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        dataSource.setUsername(config.getString("mysql.username"));
        dataSource.setPassword(config.getString("mysql.password"));
        dataSource.addDataSourceProperty("autoReconnect", "true");
        dataSource.addDataSourceProperty("autoReconnectForPools", "true");
        dataSource.addDataSourceProperty("interactiveClient", "true");
        dataSource.addDataSourceProperty("characterEncoding", "UTF-8");
        dataSource.setAutoCommit(true);
        return dataSource;
    }

    public boolean connect() {
        try (Connection connection = getDataSource().getConnection()) {
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS maps(" +
                            "`name` VARCHAR(255) UNIQUE NOT NULL," +
                            "`group` VARCHAR(255) NOT NULL," +
                            "`spawnPoint` VARCHAR(255) DEFAULT NULL," +
                            "`safeZone1` VARCHAR(255) DEFAULT NULL," +
                            "`safeZone2` VARCHAR(255) DEFAULT NULL," +
                            "`item` TEXT DEFAULT NULL," +
                            "PRIMARY KEY (`name`))"
            ).execute();
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_data(" +
                            "`uuid` VARCHAR(255) UNIQUE NOT NULL," +
                            "`elo` INTEGER DEFAULT NULL," +
                            "`claimedRewards` TEXT DEFAULT NULL," +
                            "`groupStorageContents` TEXT DEFAULT NULL," +
                            "`groupArmorContents` TEXT DEFAULT NULL," +
                            "`groupExtraContents` TEXT DEFAULT NULL," +
                            "`savedStorageContents` TEXT DEFAULT NULL," +
                            "`savedArmorContents` TEXT DEFAULT NULL," +
                            "`savedExtraContents` TEXT DEFAULT NULL," +
                            "PRIMARY KEY (`uuid`))"
            ).execute();
            Bukkit.getConsoleSender().sendMessage(MessageUtil.format("&2[PvpRemap] Successfully established a connection with the database."));
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(MessageUtil.format("&4[PvpRemap] Unable to connect to database. Disabling plugin..."));
            return false;
        }
    }

    @Override
    public void disconnect() {
        getDataSource().close();
    }

    @Override
    public void createMap(Map map) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO maps(`name`, `group`) VALUES (?, ?)");
            ps.setString(1, map.getName());
            ps.setString(2, map.getGroup().toLowerCase());
            ps.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void setSpawnPoint(String name, Location spawnPoint) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE maps SET spawnPoint = ? WHERE `name` = ?"
            );
            ps.setString(1, OtherUtil.locationToString(spawnPoint));
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void setSafeZone(String name, Cuboid safeZone) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE maps SET safeZone1 = ?, safeZone2 = ? WHERE `name` = ?"
            );
            ps.setString(1, OtherUtil.locationToString(safeZone.getPoint1()));
            ps.setString(2, OtherUtil.locationToString(safeZone.getPoint2()));
            ps.setString(3, name);
            ps.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void setItem(String name, ItemStack item) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE maps SET item = ? WHERE name = ?"
            );
            ps.setString(1, SerializationUtils.serializeItemStack(item));
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }


    @Override
    public boolean mapExists(String name) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `maps` WHERE `name` = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
        }
        return false;
    }

    @Override
    public void deleteMap(String name) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM maps WHERE `name` = ?");
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public Map getMap(String name) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM maps WHERE `name` = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String group = rs.getString("group");
                String spawnPoint = rs.getString("spawnPoint");
                String safeZone1 = rs.getString("safeZone1");
                String safeZone2 = rs.getString("safeZone2");
                String item = rs.getString("item");
                ItemStack itemStack;
                if (item == null)
                    itemStack = null;
                else
                    itemStack = SerializationUtils.deserializeItemStack(item);
                return new Map(name, group, OtherUtil.stringToLocation(spawnPoint), safeZone1, safeZone2, itemStack);
            }
            return null;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public ArrayList<Map> getMaps(String group) {
        ArrayList<Map> maps = new ArrayList<>();
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM maps WHERE `group` = ?");
            ps.setString(1, group.toLowerCase());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String spawnPoint = rs.getString("spawnPoint");
                String safeZone1 = rs.getString("safeZone1");
                String safeZone2 = rs.getString("safeZone2");
                String item = rs.getString("item");
                ItemStack itemStack;
                if (item == null)
                    itemStack = null;
                else
                    itemStack = SerializationUtils.deserializeItemStack(item);
                maps.add(new Map(name, group, OtherUtil.stringToLocation(spawnPoint), safeZone1, safeZone2, itemStack));
            }
            return maps;
        } catch (SQLException ex) {
            return maps;
        }
    }

    @Override
    public ArrayList<Group> getGroups() {
        //Improve if needed. #getMaps() query lock.
        ArrayList<Group> groups = new ArrayList<>();
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT `group` FROM maps ");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("group");
                groups.add(new Group(name, getMaps(name)));
            }
            return groups;
        } catch (SQLException ex) {
            return groups;
        }
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `player_data` WHERE `uuid` = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int elo = rs.getInt("elo");
                SavedInventory savedInventory = new SavedInventory(
                        rs.getString("groupStorageContents"),
                        rs.getString("groupArmorContents"),
                        rs.getString("groupExtraContents"),
                        rs.getString("savedStorageContents"),
                        rs.getString("savedArmorContents"),
                        rs.getString("savedExtraContents"));
                String rewardsString = rs.getString("claimedRewards");
                String[] claimedRewards = new String[]{};
                if (rewardsString != null)
                    claimedRewards = rewardsString.split(",");
                return new PlayerData(uuid, elo, savedInventory, claimedRewards);
            }
        } catch (SQLException ignored) {
        }
        return new PlayerData(uuid);
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO `player_data`(uuid, elo, claimedRewards, " +
                            "groupStorageContents, groupArmorContents, groupExtraContents," +
                            "savedStorageContents, savedArmorContents, savedExtraContents) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                            "`elo` = ?," +
                            "`claimedRewards` = ?," +
                            "`groupStorageContents` = ?," +
                            "`groupArmorContents` = ?," +
                            "`groupExtraContents` = ?," +
                            "`savedStorageContents` = ?," +
                            "`savedArmorContents` = ?," +
                            "`savedExtraContents` = ?"
            );
            populatePlayerData(ps, playerData);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void updateAllData(List<PlayerData> dataList) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO `player_data`(`uuid`, `elo`, `claimedRewards`, " +
                            "`groupStorageContents`, `groupArmorContents`, `groupExtraContents`," +
                            "`savedStorageContents`, `savedArmorContents`, `savedExtraContents`) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                            "`elo` = ?," +
                            "`claimedRewards` = ?," +
                            "`groupStorageContents` = ?," +
                            "`groupArmorContents` = ?," +
                            "`groupExtraContents` = ?," +
                            "`savedStorageContents` = ?," +
                            "`savedArmorContents` = ?," +
                            "`savedExtraContents` = ?"
            );
            for (PlayerData playerData : dataList) {
                populatePlayerData(ps, playerData);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void populatePlayerData(PreparedStatement ps, PlayerData playerData) throws SQLException {
        ps.setString(1, playerData.getUuid().toString());
        ps.setInt(2, playerData.getElo());
        ps.setString(3, String.join(",", playerData.getClaimedRewards()));
        SavedInventory savedInventory = playerData.getSavedInventory();
        ps.setString(4, savedInventory.serializeGroupStorageContents());
        ps.setString(5, savedInventory.serializeGroupArmorContents());
        ps.setString(6, savedInventory.serializeGroupExtraContents());
        ps.setString(7, savedInventory.serializeSavedStorageContents());
        ps.setString(8, savedInventory.serializeSavedArmorContents());
        ps.setString(9, savedInventory.serializeSavedExtraContents());
        ps.setInt(10, playerData.getElo());
        ps.setString(11, String.join(",", playerData.getClaimedRewards()));
        ps.setString(12, savedInventory.serializeGroupStorageContents());
        ps.setString(13, savedInventory.serializeGroupArmorContents());
        ps.setString(14, savedInventory.serializeGroupExtraContents());
        ps.setString(15, savedInventory.serializeSavedStorageContents());
        ps.setString(16, savedInventory.serializeSavedArmorContents());
        ps.setString(17, savedInventory.serializeSavedExtraContents());
    }

    @Override
    public int getEloRank(UUID uuid) {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT x.position " +
                            "FROM (SELECT t.elo," +
                            "@rownum := @rownum + 1 AS POSITION" +
                            "FROM t JOIN (SELECT @rownum := 0) r" +
                            "ORDER BY t.elo) x" +
                            "WHERE x.uuid =?"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("position");
        } catch (SQLException ignored) {
        }
        return -1;
    }

    @Override
    public LinkedHashMap<UUID, Integer> getTopSavedElo() {
        LinkedHashMap<UUID, Integer> topElo = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM `player_data` ORDER BY `elo` DESC LIMIT 11");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                topElo.put(UUID.fromString(rs.getString("uuid")), rs.getInt("elo"));
        } catch (SQLException ignored) {
        }
        return topElo;
    }
}

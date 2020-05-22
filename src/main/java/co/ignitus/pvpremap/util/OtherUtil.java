package co.ignitus.pvpremap.util;

import co.ignitus.pvpremap.PvpRemap;
import co.ignitus.pvpremap.tasks.AutoWarpTask;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OtherUtil {

    private final static PvpRemap pvpRemap = PvpRemap.getInstance();

    public static Location stringToLocation(String string) {
        if (string == null || string.isEmpty())
            return null;
        String[] parts = string.split(",");
        if (parts.length != 4)
            return null;
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Location(world, x, y, z);
    }

    public static String locationToString(Location location) {
        String world = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return world + "," + x + "," + y + "," + z;
    }

    public static ItemStack getItemFromConfig(final FileConfiguration config, final String path, final String... replace) {
        String name = MessageUtil.format(config.getString(path + ".name", ""), replace);
        List<String> lore;
        if (config.contains(path + ".lore"))
            lore = MessageUtil.format(config.getStringList(path + ".lore"));
        else
            lore = new ArrayList<>();
        Material material;
        try {
            material = Material.valueOf(config.getString(path + ".material").toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.DIRT;
            pvpRemap.getLogger().warning("Error getting material from :" + path + ", in config.yml");
        }
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null && name.length() > 0) {
            itemMeta.setDisplayName(MessageUtil.format(name, replace));
        }
        itemMeta.setLore(lore);

        config.getStringList(path + ".enchants").forEach(enchant -> {
            String[] enchantInfo = enchant.split(":");
            try {
                if (enchantInfo.length != 2)
                    return;
                String enchantName = enchantInfo[0];
                int level = Integer.parseInt(enchantInfo[1]);
                itemMeta.addEnchant(Enchantment.getByName(enchantName.toUpperCase()), level, true);
            } catch (Exception ex) {
                pvpRemap.getLogger().warning("Error getting enchant called" + enchant + " from :" + path + ", in config.yml");
            }
        });
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static String formatSeconds(int timeInSeconds) {
        int seconds = timeInSeconds % 60;
        int minutes = (timeInSeconds / 60) % 60;
        String formattedTime = "";
        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + ":";
        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds;

        return formattedTime;
    }

    public static String getName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            return player.getName();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer.getName() != null ? offlinePlayer.getName() : "unknown";
    }

    public static long getSecondsPassed(long previousTime) {
        long timeDifference = System.currentTimeMillis() - previousTime;
        return TimeUnit.MILLISECONDS.toSeconds(timeDifference);
    }
}

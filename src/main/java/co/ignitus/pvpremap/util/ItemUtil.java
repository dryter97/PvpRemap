package co.ignitus.pvpremap.util;

import co.ignitus.pvpremap.PvpRemap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    private static PvpRemap main = PvpRemap.getInstance();

    public static ItemStack createItem(String itemName, List<String> lore, Material material, int amount, boolean enchanted, boolean flags, String... replace) {

        ItemStack itemStack = new ItemStack(material, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;

        if (itemName != null && itemName.length() > 0) {
            itemMeta.setDisplayName(MessageUtil.format(itemName, replace));
        }

        if (lore != null)
            itemMeta.setLore(MessageUtil.format(lore, replace));

        if (enchanted) {
            try {
                itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            } catch (Exception e) {
                main.getLogger().warning("Couldn't add enchant to item: " + material.name());
            }
        }
        if (flags)
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static ItemStack getItemFromConfig(FileConfiguration getItemFromFile, String fullPath, String... replace) {

        String name = MessageUtil.format(getItemFromFile.getString(fullPath + ".name"));

        List<String> lore;

        if (getItemFromFile.contains(fullPath + ".lore"))
            lore = getItemFromFile.getStringList(fullPath + ".lore");
        else
            lore = new ArrayList<>();

        Material material;

        try {
            material = Material.matchMaterial(getItemFromFile.getString(fullPath + ".material").toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.DIRT;
            main.getLogger().warning("Error getting material from :" + getItemFromFile.getString(fullPath + ".material") + ", in config.yml");
        }
        boolean enchanted = getItemFromFile.getBoolean(fullPath + "enchanted");
        return ItemUtil.createItem(name, lore, material, 1, enchanted, true, replace);
    }


    public static boolean doesItemFromConfigExist(String pathName) {
        return PvpRemap.getInstance().getConfig().contains(pathName);
    }

}

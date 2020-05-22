package co.ignitus.pvpremap.entities;

import co.ignitus.pvpremap.util.SerializationUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class SavedInventory {

    private ItemStack[] groupStorageContents, groupArmorContents, groupExtraContents;
    private ItemStack[] savedStorageContents, savedArmorContents, savedExtraContents;

    public SavedInventory(String groupStorageContents, String groupArmorContents, String groupExtraContents, String savedStorageContents, String savedArmorContents, String savedExtraContents) {
        if (groupStorageContents == null)
            this.groupStorageContents = null;
        else
            this.groupStorageContents = SerializationUtils.deserializeItemStacks(groupStorageContents);

        if (groupArmorContents == null)
            this.groupArmorContents = null;
        else
            this.groupArmorContents = SerializationUtils.deserializeItemStacks(groupArmorContents);

        if (groupExtraContents == null)
            this.groupExtraContents = null;
        else
            this.groupExtraContents = SerializationUtils.deserializeItemStacks(groupExtraContents);

        if (savedStorageContents == null)
            this.savedStorageContents = null;
        else
            this.savedStorageContents = SerializationUtils.deserializeItemStacks(savedStorageContents);

        if (savedArmorContents == null)
            this.savedArmorContents = null;
        else
            this.savedArmorContents = SerializationUtils.deserializeItemStacks(savedArmorContents);

        if (savedExtraContents == null)
            this.savedExtraContents = null;
        else
            this.savedExtraContents = SerializationUtils.deserializeItemStacks(savedExtraContents);
    }

    public String serializeGroupStorageContents() {
        if (groupStorageContents != null)
            return SerializationUtils.serializeItemStacks(groupStorageContents);
        else
            return null;
    }

    public String serializeGroupArmorContents() {
        if (groupArmorContents != null)
            return SerializationUtils.serializeItemStacks(groupArmorContents);
        else
            return null;
    }

    public String serializeGroupExtraContents() {
        if (groupExtraContents != null)
            return SerializationUtils.serializeItemStacks(groupExtraContents);
        else
            return null;
    }

    public String serializeSavedStorageContents() {
        if (savedStorageContents != null)
            return SerializationUtils.serializeItemStacks(savedStorageContents);
        else
            return null;
    }

    public String serializeSavedArmorContents() {
        if (savedArmorContents != null)
            return SerializationUtils.serializeItemStacks(savedArmorContents);
        else
            return null;
    }

    public String serializeSavedExtraContents() {
        if (savedExtraContents != null)
            return SerializationUtils.serializeItemStacks(savedExtraContents);
        else
            return null;
    }
}

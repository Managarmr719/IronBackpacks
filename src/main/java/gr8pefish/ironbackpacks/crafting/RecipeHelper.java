package gr8pefish.ironbackpacks.crafting;

import gr8pefish.ironbackpacks.util.IronBackpacksConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;

/**
 * For any shared methods between recipes.
 */
public class RecipeHelper {

    public static void initNBTIfBlank(ItemStack backpack) {

        NBTTagCompound nbtTagCompound = backpack.getTagCompound();
        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setTag(IronBackpacksConstants.NBTKeys.ITEMS, new NBTTagList());
            nbtTagCompound.setTag(IronBackpacksConstants.NBTKeys.UPGRADES, new NBTTagList());
//            nbtTagCompound.setTag(IronBackpacksConstants.NBTKeys.COLOR, new NBTTag()); //ToDo: Address so upgrading doesn't lose the color
            backpack.setTagCompound(nbtTagCompound);
        }

    }

}

package gr8pefish.ironbackpacks.crafting;

import com.mojang.realmsclient.util.Pair;
import gr8pefish.ironbackpacks.api.items.backpacks.interfaces.IColorable;
import gr8pefish.ironbackpacks.api.register.ItemIUpgradeRegistry;
import gr8pefish.ironbackpacks.items.upgrades.ItemUpgrade;
import gr8pefish.ironbackpacks.registry.ItemRegistry;
import gr8pefish.ironbackpacks.util.IronBackpacksConstants;
import gr8pefish.ironbackpacks.util.Logger;
import gr8pefish.ironbackpacks.util.helpers.IronBackpacksHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class BackpackAddDyeColorRecipe extends ShapelessOreRecipe {

    private final ItemStack recipeOutput; //The outputted items after recipes

    public BackpackAddDyeColorRecipe(ItemStack recipeOutput, Object... items){
        super(recipeOutput, items);
        this.recipeOutput = recipeOutput;
    }


    /**
     *
     *
     * @param inventoryCrafting
     * @return
     */
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {

        ItemStack backpack = getFirstColorableBackpack(inventoryCrafting); //get the upgradable backpack in the recipes grid
        if (backpack == null) return null; //if no valid backpack return nothing
        ItemStack result = backpack.copy(); //the resulting backpack, copied so it's data can be more easily manipulated

        ImmutablePair<ItemStack, String> dyeToApplyPair = getFirstDye(inventoryCrafting); //get the upgrade the player is attempting to apply to the backpack
        if (dyeToApplyPair == null) return null; //if no valid dye return nothing
        ItemStack dyeToApplyStack = dyeToApplyPair.getLeft();
        String dyeToApplyColor = dyeToApplyPair.getRight();

        //save all the items from the old pack
        NBTTagCompound nbtTagCompound = result.getTagCompound();
        RecipeHelper.initNBTIfBlank(result);

        IColorable colorableBackpack = (IColorable) backpack.getItem();
        Color color = getDyeColor(dyeToApplyColor);
        if (color != null) {
            colorableBackpack.applyColor(result, color); //ToDo
            nbtTagCompound.setTag(IronBackpacksConstants.NBTKeys.COLOR, new NBTTagInt(color.getRGB()));
        }
        result = new ItemStack(recipeOutput.getItem());
        result.setTagCompound(backpack.getTagCompound());
        return result;

    }

    @Override
    public ItemStack getRecipeOutput() {
        return recipeOutput;
    }

    //=============================================================================Helper Methods====================================================================

    /**
     * Helper method for getting the first colorable backpack in the recipes grid (which will be the one used)
     * @param inventoryCrafting - the inventory to search
     * @return - the backpack to be crafted
     */
    private static ItemStack getFirstColorableBackpack(InventoryCrafting inventoryCrafting) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                ItemStack itemstack = inventoryCrafting.getStackInRowAndColumn(j, i);
                if (itemstack != null && (itemstack.getItem() instanceof IColorable)) {
                    return itemstack;
                }
            }
        }
        return null;
    }

    /**
     * Gets the first dye item in the crafting grid.
     *
     * @return - ItemStack of that dye, plus the string color
     */
    private ImmutablePair<ItemStack, String> getFirstDye(InventoryCrafting inventoryCrafting){

        ArrayList<Integer> oreDictDyeIDs = getAllDyeIDs();

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                ItemStack itemstack = inventoryCrafting.getStackInRowAndColumn(j, i);
                if (itemstack != null && itemstack.getItem() != null) {

                    int[] oreIDs = OreDictionary.getOreIDs(itemstack);

                    if (oreIDs.length > 0) {
                        for (int id : oreIDs) {
                            if (oreDictDyeIDs.contains(id)) {
                                ItemStack returnStack = itemstack.copy(); //copy stack
                                returnStack.stackSize = 1; //only apply 1 upgrade (stack size of 1)
                                return new ImmutablePair<>(returnStack, OreDictionary.getOreName(id));
                            }
                        }
                    }

                }
            }
        }
        return null;
    }

    public static ArrayList<String> getOreDictDyeNames() {

        ArrayList<String> dyesAll = new ArrayList<>();

        String[] dyes = {
            "Black",
            "Red",
            "Green",
            "Brown",
            "Blue",
            "Purple",
            "Cyan",
            "LightGray",
            "Gray",
            "Pink",
            "Lime",
            "Yellow",
            "LightBlue",
            "Magenta",
            "Orange",
            "White"
        };

        for (String s : dyes) {
            dyesAll.add("dye"+s);
        }

        return dyesAll;
    }

    private ArrayList<Integer> getAllDyeIDs(){
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> dyes = getOreDictDyeNames();
        for (String s : dyes) {
            ids.add(OreDictionary.getOreID(s));
        }
        return ids;
    }

    private Color getDyeColor(String dyeToApplyColor) {

        String colorString = dyeToApplyColor.substring(3); //remove "dye"
        colorString = colorString.substring(0, 1).toLowerCase() + colorString.substring(1); //make the first letter lowercase

        Color color = null;
        try {
            Field field = Color.class.getField(colorString); //try to get the color from the string (e.g. "red" -> Colors.red)
            color = (Color) field.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Logger.fatal("Exception getting color.");
        }

        return color;

    }


}

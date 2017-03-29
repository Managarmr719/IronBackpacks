package gr8pefish.ironbackpacks.api.items.backpacks.interfaces;

import net.minecraft.item.ItemStack;

import java.awt.*;

public interface IColorable {

    /**
     * Returns false if the item doesn't have an overlay.
     *
     * @return - if the item is colored or not
     */
    boolean isColored(ItemStack stack);

    /**
     * Gets the color of the item.
     *
     * @return - the color of the item. Null if uncolored.
     */
    Color getColor(ItemStack stack);

    /**
     * Applies the color to the item.
     *
     * @param color - the color to apply
     */
    void applyColor(ItemStack stack, Color color);

    /**
     * Removes any color from the backpack.
     * Nothing happens if the backpack isn't colored first.
     */
    void removeColor(ItemStack stack);

}

package gr8pefish.ironbackpacks.container.backpack;

import gr8pefish.ironbackpacks.items.backpacks.ItemBackpack;
import gr8pefish.ironbackpacks.util.IronBackpacksConstants;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

/**
 * The inventory used when opening the backpack normally.
 */
public class InventoryBackpack implements IInventory {

    private ItemStack backpackStack; //the itemstack instance of the backpack
    private EntityPlayer player; //the player
    private ItemStack[] inventory; //the stored items
    private String sortType; //the sort option (id/alphabetical)

    //Instantiated from GuiHandler
    public InventoryBackpack(EntityPlayer player, ItemStack backpackStack){
        this.backpackStack = backpackStack;
        this.player = player;
        this.inventory = new ItemStack[this.getSizeInventory()];
        this.sortType = "id";
        readFromNBT(backpackStack.getTagCompound());
    }

    //Instantiated for crafting result (no-player specific opening)
    public InventoryBackpack(ItemStack backpackStack, boolean noPlayer){
        this.backpackStack = backpackStack;
        this.player = null;
        this.inventory = new ItemStack[this.getSizeInventory()];
        this.sortType = "id";
        readFromNBT(backpackStack.getTagCompound(), noPlayer);
    }

    public ItemStack getBackpackStack(){
        return backpackStack;
    }

    public EntityPlayer getPlayer(){
        return player;
    }

    public String getSortType() {
        return sortType;
    }

    public void toggleSortType() {
        if (sortType.equals("id"))
            sortType = "alphabetical";
        else
            sortType = "id";
        save();
    }

    @Override
    public int getSizeInventory() {
        return ((ItemBackpack)backpackStack.getItem()).getSize(backpackStack);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return slotIndex >= this.getSizeInventory() ? null : this.inventory[slotIndex];
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        if (inventory[slotIndex] != null) {
            if (inventory[slotIndex].stackSize <= amount) {
                ItemStack itemstack = inventory[slotIndex];
                inventory[slotIndex] = null;
                return itemstack;
            }
            ItemStack itemstack1 = inventory[slotIndex].splitStack(amount);
            if (inventory[slotIndex].stackSize == 0) {
                inventory[slotIndex] = null;
            }
            return itemstack1;
        }
        else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        inventory[slotIndex] = itemStack;
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getName() {
        return ((ItemBackpack)backpackStack.getItem()).getName(backpackStack);
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {return new TextComponentString(getName());
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = null;
        if (inventory[index] != null){
            stack = inventory[index];
            inventory[index] = null;
        }
        return stack;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        //unnecessary, as it saves when closing
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        //unused
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        //unused
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack itemStack) {
        return true; //handled by BackpackSlot //TODO: fix this?
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        //nothing
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < inventory.length; i++){
            inventory[i] = null;
        }
    }

    //Helper methods for Botania's API and IBlockProvider
    public int hasStackInInv(Block blockToCheck, int meta){
        int total = 0;
        for (int i = 0; i < inventory.length; i++){
            if (inventory[i] != null && inventory[i].stackSize > 0){
                Block backpackItemAsBlock = Block.getBlockFromItem(inventory[i].getItem());
                if (backpackItemAsBlock.equals(blockToCheck) && inventory[i].getItemDamage() == meta){
                    total += inventory[i].stackSize;
                }
            }
        }
        return total;
    }

    public boolean removeOneItem(Block blockToCheck, int meta){
        for (int i = 0; i < inventory.length; i++){
            if (inventory[i] != null && inventory[i].stackSize > 0){
                Block backpackItemAsBlock = Block.getBlockFromItem(inventory[i].getItem());
                if (backpackItemAsBlock.equals(blockToCheck) && inventory[i].getItemDamage() == meta){
                    inventory[i].stackSize--;
                    if (inventory[i].stackSize == 0) inventory[i] = null;
                    save();
                    return true;
                }
            }
        }
        return false;
    }


    //=========================================================HELPER METHODS=========================================================================

    /**
     * Checks if the backpack contains any items
     * @return - true if it does, false otherwise
     */
    public boolean isEmpty(){
        for (ItemStack stack : inventory) {
            if (stack != null && stack.stackSize > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called from the container and saves the backpack.
     * @param entityPlayer - the player with the backpack
     */
    public void onGuiSaved(EntityPlayer entityPlayer){
        if (backpackStack != null){
            save();
        }
    }

    /**
     * Saves the backpack (on the server side only)
     * @param player - the player with the backpack
     */
    public void saveWithSideCheck(EntityPlayer player){
        if (!player.worldObj.isRemote) {
            onGuiSaved(player);
        }
    }

    /**
     * Updates the NBT data of the itemstack to save it
     */
    public void save(){

        NBTTagCompound nbtTagCompound = backpackStack.getTagCompound();

        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
        }

        writeToNBT(nbtTagCompound);
        backpackStack.setTagCompound(nbtTagCompound);
    }

    /**
     * Writes the data of the backpack to NBT form.
     * @param nbtTagCompound - the tag compound
     */
    public void writeToNBT(NBTTagCompound nbtTagCompound){
        if (!player.worldObj.isRemote) { //server side only
            ItemStack tempStack = backpackStack;
            ItemStack stackToUse = (tempStack == null) ? backpackStack : tempStack;

            nbtTagCompound = stackToUse.getTagCompound();

            // Write the ItemStacks in the inventory to NBT
            NBTTagList tagList = new NBTTagList();
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] != null) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setByte(IronBackpacksConstants.NBTKeys.SLOT, (byte) i);
                    inventory[i].writeToNBT(tagCompound);
                    tagList.appendTag(tagCompound);
                }
            }
            nbtTagCompound.setTag(IronBackpacksConstants.NBTKeys.ITEMS, tagList);
            nbtTagCompound.setTag(IronBackpacksConstants.NBTKeys.SORT_TYPE, new NBTTagString(sortType));
        }
    }

    /**
     * Loads in the data stored in the NBT of this stack and puts the items in their respective slots.
     * @param nbtTagCompound - the tag compound
     */
    //ToDo: Really need to remove this nonsense in next refactor
    public void readFromNBT(NBTTagCompound nbtTagCompound){
        if (!player.worldObj.isRemote) { //server side only
            ItemStack tempStack = backpackStack;
            backpackStack = (tempStack == null) ? backpackStack : tempStack;
            if (backpackStack != null) {
                nbtTagCompound = backpackStack.getTagCompound();

                if (nbtTagCompound != null) {
                    //load in sortType
                    if (nbtTagCompound.hasKey(IronBackpacksConstants.NBTKeys.SORT_TYPE)) {
                        this.sortType = nbtTagCompound.getString(IronBackpacksConstants.NBTKeys.SORT_TYPE);
                    }
                    //load in items
                    if (nbtTagCompound.hasKey(IronBackpacksConstants.NBTKeys.ITEMS)) {
                        NBTTagList tagList = nbtTagCompound.getTagList(IronBackpacksConstants.NBTKeys.ITEMS, Constants.NBT.TAG_COMPOUND);
                        this.inventory = new ItemStack[this.getSizeInventory()];

                        for (int i = 0; i < tagList.tagCount(); i++) {
                            NBTTagCompound stackTag = tagList.getCompoundTagAt(i);
                            int j = stackTag.getByte(IronBackpacksConstants.NBTKeys.SLOT);
                            if (i >= 0 && i <= inventory.length) { //ToDo: Remove 2nd equals (so just less than) as per a 1.7.10 PR; test it
                                this.inventory[j] = ItemStack.loadItemStackFromNBT(stackTag);
                            }
                        }
                    }
                }
            }
        } else { //client side: load in the sort-type for updating the sort button's tooltip
            if (nbtTagCompound.hasKey(IronBackpacksConstants.NBTKeys.SORT_TYPE)) {
                this.sortType = nbtTagCompound.getString(IronBackpacksConstants.NBTKeys.SORT_TYPE);
            }
        }
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound, boolean noPlayer){
        if (noPlayer && (backpackStack != null)) {
            nbtTagCompound = backpackStack.getTagCompound();

            if (nbtTagCompound != null) {
                //load in sortType
                if (nbtTagCompound.hasKey(IronBackpacksConstants.NBTKeys.SORT_TYPE)) {
                    this.sortType = nbtTagCompound.getString(IronBackpacksConstants.NBTKeys.SORT_TYPE);
                }
                //load in items
                if (nbtTagCompound.hasKey(IronBackpacksConstants.NBTKeys.ITEMS)) {
                    NBTTagList tagList = nbtTagCompound.getTagList(IronBackpacksConstants.NBTKeys.ITEMS, Constants.NBT.TAG_COMPOUND);
                    this.inventory = new ItemStack[this.getSizeInventory()];

                    for (int i = 0; i < tagList.tagCount(); i++) {
                        NBTTagCompound stackTag = tagList.getCompoundTagAt(i);
                        int j = stackTag.getByte(IronBackpacksConstants.NBTKeys.SLOT);
                        if (i >= 0 && i <= inventory.length) { //ToDo: Remove 2nd equals (so just less than) as per a 1.7.10 PR; test it
                            this.inventory[j] = ItemStack.loadItemStackFromNBT(stackTag);
                        }
                    }
                }
            }
        }
    }
}

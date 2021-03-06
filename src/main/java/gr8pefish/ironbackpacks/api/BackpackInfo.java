package gr8pefish.ironbackpacks.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class BackpackInfo implements INBTSerializable<NBTTagCompound> {

    @Nonnull
    private BackpackType backpackType;
    @Nonnull
    private final List<BackpackUpgrade> upgrades;
    @Nonnull
    private BackpackSpecialty specialty;
    @Nonnull
    private ItemStackHandler stackHandler;
    @Nullable
    private UUID owner;

    private BackpackInfo(@Nonnull BackpackType backpackType, @Nonnull List<BackpackUpgrade> upgrades, @Nonnull BackpackSpecialty specialty, @Nonnull ItemStackHandler stackHandler) {
        Preconditions.checkNotNull(backpackType, "Backpack type cannot be null");
        Preconditions.checkNotNull(upgrades, "Upgrade list cannot be null");
        Preconditions.checkNotNull(specialty, "Specialty type cannot be null");

        this.backpackType = backpackType;
        this.upgrades = upgrades;
        this.specialty = specialty;
        this.stackHandler = stackHandler;
    }

    public BackpackInfo(@Nonnull BackpackType backpackType, @Nonnull BackpackSpecialty specialty, @Nonnull ItemStackHandler stackHandler) {
        this(backpackType, Lists.newArrayList(), specialty, stackHandler);
    }

    private BackpackInfo() {
        //noinspection ConstantConditions - null/null is automatically registered, so we know it's always there.
        this(IronBackpacksAPI.getBackpackType(IronBackpacksAPI.NULL), Lists.newArrayList(), BackpackSpecialty.NONE, new ItemStackHandler());
    }

    @Nonnull
    public BackpackType getBackpackType() {
        return backpackType;
    }

    @Nonnull
    public ItemStackHandler getStackHandler() {
        return stackHandler;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public BackpackInfo setOwner(@Nullable UUID owner) {
        this.owner = owner;
        return this;
    }

    public boolean conflicts(@Nullable BackpackUpgrade upgrade) {
        if (upgrade == null)
            return false;

        for (BackpackUpgrade installed : upgrades)
            if (upgrade.isConflicting(installed))
                return true;

        return false;
    }

    public boolean hasUpgrade(@Nullable BackpackUpgrade backpackUpgrade) {
        return upgrades.contains(backpackUpgrade);
    }

    @Nonnull
    public BackpackInfo applyUpgrade(@Nullable BackpackUpgrade backpackUpgrade) {
        if (backpackUpgrade != null)
            upgrades.add(backpackUpgrade);

        return this;
    }

    @Nonnull
    public BackpackInfo removeUpgrade(@Nullable BackpackUpgrade backpackUpgrade) {
        if (backpackUpgrade != null)
            upgrades.remove(backpackUpgrade);

        return this;
    }

    @Nonnull
    public BackpackInfo removeUpgrade() {
        if (!upgrades.isEmpty())
            upgrades.remove(upgrades.size() - 1);

        return this;
    }

    public BackpackSpecialty getSpecialty() {
        return specialty;
    }

    public int getPointsUsed() {
        int used = 0;
        for (BackpackUpgrade backpackUpgrade : upgrades)
            used += backpackUpgrade.getApplicationCost();

        return used;
    }

    public int getMaxPoints() {
        return backpackType.getMaxPoints() + (specialty == BackpackSpecialty.UPGRADE ? 5 : 0);
    }

    public List<BackpackUpgrade> getApplied() {
        return ImmutableList.copyOf(upgrades);
    }

    // INBTSerializable

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        // Serialize backpack info
        tag.setString("type", backpackType.getIdentifier().toString());
        tag.setString("spec", specialty.name());
        if (owner != null)
            tag.setString("own", owner.toString());

        // Serialize upgrades
        NBTTagList installedUpgrades = new NBTTagList();
        for (BackpackUpgrade backpackUpgrade : upgrades)
            installedUpgrades.appendTag(new NBTTagString(backpackUpgrade.getIdentifier().toString()));
        tag.setTag("upgrades", installedUpgrades);

        // Serialize inventory
        tag.setTag("inv", stackHandler.serializeNBT());

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        // Deserialize backpack info
        backpackType = IronBackpacksAPI.getBackpackType(new ResourceLocation(nbt.getString("type")));
        specialty = BackpackSpecialty.getSpecialty(nbt.getString("spec"));
        if (nbt.hasKey("own"))
            owner = UUID.fromString(nbt.getString("own"));

        // Deserialize upgrades
        NBTTagList installedUpgrades = nbt.getTagList("upgrades", 8);
        for (int i = 0; i < installedUpgrades.tagCount(); i++) {
            ResourceLocation identifier = new ResourceLocation(installedUpgrades.getStringTagAt(i));
            BackpackUpgrade backpackUpgrade = IronBackpacksAPI.getUpgrade(identifier);
            if (!backpackUpgrade.isNull())
                upgrades.add(backpackUpgrade);
        }

        // Deserialize inventory
        stackHandler.deserializeNBT(nbt.getCompoundTag("inv"));
    }

    @Nonnull
    public static BackpackInfo fromStack(@Nonnull ItemStack stack) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null");

        if (stack.isEmpty() || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("packInfo"))
            return new BackpackInfo();

        return fromTag(stack.getTagCompound().getCompoundTag("packInfo"));
    }

    @Nonnull
    public static BackpackInfo fromTag(@Nullable NBTTagCompound tag) {
        BackpackInfo backpackInfo = new BackpackInfo();
        if (tag == null || tag.hasNoTags())
            return backpackInfo;

        backpackInfo.deserializeNBT(tag);
        return backpackInfo;
    }
}

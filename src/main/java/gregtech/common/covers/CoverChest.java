package gregtech.common.covers;

import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;

import cpw.mods.fml.common.network.ByteBufUtils;
import gregtech.api.covers.CoverContext;
import gregtech.api.gui.modularui.CoverUIBuildContext;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.util.ISerializableObject;
import gregtech.common.gui.modularui.widget.CoverDataControllerWidget;
import io.netty.buffer.ByteBuf;

public class CoverChest extends CoverBehaviorBase<CoverChest.ChestInventory> {

    private final int slots;
    private final int stackSizeLimit = 1;

    public CoverChest(CoverContext context, int slots, ITexture coverTexture) {
        super(context, ChestInventory.class, coverTexture);
        if (slots <= 0) throw new IllegalArgumentException("slots must be greater than 0");
        this.slots = slots;
        Object initializer = context.getCoverData();
        if (initializer == null || initializer instanceof ItemStack) {
            // Re-initialization required until we merge covers and their data objects,
            // since this relies on an instance field.
            coverData = initializeData();
        }
    }

    @Override
    public CoverChest.ChestInventory initializeData() {
        return new CoverChest.ChestInventory(slots, stackSizeLimit);
    }

    @Override
    public boolean hasCoverGUI() {
        return true;
    }

    @Override
    public boolean allowsCopyPasteTool() {
        return false;
    }

    @Override
    public boolean allowsTickRateAddition() {
        return false;
    }

    @Override
    public void onCoverRemoval() {
        ICoverable iCoverable = coveredTile.get();
        if (iCoverable == null || iCoverable.getWorld().isRemote) return;
        coverData.dropAll(iCoverable, coverSide);
    }

    @Override
    public int getMinimumTickRate() {
        return coverData.firstTick ? 1 : 0;
    }

    @Override
    public ChestInventory doCoverThings(byte aInputRedstone, long aTimer) {
        ICoverable coverable = coveredTile.get();
        if (coverable == null) {
            return coverData;
        }
        // migrate slots. mostly needed while in development. still can be useful if we ever resize the inventory in the
        // future
        if (coverData.items.getSlots() != slots) {
            if (coverData.items.getSlots() > slots) {
                for (int i = slots; i < coverData.items.getSlots(); i++) {
                    ItemStack item = coverData.items.getStackInSlot(i);
                    if (item != null) {
                        dropItem(coverable, coverSide, item);
                    }
                }
            }

            ChestInventory newData = initializeData();
            int toCopy = Math.min(newData.items.getSlots(), coverData.items.getSlots());
            for (int i = 0; i < toCopy; i++) {
                newData.items.setStackInSlot(i, coverData.items.getStackInSlot(i));
            }
            return newData;
        }
        coverData.firstTick = false;
        return coverData;
    }

    @Override
    public ModularWindow createWindow(CoverUIBuildContext buildContext) {
        return new ChestUIFactory(buildContext).createWindow();
    }

    public class ChestUIFactory extends UIFactory {

        private static final int spaceX = 18;
        private static final int spaceY = 18;

        public ChestUIFactory(CoverUIBuildContext buildContext) {
            super(buildContext);
        }

        @Override
        protected int getGUIHeight() {
            int height = slots / 3 * 18 + 8;
            if (!getUIBuildContext().isAnotherWindow()) {
                // player inv is 4 row
                return height + 4 * 18 + 14;
            }
            return height;
        }

        @Override
        protected void addTitleToUI(ModularWindow.Builder builder) {}

        @Override
        protected int getGUIWidth() {
            if (getUIBuildContext().isAnotherWindow()) {
                return 18 * 3 + 20;
            } else {
                return 18 * 9 + 20;
            }
        }

        @Override
        protected void addUIWidgets(ModularWindow.Builder builder) {
            CoverDataControllerWidget<ChestInventory> w = new CoverDataControllerWidget<>(
                this::getCoverData,
                this::setCoverData,
                CoverChest.this::loadFromNbt);
            ChestInventory d = getCoverData();
            LimitingItemStackHandler h;
            if (d == null) {
                // ???
                return;
            }
            h = d.items;
            SlotGroup slotGroup = SlotGroup.ofItemHandler(h, 3)
                .build();
            if (getUIBuildContext().isAnotherWindow()) {
                slotGroup.setPos(4, 4);
            } else {
                slotGroup.setPos(getGUIWidth() / 2 - 18 * 3 / 2, 6);
            }
            w.addChild(slotGroup);
            builder.widget(w);

            builder.setPos(
                (size, mainWindow) -> Alignment.Center.getAlignedPos(size, new Size(getGUIWidth(), getGUIHeight()))
                    .subtract(getUIBuildContext().isAnotherWindow() ? getGUIWidth() + 80 : 0, 0));
        }

        @Override
        protected boolean doesBindPlayerInventory() {
            return true;
        }
    }

    private static class LimitingItemStackHandler extends ItemStackHandler {

        private final int slotLimit;

        private LimitingItemStackHandler(int slots, int slotLimit) {
            super(slots);
            this.slotLimit = slotLimit;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slotLimit;
        }
    }

    public static class ChestInventory implements ISerializableObject {

        final LimitingItemStackHandler items;
        boolean firstTick;

        public ChestInventory(int slots, int stackSize) {
            items = new LimitingItemStackHandler(slots, stackSize);
        }

        @Override
        public void readFromPacket(ByteArrayDataInput aBuf) {
            items.deserializeNBT(ISerializableObject.readCompoundTagFromGreggyByteBuf(aBuf));
        }

        @Override
        public void writeToByteBuf(ByteBuf aBuf) {
            ByteBufUtils.writeTag(aBuf, items.serializeNBT());
        }

        @Override
        public void loadDataFromNBT(NBTBase aNBT) {
            if (!(aNBT instanceof NBTTagCompound)) return;
            items.deserializeNBT((NBTTagCompound) aNBT);
            firstTick = true;
        }

        @NotNull
        @Override
        public NBTBase saveDataToNBT() {
            return items.serializeNBT();
        }

        @NotNull
        @Override
        public ISerializableObject copy() {
            ChestInventory copy = new ChestInventory(items.getSlots(), items.getSlotLimit(0));
            for (int i = 0; i < items.getSlots(); i++) {
                copy.items.setStackInSlot(i, items.getStackInSlot(i));
            }
            return copy;
        }

        public void dropAll(ICoverable coverable, ForgeDirection direction) {
            for (int i = 0; i < items.getSlots(); i++) {
                ItemStack tItem = items.getStackInSlot(i);
                if (tItem == null) {
                    continue;
                }
                dropItem(coverable, direction, tItem);
                items.setStackInSlot(i, null);
            }
        }
    }

    private static void dropItem(ICoverable coverable, ForgeDirection direction, ItemStack tItem) {
        final EntityItem tItemEntity = new EntityItem(
            coverable.getWorld(),
            coverable.getXCoord() + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F + direction.offsetX,
            coverable.getYCoord() + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F + direction.offsetY,
            coverable.getZCoord() + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F + direction.offsetZ,
            new ItemStack(tItem.getItem(), tItem.stackSize, tItem.getItemDamage()));
        if (tItem.hasTagCompound()) {
            tItemEntity.getEntityItem()
                .setTagCompound(
                    (NBTTagCompound) tItem.getTagCompound()
                        .copy());
        }
        tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
        tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.05D + 0.2D);
        tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
        tItemEntity.hurtResistantTime = 999999;
        tItemEntity.lifespan = 60000;
        coverable.getWorld()
            .spawnEntityInWorld(tItemEntity);
    }
}

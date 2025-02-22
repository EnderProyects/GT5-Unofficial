package gtPlusPlus.xmod.gregtech.api.metatileentity.implementations;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.lib.GTPPCore;

public class MTEHatchNaquadah extends MTEHatchInput {

    public final FluidStack[] mFluidsToUse = new FluidStack[3];
    public final int mFluidCapacity;

    public MTEHatchNaquadah(final int aID, final String aName, final String aNameRegional) {
        super(aID, aName, aNameRegional, 6);
        mFluidCapacity = 32000;
        initHatch();
    }

    public MTEHatchNaquadah(final String aName, final String[] aDescription, final ITexture[][][] aTextures) {
        super(aName, 6, aDescription[0], aTextures);
        mFluidCapacity = 32000;
        initHatch();
    }

    private void initHatch() {
        if (mFluidsToUse[0] == null) {
            mFluidsToUse[0] = Materials.Naquadah.getMolten(1);
        }
        if (mFluidsToUse[1] == null) {
            mFluidsToUse[1] = Materials.NaquadahEnriched.getMolten(1);
        }
        if (mFluidsToUse[2] == null) {
            mFluidsToUse[2] = Materials.Naquadria.getMolten(1);
        }
    }

    @Override
    public ITexture[] getTexturesActive(final ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(BlockIcons.NAQUADAH_REACTOR_FLUID_SIDE_ACTIVE) };
    }

    @Override
    public ITexture[] getTexturesInactive(final ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(BlockIcons.NAQUADAH_REACTOR_FLUID_SIDE) };
    }

    public boolean allowPutStack(final IGregTechTileEntity aBaseMetaTileEntity, final int aIndex,
        final ForgeDirection side, final ItemStack aStack) {
        if (side == aBaseMetaTileEntity.getFrontFacing() && aIndex == 0) {
            for (FluidStack f : mFluidsToUse) {
                if (f != null) {
                    if (GTUtility.getFluidForFilledItem(aStack, true)
                        .getFluid() == f.getFluid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isFluidInputAllowed(final FluidStack aFluid) {
        for (FluidStack f : mFluidsToUse) {
            if (f != null) {
                if (aFluid.getFluid() == f.getFluid()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getCapacity() {
        return this.mFluidCapacity;
    }

    @Override
    public MetaTileEntity newMetaEntity(final IGregTechTileEntity aTileEntity) {
        return new MTEHatchNaquadah(this.mName, this.mDescriptionArray, this.mTextures);
    }

    private static final String[] aDescCache = new String[3];

    @Override
    public String[] getDescription() {
        if (aDescCache[0] == null || aDescCache[0].contains(".name") || aDescCache[0].contains("fluid.")) {
            aDescCache[0] = formatFluidString(this.mFluidsToUse[0]);
        }
        if (aDescCache[1] == null || aDescCache[1].contains(".name") || aDescCache[1].contains("fluid.")) {
            aDescCache[1] = formatFluidString(this.mFluidsToUse[1]);
        }
        if (aDescCache[2] == null || aDescCache[2].contains(".name") || aDescCache[2].contains("fluid.")) {
            aDescCache[2] = formatFluidString(this.mFluidsToUse[2]);
        }
        String aNaq = aDescCache[0];
        String aEnrNaq = aDescCache[1];
        String aNaquad = aDescCache[2];
        return new String[] { "Fluid Input for Multiblocks", "Capacity: " + getCapacity() + "L",
            "Accepted Fluid: " + aNaq, "Accepted Fluid: " + aEnrNaq, "Accepted Fluid: " + aNaquad,
            GTPPCore.GT_Tooltip.get() };
    }

    private String formatFluidString(FluidStack fluidStack) {
        String mTempMod = EnumChatFormatting.RESET.toString();
        int mLockedTemp = fluidStack.getFluid()
            .getTemperature();
        if (mLockedTemp <= -3000) {
            mTempMod = "" + EnumChatFormatting.DARK_PURPLE;
        } else if (mLockedTemp <= -500) {
            mTempMod = "" + EnumChatFormatting.DARK_BLUE;
        } else if (mLockedTemp <= -50) {
            mTempMod = "" + EnumChatFormatting.BLUE;
        } else if (mLockedTemp >= 30 && mLockedTemp <= 300) {
            mTempMod = "" + EnumChatFormatting.AQUA;
        } else if (mLockedTemp >= 301 && mLockedTemp <= 800) {
            mTempMod = "" + EnumChatFormatting.YELLOW;
        } else if (mLockedTemp >= 801 && mLockedTemp <= 1500) {
            mTempMod = "" + EnumChatFormatting.GOLD;
        } else if (mLockedTemp >= 1501) {
            mTempMod = "" + EnumChatFormatting.RED;
        }
        return mTempMod + fluidStack.getLocalizedName();
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int aColorIndex, boolean aActive, boolean aRedstone) {
        byte a1 = this.getActualTexture();
        byte a2 = this.getmTexturePage();
        int textureIndex = a1 | a2 << 7;
        byte texturePointer = (byte) (a1 & 127);
        if (side == ForgeDirection.UP || side == ForgeDirection.DOWN) {
            ITexture g = textureIndex > 0 ? BlockIcons.casingTexturePages[a2][texturePointer]
                : BlockIcons.MACHINE_CASINGS[this.mTier][aColorIndex + 1];
            return new ITexture[] { g, TextureFactory.of(BlockIcons.NAQUADAH_REACTOR_FLUID_TOP_ACTIVE) };
        }
        return side != facing
            ? (textureIndex > 0 ? new ITexture[] { BlockIcons.casingTexturePages[a2][texturePointer] }
                : new ITexture[] { BlockIcons.MACHINE_CASINGS[this.mTier][aColorIndex + 1] })
            : (textureIndex > 0
                ? (aActive ? this.getTexturesActive(BlockIcons.casingTexturePages[a2][texturePointer])
                    : this.getTexturesInactive(BlockIcons.casingTexturePages[a2][texturePointer]))
                : (aActive ? this.getTexturesActive(BlockIcons.MACHINE_CASINGS[this.mTier][aColorIndex + 1])
                    : this.getTexturesInactive(BlockIcons.MACHINE_CASINGS[this.mTier][aColorIndex + 1])));
    }
}

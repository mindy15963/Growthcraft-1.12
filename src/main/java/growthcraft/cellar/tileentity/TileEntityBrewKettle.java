package growthcraft.cellar.tileentity;

import growthcraft.cellar.blocks.BlockBrewKettle;
import growthcraft.cellar.init.GrowthcraftCellarBlocks;
import growthcraft.core.utils.GrowthcraftLogger;
import growthcraft.hops.items.ItemHops;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nullable;

public class TileEntityBrewKettle extends TileEntity implements ITickable, ICapabilityProvider {

    public int brewTime;
    public int maxBrewTime;

    private boolean heated;

    private ItemStackHandler itemStackHandlerInput;
    //private ItemStackHandler itemStackHandlerOutput;

    private IFluidHandler fluidHandlerInput;
    //private IFluidHandler fluidHandlerOutput;

    public TileEntityBrewKettle() {
        itemStackHandlerInput = new ItemStackHandler(1);
        //itemStackHandlerOutput = new ItemStackHandler(1);

        // 2000 ticks = 100 seconds
        this.maxBrewTime = 2000;
        this.heated = false;
    }

    public int getBrewProgress() {
        return brewTime > 0 ? brewTime : 0;
    }

    public int getPercentComplete() {
        float percentage = (brewTime * 100.0f) / maxBrewTime;
        GrowthcraftLogger.getLogger().info("TileEntityBrewKettle.getPercentComplete(" + brewTime + "/" + maxBrewTime + "*100) == " + percentage);
        return (int) percentage;
    }

    public boolean isHeated() {
        if (world.getBlockState(pos.down()).getBlock() instanceof BlockFire) {
            return true;
        }
        return heated;
    }

    private boolean canCook() {
        ItemStack inputStack = this.itemStackHandlerInput.getStackInSlot(0);
        // TODO: Check for the inputFluidTank.
        return inputStack.getItem() instanceof ItemHops;
    }


    /* -- ITickable -- */

    @Override
    public void update() {

        // Check to determine if we have heat or not.
        // TODO: Need to implement a better heat detection system.
        this.heated = this.world.getBlockState(this.pos.down()).getMaterial() == Material.FIRE;
        world.setBlockState(
                pos,
                GrowthcraftCellarBlocks.blockBrewKettle.getDefaultState().withProperty(BlockBrewKettle.HEATED, Boolean.valueOf(this.heated)), 3
        );

        //if (!this.world.isRemote) {
        ItemStack inputStack = this.itemStackHandlerInput.getStackInSlot(0);

        // If the BrewKettle is heated,
        if (this.heated && !inputStack.isEmpty() && this.canCook()) {
            this.brewTime += 1;

            //GrowthcraftLogger.getLogger().info("We are cooking: brewTime(" + brewTime + ") maxBrewTime" + maxBrewTime + ")");

            if (this.brewTime == this.maxBrewTime) {
                // Decrease the input stack by one item.
                itemStackHandlerInput.getStackInSlot(0).shrink(1);

                // TODO: Decrease the fluid in the input FluidTank

                // TODO: Increase the fluid in the output FluidTank

                // Reset the cooking timers
                this.brewTime = 0;
            }

        }
        //} else {
        IBlockState iblockstate = this.world.getBlockState(pos);
        final int FLAGS = 3;
        world.notifyBlockUpdate(pos, iblockstate, iblockstate, FLAGS);
        //}

        this.markDirty();
    }


    /* -- ICapabilityProvider -- */

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //if (facing != null) {
            //return (T) new CombinedInvWrapper(itemStackHandlerInput, itemStackHandlerOutput);
            return (T) new CombinedInvWrapper(itemStackHandlerInput);

            //}
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            //if (facing != null) {
            //return (T) new CombinedInvWrapper(itemStackHandlerInput, itemStackHandlerOutput);
            return (T) new CombinedInvWrapper(itemStackHandlerInput);

            //}
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return this.getCapability(capability, facing) != null;
    }

    /* -- TileEntity -- */

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.brewTime = compound.getInteger("brewTime");
        this.maxBrewTime = compound.getInteger("maxBrewTime");
        //this.itemStackHandlerOutput.deserializeNBT(compound.getCompoundTag("inventoryOutput"));
        this.itemStackHandlerInput.deserializeNBT(compound.getCompoundTag("inventoryInput"));
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("brewTime", this.brewTime);
        compound.setInteger("maxBrewTime", this.maxBrewTime);
        //compound.setTag("inventoryOutput", this.itemStackHandlerOutput.serializeNBT());
        compound.setTag("inventoryInput", this.itemStackHandlerInput.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        int metadata = getBlockMetadata();
        return new SPacketUpdateTileEntity(this.pos, metadata, compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return compound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound getTileData() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return compound;
    }


    // TODO: setup setField and getField these are needed to update the progress bar in real time.
    // https://github.com/TheGreyGhost/MinecraftByExample/tree/master/src/main/java/minecraftbyexample/mbe31_inventory_furnace

}

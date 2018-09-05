package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileBandAmp extends TileInstrument
{
    private boolean previousRedStoneState;

    public TileBandAmp() { /* NOP */ }

    public TileBandAmp(EnumFacing facing)
    {
        this.inventory =  new StackHandler(8);
        this.facing = facing;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        previousRedStoneState = tag.getBoolean("powered");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.setBoolean("powered", this.previousRedStoneState);
        return super.writeToNBT(tag);
    }

    public void setPowered(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        ModLogger.info("Powered facing: %s, name: %s", facing.getName(),  blockIn.getBlockState().getBlock().getLocalizedName());
    }

    /**
     * @return the previousRedStoneState
     */
    public boolean getPreviousRedStoneState()
    {
        return previousRedStoneState;
    }

    /**
     * @param previousRedStoneState the previousRedStoneState to set
     */
    public void setPreviousRedStoneState(boolean previousRedStoneState)
    {
        this.previousRedStoneState = previousRedStoneState;
        markDirty();
    }
}

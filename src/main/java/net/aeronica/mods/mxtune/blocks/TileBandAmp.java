/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.EnumRelativeSide;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.LockCode;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileBandAmp extends TileInstrument implements IModLockableContainer
{
    private static final int MAX_SLOTS = 8;
    private boolean previousInputPowerState;
    private Integer playID;
    private int lastPlayID;
    private LockCode code = LockCode.EMPTY_CODE;
    private OwnerUUID ownerUUID = OwnerUUID.EMPTY_UUID;
    private String bandAmpCustomName;
    private int duration;

    public TileBandAmp() { /* NOP */ }

    public TileBandAmp(EnumFacing facing)
    {
        this.inventory =  new InstrumentStackHandler(MAX_SLOTS);
        this.facing = facing;
        this.playID = -1;
    }

    @Override
    public void onLoad()
    {
        clearLastPlayID();
        setPlayID(-1);
    }

    public Integer getPlayID() { return playID; }

    public void setPlayID(Integer playID)
    {
        this.playID = playID != null ? playID : -1;
        if (isPlaying()) this.lastPlayID = this.playID;
    }

    public boolean lastPlayIDSuccess() { return this.lastPlayID > 0; }

    public void clearLastPlayID() { this.lastPlayID = -1; }

    private boolean isPlaying() { return (this.playID != null) && (this.playID > 0); }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        inventory = new InstrumentStackHandler(MAX_SLOTS);
        inventory.deserializeNBT(tag);
        duration = tag.getInteger("Duration");
        previousInputPowerState = tag.getBoolean("powered");
        code = LockCode.fromNBT(tag);
        ownerUUID = OwnerUUID.fromNBT(tag);

        if (tag.hasKey("CustomName", 8))
        {
            bandAmpCustomName = tag.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.merge(inventory.serializeNBT());
        tag.setBoolean("powered", previousInputPowerState);
        tag.setInteger("Duration", duration);

        if (code != null)
        {
            code.toNBT(tag);
        }
        if (ownerUUID != null)
        {
            ownerUUID.toNBT(tag);
        }
        if (hasCustomName())
        {
            tag.setString("CustomName", bandAmpCustomName);
        }
        return super.writeToNBT(tag);
    }

    public int getDuration()
    {
        return this.duration;
    }

    public void setDuration(int duration)
    {
            this.duration = duration;
            markDirty();
    }

    /** This does nothing but log the side that's powered */
    void logInputPower(BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        Vec3i vec3i = pos.subtract(fromPos);
        ModLogger.info("TileBandAmp: Powered from %s's %s face",
                       blockIn.getBlockState().getBlock().getLocalizedName(),
                       EnumFacing.getFacingFromVector(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
    }

    /**
     * @return the previousInputPowerState
     */
    boolean getPreviousInputState()
    {
        return previousInputPowerState;
    }

    /**
     * @param previousRedStoneState the previousInputPowerState to set
     */
    void setPreviousInputState(boolean previousRedStoneState)
    {
        this.previousInputPowerState = previousRedStoneState;
        markDirty();
    }

    private void syncClient()
    {
        if(world != null && !world.isRemote)
        {
            world.notifyBlockUpdate(getPos(), world.getBlockState(pos), world.getBlockState(pos), 2);
        }
    }
    class InstrumentStackHandler extends ItemStackHandler
    {
        InstrumentStackHandler(int size) {super(size);}

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if ((stack.getItem() instanceof ItemInstrument))
                return super.insertItem(slot, stack, simulate);
            else
                return stack;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> cap, @Nullable EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        return (((enumRelativeSide == EnumRelativeSide.TOP) && !isPlaying()) || ((enumRelativeSide == EnumRelativeSide.BOTTOM) && isPlaying()) && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, @Nullable EnumFacing side)
    {
        EnumRelativeSide enumRelativeSide = EnumRelativeSide.getRelativeSide(side, getFacing());
        if ((((enumRelativeSide == EnumRelativeSide.TOP) && !isPlaying()) || ((enumRelativeSide == EnumRelativeSide.BOTTOM) && isPlaying())) && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        return super.getCapability(cap, side);
    }

    /*
     Lockable
     */
    @Override
    public boolean isLocked()
    {
        return this.code != null && !this.code.isEmpty();
    }

    @Override
    public void setLockCode(LockCode code)
    {
        this.code = code;
        markDirty();
        syncClient();
    }

    @Override
    public LockCode getLockCode() { return this.code; }

    @Override
    public boolean isOwner(OwnerUUID ownerUUID)
    {
        return this.ownerUUID != null && this.ownerUUID.equals(ownerUUID);
    }

    @Override
    public void setOwner(OwnerUUID ownerUUID)
    {
        this.ownerUUID = ownerUUID;
        markDirty();
    }

    @Override
    public OwnerUUID getOwner() { return ownerUUID; }

    /*
     Nameable
    */
    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.bandAmpCustomName : "tile.mxtune:band_amp.name";
    }

    @Override
    public boolean hasCustomName()
    {
        return this.bandAmpCustomName != null && !this.bandAmpCustomName.isEmpty();
    }

    void setCustomInventoryName(String customInventoryName)
    {
        this.bandAmpCustomName = customInventoryName;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if (this.world.getTileEntity(this.pos) != this)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }
}

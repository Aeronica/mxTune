/**
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
 *
 * ********************************************************************
 * Changes for Aeronica's mxTune MOD:
 * Updated for MC 1.9+, added additional constructors for more control
 * over rotation and offset. Added ability to stand while riding and a
 * "data-watcher" to sync the boolean SHOULD_SIT to the client.
 * ********************************************************************
 * 
 * MrCrayfish's Furniture Mod
 * Copyright (C) 2016  MrCrayfish (http://www.mrcrayfish.com/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.aeronica.mods.mxtune.entity;

import net.aeronica.mods.mxtune.groups.PlayManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntitySittableBlock extends Entity
{

    static final DataParameter<Boolean> SHOULD_SIT = EntityDataManager.<Boolean> createKey(EntitySittableBlock.class, DataSerializers.BOOLEAN);
    static final DataParameter<BlockPos> BLOCK_POS = EntityDataManager.<BlockPos> createKey(EntitySittableBlock.class, DataSerializers.BLOCK_POS);
    BlockPos blockPos;
    float yaw;
    Integer playID = null;
    
    public EntitySittableBlock(World world)
    {
        super(world);
        this.noClip = true;
        this.height = 0.0001F;
        this.width = 0.0001F;
        this.dataManager.set(SHOULD_SIT, Boolean.valueOf(true));
        this.dataManager.set(BLOCK_POS, new BlockPos(0,0,0));
    }

    /** Allow riding standing up if shouldRiderSit is false */
    public EntitySittableBlock(World world, BlockPos posIn, double y0ffset, boolean shouldRiderSit)
    {
        this(world);
        this.blockPos = posIn;
        setPosition(posIn.getX() + 0.5D, posIn.getY() + y0ffset, posIn.getZ() + 0.5D);
        this.dataManager.set(SHOULD_SIT, Boolean.valueOf(shouldRiderSit));
        this.dataManager.set(BLOCK_POS, posIn);
    }

    public EntitySittableBlock(World world, BlockPos posIn, double xOffset, double yOffset, double zOffset)
    {
        this(world);
        this.blockPos = posIn;
        setPosition(posIn.getX() + xOffset, posIn.getY() + yOffset, posIn.getZ() + zOffset);
        this.dataManager.set(SHOULD_SIT, Boolean.valueOf(true));
        this.dataManager.set(BLOCK_POS, posIn);
    }

    public EntitySittableBlock(World world, BlockPos posIn, double xOffset, double yOffset, double zOffset, float yaw)
    {
        this(world);
        this.blockPos = posIn;
        this.yaw = yaw;
        this.setPositionAndRotation(posIn.getX() + xOffset, posIn.getY() + yOffset, posIn.getZ() + zOffset, yaw, 0);
        this.dataManager.set(SHOULD_SIT, Boolean.valueOf(true));
        this.dataManager.set(BLOCK_POS, posIn);
    }

    public EntitySittableBlock(World world, BlockPos posIn, double y0ffset, int rotation, double rotationOffset)
    {
        this(world);
        this.blockPos = posIn;
        setPostionConsideringRotation(posIn.getX() + 0.5D, posIn.getY() + y0ffset, posIn.getZ() + 0.5D, rotation, rotationOffset);
        this.dataManager.set(SHOULD_SIT, Boolean.valueOf(true));
        this.dataManager.set(BLOCK_POS, posIn);
    }

    private void setPostionConsideringRotation(double xIn, double yIn, double zIn, int rotationIn, double rotationOffsetIn)
    {
        double x = xIn;
        double y = yIn;
        double z = zIn;
        switch (rotationIn)
        {
        case 2:
            z += rotationOffsetIn;
            break;
        case 0:
            z -= rotationOffsetIn;
            break;
        case 3:
            x -= rotationOffsetIn;
            break;
        case 1:
            x += rotationOffsetIn;
            break;
        }
        setPosition(x, y, z);
    }

    @Override
    public double getMountedYOffset() {return this.height * 0.0D;}

    @Override
    protected boolean shouldSetPosAfterLoading() {return false;}

    @Override
    public void onEntityUpdate()
    {
        if (!this.world.isRemote &&
                ((this.getPassengers().isEmpty() && !this.isDead) ||
                        this.world.isAirBlock(blockPos)))
        {
            this.setDead();
            world.updateComparatorOutputLevel(getPosition(), world.getBlockState(getPosition()).getBlock());
            if (playID != null) PlayManager.stopPlayID(playID);
        }
    }

    @Override
    protected void entityInit()
    {
        this.dataManager.register(SHOULD_SIT, Boolean.valueOf(true));
        this.dataManager.register(BLOCK_POS, blockPos);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {}

    @Override
    public boolean shouldRiderSit() {return ((Boolean) this.dataManager.get(SHOULD_SIT)).booleanValue();}

    public BlockPos getBlockPos1() {return blockPos;}

    public float getYaw() {return yaw;}
    
    public void setPlayID(Integer playID)
    {
        this.playID = playID;
    }

    public Integer getPlayID()
    {
        return playID;
    }

    public BlockPos getBlockPos()
    {
        return (this.dataManager.get(BLOCK_POS)).toImmutable();
    }
    
    @Override
    public boolean equals(Object otherEntity)
    {
        // Entities are unique in each world so there should never be a case where they are equal
        // At the the super class level they are tested by their assigned entityID.
        // Overridden as a SonarQube recommendation
        return super.equals(otherEntity);
    }

    @Override
    public int hashCode()
    {
        // At the the super class level the hash code is the entityID.
        // Overridden as a SonarQube recommendation
        return super.hashCode();
    }

}

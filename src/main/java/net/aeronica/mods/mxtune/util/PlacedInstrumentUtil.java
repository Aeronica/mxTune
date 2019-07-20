/*
 * Aeronica's mxTune Mod
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 * Updated for 1.9+, added additional constructors for more control over
 * offset and rotation. Allow standing while riding.
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
package net.aeronica.mods.mxtune.util;

import net.aeronica.mods.mxtune.entity.EntitySittableBlock;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PlacedInstrumentUtil
{    
    private PlacedInstrumentUtil() {/* NOP */}
    /** Gets a Block position under a players feet. works on blocks, half slabs, carpets. */
    private static BlockPos blockUnderFoot(PlayerEntity playerIn)
    {
        int x = (int) Math.floor(playerIn.posX);
        int y = (int) Math.floor(playerIn.posY - playerIn.getYOffset() - 0.6D);
        int z = (int) Math.floor(playerIn.posZ);
        return new BlockPos(x, y, z);
    }

    @SuppressWarnings("unused")
    public static boolean standOnBlock(World worldIn, BlockPos posIn, PlayerEntity playerIn)
    {
        if (canPlaceEntity(worldIn, posIn))
        {
            BlockPos underFoot = blockUnderFoot(playerIn);
            /* Standing on Fluids or Air is not allowed */
            if ((worldIn.getBlockState(underFoot).getBlock() instanceof BlockLiquid) || worldIn.isAirBlock(underFoot))
                return false;
            double blockHeight = worldIn.getBlockState(underFoot).getBoundingBox(Miscellus.nonNullInjected(), underFoot).maxY;
            EntitySittableBlock sittableBlock = new EntitySittableBlock(worldIn, underFoot, blockHeight + 6 * 0.0625F, false);
            worldIn.spawnEntity(sittableBlock);
            playerIn.startRiding(sittableBlock, true);
        }
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean sitOnBlock(World worldIn, BlockPos posIn, PlayerEntity playerIn, double yOffset)
    {
        if (canPlaceEntity(worldIn, posIn))
        {
            EntitySittableBlock sittableBlock = new EntitySittableBlock(worldIn, posIn, yOffset, true);
            worldIn.spawnEntity(sittableBlock);
            playerIn.startRiding(sittableBlock, true);
        }
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean sitOnBlock(World worldIn, BlockPos posIn, PlayerEntity playerIn, double xOffset, double yOffset, double zOffset)
    {
        if (canPlaceEntity(worldIn, posIn))
        {
            EntitySittableBlock sittableBlock = new EntitySittableBlock(worldIn, posIn, xOffset, yOffset, zOffset);
            worldIn.spawnEntity(sittableBlock);
            playerIn.startRiding(sittableBlock, true);
        }
        return true;
    }

    public static boolean sitOnBlock(World worldIn, BlockPos posIn, PlayerEntity playerIn, double xOffset, double yOffset, double zOffset, float yaw)
    {
        if (canPlaceEntity(worldIn, posIn))
        {
            EntitySittableBlock sittableBlock = new EntitySittableBlock(worldIn, posIn, xOffset, yOffset, zOffset, yaw);
            worldIn.spawnEntity(sittableBlock);
            playerIn.startRiding(sittableBlock, true);
        }
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean sitOnBlockWithRotationOffset(World worldIn, BlockPos posIn, PlayerEntity playerIn, double yOffset, int metadata, double offset)
    {
        if (canPlaceEntity(worldIn, posIn))
        {
            EntitySittableBlock sittableBlock = new EntitySittableBlock(worldIn, posIn, yOffset, metadata, offset);
            worldIn.spawnEntity(sittableBlock);
            playerIn.startRiding(sittableBlock);
        }
        return true;
    }

    private static boolean canPlaceEntity(World par1World, BlockPos posIn)
    {
        int x = posIn.getX();
        int y = posIn.getY();
        int z = posIn.getZ();
        List<EntitySittableBlock> listEMB = par1World.getEntitiesWithinAABB(EntitySittableBlock.class, new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).grow(1D, 1D, 1D));
        for (EntitySittableBlock mount : listEMB)
        {
            if (mount.getMountedPosition().equals(posIn))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isPlayerSitting(World worldIn, PlayerEntity playerIn, BlockPos posIn)
    {
        int x = posIn.getX();
        int y = posIn.getY();
        int z = posIn.getZ();  
        List<EntitySittableBlock> listEMB = worldIn.getEntitiesWithinAABB(EntitySittableBlock.class, new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).grow(1.0D, 1.0D, 1.0D));
        if (listEMB.isEmpty())
            return false;
        
        for (EntitySittableBlock mount : listEMB)
        {
           if (mount.getMountedPosition().equals(posIn))
                return mount.isPassenger(playerIn);
        }
        return false;
    }
    
    public static boolean isSomeoneSitting(World world, BlockPos pos)
    {
        return isSomeoneSitting(world, pos.getX(), pos.getY(), pos.getZ());
    }
    
    private static boolean isSomeoneSitting(World world, double x, double y, double z)
    {
        List<EntitySittableBlock> listEMB = world.getEntitiesWithinAABB(EntitySittableBlock.class, new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).grow(0.5D, 0.5D, 0.5D));
        return !listEMB.isEmpty();
    }

    /**
     * @param playerIn the player of interest
     * @return true if playerIn is riding a placed instrument
     */
    public static boolean isRiding(@Nullable PlayerEntity playerIn)
    {
        return (playerIn !=null) && !playerIn.isDead && (playerIn.getRidingEntity() instanceof EntitySittableBlock);
    }
    
    public static BlockPos getRiddenBlock(PlayerEntity playerIn)
    {
        return isRiding(playerIn) ? ((EntitySittableBlock)playerIn.getRidingEntity()).getBlockPos() : BlockPos.ORIGIN;
    }
}

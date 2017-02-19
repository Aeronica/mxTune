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
 */
package net.aeronica.mods.mxtune.util;

import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.blocks.TileInstrument;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.inventory.IMusic;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class SheetMusicUtil
{
    private static SheetMusicUtil INSTANCE = new SheetMusicUtil();
    
    public static SheetMusicUtil getInstance()
    {
       return INSTANCE; 
    }
    
    public static String getMusicTitle(ItemStack stackIn)
    {
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackIn);
        if (sheetMusic != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                return sheetMusic.getDisplayName();
            }
        }
        return new String();
    }

    public static ItemStack getSheetMusic(BlockPos pos, EntityPlayer playerIn, boolean isPlaced)
    {
        if (isPlaced)
        {
            if (playerIn.getEntityWorld().getBlockState(pos).getBlock() instanceof IPlacedInstrument)
            {
                Block placedInst = (Block) playerIn.getEntityWorld().getBlockState(pos).getBlock();
                TileInstrument te = ((IPlacedInstrument) placedInst).getTE(playerIn.getEntityWorld(), pos);
                if(te.getInventory().getStackInSlot(0) != null)
                    return te.getInventory().getStackInSlot(0).copy();
            }
        } else
        {
            ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(playerIn.getHeldItemMainhand());
            if (sheetMusic != null && sheetMusic.hasDisplayName() && (sheetMusic.getItem() instanceof IMusic) && sheetMusic.getTagCompound().hasKey("MusicBook", Constants.NBT.TAG_COMPOUND))
            {
                return sheetMusic;
            }
        }
        return null;
    }
    
    public static ItemStack getSheetMusic(ItemStack stackIn)
    {
        if (stackIn == null) return null;
        
        if (stackIn.hasTagCompound() && stackIn.getItem() instanceof IInstrument)
        {
            NBTTagList items = stackIn.getTagCompound().getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
            if (items.tagCount() == 1)
            {
                NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(0);
                ItemStack sheetMusicOld = ItemStack.loadItemStackFromNBT(item);
                NBTTagCompound contents = (NBTTagCompound) sheetMusicOld.getTagCompound().getTag("MusicBook");
                if (contents != null)
                {
                    return sheetMusicOld;
                }
            }
        }
        return null;
    }
}

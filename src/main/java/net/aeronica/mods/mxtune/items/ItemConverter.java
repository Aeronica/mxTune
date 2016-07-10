/**
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.items;

import java.util.List;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.init.StartupItems;
import net.aeronica.mods.mxtune.inventory.InventoryInstrument;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;

public class ItemConverter extends ItemBase
{

    public ItemConverter(String itemName)
    {
        super(itemName);
        this.setMaxStackSize(1);
        this.setCreativeTab(MXTuneMain.TAB_MUSIC);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        boolean invChanged = false;
        int countUpdated = 0;
        if (!worldIn.isRemote)
        {
            /** Convert old sheet music to new sheet music */
            ItemStack stack;
            int size = playerIn.inventory.getSizeInventory();
            ModLogger.debug("inventorySlots.size: " + size);
            for (int index = 0; index < size; index++)
            {
                stack = playerIn.inventory.getStackInSlot(index);
                if (stack != null)
                {
                    ItemStack sheetMusicStack = this.sheetMusicConversion(stack);
                    ModLogger.debug("inventorySlot index: " + index + ", stack: " + stack);
                    if (sheetMusicStack != null)
                    {                      
                        //playerIn.inventory.decrStackSize(index, playerIn.inventory.getInventoryStackLimit());
                        playerIn.inventory.decrStackSize(index, playerIn.inventory.getInventoryStackLimit());
                        playerIn.inventory.setInventorySlotContents(index, sheetMusicStack);
                        invChanged = true;
                        countUpdated++;
                    }
                }
            }
            if (invChanged)
            {
                playerIn.inventoryContainer.detectAndSendChanges();
                ModLogger.logInfo("ItemConverter#onItemRightClick: " + countUpdated + " Sheet Music Item(s) Updated");
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }

    public ItemStack sheetMusicConversion(ItemStack musicPaper)
    {
        if (musicPaper != null && (musicPaper.getItem() instanceof ItemMusicPaper) && musicPaper.hasDisplayName() && musicPaper.hasTagCompound()) {
            NBTTagCompound contentsOld = musicPaper.getTagCompound();
            if (contentsOld.hasKey("MusicBook"))
            {
                NBTTagCompound musicBookOld = contentsOld.getCompoundTag("MusicBook");
                
                ModLogger.logInfo("sheetMusicConversion: " + musicPaper.getDisplayName());
                ItemStack sheetMusic = new ItemStack(StartupItems.item_sheetmusic);
                sheetMusic.setStackDisplayName(musicPaper.getDisplayName());
                
                NBTTagCompound compoundNew = sheetMusic.getTagCompound();
                if (compoundNew != null)
                {
                    NBTTagCompound musicBookNew = new NBTTagCompound();
                    musicBookNew.setString("MML", musicBookOld.getString("MML"));
                    compoundNew.setTag("MusicBook", musicBookNew);
                    return sheetMusic;
                }
            }
        }
        return null;
    }
    
    /**
     * copy out any item from IInventory and if possible convert
     * it to the new format, then put it in CapItemInv and delete
     * ItemInventory.
     * 
     * NOTE: Assumes a ONE slot inventory
     * 
     */
    public void destroyIInventory(IItemHandler itemHandlerIn, ItemStack stackIn)
    {
        ModLogger.logInfo("itemHandlerIn: " +  itemHandlerIn.getStackInSlot(0));
        ModLogger.logInfo("stackIn:       " +  stackIn);
        
        if (stackIn.hasTagCompound())
        {
            NBTTagList items = stackIn.getTagCompound().getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
            if (items.tagCount() == 1)
            {
                ModLogger.logInfo("stackIn Has ItemInventory: true");
                NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(0);
                ItemStack sheetMusicOld = ItemStack.loadItemStackFromNBT(item);
                NBTTagCompound contents = (NBTTagCompound) sheetMusicOld.getTagCompound().getTag("MusicBook");
                if (contents != null)
                {
                    /** This ItemStack has Music */
                    ModLogger.logInfo("stackIn Has MusicBook/MML: " + contents.getTag("MML"));
                    /** Convert to new format and place it in CapItemInv slot */
                    ItemStack sheetMusicNew = this.sheetMusicConversion(sheetMusicOld);
                    itemHandlerIn.insertItem(0, sheetMusicNew, false);
                }
            }
            /** Blow Away the old ItemInventory and tag */
            ModLogger.logInfo("stackIn Delete OLD ItemInventory");
            stackIn.getTagCompound().removeTag("ItemInventory");
            if (stackIn.getTagCompound().hasNoTags())
                stackIn.setTagCompound(null);
        }
    }

    public void convertIInventory(InventoryInstrument invInst, ItemStack stackIn)
    {
        ModLogger.logInfo("InventoryInstrument: " +  invInst.getStackInSlot(0));
        ModLogger.logInfo("stackIn:             " +  stackIn);
        
        if (stackIn.hasTagCompound())
        {
            NBTTagList items = stackIn.getTagCompound().getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
            if (items.tagCount() == 1)
            {
                ModLogger.logInfo("stackIn Has ItemInventory: true");
                NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(0);
                ItemStack sheetMusicOld = ItemStack.loadItemStackFromNBT(item);
                NBTTagCompound contents = (NBTTagCompound) sheetMusicOld.getTagCompound().getTag("MusicBook");
                if (contents != null  && (sheetMusicOld.getItem() instanceof ItemMusicPaper))
                {
                    /** This ItemStack has Music */
                    ModLogger.logInfo("stackIn Has MusicBook/MML: " + contents.getTag("MML"));
                    /** Convert to new format and place it in CapItemInv slot */
                    ItemStack sheetMusicNew = this.sheetMusicConversion(sheetMusicOld);
                    invInst.setInventorySlotContents(0, sheetMusicNew);
                    invInst.markDirty();
                }
            }
        }
    }

    public void convertIItemStackHander(IItemHandler itemHandlerIn)
    {
        ModLogger.logInfo("itemHandlerIn: " + itemHandlerIn);
        ModLogger.logInfo("stackIn:       " + itemHandlerIn.getStackInSlot(0));

        if (MXTuneMain.proxy.getEffectiveSide() == Side.CLIENT) return;
                
        ItemStack sheetMusicOld = itemHandlerIn.getStackInSlot(0);
        if (sheetMusicOld != null && sheetMusicOld.hasTagCompound() && (sheetMusicOld.getItem() instanceof ItemMusicPaper))
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusicOld.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                /** This ItemStack has Music */
                ModLogger.logInfo("stackIn Has MusicBook/MML: " + contents.getTag("MML"));
                /** Convert to new format and place it in CapItemInv slot */
                ItemStack sheetMusicNew = this.sheetMusicConversion(sheetMusicOld);
                itemHandlerIn.insertItem(0, sheetMusicNew, false);
            }
        }
    }
    
    /** Activate this item unconditionally */
    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        return EnumActionResult.FAIL;
    }

    /** In order for clicking to work this needs to be at least 1. */
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 1;
    }

    @SuppressWarnings(
    {
            "rawtypes", "unchecked"
    })
    public void addInformation(ItemStack stackIn, EntityPlayer playerIn, List tooltip, boolean advanced)
    {
        if (stackIn == null) return;
        /** Display the contents of the sheet music. */
        tooltip.add(TextFormatting.YELLOW + "The Old Sheet Music Format has been Depricated!");
        tooltip.add(TextFormatting.YELLOW + "Place the old Red Bordered Sheet Music into your player inventory, then hold this item");
        tooltip.add(TextFormatting.YELLOW + "in your main hand and right click to activate.");
    }
}

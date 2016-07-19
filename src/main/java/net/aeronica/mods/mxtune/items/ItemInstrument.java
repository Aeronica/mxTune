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
package net.aeronica.mods.mxtune.items;

import java.util.List;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.gui.GuiInstrumentInventory;
import net.aeronica.mods.mxtune.handler.IKeyListener;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemInstrument extends ItemBase implements IInstrument, IKeyListener
{
    public ItemInstrument(String itemName)
    {
        super(itemName);
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setCreativeTab(MXTuneMain.TAB_MUSIC);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int metadata = stack.getMetadata();
        EnumInstruments inst = EnumInstruments.byMetadata(metadata);
        return super.getUnlocalizedName() + "_" + inst.getName();
    }

    @Override
    public boolean getShareTag() {return true;}

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (EnumInstruments inst : EnumInstruments.values())
        {
            ItemStack subItemStack = new ItemStack(itemIn, 1, inst.getMetadata());
            subItems.add(subItemStack);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        BlockPos pos = new BlockPos((int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        if (!worldIn.isRemote)
        {
            /** Server Side - Open the instrument inventory GuiInstInvAdjustRotations */
            if (playerIn.isSneaking() && hand.equals(EnumHand.MAIN_HAND))
            {
                playerIn.openGui(MXTuneMain.instance, GuiInstrumentInventory.GUI_ID, worldIn, 0,0,0);
            }
            if (!playerIn.isSneaking() && itemStackIn.hasTagCompound() && hand.equals(EnumHand.MAIN_HAND))
            {
                PlayManager.getInstance().playMusic(playerIn, itemStackIn, pos, false);
            }
        } else
        {
            // Client Side - play the instrument
            // if (!playerIn.isSneaking() && itemStackIn.hasTagCompound() && hand.equals(EnumHand.MAIN_HAND)) playMusic(playerIn, itemStackIn);
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }

    /** Activate the instrument unconditionally */
    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        // ModLogger.logInfo("Inst#onItemUseFirst hand: " + hand + ", side: " +
        // side + ", pos: " + pos);
        // return EnumActionResult.SUCCESS to activate on AIR only
        // return EnumActionResult.FAIL to activate unconditionally and skip
        // vanilla processing
        // return EnumActionResult.PASS to activate on AIR, or let Vanilla
        // process
        return EnumActionResult.FAIL;
    }

    @Override
    public void onKeyPressed(String key, EntityPlayer playerIn, ItemStack stackIn)
    {
        /** Client Side - play the instrument */
//        if (key.equalsIgnoreCase("key.playInstrument"))
//            playMusic(playerIn, stackIn, new BlockPos((int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ));
    }

    /**
     * This is where we decide how our item interacts with other entities
     */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
    {
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    /**
     * NOTE: If you want to open your GUI on right click and your ItemStore, you
     * MUST override getMaxItemUseDuration to return a value of at least 1,
     * otherwise you won't be able to open the GUI. That's just how it works.
     */
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 1;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addInformation(ItemStack stackIn, EntityPlayer playerIn, List tooltip, boolean advanced)
    {
        String musicTitle = SheetMusicUtil.getMusicTitle(stackIn);
        if (!musicTitle.isEmpty())
        {
            tooltip.add(TextFormatting.GREEN + "Title: " + musicTitle);
        }
    }

    public int getPatch(ItemStack stackIn)
    {
        int patch = (stackIn != null) ? stackIn.getMetadata() : 0;
        EnumInstruments inst = EnumInstruments.byMetadata(patch);
        return inst.getPatch();
    }

    public static enum EnumInstruments implements IStringSerializable
    {
        TUBA(0, "tuba", 59),
        MANDO(1, "mando", 25),
        FLUTE(2, "flute", 74),
        BONGO(3, "bongo", 117),
        BALAL(4, "balalaika", 28),
        CLARI(5, "clarinet", 72);

        public int getMetadata() {return this.meta;}

        @Override
        public String toString() {return this.name;}

        public static EnumInstruments byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length) {meta = 0;}
            return META_LOOKUP[meta];
        }

        public String getName() {return this.name;}

        public int getPatch() {return this.patch;}

        private final int meta;
        private final String name;
        private final int patch;
        private static final EnumInstruments[] META_LOOKUP = new EnumInstruments[values().length];

        private EnumInstruments(int i_meta, String i_name, int i_patch)
        {
            this.meta = i_meta;
            this.name = i_name;
            this.patch = i_patch;
        }

        static
        {
            for (EnumInstruments value : values())
            {
                META_LOOKUP[value.getMetadata()] = value;
            }
        }
    }
}

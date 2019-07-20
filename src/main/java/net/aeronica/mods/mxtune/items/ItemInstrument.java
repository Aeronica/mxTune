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
package net.aeronica.mods.mxtune.items;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.advancements.ModCriteriaTriggers;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.gui.GuiGuid;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.managers.PlayManager;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.IVariant;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.libs.mml.core.MMLUtil.preset2PackedPreset;
import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType;

/**
 * @author Paul Boese a.k.a Aeronica
 *
 */
public class ItemInstrument extends Item implements IInstrument
{
    public ItemInstrument()
    {
        setHasSubtypes(true);
        setMaxStackSize(1);
        setMaxDamage(0);
        setCreativeTab(MXTune.TAB_MUSIC);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + EnumType.byMetadata(stack.getMetadata()).getName();
    }

    @Override
    public boolean getShareTag() {return true;}

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(ItemGroup tab, NonNullList<ItemStack> subItems)
    {
        if (isInCreativeTab(tab)) {
            final List<ItemStack> items = Stream.of(EnumType.values())
                    .map(enumType -> new ItemStack(this, 1, enumType.getMeta()))
                    .collect(Collectors.toList());

            subItems.addAll(items);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote)
        {
            // Server Side - Open the instrument inventory GuiInstInvAdjustRotations
            if (playerIn.isSneaking() && handIn.equals(Hand.MAIN_HAND))
            {
                playerIn.openGui(MXTune.instance, GuiGuid.GUI_INSTRUMENT_INVENTORY, worldIn, 0, 0, 0);
            }
            if (!playerIn.isSneaking() && itemStackIn.hasTagCompound() && handIn.equals(Hand.MAIN_HAND))
            {
                if (ServerCSDManager.canMXTunesPlay(playerIn))
                {
                    if (!PlayManager.isPlayerPlaying(playerIn))
                    {
                        int playID = PlayManager.playMusic(playerIn);
                        itemStackIn.setRepairCost(playID);
                        if (playID != PlayType.INVALID)
                            ModCriteriaTriggers.PLAY_INSTRUMENT.trigger((ServerPlayerEntity) playerIn, EnumType.byMetadata(itemStackIn.getMetadata()).getName());
                    }
                } 
                else
                {
                    ServerCSDManager.sendErrorViaChat(playerIn);
                }
            }
        }
        // return EnumActionResult.SUCCESS to activate on AIR only
        // return EnumActionResult.FAIL to activate unconditionally and skip vanilla processing
        // return EnumActionResult.PASS to activate on AIR, or let Vanilla process
        return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
    }

    /**
     * Off-hand (shield-slot) instrument will allow sneak-right click to remove music from a placed instrument.
     */
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, PlayerEntity player)
    {   
        return world.getBlockState(pos).getBlock() instanceof IPlacedInstrument;
    }
    
    /* 
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    @Override
    public void onUpdate(ItemStack stackIn, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isRemote)
        {         
            Integer playID = stackIn.getRepairCost();
            if (!isSelected && (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID)))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(PlayType.INVALID);
            }
        }
    }
    
    /*
     * Called if moved from inventory into the world.
     * This is distinct from onDroppedByPlayer method
     * 
     */
    @Override
    public int getEntityLifespan(ItemStack stackIn, World worldIn)
    {
        if (!worldIn.isRemote)
        {
            Integer playID = stackIn.getRepairCost();
            if (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(PlayType.INVALID);
            }
        }
        return super.getEntityLifespan(stackIn, worldIn);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stackIn, PlayerEntity playerIn)
    {
        if (!playerIn.getEntityWorld().isRemote)
        {
            Integer playID = stackIn.getRepairCost();
            if (PlayManager.hasPlayID(playID)||PlayManager.isActivePlayID(playID))
            {
                PlayManager.stopPlayID(playID);
                stackIn.setRepairCost(PlayType.INVALID);
            }
        }
        return true;
    }

    /**
     * This is where we decide how our item interacts with other entities
     */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand)
    {
        return true;
    }

    /**
     * NOTE: If you want to open your GUI on right click and your ItemStore, you
     * MUST override getMaxItemUseDuration to return a value of at least 1,
     * otherwise you won't be able to open the GUI. That's just how it works.
     */
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 72000;
    }

    @Override
    public void addInformation(ItemStack stackIn, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        String musicTitle = SheetMusicUtil.getMusicTitle(stackIn);
        if (!musicTitle.isEmpty())
            tooltip.add(TextFormatting.GREEN + I18n.format("item.mxtune:instrument.title") + ": " + musicTitle);
        
        tooltip.add(TextFormatting.RESET + I18n.format("item.mxtune:instrument.help"));
    }

    @Override
    public int getPatch(ItemStack itemStack)
    {
        return EnumType.byMetadata(this.getMetadata(itemStack)).getPatch();
    }

    public enum EnumType implements IVariant
    {
        LUTE(0, "lute", preset2PackedPreset(12, 0)),
        UKULELE(1, "ukulele", preset2PackedPreset(12, 1)),
        MANDOLIN(2, "mandolin", preset2PackedPreset(12, 2)),
        WHISTLE(3, "whistle", preset2PackedPreset(12, 3)),
        RONCADORA(4, "roncadora", preset2PackedPreset(12, 4)),
        FLUTE(5, "flute", preset2PackedPreset(12, 5)),
        CHALUMEAU(6, "chalumeau", preset2PackedPreset(12, 6)),
        TUBA(7, "tuba", preset2PackedPreset(12, 18)),
        LYRE(8, "lyre", preset2PackedPreset(12, 19)),
        ELECTRIC_GUITAR(9, "electric_guitar", preset2PackedPreset(12, 20)),
        VIOLIN(10, "violin", preset2PackedPreset(12, 22)),
        CELLO(11, "cello", preset2PackedPreset(12, 23)),
        HARP(12, "harp", preset2PackedPreset(12, 24)),
        TUNED_FLUTE(13, "tuned_flute", preset2PackedPreset(12, 55)),
        TUNED_WHISTLE(14, "tuned_whistle", preset2PackedPreset(12, 56)),
        BASS_DRUM(15, "bass_drum", preset2PackedPreset(12, 66)),
        SNARE_DRUM(16, "snare_drum", preset2PackedPreset(12, 67)),
        CYMBELS(17, "cymbels", preset2PackedPreset(12, 68)),
        HAND_CHIMES(18, "hand_chimes", preset2PackedPreset(12, 77)),
        RECORDER(19, "recorder", 74),
        TRUMPET(20, "trumpet", 56),
        HARPSICHORD(21, "harpsichord", 6),
        HARPSICHORD_COUPLED(22, "harpsichord_coupled", preset2PackedPreset(1, 6)),
        STANDARD_SET(23, "standard_set", preset2PackedPreset(128, 0)),
        ORCHESTRA_SET(24, "orchestra_set", preset2PackedPreset(128, 48)),
        PIANO(25, "piano", preset2PackedPreset(12, 21)),
        ;

        @Override
        public String toString() {return this.name;}

        public static EnumType byMetadata(int metaIn)
        {
            int metaLocal = metaIn;
            if (metaLocal < 0 || metaLocal >= META_LOOKUP.length) {metaLocal = 0;}
            return META_LOOKUP[metaLocal];
        }

        public String getName() {return this.name;}

        public int getPatch() {return this.patch;}

        private final int meta;
        private final String name;
        private final int patch;
        private static final EnumType[] META_LOOKUP = new EnumType[values().length];

        EnumType(int metaIn, String nameIn, int patchIn)
        {
            this.meta = metaIn;
            this.name = nameIn;
            this.patch = patchIn;
        }

        static
        {
            for (EnumType value : values())
            {
                META_LOOKUP[value.getMeta()] = value;
            }
        }

        @Override
        public int getMeta()
        {
            return meta;
        }
    }
}

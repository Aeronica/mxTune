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
package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotBandAmp extends SlotItemHandler
{
    private TileBandAmp tileBandAmp;

    public SlotBandAmp(IItemHandler itemHandler, TileBandAmp tileBandAmp, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.tileBandAmp = tileBandAmp;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack)
    {
        return super.isItemValid(stack) && stack.getItem() instanceof ItemInstrument;
    }

    @Override
    public void onSlotChanged() {
        tileBandAmp.markDirty();
    }
}

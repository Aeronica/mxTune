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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MusicTab extends CreativeTabs
{
    public MusicTab(int id, String name) {super(id, name);}

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslationKey()
    {
        return Reference.MOD_NAME;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getIcon() {return new ItemStack(ModItems.ITEM_MULTI_INST, 1, 28);}

    @Override
    public ItemStack createIcon() {return ItemStack.EMPTY;}
}

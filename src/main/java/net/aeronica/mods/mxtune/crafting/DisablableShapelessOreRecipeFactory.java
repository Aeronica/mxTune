/**
 * Copyright {2017} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.crafting;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Disableable Shapeless Ore Recipe
 * @author Aeronica
 *
 */
public class DisablableShapelessOreRecipeFactory implements IRecipeFactory
{

    @Override
    public IRecipe parse(JsonContext context, JsonObject json)
    {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        
        ItemStack result = recipe.getRecipeOutput();
        NonNullList<Ingredient> input = recipe.getIngredients();
        return new DisablableRecipe(new ResourceLocation(MXTuneMain.MODID, "disableable_shapeless_ore_crafting"), input, result);
    }

    public static class DisablableRecipe extends ShapelessOreRecipe {

        public DisablableRecipe(ResourceLocation group, NonNullList<Ingredient> input, ItemStack result)
        {
            super(group, input, result);
        }

        @Override
        @Nonnull
        public ItemStack getCraftingResult(InventoryCrafting var1)
        {
            ItemStack newOutput = this.output.copy();
            ItemStack itemstack = ItemStack.EMPTY;
            ModLogger.info("*** unlocalized name: %s", newOutput.getUnlocalizedName());
            return newOutput;
        }

        @Override
        public boolean isHidden()
        {
            return false;
        }
    }
    
}

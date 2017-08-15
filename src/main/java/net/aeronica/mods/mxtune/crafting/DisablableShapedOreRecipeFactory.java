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
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.items.ItemInstrument.EnumType;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Disableable Shaped Ore Recipe
 * @author Aeronica
 *
 */
public class DisablableShapedOreRecipeFactory implements IRecipeFactory
{

    @Override
    public IRecipe parse(JsonContext context, JsonObject json)
    {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getWidth();
        primer.height = recipe.getHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();
        
        return new DisablableRecipe(new ResourceLocation(MXTuneMain.MODID, "disableable_shaped_ore_crafting"), recipe.getRecipeOutput(), primer); 
    }

    public static class DisablableRecipe extends ShapedOreRecipe {
        public DisablableRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
            super(group, result, primer);
        }

        @Override
        @Nonnull
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
            ItemStack newOutput = this.output.copy();

            ItemStack itemstack = ItemStack.EMPTY;
            ModLogger.info("*** unlocalized name: %s", newOutput.getUnlocalizedName().replaceFirst("item\\.", "")); 
            IForgeRegistry<Item> items = GameRegistry.findRegistry(Item.class);
            for(ResourceLocation key: items.getKeys())
            {
                if (key.getResourceDomain().equals(MXTuneMain.MODID))
                {
                    Item item = Item.REGISTRY.getObject(key);
                    if (item.getHasSubtypes())
                    {
                        for (EnumType e: ItemInstrument.EnumType.values())
                        {
                            ModLogger.info("*** %s.%s", key, e.getName());
                        }
                    } 
                    else
                        ModLogger.info("*** %s", key);
                }
            }
            return itemstack;
        }
        
        @Override
        public boolean isHidden()
        {
            return true;
        }                
    }

}

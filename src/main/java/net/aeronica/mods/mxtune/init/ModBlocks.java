/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.blocks.BlockBandAmp;
import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.blocks.TilePiano;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ModBlocks
{
    public static final BlockPiano SPINET_PIANO = RegistrationHandler.registerBlock(new BlockPiano(), "spinet_piano");
    static final BlockBandAmp BAND_AMP = RegistrationHandler.registerBlock(new BlockBandAmp(), "band_amp");

    private ModBlocks() {}
    
    @Mod.EventBusSubscriber
    public static class RegistrationHandler {
        private static final Set<Item> ITEM_BLOCKS = new HashSet<>();
        private RegistrationHandler() { /* NOP */ }
        
        /**
         * Register this mod's {@link Block}s.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            final IForgeRegistry<Block> registry = event.getRegistry();

            final Block[] blocks = {
                    SPINET_PIANO,
                    BAND_AMP,
            };

            registry.registerAll(blocks);
        }

        /**
         * Register this mod's {@link ItemBlock}s.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
            final ItemBlock[] items = {
            };

            final IForgeRegistry<Item> registry = event.getRegistry();

            for (final ItemBlock item : items) {
                registry.register(item.setRegistryName(item.getBlock().getRegistryName()));
                ITEM_BLOCKS.add(item);
            }
            
            registerTileEntities();
        }

        private static void registerTileEntities() {
            registerTileEntity(TilePiano.class, "tile_piano");
            registerTileEntity(TileBandAmp.class, "tile_band_amp");
        }

        private static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String name) {
            GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(Reference.MOD_ID, name));
        }

        private static <T extends Block> T registerBlock(T block, String name) {
            block.setRegistryName(name.toLowerCase(Locale.US));
            block.setTranslationKey(Objects.requireNonNull(block.getRegistryName()).toString());
            return block;
        }

        @SuppressWarnings("unused")
        private static <T extends Block> T registerBlock(T block) {
            return registerBlock(block, block.getClass().getSimpleName());
        }
    }
}

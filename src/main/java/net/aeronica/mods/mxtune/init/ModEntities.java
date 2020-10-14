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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.entity.EntitySittableBlock;
import net.aeronica.mods.mxtune.entity.living.EntityGoldenSkeleton;
import net.aeronica.mods.mxtune.entity.living.EntityTimpani;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public enum ModEntities
{
    ;
    protected static int entityID = 0;
    
    private static int getEntityID() {return entityID++;}

    public static void init()
    {
        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "mountableblock"), EntitySittableBlock.class, "mountableblock", getEntityID(), MXTune.instance,80, 1, false);
        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "mob_golden_skeleton"), EntityGoldenSkeleton.class, "mxtune:mob_golden_skeleton", getEntityID(), MXTune.instance, 64, 1, true, 0x000000, 0xE6BA50);
        EntityRegistry.addSpawn(EntityGoldenSkeleton.class, 100, 1, 2, EnumCreatureType.MONSTER, Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.FOREST_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.DESERT);
        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "mob_timpani"), EntityTimpani.class, "mxtune:mob_timpani", getEntityID(), MXTune.instance, 64, 1, true, 0x000000, 0xFF5121);
        EntityRegistry.addSpawn(EntityTimpani.class, 20, 1, 3, EnumCreatureType.MONSTER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.DESERT);
    }
}

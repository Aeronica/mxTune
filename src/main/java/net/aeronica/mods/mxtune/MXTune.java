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
package net.aeronica.mods.mxtune;

import net.aeronica.mods.mxtune.advancements.ModCriteriaTriggers;
import net.aeronica.mods.mxtune.blocks.TilePiano;
import net.aeronica.mods.mxtune.datafixers.ItemInventoryWalker;
import net.aeronica.mods.mxtune.datafixers.SheetMusicFixer;
import net.aeronica.mods.mxtune.datafixers.TileEntityInventoryWalker;
import net.aeronica.mods.mxtune.datafixers.TileIdFixer;
import net.aeronica.mods.mxtune.handler.GUIHandler;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.options.PlayerMusicOptionsCapability;
import net.aeronica.mods.mxtune.proxy.ServerProxy;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.MusicTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
     acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS,
     dependencies = Reference.DEPENDENTS, updateJSON = Reference.UPDATE,
     certificateFingerprint = Reference.CERTIFICATE_FINGERPRINT)

@SuppressWarnings("deprecation")
public class MXTune
{
    @Mod.Instance(Reference.MOD_ID)
    public static MXTune instance;

    @SidedProxy(clientSide = "net.aeronica.mods.mxtune.proxy.ClientProxy", serverSide = "net.aeronica.mods.mxtune.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final CreativeTabs TAB_MUSIC = new MusicTab(CreativeTabs.getNextID(), Reference.MOD_NAME);
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ModLogger.setLogger(event.getModLog());
        ModCriteriaTriggers.init();
        PlayerMusicOptionsCapability.register();
        PacketDispatcher.registerPackets();
        proxy.preInit();
        proxy.registerEventHandlers();
        proxy.initEntities();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
        proxy.registerKeyBindings();
        proxy.initMML();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GUIHandler.getInstance());

        CompoundDataFixer fixer = FMLCommonHandler.instance().getDataFixer();
        ModFixs modFixer = fixer.init(Reference.MOD_ID, Reference.MXTUNE_DATA_FIXER_VERSION);
        modFixer.registerFix(FixTypes.BLOCK_ENTITY, new TileIdFixer());
        modFixer.registerFix(FixTypes.ITEM_INSTANCE, new SheetMusicFixer());

        // Pedestal type capability te
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TilePiano.class, "Items"));
        // Fix SheetMusic in ItemInstrument ItemInventory slot
        fixer.registerWalker(FixTypes.ITEM_INSTANCE, new ItemInventoryWalker());
        // Fix SheetMusItemStackHandler, ItemInstrument, ItemSheetMusic
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, new TileEntityInventoryWalker());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
        proxy.registerHUD();
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        FMLLog.warning("*** [mxTune] Invalid fingerprint detected! ***");
    }
}

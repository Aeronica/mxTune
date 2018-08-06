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
package net.aeronica.mods.mxtune;

import net.aeronica.mods.mxtune.advancements.ModCriteriaTriggers;
import net.aeronica.mods.mxtune.handler.GUIHandler;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.options.PlayerMusicOptionsCapability;
import net.aeronica.mods.mxtune.proxy.IProxy;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.MusicTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = MXTuneMain.MODID, name = MXTuneMain.MODNAME, version = MXTuneMain.VERSION,
     acceptedMinecraftVersions = "[1.12,1.13)",
     dependencies = MXTuneMain.DEPS, updateJSON = MXTuneMain.UPDATE,
     certificateFingerprint = "999640c365a8443393a1a21df2c0ede9488400e9")

public class MXTuneMain
{
    public static final String MODID = "mxtune";
    public static final String MODNAME = "mxTune";
    public static final String VERSION = "{@VERSION}";
    public static final String DEPS = "required-after:forge@[1.12.2-14.23.4.2705,)";
    public static final String UPDATE = "https://gist.githubusercontent.com/Aeronica/dbc2619e0011d5bdbe7a162d0c6aa82b/raw/update.json";
    
    @Mod.Instance(MODID)
    public static MXTuneMain instance;

    @SidedProxy(clientSide = "net.aeronica.mods.mxtune.proxy.ClientProxy", serverSide = "net.aeronica.mods.mxtune.proxy.ServerProxy")
    public static IProxy proxy;

    public static final CreativeTabs TAB_MUSIC = new MusicTab(CreativeTabs.getNextID(), MODNAME);
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ModLogger.setLogger(event.getModLog());
        ModCriteriaTriggers.init();
        PlayerMusicOptionsCapability.register();
        PacketDispatcher.registerPackets();
        proxy.preInit(event);
        proxy.registerEventHandlers();
        proxy.initEntities();
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        proxy.registerKeyBindings();
        proxy.initMML();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GUIHandler.getInstance());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
        proxy.replacePlayerModel();
        proxy.registerHUD();
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        System.out.printf("*** [mxTune] Invalid fingerprint detected! ***\n");
    }
    
    /**
     * Prepend the name with the mod ID, suitable for ResourceLocations such as
     * textures.
     * 
     * @param name
     * @return eg "xyzmodid:xyzblockname"
     */
    public static String prependModID(String name)
    {
        return MODID + ":" + name;
    }
}

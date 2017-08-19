/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.config;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ModConfig
{
    
    private ModConfig() { /* NOP */ }

    @Config(modid=MXTuneMain.MODID, name="mxtune/mxtune", type=Config.Type.INSTANCE, category="server")
    @Config.LangKey("config.mxtune:ctgy.general")
    public static class Server
    {
        /** General Configuration Settings */
        @Config.Name("Listener Range")
        @Config.LangKey("config.mxtune:listenerRange")
        @Config.RangeDouble(min=10.0D, max=64.0D)
        public static float listenerRange = 24.0F;

        @Config.Name("Group Play Abort Distance")
        @Config.LangKey("config.mxtune:groupPlayAbortDistance")
        @Config.RangeDouble(min=10.0D, max=24.0D)    
        public static float groupPlayAbortDistance = 10.0F;

        @Config.Name("Hide Welcome Status Message")
        @Config.LangKey("config.mxtune:hideWelcomeStatusMessage")   
        public static boolean hideWelcomeStatusMessage = false;

        @Config.Name("Enabled Recipes")
        @Config.LangKey("config.mxtune:enabledRecipes")
        @Config.RequiresMcRestart
        public static String[] enabledRecipes = {
                "mxtune:music_paper", "mxtune:spinet_piano",
                "mxtune:instrument.bass_drum", "mxtune:instrument.cello", "mxtune:instrument.chalumeau", "mxtune:instrument.cymbels",
                "mxtune:instrument.electric_guitar", "mxtune:instrument.flute", "mxtune:instrument.hand_chimes","mxtune:instrument.harp",
                "mxtune:instrument.harpsichord", "mxtune:instrument.harpsichord_coupled", "mxtune:instrument.lute", "mxtune:instrument.lyre",
                "mxtune:instrument.mandolin", "mxtune:instrument.orchestra_set", "mxtune:instrument.recorder", "mxtune:instrument.roncadora",
                "mxtune:instrument.snare_drum", "mxtune:instrument.standard_set", "mxtune:instrument.trumpet", "mxtune:instrument.tuba",
                "mxtune:instrument.tuned_flute", "mxtune:instrument.tuned_whistle", "mxtune:instrument.ukulele",
                "mxtune:instrument.violin", "mxtune:instrument.whistle"
        };
    }

    /** Client Configuration Settings */
    @Config(modid = MXTuneMain.MODID, name="mxtune/mxtune_client", category="client")
    @Config.LangKey("config.mxtune:ctgy.client")
    public static class Client
    {   

        @Config.Comment("Sound Channel Configuration")
        @Config.LangKey("config.mxtune:soundChannelConfig")
        public static final Sound sound = new Sound();
        
        @Config.Comment("Internet Resources")
        @Config.LangKey("config.mxtune:internetResouces")
        public static final Links links = new Links();
        
        public static class Sound
        {
            @Config.Name("Automatically configure sound channels")
            @Config.LangKey("config.mxtune:autoConfigureChannels")
            public boolean autoConfigureChannels = true;

            @Config.Name("Number of normal sound channels (manual)")
            @Config.LangKey("config.mxtune:normalSoundChannelCount")
            @Config.RangeInt(min=4, max=60)
            public int normalSoundChannelCount = 24;

            @Config.Name("Number of streaming sound channels (manual)")
            @Config.LangKey("config.mxtune:streamingSoundChannelCount")
            @Config.RangeInt(min=4, max=60)
            public int streamingSoundChannelCount = 8;
        }
        
        public static class Links
        {
            @Config.Name("Site Links")
            @Config.LangKey("config.mxtune:mmlLink")
            public String site = "https://mabibeats.com/";
        }
    }

    /** @return the configFile */
    public static Configuration getConfigFile() {return null;}

    public static float getListenerRange() {return Server.listenerRange;}

    public static float getGroupPlayAbortDistance() {return Server.groupPlayAbortDistance;}

    public static boolean hideWelcomeStatusMessage() {return Server.hideWelcomeStatusMessage;}

    public static boolean getAutoConfigureChannels() {return Client.sound.autoConfigureChannels;}

    public static int getNormalSoundChannelCount() {return Client.sound.normalSoundChannelCount;}

    public static int getStreamingSoundChannelCount() {return Client.sound.streamingSoundChannelCount;}

    public static String getMmlLink() {return Client.links.site;}

    public static String[] getEnabledRecipes() {return Server.enabledRecipes;}
    
    public static String[] getDefaultRecipes()
    {        
        final String[] defaultRecipes = {
                "mxtune:music_paper", "mxtune:spinet_piano",
                "mxtune:instrument.bass_drum", "mxtune:instrument.cello", "mxtune:instrument.chalumeau", "mxtune:instrument.cymbels",
                "mxtune:instrument.electric_guitar", "mxtune:instrument.flute", "mxtune:instrument.hand_chimes","mxtune:instrument.harp",
                "mxtune:instrument.harpsichord", "mxtune:instrument.harpsichord_coupled", "mxtune:instrument.lute", "mxtune:instrument.lyre",
                "mxtune:instrument.mandolin", "mxtune:instrument.orchestra_set", "mxtune:instrument.recorder", "mxtune:instrument.roncadora",
                "mxtune:instrument.snare_drum", "mxtune:instrument.standard_set", "mxtune:instrument.trumpet", "mxtune:instrument.tuba",
                "mxtune:instrument.tuned_flute", "mxtune:instrument.tuned_whistle", "mxtune:instrument.ukulele",
                "mxtune:instrument.violin", "mxtune:instrument.whistle"
        };
        return defaultRecipes;
    }

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            ModLogger.info("On ConfigChanged: %s", event.getModID());
            if(event.getModID().equals(MXTuneMain.MODID))
            {
                ConfigManager.sync(MXTuneMain.MODID, Config.Type.INSTANCE);
            }
        }
    }

}

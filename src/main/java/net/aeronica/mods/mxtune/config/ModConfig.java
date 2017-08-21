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

import java.util.Map;

import com.google.common.collect.Maps;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModConfig
{
    
    private ModConfig() { /* NOP */ }

    @Config(modid=MXTuneMain.MODID, name = MXTuneMain.MODID + "/" +MXTuneMain.MODID, type=Config.Type.INSTANCE, category="general")
    @Config.LangKey("config.mxtune:ctgy.general")
    public static class CFG_GENERAL
    {
        @Config.Comment("Sound Configuration")
        @Config.LangKey("config.mxtune:generalSoundConfig")
        public static final Sound sound = new Sound();
        
        public static class Sound
        {
            /** General Configuration Settings */
            @Config.Name("Listener Range")
            @Config.LangKey("config.mxtune:listenerRange")
            @Config.RangeDouble(min=10.0D, max=64.0D)
            public float listenerRange = 24.0F;

            @Config.Name("Group Play Abort Distance")
            @Config.LangKey("config.mxtune:groupPlayAbortDistance")
            @Config.RangeDouble(min=10.0D, max=24.0D)    
            public float groupPlayAbortDistance = 10.0F;

            @Config.Name("Hide Welcome Status Message")
            @Config.LangKey("config.mxtune:hideWelcomeStatusMessage")   
            public boolean hideWelcomeStatusMessage = false;
        }
    }

    /** Client Configuration Settings */
    @Config(modid = MXTuneMain.MODID, name = MXTuneMain.MODID + "/" +MXTuneMain.MODID, category="client")
    @Config.LangKey("config.mxtune:ctgy.client")
    public static class CFG_CLIENT
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
    
    @Config(modid = MXTuneMain.MODID, name = MXTuneMain.MODID + "/" +MXTuneMain.MODID + "_recipes", type = Config.Type.INSTANCE, category="recipe")
    @Config.LangKey("config.mxtune:ctgy.recipes")
    public static class CONFIG_RECIPE
    { 
        @Config.Name("toggles")
        @Config.Comment({"mxTune Recipes", "Requires a Server Restart if Changed!", "B:<name>=(true|false)"})
        @Config.RequiresMcRestart
        public static Map<String, Boolean> recipeToggles;

        private static final String[] modItemRecipeNames = {
                "music_paper", "spinet_piano",
                "bass_drum", "cello", "chalumeau", "cymbels",
                "electric_guitar", "flute", "hand_chimes","harp",
                "harpsichord", "harpsichord_coupled", "lute", "lyre",
                "mandolin", "orchestra_set", "recorder", "roncadora",
                "snare_drum", "standard_set", "trumpet", "tuba",
                "tuned_flute", "tuned_whistle", "ukulele",
                "violin", "whistle"
        };
        
        static
        {
            recipeToggles = Maps.newHashMap();
            for (int i = 0; i < modItemRecipeNames.length; i++)
            {
                recipeToggles.put(modItemRecipeNames[i], true);
            }
        }
    }
    
    public static float getListenerRange() {return CFG_GENERAL.sound.listenerRange;}

    public static float getGroupPlayAbortDistance() {return CFG_GENERAL.sound.groupPlayAbortDistance;}

    public static boolean hideWelcomeStatusMessage() {return CFG_GENERAL.sound.hideWelcomeStatusMessage;}

    public static boolean getAutoConfigureChannels() {return CFG_CLIENT.sound.autoConfigureChannels;}

    public static int getNormalSoundChannelCount() {return CFG_CLIENT.sound.normalSoundChannelCount;}

    public static int getStreamingSoundChannelCount() {return CFG_CLIENT.sound.streamingSoundChannelCount;}

    public static String getMmlLink() {return CFG_CLIENT.links.site;}

    /**
     * Will only allow this mods recipes to be disabled
     * @param stackIn
     * @return recipe state
     */
    public static boolean isRecipeEnabled(ItemStack stackIn)
    {
        String modName = stackIn.getUnlocalizedName().replaceFirst("item\\." + MXTuneMain.MODID + ":", "");
        modName = modName.replaceFirst("instrument\\.", "");
        boolean enableState = CONFIG_RECIPE.recipeToggles.containsKey(modName) ? CONFIG_RECIPE.recipeToggles.get(modName) && !modName.contains(":"): true;
        ModLogger.debug("Recipe Enabled? %s %s", modName, enableState);
        return enableState;
    }
    
    public static boolean isRecipeHidden(ItemStack stackIn)
    {
        return !isRecipeEnabled(stackIn);
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

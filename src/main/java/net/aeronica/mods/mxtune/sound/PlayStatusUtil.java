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
package net.aeronica.mods.mxtune.sound;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PlayStatusUtil
{
    
    @CapabilityInject(IPlayStatus.class)
    public static final Capability<IPlayStatus> PLAY_STATUS = null;
    
    public static boolean isPlaying(EntityPlayer playerIn)
    {
        if (playerIn.hasCapability(PLAY_STATUS, null))
            return playerIn.getCapability(PLAY_STATUS, null).isPlaying();
        else
            return false;
    }
    
    public static void setPlaying(EntityPlayer playerIn, boolean playing){
        if (playerIn.hasCapability(PLAY_STATUS, null))
            playerIn.getCapability(PLAY_STATUS, null).setPlaying(playerIn, playing);
    }

    public static void stopPlaying(Set<Integer> setEntityIDs)
    {
        for(Integer entityID: setEntityIDs)
        {
            EntityPlayer player = (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(entityID);
            if (player != null)
            {
                setPlaying(player, false);
            }
        }
    }
}

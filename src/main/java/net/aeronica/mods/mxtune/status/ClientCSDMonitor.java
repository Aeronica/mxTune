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
package net.aeronica.mods.mxtune.status;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.ClientStateDataMessage;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsSoundsScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;

/**
 * ClientStateMonitor<p>
 * 
 * Pushes initial state and changes to state to the server. Items tracked are the 
 * availability of the MIDI system, the on/off state of MASTER and MXTUNE volume settings.</p>
 * 
 * @author Paul Boese aka Aeronica
 *
 */

public class ClientCSDMonitor
{
    private static ClientStateData csd = null;
    private static Minecraft mc = Minecraft.getInstance();
    private static GameSettings gameSettings = Minecraft.getInstance().gameSettings;

    ClientCSDMonitor() { /* NOP */ }

    /*
     * Collect initial state just after logging on to a server then post it to the server.
     */
    public static void collectAndSend()
    {
        csd = snapShot();
        sendToServer();
        ModLogger.debug("ClientStateMonitor#initialize: " + csd);
    }
    
    private static ClientStateData snapShot()
    {
        return new ClientStateData(
                !MIDISystemUtil.midiUnavailable(),
                gameSettings.getSoundLevel(SoundCategory.MASTER)>0F,
                gameSettings.getSoundLevel(SoundCategory.RECORDS)>0F);
    }
    
    /*
     * Monitor state changes and post them as they are detected.
     */
    
    private static void sendToServer()
    {
        ClientStateDataMessage message = new ClientStateDataMessage(csd);
        PacketDispatcher.sendToServer(message);
    }
    
    private static boolean inGui = false;
    public static void detectAndSend()
    {

        if (mc.currentScreen instanceof OptionsSoundsScreen && !inGui)
        {
            ModLogger.debug("Opened GuiScreenOptionsSounds");
            inGui=true;
        }
        else if(!(mc.currentScreen instanceof OptionsSoundsScreen) && inGui)
        {
            ModLogger.debug("Closed GuiScreenOptionsSounds");
            inGui=false;
            ClientStateData ss = snapShot();
            if(csd!=null && !csd.isEqual(ss)) 
            {
                csd = ss;
                sendToServer();
                ModLogger.debug("ClientStateData ***Changed*** Sending to server");
            }
        }
    }
    
    public static boolean canMXTunesPlay()
    {
        return (csd != null && csd.isGood());
    }
    
    /**
     * A Client side version to send the current status to the players chat.
     * @param playerIn to whom it concerns
     */
    public static void sendErrorViaChat(PlayerEntity playerIn)
    {
        if (csd == null)
        {
            csd=snapShot();
        }
        new CSDChatStatus(playerIn, csd);   
    }
}

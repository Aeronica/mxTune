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
package net.aeronica.mods.mxtune.options;

import java.util.ArrayList;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayerMusicOptionsMessage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerMusicDefImpl implements IPlayerMusicOptions
{
    /** Music Options*/
    private int muteOption;
    private float midiVolume;
    /** Strings for passing parameters from server to client: for a GUI for example */
    private String sParam1, sParam2, sParam3;
    private ArrayList<PlayerLists> whiteList, blackList;

    public PlayerMusicDefImpl()
    {
        this.midiVolume = 0.70F;
        this.muteOption = 0;
        this.sParam1 = this.sParam2 = this.sParam3 = new String("");
        this.whiteList = new ArrayList<PlayerLists>();
        this.blackList = new ArrayList<PlayerLists>();
    }
    
    public PlayerMusicDefImpl(EntityLivingBase entity)
    {
        this.midiVolume = 0.70F;
        this.muteOption = 0;
        this.sParam1 = this.sParam2 = this.sParam3 = new String("");
        this.whiteList = new ArrayList<PlayerLists>();
        this.blackList = new ArrayList<PlayerLists>();
    }

    @Override
    public void clearAll(EntityPlayer playerIn)
    {
        this.midiVolume = 0.70F;
        this.muteOption = 0;
        this.sParam1 = this.sParam2 = this.sParam3 = new String("");
        this.syncAll(playerIn);
    }

    @Override
    public void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3)
    {
        this.sParam1 = sParam1;
        this.sParam2 = sParam2;
        this.sParam3 = sParam3;
        this.sync(playerIn, SYNC_SPARAMS);
    }

    @Override
    public void setSParams(String sParam1, String sParam2, String sParam3)
    {
        this.sParam1 = sParam1;
        this.sParam2 = sParam2;
        this.sParam3 = sParam3;
    }
    
    @Override
    public String getSParam1() {return sParam1;}

    @Override
    public String getSParam2() {return sParam2;}

    @Override
    public String getSParam3() {return sParam3;}

    @Override
    public void setMidiVolume(EntityPlayer playerIn, float volumeIn) {this.midiVolume = Math.min(1.0F, Math.max(0.0F, volumeIn)); sync(playerIn, SYNC_MIDI_VOLUME);}

    @Override
    public void setMidiVolume(float volumeIn) {this.midiVolume = Math.min(1.0F, Math.max(0.0F, volumeIn));}

    @Override
    public float getMidiVolume() {return this.midiVolume;}

    @Override
    public void setMuteOption(EntityPlayer playerIn, int muteOptionIn) {this.muteOption = muteOptionIn; sync(playerIn, SYNC_MUTE_OPTION);}

    @Override
    public void setMuteOption(int muteOptionIn) {this.muteOption = muteOptionIn;}

    @Override
    public int getMuteOption() {return muteOption;}
    
    @Override
    public void setWhiteList(ArrayList<PlayerLists> list) {this.whiteList = list;}

    @Override
    public void setWhiteList(EntityPlayer playerIn, ArrayList<PlayerLists> list) {this.whiteList = list; sync(playerIn, SYNC_WHITE_LIST);}

    @Override
    public ArrayList<PlayerLists> getWhiteList() {return this.whiteList;}

    @Override
    public void setBlackList(EntityPlayer playerIn, ArrayList<PlayerLists> list) {this.blackList = list; sync(playerIn, SYNC_BLACK_LIST);}

    @Override
    public void setBlackList(ArrayList<PlayerLists> list) {this.blackList = list;}

    @Override
    public ArrayList<PlayerLists> getBlackList() {return this.blackList;}

    public static final byte SYNC_ALL = 0;
    public static final byte SYNC_MIDI_VOLUME = 1;
    public static final byte SYNC_MUTE_OPTION = 2;
    public static final byte SYNC_SPARAMS = 3;
    public static final byte SYNC_WHITE_LIST = 4;
    public static final byte SYNC_BLACK_LIST = 5;    
    
    public void syncAll(EntityPlayer playerIn) {sync(playerIn, SYNC_ALL);}
    
    public void sync(EntityPlayer playerIn, byte propertyID)
    {
        if (playerIn != null && !playerIn.getEntityWorld().isRemote)
        {
            PacketDispatcher.sendTo(new SyncPlayerMusicOptionsMessage(playerIn.getCapability(MusicOptionsUtil.MUSIC_OPTIONS, null), propertyID), (EntityPlayerMP) playerIn);
        }
    }
}

/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
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

package net.aeronica.mods.mxtune.caps.player;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayerMusicOptionsMessage;
import net.aeronica.mods.mxtune.network.server.ChunkToolMessage;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;
import java.util.List;

import static net.aeronica.mods.mxtune.caps.player.PlayerMusicOptionsCapability.getOptionsCap;

@SuppressWarnings("ConstantConditions")
public class MusicOptionsUtil
{
    public static final int SYNC_ALL = 0;
    public static final int SYNC_DISPLAY_HUD = 1;
    public static final int SYNC_MUTE_OPTION = 2;
    public static final int SYNC_S_PARAMS = 3;
    public static final int SYNC_WHITE_LIST = 4;
    public static final int SYNC_BLACK_LIST = 5;
    public static final int SYNC_MUSIC_OP = 6;
    public static final int SYNC_SELECTED_PLAY_LIST_GUID = 7;
    public static final int SYNC_CTRL_KEY_DOWN = 8;
    public static final int SYNC_CHUNK_OPERATION = 9;


    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Miscellus.nonNullInjected();
    
    private MusicOptionsUtil() {}
    
    public static void setHudOptions(PlayerEntity playerIn, boolean disableHud, int positionHud, float sizeHud)
    {
        getOptionsCap(playerIn).orElse(null).setHudOptions(disableHud, positionHud, sizeHud);
    }

    public static boolean isHudDisabled(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).isHudDisabled();
    }
    
    public static int getPositionHUD(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getPositionHud();
    }

    public static float getSizeHud(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getSizeHud();
    }
    
    public static boolean isMuteAll(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getMuteOption() == MusicOptionsUtil.EnumMuteOptions.ALL.getIndex();
    }

    public static void setMuteOption(PlayerEntity playerIn, int muteOptionIn)
    {
        getOptionsCap(playerIn).orElse(null).setMuteOption(muteOptionIn);
        sync(playerIn, SYNC_MUTE_OPTION);
    }

    private static MusicOptionsUtil.EnumMuteOptions getMuteOptionEnum(PlayerEntity playerIn)
    {
        return MusicOptionsUtil.EnumMuteOptions.byIndex(getOptionsCap(playerIn).orElse(null).getMuteOption());
    }
    
    public static int getMuteOption(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getMuteOption();
    }
    
    public static void setSParams(PlayerEntity playerIn, String sParam1, String sParam2, String sParam3)
    {
        getOptionsCap(playerIn).orElse(null).setSParams(sParam1, sParam2, sParam3);
        sync(playerIn, SYNC_S_PARAMS);
    }
    
    public static String getSParam1(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getSParam1();
    }

    @SuppressWarnings("unused")
    public static String getSParam2(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getSParam2();
    }

    @SuppressWarnings("unused")
    public static String getSParam3(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getSParam3();
    }
    
    public static void setBlackList(PlayerEntity playerIn, List<ClassifiedPlayer> blackList)
    {
        getOptionsCap(playerIn).orElse(null).setBlackList(blackList);
        sync(playerIn, SYNC_BLACK_LIST);
    }

    public static List<ClassifiedPlayer> getBlackList(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getBlackList();
    }
    
    public static void setWhiteList(PlayerEntity playerIn, List<ClassifiedPlayer> whiteList)
    {
        getOptionsCap(playerIn).orElse(null).setWhiteList(whiteList);
        sync(playerIn, SYNC_WHITE_LIST);
    }

    public static List<ClassifiedPlayer> getWhiteList(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getWhiteList();
    }

    public static boolean isSoundRangeInfinityAllowed(PlayerEntity playerIn) { return getOptionsCap(playerIn).orElse(null).isSoundRangeInfinityRangeAllowed(); }

    public static void setSoundRangeInfinityAllowed(PlayerEntity playerIn, boolean isAllowed) { getOptionsCap(playerIn).orElse(null).setSoundRangeInfinityAllowed(isAllowed); }

    public static boolean isMxTuneServerUpdateAllowed(PlayerEntity playerIn) { return getOptionsCap(playerIn).orElse(null).isMxTuneServerUpdateAllowed(); }

    public static void setMxTuneServerUpdateAllowed(PlayerEntity playerIn, boolean isAllowed)
    {
        getOptionsCap(playerIn).orElse(null).setMxTuneServerUpdateAllowed(isAllowed);
        sync(playerIn, SYNC_MUSIC_OP);
    }

    public static void setSelectedPlayListGuid(PlayerEntity playerIn, GUID guidPlayList)
    {
        getOptionsCap(playerIn).orElse(null).setSelectedPlayListGuid(guidPlayList);
        sync(playerIn, SYNC_SELECTED_PLAY_LIST_GUID);
    }

    public static GUID getSelectedPlayListGuid(PlayerEntity playerIn) { return getOptionsCap(playerIn).orElse(null).getSelectedPlayListGuid(); }


    public static void setCtrlKey(PlayerEntity playerIn, boolean isDown)
    {
        getOptionsCap(playerIn).orElse(null).setCtrlKey(isDown);
        sync(playerIn, SYNC_CTRL_KEY_DOWN);
    }

    public static boolean isCtrlKeyDown(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).isCtrlKeyDown();
    }


    public static void setChunkToolOperation(PlayerEntity playerIn, ChunkToolMessage.Operation operation){
        getOptionsCap(playerIn).orElse(null).setChunkToolOperation(operation);
        sync(playerIn, SYNC_CHUNK_OPERATION);
    }

    public static ChunkToolMessage.Operation getChunkToolOperation(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getChunkToolOperation();
    }

    public static void setChunkStart(PlayerEntity playerIn, @Nullable Chunk chunkStart)
    {
        getOptionsCap(playerIn).orElse(null).setChunkStart(chunkStart);
    }

    @Nullable
    public static Chunk getChunkStart(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getChunkStart();
    }

    public static void setChunkEnd(PlayerEntity playerIn, @Nullable Chunk chunkEnd)
    {
        getOptionsCap(playerIn).orElse(null).setChunkEnd(chunkEnd);
    }

    @Nullable
    public static Chunk getChunkEnd(PlayerEntity playerIn)
    {
        return getOptionsCap(playerIn).orElse(null).getChunkEnd();
    }

    /*
     * GuiHudAdjust positionHud temporary value for use when adjusting the Hud.
     */
    private static int adjustPositionHud = 0;
    public static int getAdjustPositionHud() {return adjustPositionHud;}
    public static void setAdjustPositionHud(int posHud) {adjustPositionHud = posHud;}
    
    private static float adjustSizeHud = 1.0F;
    public static void setAdjustSizeHud(float sizeHud) {adjustSizeHud=sizeHud;}
    public static float getAdjustSizeHud() {return adjustSizeHud;}

    /**
     * Mute per the muteOptions setting taking care to not mute THEPLAYER (playerIn) except for case ALL
     * 
     * @param playerIn this persons mute setting
     * @param otherPlayer is the other person muted
     * @return true if muted
     */
    public static boolean playerNotMuted(@Nullable PlayerEntity playerIn, @Nullable PlayerEntity otherPlayer)
    {
        boolean result = false;
        if (playerIn != null && otherPlayer != null)
        {
            switch (getMuteOptionEnum(playerIn))
            {
            case OFF:
                break;
            case ALL:
                result = true;
                break;
            case OTHERS:
                result = !playerIn.equals(otherPlayer);
                break;
            case WHITELIST:
                result = !isPlayerInList(playerIn, otherPlayer, getWhiteList(playerIn));
                break;
            case BLACKLIST:
                result = isPlayerInList(playerIn, otherPlayer, getBlackList(playerIn));
                break;
            default:
            }
        }
        return !result;
    }

    private static boolean isPlayerInList(PlayerEntity playerIn, PlayerEntity otherPlayer, List<ClassifiedPlayer> playerList)
    {
        boolean inList = false;
        if (!playerIn.equals(otherPlayer))
            for (ClassifiedPlayer w : playerList)
                if (w.getUuid().equals(otherPlayer.getUniqueID()))
                {
                    inList = true;
                    break;
                }
        return inList;
    }

    public enum EnumMuteOptions implements IStringSerializable
    {
        OFF(0, "mxtune.gui.musicOptions.muteOption.off"),
        OTHERS(1, "mxtune.gui.musicOptions.muteOption.others"),
        BLACKLIST(2, "mxtune.gui.musicOptions.muteOption.blacklist"),
        WHITELIST(3, "mxtune.gui.musicOptions.muteOption.whitelist"),
        ALL(4, "mxtune.gui.musicOptions.muteOption.all");

        private final int index;
        private final String translateKey;
        private static final EnumMuteOptions[] INDEX_LOOKUP = new EnumMuteOptions[values().length];

        EnumMuteOptions(int index, String translateKey)
        {
            this.index = index;
            this.translateKey = translateKey;
        }
        
        public int getIndex() {return this.index;}
        
        static
        {
            for (EnumMuteOptions value : values())
            {
                INDEX_LOOKUP[value.getIndex()] = value;
            }
        }

        public static EnumMuteOptions byIndex(int indexIn)
        {
            int index = indexIn;
            if (index < 0 || index >= INDEX_LOOKUP.length)
            {
                index = 0;
            }
            return INDEX_LOOKUP[index];
        }
        
        @Override
        public String toString(){return I18n.format(this.translateKey);}  

        @Override
        public String getName() {return this.translateKey;}        
    }

    /**
     * Sync all properties for the specified player to the client.
     *
     * @param playerIn synchronize this players music options
     */
    static void syncAll(PlayerEntity playerIn)
    {
        sync(playerIn, SYNC_ALL);
    }

    /**
     * Sync the specified property ID for the specified player
     * to the client.
     *  @param playerIn synchronize this players music options
     * @param propertyID to synchronize
     */
    public static void sync(PlayerEntity playerIn, int propertyID)
    {
        if (!playerIn.getEntityWorld().isRemote)
        {
            PacketDispatcher.sendTo(new SyncPlayerMusicOptionsMessage(getOptionsCap(playerIn).orElse(null), propertyID), (ServerPlayerEntity) playerIn);
        }
    }
}

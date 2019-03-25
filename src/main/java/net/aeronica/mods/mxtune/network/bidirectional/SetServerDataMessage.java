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

package net.aeronica.mods.mxtune.network.bidirectional;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.UUID;

public class SetServerDataMessage extends AbstractMessage<SetServerDataMessage>
{
    public enum SetType {AREA, MUSIC}
    private SetType type = SetType.AREA;
    private boolean errorResult = false;
    private String errorMessage = "";
    private NBTTagCompound dataCompound = new NBTTagCompound();
    private long dataTypeUuidMSB = 0;
    private long dataTypeUuidLSB = 0;
    private UUID dataTypeUuid = Reference.EMPTY_UUID;

    @SuppressWarnings("unused")
    public SetServerDataMessage() { /* Required by the PacketDispatcher */ }

    public SetServerDataMessage(String errorMessage, Boolean errorResult)
    {
        this.errorMessage = errorMessage;
        this.errorResult = errorResult;
    }
    /**
     * Client Submission for data type
     * @param uuidType data type unique id
     * @param type data type
     */
    public SetServerDataMessage(UUID uuidType, SetType type , NBTTagCompound dataCompound)
    {
        this.type = type;
        this.dataCompound = dataCompound;
        dataTypeUuidMSB = uuidType.getMostSignificantBits();
        dataTypeUuidLSB = uuidType.getLeastSignificantBits();
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.type = buffer.readEnumValue(SetType.class);
        this.dataCompound = buffer.readCompoundTag();
        this.dataTypeUuidMSB = buffer.readLong();
        this.dataTypeUuidLSB = buffer.readLong();
        this.errorMessage = buffer.readString(512);
        this.errorResult = buffer.readBoolean();
        dataTypeUuid = new UUID(dataTypeUuidMSB, dataTypeUuidLSB);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeEnumValue(type);
        buffer.writeCompoundTag(dataCompound);
        buffer.writeLong(dataTypeUuidMSB);
        buffer.writeLong(dataTypeUuidLSB);
        buffer.writeString(errorMessage);
        buffer.writeBoolean(errorResult);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
        {
            handleClientSide(player);
        } else
        {
            handleServerSide(player);
        }
    }

    private void handleClientSide(EntityPlayer player)
    {
        switch (type)
        {
            case AREA:
                break;
            case MUSIC:
                break;
            default:
        }
        ModLogger.debug("Error: %s, error %s", errorMessage, errorResult);
    }

    private void  handleServerSide(EntityPlayer player)
    {
        if (player.canUseCommand(3, "mxTuneServerUpdateAllowed"))
        {
            switch (type)
            {
                case AREA:
                    ServerFileManager.setArea(dataTypeUuid, dataCompound);
                    break;
                case MUSIC:
                    break;
                default:
            }
        }
        else
        {
            player.sendStatusMessage(new TextComponentTranslation("mxtune.chat.set_server_data_not_allowed") {}, false);
        }
    }
}

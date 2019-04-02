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
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.ResultMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class SetServerDataMessage extends AbstractMessage<SetServerDataMessage>
{
    private SetType type = SetType.AREA;
    private boolean errorResult = false;
    private ITextComponent component = new TextComponentTranslation("mxtune.no_error", "");
    private NBTTagCompound dataCompound = new NBTTagCompound();
    private long ddddSigBits;
    private long ccccSigBits;
    private long bbbbSigBits;
    private long aaaaSigBits;
    private GUID dataTypeUuid = Reference.EMPTY_GUID;

    @SuppressWarnings("unused")
    public SetServerDataMessage() { /* Required by the PacketDispatcher */ }

    public SetServerDataMessage(ITextComponent component, Boolean errorResult)
    {
        this.component = component;
        this.errorResult = errorResult;
    }

    /**
     * Client Submission for data type
     * @param guidType data type unique id
     * @param type data type
     */
    public SetServerDataMessage(GUID guidType, SetType type , NBTTagCompound dataCompound)
    {
        this.type = type;
        this.dataCompound = dataCompound;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.type = buffer.readEnumValue(SetType.class);
        this.dataCompound = buffer.readCompoundTag();
        ddddSigBits = buffer.readLong();
        ccccSigBits = buffer.readLong();
        bbbbSigBits = buffer.readLong();
        aaaaSigBits = buffer.readLong();
        this.component = ITextComponent.Serializer.jsonToComponent(buffer.readString(32767));
        this.errorResult = buffer.readBoolean();
        dataTypeUuid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeEnumValue(type);
        buffer.writeCompoundTag(dataCompound);
        buffer.writeLong(ddddSigBits);
        buffer.writeLong(ccccSigBits);
        buffer.writeLong(bbbbSigBits);
        buffer.writeLong(aaaaSigBits);
        buffer.writeString(ITextComponent.Serializer.componentToJson(component));
        buffer.writeBoolean(errorResult);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
        {
            handleClientSide();
        } else
        {
            handleServerSide((EntityPlayerMP) player);
        }
    }

    private void handleClientSide()
    {
        switch (type)
        {
            case AREA:
                break;
            case MUSIC:
                break;
            default:
        }
        ModLogger.debug("Error: %s, error %s", component.getFormattedText(), errorResult);
    }

    private void  handleServerSide(EntityPlayerMP player)
    {
        ResultMessage resultMessage = ResultMessage.NO_ERROR;
        if (MusicOptionsUtil.isMxTuneServerUpdateAllowed(player))
        {
            switch (type)
            {
                case AREA:
                    resultMessage = ServerFileManager.setArea(dataTypeUuid, dataCompound);
                    break;
                case MUSIC:
                    resultMessage = ServerFileManager.setSong(dataTypeUuid, dataCompound);
                    break;
                default:
                    resultMessage = new ResultMessage(true, new TextComponentTranslation("mxtune.error.unexpected_type", type.name()));
            }
        }
        else
            PacketDispatcher.sendTo(new SetServerDataMessage((new TextComponentTranslation("mxtune.warning.set_server_data_not_allowed")), true), player);

        if (resultMessage.hasError())
            PacketDispatcher.sendTo(new SetServerDataMessage(resultMessage.getMessage(), resultMessage.hasError()), player);

    }

    public enum SetType {AREA, MUSIC}
}

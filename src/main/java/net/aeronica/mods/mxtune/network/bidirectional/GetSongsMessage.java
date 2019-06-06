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

import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetSongsMessage extends AbstractMessage<GetSongsMessage>
{
    private byte[] byteBuffer = null;
    private long callbackUuidMSB = 0;
    private long callbackUuidLSB = 0;
    private ITextComponent message = new TextComponentTranslation("mxtune.no_error", "");
    private UUID callbackUuid;
    private List<SongProxy> songProxies;
    private boolean readError = false;

    public GetSongsMessage() { /* Required by the PacketDispatcher */ }

    public GetSongsMessage(UUID callback)
    {
        callbackUuidMSB = callback.getMostSignificantBits();
        callbackUuidLSB = callback.getLeastSignificantBits();
    }

    public GetSongsMessage(List<SongProxy> songProxies, UUID callbackUuid, ITextComponent message)
    {
        this.songProxies = songProxies;
        this.message = message;
        this.callbackUuidMSB = callbackUuid.getMostSignificantBits();
        this.callbackUuidLSB = callbackUuid.getLeastSignificantBits();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void read(PacketBuffer buffer)
    {
        callbackUuidMSB = buffer.readLong();
        callbackUuidLSB = buffer.readLong();
        this.message = ITextComponent.Serializer.jsonToComponent(buffer.readString(32767));
        callbackUuid = new UUID (callbackUuidMSB, callbackUuidLSB);
        try {
            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis);
            songProxies =  (ArrayList<SongProxy>) in.readObject();
            in.close();

        } catch (ClassNotFoundException | IOException e)
        {
            readError = true;
            ModLogger.error(e);
        }
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeLong(callbackUuidMSB);
        buffer.writeLong(callbackUuidLSB);
        buffer.writeString(ITextComponent.Serializer.componentToJson(message));
        try {
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject((Serializable) songProxies);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
            buffer.writeByteArray(byteBuffer);

        } catch (IOException e) {
            ModLogger.error(e);
        }
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
        CallBackManager.CallBackData callBackData = CallBackManager.getCaller(callbackUuid);
        if (!readError)
        {
            if (callBackData != null)
                callBackData.callBack.onResponse(songProxies, callBackData.xEnum);
        }
        else
            if (callBackData != null)
                callBackData.callBack.onFailure(message.appendText("/n").appendSibling(new TextComponentTranslation("mxtune.error.network_data_error", "CLIENT Read Error.")));
    }

    private void handleServerSide(EntityPlayerMP playerMP)
    {
        PacketDispatcher.sendTo(new GetSongsMessage(ServerFileManager.getSongProxies(), callbackUuid, new TextComponentTranslation("mxtune.no_error", "SERVER")), playerMP);
    }
}

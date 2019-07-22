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

package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerSelectedPlayListMessage extends AbstractMessage.AbstractServerMessage<PlayerSelectedPlayListMessage>
{
    private GUID selectedAreaGuid;
    private long ddddSigBits;
    private long ccccSigBits;
    private long bbbbSigBits;
    private long aaaaSigBits;

    public PlayerSelectedPlayListMessage() { /* Required by the Packet Dispatcher */}

    public PlayerSelectedPlayListMessage(GUID selectedAreaGuid)
    {
        ddddSigBits = selectedAreaGuid.getDdddSignificantBits();
        ccccSigBits = selectedAreaGuid.getCcccSignificantBits();
        bbbbSigBits = selectedAreaGuid.getBbbbSignificantBits();
        aaaaSigBits = selectedAreaGuid.getAaaaSignificantBits();
    }

    @Override
    protected void decode(PacketBuffer buffer)
    {
        ddddSigBits = buffer.readLong();
        ccccSigBits = buffer.readLong();
        bbbbSigBits = buffer.readLong();
        aaaaSigBits = buffer.readLong();
        selectedAreaGuid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
    }

    @Override
    protected void encode(PacketBuffer buffer)
    {
        buffer.writeLong(ddddSigBits);
        buffer.writeLong(ccccSigBits);
        buffer.writeLong(bbbbSigBits);
        buffer.writeLong(aaaaSigBits);
    }

    @Override
    public void handle(PlayerEntity player, Side side)
    {
        MusicOptionsUtil.setSelectedPlayListGuid(player, selectedAreaGuid);
    }
}

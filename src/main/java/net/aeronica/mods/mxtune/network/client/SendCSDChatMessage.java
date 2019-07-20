/*
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
package net.aeronica.mods.mxtune.network.client;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.network.bidirectional.ClientStateDataMessage;
import net.aeronica.mods.mxtune.status.CSDChatStatus;
import net.aeronica.mods.mxtune.status.ClientStateData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class SendCSDChatMessage extends AbstractClientMessage<SendCSDChatMessage>
{

    private ClientStateData csd;

    @SuppressWarnings("unused")
    public SendCSDChatMessage() {/* Required by the PacketDispatcher */}
    
    public SendCSDChatMessage(ClientStateData csd) {this.csd = csd;}
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        this.csd = ClientStateDataMessage.readCSD(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        ClientStateDataMessage.writeCSD(buffer, this.csd);
    }

    @Override
    public void process(PlayerEntity player, Side side)
    {
        new CSDChatStatus(player, csd); 
    }

}

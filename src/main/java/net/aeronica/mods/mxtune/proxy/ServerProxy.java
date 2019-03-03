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
package net.aeronica.mods.mxtune.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import net.aeronica.mods.mxtune.init.ModEntities;
import net.aeronica.mods.mxtune.managers.GroupManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerProxy
{
    public void preInit() { /*  NOP */ }

    public void init() { /*  NOP */ }
    
    public void postInit() { /*  NOP */ }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnableToSchedule);
    }

    public void initEntities() { ModEntities.init(); }

    public Side getPhysicalSide()  { return Side.SERVER ;}

    public Side getEffectiveSide() { return getPhysicalSide(); }

    public EntityPlayer getClientPlayer() { return null; }

    public EntityPlayer getPlayerByEntityID(int entityID)
    {
        return (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(entityID);
    }

    public Minecraft getMinecraft() { return null; }

    public World getClientWorld() { return null; }

    public void spawnMusicParticles(EntityPlayer player) { /* NOP */ }
    
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(GroupManager.INSTANCE);
    }

    public void registerKeyBindings() { /* NOP */ }

    public void initMML() { /*  NOP */ }

    public void registerHUD() { /*  NOP */ }

    public boolean playerIsInCreativeMode(EntityPlayer player)
    {
        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
            return entityPlayerMP.isCreative();
        }
        return false;
    }

    public EntityPlayer getPlayerEntity(MessageContext ctx) { return ctx.getServerHandler().player; }

    public IThreadListener getThreadFromContext(MessageContext ctx) { return ctx.getServerHandler().player.getServer(); }
}

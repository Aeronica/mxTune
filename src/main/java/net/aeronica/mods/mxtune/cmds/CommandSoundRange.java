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
package net.aeronica.mods.mxtune.cmds;

import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandSoundRange extends CommandBase
{
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return server.isSinglePlayer() || super.checkPermission(server, sender);
    }

    @Override
    public int getRequiredPermissionLevel() { return 2; }

    @Override
    public String getName()
    {
        return "soundRangeInfinityAllowed";
    }

    @Override
    public String getUsage(ICommandSender sender) { return "commands.mxtune.soundRangeInfinityAllowed.usage"; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String playerName = "";
        EntityPlayer entityPlayer = null;

        if (args.length > 0)
        {
            entityPlayer = getPlayer(server, sender, args[0]);
            playerName = args[0];
        }

        if (entityPlayer == null) return;

        switch (args.length)
        {
            case 1:
                sender.sendMessage((new TextComponentString(playerName)).appendText(": soundRangeInfinityAllowed = ").appendText(MusicOptionsUtil.isSoundRangeInfinityAllowed(entityPlayer) ? "true" : "false"));
                break;

            case 2:
                String option = buildString(args, 1);
                if ("true".equals(option))
                    MusicOptionsUtil.setSoundRangeInfinityAllowed(entityPlayer, true);
                else if ("false".equals(option))
                    MusicOptionsUtil.setSoundRangeInfinityAllowed(entityPlayer, false);
                break;

            default:
                throw new WrongUsageException("commands.mxtune.soundRangeInfinityAllowed.usage");
        }
    }

    /**
     * Get a list of options for when the user presses the TAB key
     */
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, "true", "false");

        return Collections.emptyList();
    }
}

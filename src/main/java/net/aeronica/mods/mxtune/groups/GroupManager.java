/*
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
package net.aeronica.mods.mxtune.groups;

import com.google.common.collect.Sets;
import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.JoinGroupMessage;
import net.aeronica.mods.mxtune.network.client.SyncGroupMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
// UUID does not work on the client.
public class GroupManager
{
    public static final GroupManager INSTANCE = new GroupManager();
    private static Integer groupID = 1;
    
    private GroupManager() {}

    /*
     * The guts of the GroupManager - After looking over this weeks later I can
     * see I made some bad decisions, but I'll live with it for now. It's called
     * fail fast! Learn from your mistake and move on.
     *
     * 2018-Sep-27 Thanks for IntelliJ cleaning this up took little time.
     *
     * 2016-Oct-21 Converted the whole shibang from String Player Names to Integer
     * Entity IDs. That simplified usage a bit. 
     * 
     * Sync'd to the Client side using the GROUPS.class and
     * the associated networking classes.
     */
    private static class Member
    {
        Integer memberEntityID;
    }

    public static class Group
    {
        Integer groupID;
        Integer playID;
        Integer leaderEntityID;
        HashSet<Member> members;
    }

    private static final HashSet<Group> groups = new HashSet<>(1, 0.3f);

    public static Set<Integer> getGroups()
    {
        Set<Integer> setGroups = Sets.newHashSet();
        if (groups != null && !groups.isEmpty())
        {
            for (Group theGroup : groups)
            {
                setGroups.add(theGroup.groupID);
            }
        }
        return setGroups;
    }
    
    private static Integer getNextGroupID() {return (groupID == Integer.MAX_VALUE) ? groupID=1 : groupID++;}
    
    /**
     * Any player can be a leader or in a group. A player who makes a group is
     * the leader of the group. A player in a group can't join another group.
     * If the leader leaves a group another member will be promoted to group
     * leader automatically.
     * 
     * @param creatorID the leader/creator of a group
     */
    public static void addGroup(int creatorID)
    {
        log("addGroup " + creatorID);

        if (getGroup(creatorID) == null && !groupsHaveMember(creatorID))
        {
            Group theGroup = new Group();

            theGroup.groupID = getNextGroupID();
            theGroup.playID = null;
            
            theGroup.leaderEntityID = creatorID;

            Member theMember = new Member();
            theMember.memberEntityID = creatorID;

            theGroup.members = new HashSet<Member>(GROUPS.MAX_MEMBERS);
            theGroup.members.add(theMember);

            groups.add(theGroup);
            sync();
        } else
            log("----- Can't create a group if you are a member of a group.");
    }
    
    /**
     * addMember TODO: setup language file keys
     * 
     * @param groupID target group
     * @param memberID member to add
     */
    public static void addMember(int groupID, int memberID)
    {
        if (groups != null && !groups.isEmpty())
        {
            Group g = getGroup(groupID);

            /* Grab instance of the other player */
            EntityPlayer playerInitiator = getEntityPlayer(memberID);

            log("addMember " + groupID + " : " + memberID);
            if ((g != null) && !groupsHaveMember(memberID))
            {
                /* Grab instance of the leader */
                EntityPlayer playerTarget = getEntityPlayer(g.leaderEntityID);
                if (g.members.size() < GROUPS.MAX_MEMBERS)
                {
                    Member m = new Member();
                    m.memberEntityID = memberID;
                    g.members.add(m);
                    sync();

                    playerInitiator.sendMessage(new TextComponentString("You Joined " + playerTarget.getDisplayName().getFormattedText() + "'s group"));
                    playerTarget.sendMessage(new TextComponentString(playerInitiator.getDisplayName().getFormattedText() + " joined the group"));
                } else
                {
                    log("----- Can't join. Too many members.");
                    playerInitiator.sendMessage(new TextComponentString("You can't join " + playerTarget.getDisplayName().getFormattedText() + "'s group. Too many members."));
                    playerTarget.sendMessage(new TextComponentString(playerInitiator.getDisplayName().getFormattedText() + " can't join group. Too many members."));
                }
            } else
            {
                log("----- Can't join a group if you are a member of a group.");
                playerInitiator.sendMessage(new TextComponentString("You can't join a group if you are a member of a group."));
            }
        } else
            log("----- No group exists!");
    }

    /**
     * Removes a member from all groups potentially changing the leader of a
     * group or removing the group entirely.
     * 
     * @param memberID to be removed
     * @return the group of the member or null.
     */
    public static void removeMember(int memberID)
    {
        log("removeMember " + memberID);
        PlayManager.purgeMember(memberID);
        if (!groups.isEmpty())
        {
            Group theGroup;
            Member theMember;
            Iterator<Group> ig = groups.iterator();
            while (ig.hasNext())
            {
                theGroup = ig.next();
                Iterator<Member> im = theGroup.members.iterator();
                while (im.hasNext())
                {
                    theMember = (Member) im.next();
                    if (theMember.memberEntityID.equals(memberID))
                    {

                        if (!theGroup.leaderEntityID.equals(memberID))
                        {
                            /* This is not the leader so simply remove the member. */
                            im.remove();
                            log("----- removed " + memberID);
                            sync();
                        } else
                        {
                            /* This is the leader of the group and if we are the last or only member then we will remove the group. */
                            if (theGroup.members.size() == 1)
                            {
                                log("----- " + theMember.memberEntityID + " is the last member so remove the group");
                                theGroup.members.clear();
                                theGroup.members = null;
                                ig.remove();
                                sync();
                                return;
                            }
                            /* Remove the leader */
                            im.remove();
                            sync();
                            /* Promote the next member of the group to leader. */
                            Iterator<Member> ix = theGroup.members.iterator();
                            if (ix.hasNext())
                            {
                                theMember = (Member) ix.next();
                                theGroup.leaderEntityID = theMember.memberEntityID;
                                log("----- " + theMember.memberEntityID + " is promoted to the group leader");
                                sync();
                            }
                        }
                    }
                }
            }
        } else
            log("----- " + memberID + " is not a member of a group.");
    }

    public static boolean isLeader(int entityID)
    {
        Group g = getMembersGroup(entityID);
        return (g != null) && g.leaderEntityID.equals(entityID);
    }
    
    /**
     * setLeader A rather unsafe way to change the leader of the group, but this
     * will do for now
     * 
     * @param memberID the new leader
     */
    public static void setLeader(int memberID)
    {
        Group g = getMembersGroup(memberID);
        if (g != null)
        {
            g.leaderEntityID = memberID;
            sync();
        }
    }

    public static Integer getMembersGroupID(int memberID)
    {
        Group group = getMembersGroup(memberID);
        return group == null ? null : group.groupID;
    }

    /**
     * Searches all groups and returns the group or null.
     * 
     * @param creatorID member in question
     * @return the group or null.
     */
    private static Group getGroup(int creatorID)
    {
        for (Group theGroup : groups)
        {
            if (theGroup.groupID.equals(creatorID)) return theGroup;
        }
        return null;
    }

    /**
     * @param memberID search all groups for thia member.
     * @return the Group if found or null.
     */
    public static Group getMembersGroup(int memberID)
    {
        for (Group theGroup : groups)
        {
            for (Member theMember : theGroup.members)
            {
                if (theMember.memberEntityID.equals(memberID)) return theGroup;
            }
        }
        return null;
    }

    /**
     * Search all groups for the memberID.
     * 
     * @param memberID member to search for
     * @return true if the memberID is found.
     */
    private static boolean groupsHaveMember(int memberID)
    {
        boolean hasMember = false;
        for (Group theGroup : groups)
        {
            for (Member theMember : theGroup.members)
            {
                if (theMember.memberEntityID.equals(memberID)) hasMember = true;
            }
        }
        return hasMember;
    }

    public static void dump()
    {
        for (Group theGroup : groups)
        {
            debug("Group: " + theGroup.groupID);
            debug("  Leader: " + theGroup.leaderEntityID);
            for (Member theMember : theGroup.members)
            {
                debug("    member: " + theMember.memberEntityID);
            }
        }
    }

    public static void sync()
    {
        StringBuilder buildGroups = new StringBuilder("|");
        StringBuilder buildMembers = new StringBuilder("|");

        for (Group theGroup : groups)
        {
            debug("Group: " + theGroup.groupID);
            debug("  Leader: " + theGroup.leaderEntityID);
            buildGroups.append(theGroup.groupID).append("=").append(theGroup.leaderEntityID).append("|");
            for (Member theMember : theGroup.members)
            {
                debug("    member: " + theMember.memberEntityID);
                buildMembers.append(theMember.memberEntityID).append("=").append(theGroup.groupID).append("|");
            }
        }
        /* sync server */
        GROUPS.setClientGroups(buildGroups.toString());
        GROUPS.setClientMembers(buildMembers.toString());
        /* sync to clients */
        PacketDispatcher.sendToAll(new SyncGroupMessage(buildGroups.toString(), buildMembers.toString()));
    }

    private static int interactFlag = 0;
    /* Forge and FML Event Handling */
    /*
     * TODO: Add a yes/no gui to ask user if that want to join. Indicate if a
     * party is full, or if it requires a password.
     * @param event
     */
    @SubscribeEvent
    public void onEntityInteractEvent(EntityInteract event)
    {
        if (event.getTarget() != null && event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer playerInitiator = (EntityPlayer) event.getEntityPlayer();
            EntityPlayer playerTarget = (EntityPlayer) event.getTarget();

            ModLogger.debug(playerInitiator.getDisplayName().getUnformattedText() + " pokes " + playerTarget.getDisplayName().getUnformattedText());

            if ((event.getSide() == Side.SERVER) && (interactFlag++ % 2) == 0)
            {
                
                Group targetGroup = getMembersGroup(playerTarget.getEntityId());
                if (targetGroup != null && targetGroup.leaderEntityID.equals(playerTarget.getEntityId()) /* && initatorGroup == null */)
                {
                    if (!MusicOptionsUtil.isMuteAll(playerInitiator))
                    {
                        if (!MusicOptionsUtil.isPlayerMuted(playerInitiator, playerTarget))
                        {
                            MusicOptionsUtil.setSParams(playerInitiator, targetGroup.groupID.toString(), "", "");
                            PacketDispatcher.sendTo(new JoinGroupMessage(targetGroup.groupID), (EntityPlayerMP) playerInitiator);
                        } else
                        {
                            /* target fails the mute options check */
                            playerInitiator.sendMessage(new TextComponentTranslation("mxtune.chat.gm.noJoinGroupWhenPlayerIsMuted", new Object[] {playerTarget.getDisplayName().getUnformattedText()}));
                        }
                    } else
                    {
                        /* MuteALL is true so playerInitator can't join */
                        playerInitiator.sendMessage(new TextComponentTranslation("mxtune.chat.gm.noJoinGroupWhenMuteAll"));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event)
    {
        Group group = getMembersGroup(event.getEntityPlayer().getEntityId());
        if (group != null)
        {
            event.setResult(SleepResult.NOT_POSSIBLE_NOW);
            event.getEntityPlayer().sendMessage(new TextComponentTranslation("mxtune.chat.gm.noSleepInJam"));
        }
    }

    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            removeMember(player.getEntityId());
        }
    }

    /* FML Gaming Events */
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP)
        {
            GroupManager.sync();
        }
    }
    
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        if(MXTuneMain.proxy.getEffectiveSide() == Side.SERVER)
            ServerCSDManager.queryClient(event.player);
    } 
    
    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerLoggedOutEvent event)
    {
        removeMember(event.player.getEntityId());
    }

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event)
    {
        removeMember(event.player.getEntityId());
    }

    private static void debug(String strMessage) {/*ModLogger.debug(strMessage);*/}

    private static void log(String strMessage) {/*ModLogger.logInfo(strMessage);*/}

    private static EntityPlayer getEntityPlayer(Integer leaderEntityID)
    {
        MinecraftServer world = FMLCommonHandler.instance().getMinecraftServerInstance();
        return (EntityPlayer) world.getServer().getEntityWorld().getEntityByID(leaderEntityID);
    }
}

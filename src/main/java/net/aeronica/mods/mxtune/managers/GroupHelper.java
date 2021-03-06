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

package net.aeronica.mods.mxtune.managers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.MapListHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class GroupHelper
{
    private static final GroupHelper INSTANCE = new GroupHelper();
    public static final int GROUP_ADD = 1;
    public static final int MEMBER_ADD = 2;
    public static final int MEMBER_REMOVE =3;
    public static final int MEMBER_PROMOTE = 4;
    static final int QUEUED = 5;
    static final int PLAYING = 6;

    public static final int MAX_MEMBERS = 8;

    /* Server side, Client side is sync'd with packets */
    /* GroupManager */
    private static Map<Integer, Integer> clientGroups = Collections.emptyMap();
    private static Map<Integer, Integer> clientMembers = Collections.emptyMap();
    private static ListMultimap<Integer, Integer> groupsMembers = ArrayListMultimap.create();
    /* PlayManager */
    private static Map<Integer, Integer> membersQueuedStatus = Collections.emptyMap();
    private static Map<Integer, Integer> membersPlayID = Collections.emptyMap();
    private static Set<Integer> activeServerManagedPlayIDs = new ConcurrentSkipListSet<>();

    private GroupHelper() { /* NOP */ }

    /* GroupManager Client Status Methods */
    public static int getLeaderOfGroup(int leaderID)
    {
        Integer leader = GroupHelper.clientGroups.get(leaderID);
        return (leader != null) ? leader : -1;
    }

    public static int getMembersGroupLeader(int memberID){
        return getLeaderOfGroup(getMembersGroupID(memberID));
    }

    public static int getMembersGroupID(int memberID)
    {
        Integer groupID = GroupHelper.clientMembers.get(memberID);
        return (groupID != null) ? GroupHelper.clientMembers.get(memberID) : -1;
    }

    private static Set<Integer> getPlayersGroupMembers(EntityPlayer playerIn)
    {
        int groupID = GroupHelper.getMembersGroupID(playerIn.getEntityId());
        if(groupID != -1)
        {
            Set<Integer> members = Sets.newHashSet();
            for (Integer group: groupsMembers.keySet())
            {
                if(group != null && groupID == group)
                    members.addAll(groupsMembers.get(group));
            }       
            return members;
        }
        return Collections.emptySet();
    }
    
    private static boolean isLeader(Integer memberID)
    {
        return memberID.equals(getLeaderOfGroup(getMembersGroupID(memberID)));
    }

    public static Map<Integer, Integer> getClientMembers()
    {
        return GroupHelper.clientMembers;
    }
    
    public static void setClientMembers(String members)
    {
        GroupHelper.clientMembers = MapListHelper.deserializeIntIntMap(members);
    }
    
    public static Map<Integer, Integer> getClientGroups()
    {
        return GroupHelper.clientGroups;
    }
    
    public static void setClientGroups(String groups)
    {
        GroupHelper.clientGroups = MapListHelper.deserializeIntIntMap(groups);
    }
    
    public static void setGroupsMembers(String members)
    {
        GroupHelper.groupsMembers = MapListHelper.deserializeIntIntListMultimapSwapped(members);
    }

    // This is a workaround to force the playID into the active list on the client side presuming a network order
    // incident occurred and the playID is either not present, AND/OR a race condition, or threading issue made it not
    // available.
    @SideOnly(Side.CLIENT)
    public static void addServerManagedActivePlayID(int playId)
    {
        if (playId != PlayIdSupplier.PlayType.INVALID)
            activeServerManagedPlayIDs.add(playId);
    }

    public static Set<Integer> getAllPlayIDs()
    {
        return mergeSets(activeServerManagedPlayIDs, ClientAudio.getActivePlayIDs());
    }

    private static<T> Set<T> mergeSets(Set<T> a, Set<T> b)
    {
        Set<T> set = new HashSet<>(a);
        set.addAll(b);
        return Collections.unmodifiableSet(set);
    }

    public static void removeClientManagedPlayID(int playId)
    {
        synchronized (INSTANCE)
        {
            ClientAudio.queueAudioDataRemoval(playId);
        }
    }

    public static void clearServerManagedPlayIDs()
    {
        synchronized (INSTANCE)
        {
            activeServerManagedPlayIDs.clear();
        }
    }

    public static Set<Integer> getServerManagedPlayIDs()
    {
        return activeServerManagedPlayIDs;
    }

    public static ListMultimap<Integer, Integer> getGroupsMembers()
    {
        return groupsMembers;
    }
    
    /**
     * getIndex(Integer playerID)
     * 
     * This is used to return a the index for the playing status icons
     * 
     * @param playerID (EntityID)
     * @return int
     */
    public static int getIndex(Integer playerID)
    {
        int result = 0;
        if (GroupHelper.membersQueuedStatus != null && GroupHelper.membersQueuedStatus.containsKey(playerID))
        {
            switch (GroupHelper.membersQueuedStatus.get(playerID))
            {
            case QUEUED:
                result = 1;
                break;
            case PLAYING:
                result = 2;
                break;
            default:
            }
        }
        return result + (GroupHelper.isLeader(playerID) ? 8 : 0);
    }
    
    public static Map<Integer, Integer> getClientPlayStatuses()
    {
        return membersQueuedStatus;
    }

     /* PlayManager Client Status Methods */
    private static Set<Integer> getMembersByPlayID(Integer playID)
    {
        Set<Integer> members = Sets.newHashSet();
        if (membersPlayID != null)
        {
            for(Integer someMember: GroupHelper.membersPlayID.keySet())
            {
                if(GroupHelper.membersPlayID.get(someMember).equals(playID))
                {
                    members.add(someMember);
                }
            }
        }
        return members;
    }

    /**
     * Returns the median position of a group or player by playID.
     * Client side only until fixed. Probably need to take into account the dimension.
     *
     * @param playID for solo or JAM
     * @return the median position for a group, or the position of a solo player
     */
    public static Vec3d getMedianPos(int playID)
    {
        double x;
        double y;
        double z;
        x = y = z = 0;
        int count = 0;
        Vec3d pos;
        for(int member: getMembersByPlayID(playID))
        {
            EntityPlayer player = MXTune.proxy.getPlayerByEntityID(member);
            if(player == null)
                continue;
            x = x + player.getPositionVector().x;
            y = y + player.getPositionVector().y;
            z = z + player.getPositionVector().z;
            count++;
        }            

        if (count == 0)
            return Vec3d.ZERO;
        x/=count;
        y/=count;
        z/=count;
        pos = new Vec3d(x,y,z);
        return pos;
    }

    /**
     * Called by client tick once every two seconds to calculate the distance between
     * group members in relation to a maximum after which the server will stop any music the
     * group is performing.
     *
     * @param playerIn get the scaled distance for this player
     * #return 0-1D, where 1 represents the critical stop.
     */
    public static double getGroupMembersScaledDistance(EntityPlayer playerIn)
    {
        Set<Integer> members = getPlayersGroupMembers(playerIn);
        double abortDistance = ModConfig.getGroupPlayAbortDistance();
        double distance = 0D;
        double maxDistance = 0D;
        for (Integer memberA : members)
            for (Integer memberB : members)
                if (memberA.intValue() != memberB)
                {
                    double playerDistance = getMemberVector(memberA).distanceTo(getMemberVector(memberB));
                    if (playerDistance > maxDistance) maxDistance = playerDistance;
                    distance = Math.min(1.0D, (maxDistance / abortDistance));
                }
        return distance;
    }

    private static Vec3d getMemberVector(Integer entityID)
    {
        Vec3d v3d;
        EntityPlayer player = (EntityPlayer) MXTune.proxy.getClientPlayer().getEntityWorld().getEntityByID(entityID);
        if (player != null)
            v3d = new Vec3d(player.posX, player.prevPosY, player.posZ);
        else
            v3d = new Vec3d(0,0,0);
        return v3d;
    }
    
    public static boolean isClientPlaying(Integer playID)
    {
        Set<Integer> members = GroupHelper.getMembersByPlayID(playID);
        return (!members.isEmpty()) && members.contains(MXTune.proxy.getClientPlayer().getEntityId());
    }

    @SuppressWarnings("unused")
    public static boolean playerHasPlayID(Integer entityID, int playID)
    {
        Set<Integer> members = GroupHelper.getMembersByPlayID(playID);
        return (!members.isEmpty()) && members.contains(entityID);
    }

    @SuppressWarnings("unused")
    public static boolean isPlayIDPlaying(Integer playID) { return activeServerManagedPlayIDs != null && activeServerManagedPlayIDs.contains(playID); }

    public static void setClientPlayStatuses(String clientPlayStatuses)
    {
        GroupHelper.membersQueuedStatus = MapListHelper.deserializeIntIntMap(clientPlayStatuses);
    }
        
    public static Map<Integer, Integer> getPlayIDMembers()
    {
        return membersPlayID;
    }

    @Nullable
    public static Integer getSoloMemberByPlayID(int playID)
    {
        for(Integer someMember: GroupHelper.membersPlayID.keySet())
        {
            if(GroupHelper.membersPlayID.get(someMember).equals(playID))
            {
                return someMember;
            }
        }
        return null;
    }
    
    public static void setPlayIDMembers(String playIDMembers)
    {
        GroupHelper.membersPlayID = MapListHelper.deserializeIntIntMap(playIDMembers);
    }

    /**
     * Update the active play IDs without replacing the collection instance.
     * @param setIntString serialized set
     */
    public static void setActiveServerManagedPlayIDs(String setIntString)
    {
        Set<Integer> receivedSet = MapListHelper.deserializeIntegerSet(setIntString);
        activeServerManagedPlayIDs.addAll(receivedSet);
        activeServerManagedPlayIDs.removeIf(playID -> !receivedSet.contains(playID));
    }

}

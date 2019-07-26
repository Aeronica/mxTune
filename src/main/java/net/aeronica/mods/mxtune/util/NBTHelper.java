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

package net.aeronica.mods.mxtune.util;

import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

public class NBTHelper
{
    private static final String TAG_GUID_DSB = "guid_dsb";
    private static final String TAG_GUID_CSB = "guid_csb";
    private static final String TAG_GUID_BSB = "guid_bsb";
    private static final String TAG_GUID_ASB = "guid_asb";

    private static final String TAG_UUID_MSB = "uuid_msb";
    private static final String TAG_UUID_LSB = "uuid_lsb";

    private NBTHelper() { /* NOP */ }

    // GUID NBT Helpers

    public static GUID getGuidFromCompound(CompoundNBT compound)
    {
        long dsb = compound.getLong(TAG_GUID_DSB);
        long csb = compound.getLong(TAG_GUID_CSB);
        long bsb = compound.getLong(TAG_GUID_BSB);
        long asb = compound.getLong(TAG_GUID_ASB);
        return new GUID(dsb, csb, bsb, asb);
    }

    public static void setGuidToCompound(CompoundNBT compound, GUID guid)
    {
        compound.putLong(TAG_GUID_DSB, guid.getDdddSignificantBits());
        compound.putLong(TAG_GUID_CSB, guid.getCcccSignificantBits());
        compound.putLong(TAG_GUID_BSB, guid.getBbbbSignificantBits());
        compound.putLong(TAG_GUID_ASB, guid.getAaaaSignificantBits());
    }

    public static GUID getGuidFromTag(CompoundNBT compound, String tagKey)
    {
        CompoundNBT compoundTag = compound.getCompound(tagKey);
        return getGuidFromCompound(compoundTag);
    }

    public static void setGuidToTag(GUID guid, CompoundNBT compound, String tagKey)
    {
        CompoundNBT tagCompound = new CompoundNBT();
        setGuidToCompound(tagCompound, guid);
        compound.put(tagKey, tagCompound);
    }

    // UUID NBT Helpers

    public static UUID getUuidFromCompound(CompoundNBT compound)
    {
        long msb = compound.getLong(TAG_UUID_MSB);
        long lsb = compound.getLong(TAG_UUID_LSB);
        return new UUID(msb, lsb);
    }

    public static void setUuidToCompound(CompoundNBT compound, UUID uuid)
    {
        compound.putLong(TAG_UUID_MSB, uuid.getMostSignificantBits());
        compound.putLong(TAG_UUID_LSB, uuid.getLeastSignificantBits());
    }

    public static UUID getUuidFromTag(CompoundNBT compound, String tagKey)
    {
        CompoundNBT compoundTag = compound.getCompound(tagKey);
        return getUuidFromCompound(compoundTag);
    }

    public static void setUuidToTag(UUID uuid, CompoundNBT compound, String tagKey)
    {
        CompoundNBT tagCompound = new CompoundNBT();
        setUuidToCompound(tagCompound, uuid);
        compound.put(tagKey, tagCompound);
    }
}

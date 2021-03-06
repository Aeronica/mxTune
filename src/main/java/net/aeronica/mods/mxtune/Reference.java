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
package net.aeronica.mods.mxtune;

import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.util.GUID;

import java.util.UUID;

public class Reference
{
    private Reference() {/* NOP */}

    public static final UUID EMPTY_UUID = new UUID(0L, 0L);
    public static final GUID EMPTY_GUID = new GUID(0L,0L,0L,0L);
    public static final GUID NO_MUSIC_GUID = PlayList.emptyPlaylist().getGUID();
    public static final String MOD_ID = "mxtune";
    public static final String MOD_NAME = "mxTune";
    public static final String VERSION = "{@VERSION}";
    static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12.2,1.13)";
    static final String DEPENDENTS = "required-after:forge@[1.12.2-14.23.5.2802,)";
    static final String UPDATE = "https://gist.githubusercontent.com/Aeronica/dbc2619e0011d5bdbe7a162d0c6aa82b/raw/update.json";
    static final String CERTIFICATE_FINGERPRINT = "999640c365a8443393a1a21df2c0ede9488400e9";
    public static final int MXTUNE_DATA_FIXER_VERSION = 2;
    public static final int MAX_MML_PART_LENGTH = 12000;
    public static final int MXT_SONG_TITLE_LENGTH = 80;
    public static final int MXT_SONG_AUTHOR_LENGTH = 80;
    public static final int MXT_SONG_SOURCE_LENGTH = 320;
    public static final int MXT_TAG_NAME_LENGTH = 25;
    public static final int MXT_TAG_DISPLAY_NAME_LENGTH = 50;
}
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
package net.aeronica.mods.mxtune.util;

import net.minecraft.util.Tuple;

@SuppressWarnings("unchecked")
public class ValidDuration extends Tuple
{
    public static final ValidDuration INVALID = new ValidDuration(false, 0);

    public ValidDuration(Boolean isValidMML, Integer duration)
    {
        super(isValidMML, duration);
    }

    public Boolean isValidMML()
    {
        return (Boolean) super.getFirst();
    }

    public Integer getDuration()
    {
        return (Integer) super.getSecond();
    }
}

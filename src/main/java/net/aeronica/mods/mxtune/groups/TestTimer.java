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

package net.aeronica.mods.mxtune.groups;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.util.ModLogger;

import java.util.Timer;
import java.util.TimerTask;

import static net.aeronica.mods.mxtune.util.SheetMusicUtil.formatDuration;

/**
 * Experimental: Schedule removal of the playID after a specified duration in seconds.
 */
class TestTimer
{
    private TestTimer() { /* NOP */ }

    static void scheduleStop(String message, int playID, int duration)
    {
        TimerTask task = new TimerTask() {
            @Override
            public void run()
            {
                MXTune.proxy.addScheduledTask(() -> stop(message, playID, duration));
            }
        };
        Timer timer = new Timer("Timer");
        long delay = duration * 1000L;
        timer.schedule(task, delay);
    }

    private static void stop(String messageIn, int playIDIn, int durationIn)
    {
        String messaage = messageIn;
        int playID = playIDIn;
        int duration = durationIn;

        ModLogger.info("...Beep! A scheduled stop arrived for playID: %d with a duration of %s, \'%s\'", playID, formatDuration(duration), messaage);
        PlayManager.stopPlayID(playID);
    }
}

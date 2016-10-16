/**
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
package net.aeronica.mods.mxtune.sound;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.aeronica.libs.mml.core.MMLLexer;
import net.aeronica.libs.mml.core.MMLParser;
import net.aeronica.libs.mml.core.MMLToMIDI;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.util.ModLogger;

public class MML2PCM
{
    private byte[] mmlBuf = null;
    private InputStream is;

    public boolean process(int entityID, String musicText)
    {
        try
        {
            mmlBuf = musicText.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return false;
        }
        is = new java.io.ByteArrayInputStream(mmlBuf);

        /** ANTLR4 MML Parser BEGIN */
        MMLToMIDI mmlTrans = new MMLToMIDI(1.0F);
        ANTLRInputStream input = null;

        try
        {
            input = new ANTLRInputStream(is);
        } catch (IOException e1)
        {
            e1.printStackTrace();
            return false;
        }
        MMLLexer lexer = new MMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MMLParser parser = new MMLParser(tokens);
        // parser.removeErrorListeners();
        // parser.addErrorListener(new UnderlineListener());
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(mmlTrans, tree);
        /** ANTLR4 MML Parser END */

        Integer[] patches = new Integer[mmlTrans.getPatches().size()];
        mmlTrans.getPatches().toArray(patches);
        for (int patch: patches)
            ModLogger.logInfo("  Patches: " + patch);
        Midi2WavRenderer mw;
        try
        {
            mw = new Midi2WavRenderer();
            ClientAudio.setEntityAudioStream(entityID, mw.createPCMStream(mmlTrans.getSequence(), patches, ClientAudio.getAudioFormat()));
        } catch (MidiUnavailableException e)
        {
            e.printStackTrace();
            ClientAudio.setEntityAudioDataStatus(entityID, Status.ERROR);
            return false;
        } catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
            ClientAudio.setEntityAudioDataStatus(entityID, Status.ERROR);
            return false;
        } catch (IOException e)
        {
            e.printStackTrace();
            ClientAudio.setEntityAudioDataStatus(entityID, Status.ERROR);
            return false;
        }
        ClientAudio.setEntityAudioDataStatus(entityID, Status.READY);
        return true;
    }

}
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
package net.aeronica.mods.mxtune.sound;

import net.aeronica.mods.mxtune.util.Util;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Random;

public class CodecPCM implements ICodec
{
    /**
     * Used to return a current value from one of the synchronized
     * boolean-interface methods.
     */
    private static final boolean GET = false;

    /**
     * Used to set the value in one of the synchronized boolean-interface
     * methods.
     */
    private static final boolean SET = true;

    /**
     * Used when a parameter for one of the synchronized boolean-interface
     * methods is not applicable.
     */
    private static final boolean XXX = false;

    /**
     * True if there is no more data to read in.
     */
    private boolean endOfStream = false;

    /**
     * True if the stream has finished initializing.
     */
    private boolean initialized = false;

    /**
     * Format the converted audio will be in.
     */
    private AudioFormat myAudioFormat = null;

    /**
     * True if the using library requires data read by this codec to be
     * reverse-ordered before returning it from methods read() and readAll().
     */
    private boolean reverseBytes = false;

    /**
     * The audio stream from the synthesizer
     */
    private AudioInputStream audioInputStream = null;

    /**
     * A dummy stream just to open a handle to the proxy sound file.
     */
    private AudioInputStream dummyInputStream = null;

    private static final int SAMPLE_SIZE = 11025 * 4;

    private byte[] noiseBuffer = new byte[SAMPLE_SIZE];
    private byte[] zeroBuffer = new byte[SAMPLE_SIZE];

    private boolean hasStream = false;
    private int zeroBufferCount = 0;

    private Random randInt;

    private Integer playID = null;
    private AudioData audioData = null;

    /**
     * Processes status messages, warnings, and error messages.
     */
    private SoundSystemLogger logger;

    public CodecPCM()
    {
        logger = SoundSystemConfig.getLogger();
        randInt = new java.util.Random(System.currentTimeMillis());
        nextNoiseZeroBuffer();
    }

    private void nextNoiseZeroBuffer()
    {
        for (int i = 0; i < SAMPLE_SIZE; i += 2)
        {
            int x = (short) (randInt.nextInt() / 3) * 2;
            noiseBuffer[i] = (byte) x;
            noiseBuffer[i + 1] = (byte) (x >> 8);
            zeroBuffer[i] = zeroBuffer[i + 1] = 0;
        }
    }

    /**
     * Tells this CODEC when it will need to reverse the byte order of the data
     * before returning it in the read() and readAll() methods. The MML2PCM
     * class produces audio data in a format that some external audio libraries
     * require to be reversed. Derivatives of the Library and Source classes for
     * audio libraries which require this type of data to be reversed will call
     * the reverseByteOrder() method.
     *
     * @param b True if the calling audio library requires byte-reversal.
     */
    @Override
    public void reverseByteOrder(boolean b)
    {
        reverseBytes = b;
    }

    @Override
    public boolean initialize(URL url)
    {
        initialized(SET, false);
        if (playID == null)
        {
            playID = ClientAudio.pollPlayIDQueue02();
            if (playID == null)
            {
                errorMessage("playID not initialized");
                return false;
            }
            else
            {
                audioData = ClientAudio.getAudioData(playID);
                myAudioFormat = audioData.getAudioFormat();
            }
        }

        if (url == null)
        {
            errorMessage("url null in method 'initialize'");
            cleanup();
            return false;
        }
        try
        {
            dummyInputStream = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(url.openStream()));
        } catch (UnsupportedAudioFileException uafe)
        {
            errorMessage("Unsupported audio format in method 'initialize'");
            printStackTrace(uafe);
            return false;
        } catch (IOException ioe)
        {
            errorMessage("Error setting up audio input stream in method " + "'initialize'");
            printStackTrace(ioe);
            return false;
        }

        endOfStream(SET, false);
        initialized(SET, true);

        return true;
    }
    	
	@Override
	public boolean initialized() {
		return initialized(GET, XXX);
	}

	@Override
    public SoundBuffer read()
    {
        if (hasInputStreamError())
        {
            errorMessage("Not initialized in 'read'");
            return null;
        }
        notifyOnInputStreamAvailable();

        int bufferSize;
        byte[] readBuffer = new byte[SoundSystemConfig.getStreamingBufferSize()];
        byte[] outputBuffer = Util.nonNullInjected();
        try
        {
            if (hasStream && (audioInputStream != null))
            {
                bufferSize = audioInputStream.read(readBuffer);
                if (bufferSize > 0)
                    outputBuffer = appendByteArrays(outputBuffer, readBuffer, bufferSize);

                if (bufferSize == -1)
                {
                    endOfStream(SET, true);
                    audioData.setStatus(ClientAudio.Status.DONE);
                    return null;
                }
            }
            else
            {
                nextNoiseZeroBuffer();
                outputBuffer = appendByteArrays(outputBuffer, zeroBuffer, SAMPLE_SIZE);
                if (zeroBufferCount++ > 64)
                {
                    errorMessage("MML to PCM audio processing took too long. Aborting!");
                    endOfStream(SET, true);
                    return null;
                }
            }
        } catch (IOException e)
        {
            printStackTrace(e);
            endOfStream(SET, true);
            return null;
        }
        if (!reverseBytes && outputBuffer != null)
            reverseBytes(outputBuffer, 0, outputBuffer.length);
        return new SoundBuffer(outputBuffer, myAudioFormat);
	}

	private boolean hasInputStreamError()
    {
        if (!initialized || myAudioFormat == null || audioData == null || audioData.getStatus() == ClientAudio.Status.ERROR)
        {
            errorMessage("Not initialized in 'read'");
            return true;
        }

        return false;
    }

    private void notifyOnInputStreamAvailable()
    {
        if (!hasStream && (audioData.getStatus() == ClientAudio.Status.READY))
        {
            audioInputStream = audioData.getAudioStream();
            message("Waiting buffer count: " + zeroBufferCount);
            hasStream = true;
        }
    }

	/** Load the entire buffer at once.
     *
     * Note: The audio data is cached and if found the sound
     * system will not call this again unless the sound is
     * invalidated.
     * */
    @Override
    public SoundBuffer readAll()
    {
        if (!initialized)
        {
            errorMessage("Not initialized in 'readAll'");
            return null;
        }
        if ((myAudioFormat == null) || (audioData == null) || (audioData.getStatus() == ClientAudio.Status.ERROR))
        {
            errorMessage("Audio Format null in method 'readAll'");
            return null;
        }
        if (endOfStream())
            return null;

        byte[] outputBuffer = null;

        for (int i = 0; i < 25; i++)
        {
            nextNoiseZeroBuffer();
            outputBuffer = appendByteArrays(outputBuffer, noiseBuffer, SAMPLE_SIZE);
        }
        errorMessage("ReadAll NOT Supported! Always use stream = true. You have been warned.");
        if (!reverseBytes && outputBuffer != null)
            reverseBytes(outputBuffer, 0, outputBuffer.length);
        return new SoundBuffer(outputBuffer, myAudioFormat);
    }

    @Override
    public boolean endOfStream()
    {
        return endOfStream(GET, XXX);
    }

    @Override
    public void cleanup()
    {
        message("cleanup");
        if (audioInputStream != null)
            try
            {
                audioInputStream.close();
            } catch (IOException e)
            {
                printStackTrace(e);
            }
        audioInputStream = null;

        if (dummyInputStream != null)
            try
            {
                dummyInputStream.close();
            } catch (Exception e)
            {
                printStackTrace(e);
            }
        dummyInputStream = null;
    }

    @Override
    public AudioFormat getAudioFormat()
    {
        return myAudioFormat;
    }

    /**
     * Internal method for synchronizing access to the boolean 'initialized'.
     *
     * @param action GET or SET.
     * @param value  New value if action == SET, or XXX if action == GET.
     * @return True if steam is initialized.
     */
    private synchronized boolean initialized(boolean action, boolean value)
    {
        if (action == SET)
            initialized = value;
        return initialized;
    }

    /**
     * Internal method for synchronizing access to the boolean 'endOfStream'.
     *
     * @param action GET or SET.
     * @param value  New value if action == SET, or XXX if action == GET.
     * @return True if end of stream was reached.
     */
    private synchronized boolean endOfStream(boolean action, boolean value)
    {
        if (action == SET)
            endOfStream = value;
        return endOfStream;
    }

    /**
     * Trims down the size of the array if it is larger than the specified
     * maximum length.
     *
     * @param array     Array containing audio data.
     * @param maxLength Maximum size this array may be.
     * @return New array.
     */
    @SuppressWarnings("unused") // Forge
    private static byte[] trimArray(byte[] array, int maxLength)
    {
        byte[] trimmedArray = null;
        if (array != null && array.length > maxLength)
        {
            trimmedArray = new byte[maxLength];
            System.arraycopy(array, 0, trimmedArray, 0, maxLength);
        }
        return trimmedArray;
    }

	/**
	 * Reverse-orders all bytes contained in the specified array.
	 * 
	 * @param buffer
	 *            Array containing audio data.
	 */
    @SuppressWarnings("unused")
    public static void reverseBytes(byte[] buffer)
    {
        reverseBytes(buffer, 0, buffer.length);
    }

    /**
     * Reverse-orders the specified range of bytes contained in the specified
     * array.
     *
     * @param buffer Array containing audio data.
     * @param offset Array index to begin.
     * @param size   number of bytes to reverse-order.
     */
    @SuppressWarnings("all")
    private static void reverseBytes(byte[] buffer, int offset, int size)
    {

        byte b;
        for (int i = offset; i < (offset + size); i += 2)
        {
            b = buffer[i];
            buffer[i] = buffer[i + 1];
            buffer[i + 1] = b;
        }
    }

    /**
     * Converts sound bytes to little-endian format.
     *
     * @param audioBytes   The original wave data
     * @param twoBytesData For stereo sounds.
     * @return byte array containing the converted data.
     */
    @SuppressWarnings("unused") // Forge
    private static byte[] convertAudioBytes(byte[] audioBytes, boolean twoBytesData)
    {
        ByteBuffer dest = ByteBuffer.allocateDirect(audioBytes.length);
        dest.order(ByteOrder.nativeOrder());
        ByteBuffer src = ByteBuffer.wrap(audioBytes);
        src.order(ByteOrder.LITTLE_ENDIAN);
        if (twoBytesData)
        {
            ShortBuffer destShort = dest.asShortBuffer();
            ShortBuffer srcShort = src.asShortBuffer();
            while (srcShort.hasRemaining())
            {
                destShort.put(srcShort.get());
            }
        }
        else
        {
            while (src.hasRemaining())
            {
                dest.put(src.get());
            }
        }
        dest.rewind();

        if (!dest.hasArray())
        {
            byte[] arrayBackedBuffer = new byte[dest.capacity()];
            dest.get(arrayBackedBuffer);
            dest.clear();

            return arrayBackedBuffer;
        }

        return dest.array();
    }

    /**
     * Creates a new array with the second array appended to the end of the
     * first array.
     *
     * @param arrayOne The first array.
     * @param arrayTwo The second array.
     * @param length   How many bytes to append from the second array.
     * @return Byte array containing information from both arrays.
     */
    private static byte[] appendByteArrays(byte[] arrayOne, byte[] arrayTwo, int length)
    {
        byte[] newArray;
        if (arrayOne == null && arrayTwo == null)
        {
            // no data, just return
            return new byte[0];
        }
        else if (arrayOne == null)
        {
            // create the new array, same length as arrayTwo:
            newArray = new byte[length];
            // fill the new array with the contents of arrayTwo:
            System.arraycopy(arrayTwo, 0, newArray, 0, length);
        }
        else if (arrayTwo == null)
        {
            // create the new array, same length as arrayOne:
            newArray = new byte[arrayOne.length];
            // fill the new array with the contents of arrayOne:
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
        }
        else
        {
            // create the new array large enough to hold both arrays:
            newArray = new byte[arrayOne.length + length];
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
            // fill the new array with the contents of both arrays:
            System.arraycopy(arrayTwo, 0, newArray, arrayOne.length, length);
        }

        return newArray;
    }

    /**
     * Prints an error message.
     *
     * @param message Message to print.
     */
    private void errorMessage(String message)
    {
        logger.errorMessage("[mxtune] CodecPCM", message, 1);
    }

    /**
     * Prints an exception's error message followed by the stack trace.
     *
     * @param e Exception containing the information to print.
     */
    private void printStackTrace(Exception e)
    {
        logger.printStackTrace(e, 1);
    }

    private void message(String message)
    {
        logger.message("[mxtune]: CodecPCM " + message, 1);
    }
}

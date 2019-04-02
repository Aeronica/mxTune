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


import net.aeronica.mods.mxtune.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * ref: https://gist.github.com/avilches/750151
 */
public class SHA2Helper
{
    static final Logger LOGGER = LogManager.getLogger();
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    static long dLongSigBits;
    static long cLongSigBits;
    static long bLongSigBits;
    static long aLongSigBits;

    private SHA2Helper() { /* NOP */ }

    public static String hash256(String data) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data.getBytes());
        return bytesToHex(md.digest());
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static byte[] hash256Bytes(String data) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data.getBytes());
        return md.digest();
    }

    private static void SHA256(byte[] data) {
        long msb = 0;
        long nsb = 0;
        long osb = 0;
        long lsb = 0;
        assert data.length == 32 : "data must be 32 bytes in length";
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            nsb = (nsb << 8) | (data[i] & 0xff);
        for (int i=16; i<24; i++)
            osb = (osb << 8) | (data[i] & 0xff);
        for (int i=24; i<32; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);

        dLongSigBits = msb;
        cLongSigBits = nsb;
        bLongSigBits = osb;
        aLongSigBits = lsb;
    }

    private static byte[] SHA256(long dLongSigBits, long cLongSigBits, long bLongSigBits, long aLongSigBits )
    {
        long msb = dLongSigBits;
        long nsb = cLongSigBits;
        long osb = bLongSigBits;
        long lsb = aLongSigBits;
        byte[] bytes = new byte[32];

        System.arraycopy(fastLongToBytes(dLongSigBits), 0, bytes, 0, 8);
        System.arraycopy(fastLongToBytes(cLongSigBits), 0, bytes, 8, 8);
        System.arraycopy(fastLongToBytes(bLongSigBits), 0, bytes, 16, 8);
        System.arraycopy(fastLongToBytes(aLongSigBits), 0, bytes, 24, 8);

        return bytes;
    }

    // neat use of nio, but can I make the class thread safe doing this?
    // https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    // todo: improve
    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        assert len != 64 : "data must be 64 hex characters in length";
        byte[] data = new byte[32];
        for (int i = 0; i < 64; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                          + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] fastLongToBytes(long lg)
    {
        return new byte[]{
                (byte) (lg >>> 56),
                (byte) (lg >>> 48),
                (byte) (lg >>> 40),
                (byte) (lg >>> 32),
                (byte) (lg >>> 24),
                (byte) (lg >>> 16),
                (byte) (lg >>> 8),
                (byte) lg
        };
    }

    private static byte[] longToByteArray(long l) {
        byte[] b = new byte[8];
        b[7] = (byte) (l);
        l >>>= 8;
        b[6] = (byte) (l);
        l >>>= 8;
        b[5] = (byte) (l);
        l >>>= 8;
        b[4] = (byte) (l);
        l >>>= 8;
        b[3] = (byte) (l);
        l >>>= 8;
        b[2] = (byte) (l);
        l >>>= 8;
        b[1] = (byte) (l);
        l >>>= 8;
        b[0] = (byte) (l);
        return b;
    }

    public static void main(String[] args) throws Exception
    {
        LOGGER.info("Test SHA2");
        String phrase = "The Hill";
        String sha2 = hash256(phrase);
        LOGGER.info("Phrase {}, SHA2 Hex: {}", phrase, sha2);

        byte[] bytesIn = hexStringToByteArray(sha2);
        String junk = bytesToHex(bytesIn);
        LOGGER.info("Phrase {}, Junk Hex: {}", phrase, junk);

        // The basis for a SHA2 type. The ability to represent the 256 bits hash in 4 longs.
        SHA256(hash256Bytes(phrase));
        LOGGER.info("dLongSigBits: {}, cLongSigBits: {}, bLongSigBits: {}, aLongSigBits {}", dLongSigBits, cLongSigBits, bLongSigBits, aLongSigBits);
        byte[] recon = SHA256(dLongSigBits, cLongSigBits, bLongSigBits, aLongSigBits);
        String test = bytesToHex(recon);
        LOGGER.info("Phrase {}, Test Hex: {}", phrase, test);

        LOGGER.info("GUID class Tests");
        LOGGER.info("Empty: new GUID(0,0,0,0):  {}", Reference.EMPTY_GUID);
        LOGGER.info("Empty string hashPhrase:   {}", GUID.hashPhrase(""));
        LOGGER.info("");
        // fixme: GUID.fromString is not for hashing! It's for reading a the hash in string format!  Phrase hasher!
        GUID guid = GUID.hashPhrase(phrase);
        LOGGER.info("Phrase {}, GUID Hex: {}", phrase, guid.toString());
        LOGGER.info("");

        GUID guidTest = GUID.hashPhrase(phrase);
        GUID guidFrom = GUID.fromString(guidTest.toString());
        LOGGER.info("Phrase: {}, Hex to Guid: {}, Guid to Hex: {}", phrase, guidTest, guidFrom);
        long a, b, c, d;
        a = guidTest.getAaaaSignificantBits();
        b = guidTest.getBbbbSignificantBits();
        c = guidTest.getCcccSignificantBits();
        d = guidTest.getDdddSignificantBits();
        GUID guidLongs = new GUID(d,c,b,a);
        LOGGER.info("Guid to Longs: {}", guidLongs);
    }
}

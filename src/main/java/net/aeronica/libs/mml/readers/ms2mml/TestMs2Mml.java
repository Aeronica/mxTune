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

package net.aeronica.libs.mml.readers.ms2mml;

import net.aeronica.libs.mml.core.MMLAllowedCharacters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class TestMs2Mml
{


    private static Logger LOGGER = LogManager.getLogger();

    static void test(InputStream is) throws JAXBException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance("net.aeronica.libs.mml.readers.ms2mml");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Ms2 ms2 = (Ms2) unmarshaller.unmarshal(is);

        LOGGER.info("Melody {}", MMLAllowedCharacters.filterAllowedCharacters(ms2.melody));
        if (ms2.chord != null)
            for (Ms2.Chord chord : ms2.chord)
            {
                LOGGER.info("Chord {}: {}", chord.index, MMLAllowedCharacters.filterAllowedCharacters(chord.value));
            }
    }


    static FileInputStream getFile(String path)
    {
        FileInputStream reader = null;
        LOGGER.info("File: {}", path.substring(path.lastIndexOf("\\")+1));
        try
        {
            reader = new FileInputStream(path);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return reader;
    }

    static void viewZipFileContents(String path)
    {
        try (ZipFile file = new ZipFile(path))
        {
            //File temp = File.createTempFile("blah", "tmp");

            Enumeration<? extends ZipEntry> entries = file.entries();
            LOGGER.info("Zip File: {}", file.getName().substring(file.getName().lastIndexOf("\\")+1));
            while(entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory())
                {
                } else
                {
                    LOGGER.info("File: {}, size: {}", entry.getName(), entry.getSize());
                    InputStream is = file.getInputStream(entry);
                    test(is);
                }
            }
            //temp.delete();
        } catch (IOException|JAXBException e)
        {
            LOGGER.error(e);
        }
        LOGGER.info("----");
    }

    public static void main(String[] args) throws Exception
    {
        test(getFile("E:\\Users\\Paul\\Downloads\\darling-in-the-franxx-ed5-escape-r2518.ms2mml"));
        viewZipFileContents("E:\\Users\\Paul\\Downloads\\undertale-megalovania-r48.zip");
        viewZipFileContents("E:\\Users\\Paul\\Downloads\\kingdom-hearts-3-dont-think-twice-chikai-kyle-landry-version.zip");
    }
}

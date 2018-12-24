package net.aeronica.libs.mml.core;

import javax.sound.midi.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.aeronica.libs.mml.core.MMLUtil.*;

public class MMLToMIDI extends MMLTransformBase
{
    private static final double PPQ = 480.0;
    private static final int TICKS_OFFSET = 10;
    private Sequence sequence;
    private Set<Integer> packedPresets = new HashSet<>();

    public MMLToMIDI() {/* NOP */}

    @Override
    public long durationTicks(int mmlNoteLength, boolean dottedLEN)
    {
        double dot = dottedLEN ? 15.0d : 10.0d;
        return (long) (((4.0d / (double) mmlNoteLength) * dot / 10.0d) * PPQ);
    }

    public Sequence getSequence() {return sequence;}
    
    public List<Integer> getPackedPresets()
    {
        return new ArrayList<>(packedPresets);
    }

    @Override
    public void processMObjects(List<MObject> mmlObject)
    {
        int ch = 0;
        int tk = 0;
        long ticksOffset = TICKS_OFFSET;
        int currentTempo;

        try
        {
            sequence = new Sequence(Sequence.PPQ, (int) PPQ);
            for (int i = 0; i < 24; i++)
            {
                sequence.createTrack();
            }
            Track[] tracks = sequence.getTracks();

            for (MObject mmo: mmlObject)
            {
                switch (mmo.getType())
                {
                case INST_BEGIN:
                case REST:
                    break;

                case TEMPO:
                    currentTempo = mmo.getTempo();
                    tracks[0].add(createTempoMetaEvent(currentTempo, mmo.getStartingTicks() + ticksOffset));
                    break;

                case INST:
                    Patch preset = packedPreset2Patch(mmo.getInstrument());
                    int bank =  preset.getBank();
                    int programPreset = preset.getProgram();
                    /* Detect a percussion set */
                    if (bank == 128)
                    {
                        /* Set Bank Select for Rhythm Channel MSB 0x78, LSB 0x00  - 14 bits only */
                        bank = 0x7800 >>> 1;
                    }
                    else
                    {
                        /* Convert the preset bank to the Bank Select bank */
                        bank = bank << 7;
                    }
                    tracks[tk].add(createBankSelectEventMSB(ch, bank, mmo.getStartingTicks() + ticksOffset-2L));
                    tracks[tk].add(createBankSelectEventLSB(ch, bank, mmo.getStartingTicks() + ticksOffset-1L));
                    tracks[tk].add(createProgramChangeEvent(ch, programPreset, mmo.getStartingTicks() + ticksOffset));
                    packedPresets.add(mmo.getInstrument());
                    break;

                case PART:
                    tk++;
                    if (tk > 23) tk = 23;
                    break;

                case NOTE:
                    tracks[tk].add(createNoteOnEvent(ch, smartClampMIDI(mmo.getMidiNote()), mmo.getNoteVolume(), mmo.getStartingTicks() + ticksOffset));
                    tracks[tk].add(createNoteOffEvent(ch, smartClampMIDI(mmo.getMidiNote()), mmo.getNoteVolume(), mmo.getStartingTicks() + mmo.getLengthTicks() + ticksOffset - 1));
                    if (mmo.getText() != null)
                    {
                        String text = "{\"Note\": {\"Midi\":" + String.format("%d", mmo.getMidiNote()) + ", \"Track\":" + tk + ", \"Text\":\"" + mmo.getText() + "\"}}";
                        tracks[0].add(createTextMetaEvent(text, mmo.getStartingTicks() + ticksOffset));
                    }
                    break;

                case INST_END:
                    tk++;
                    if (tk > 23) tk = 23;
                    ch++;
                    if (ch > 15) ch = 15;
                    break;

                case DONE:
                    // Create a fake note to extend the play time so decaying audio does not cutoff suddenly
                    tracks[0].add(createNoteOnEvent(ch, 1, 0, mmo.getlongestPartTicks() + ticksOffset + 200));
                    tracks[0].add(createNoteOffEvent(ch, 1, 0, mmo.getlongestPartTicks() + ticksOffset + 400));
                    break;

                default:
                    MML_LOGGER.debug("MMLToMIDI#processMObjects Impossible?! An undefined enum?");
                }
            }
        } catch (InvalidMidiDataException e)
        {
            MML_LOGGER.error("MMLToMIDI#processMObjects failed: {}", e);
        }
    }

    private MidiEvent createProgramChangeEvent(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, value, 0);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent createBankSelectEventMSB(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 0, value >> 7);
        return new MidiEvent(msg, tick);
    }
    
    private MidiEvent createBankSelectEventLSB(int channel, int value, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 32, value & 0x7F);
        return new MidiEvent(msg, tick);
    }
    
    private MidiEvent createNoteOnEvent(int channel, int pitch, int velocity, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent createNoteOffEvent(int channel, int pitch, int velocity, long tick) throws InvalidMidiDataException
    {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent createTempoMetaEvent(int tempo, long tick) throws InvalidMidiDataException
    {
        MetaMessage msg = new MetaMessage();
        byte[] data = ByteBuffer.allocate(4).putInt(1000000 * 60 / tempo).array();
        data[0] = data[1];
        data[1] = data[2];
        data[2] = data[3];
        msg.setMessage(0x51, data, 3);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent createTextMetaEvent(String text, long tick) throws InvalidMidiDataException
    {
        MetaMessage msg = new MetaMessage();
        byte[] data = text.getBytes();
        msg.setMessage(0x01, data, data.length);
        return new MidiEvent(msg, tick);
    }
}

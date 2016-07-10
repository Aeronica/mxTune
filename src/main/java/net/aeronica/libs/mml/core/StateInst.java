package net.aeronica.libs.mml.core;

public class StateInst
{
    private int tempo;
    private int instrument;
    private long longestPart;

    StateInst()
    {
        this.init();
        this.longestPart = 0;
    }

    public void init()
    {
        tempo = 120;
        instrument = 0;
    }

    public void setTempo(int tempo)
    {
        /** tempo 32-255, anything outside the range resets to 120 */
        if (tempo < 32 || tempo > 255)
        {
            this.tempo = 120;
        } else
        {
            this.tempo = tempo;
        }
    }

    public int getTempo() {return tempo;}

    public int getInstrument() {return instrument;}

    public void setInstrument(int gmInstrument)
    {
        /* MIDI patch number = GM instrument - 1 */
        this.instrument = (getMinMax(1, 128, gmInstrument)) - 1;
    }

    public void collectDurationTicks(long durationTicks)
    {
        if (durationTicks > this.longestPart) this.longestPart = durationTicks;
    }

    public long getLongestDurationTicks() {return this.longestPart;}

    @Override
    public String toString()
    {
        return new String("\n@CommonState: tempo=" + tempo + ", instrument=" + instrument);
    }

    private int getMinMax(int min, int max, int value) {return (int) Math.max(Math.min(max, value), min);}
}

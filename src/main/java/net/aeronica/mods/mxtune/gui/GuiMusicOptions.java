/**
 * Aeronica's mxTune MOD
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
package net.aeronica.mods.mxtune.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicOptionsMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.MusicOptionsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiMusicOptions extends GuiScreen
{
    public static final int GUI_ID = 8;
    private Minecraft mc;
    private String TITLE;

    private GuiButtonExt btn_muteOption;
    private GuiSliderMX btn_midiVolume;
    private GuiButtonExt btn_cancel, btn_done;
    private GuiLabel lbl_desc;
    
    private EntityPlayer player;
    private float midiVolume;
    private int muteOption;
    private int lastMuteOption = -1;
    
    public GuiMusicOptions(EntityPlayer playerIn) {this.player = playerIn; midiInit();}
    
    @Override
    public void updateScreen()
    {
        updateGuiElments();
        midiUpdate();
        super.updateScreen();
    }

    @Override
    public void initGui()
    {
        this.mc = Minecraft.getMinecraft();
        TITLE = I18n.format("mxtune.gui.GuiMusicOptions.title");
        midiVolume = MusicOptionsUtil.getMidiVolume(player);
        muteOption = MusicOptionsUtil.getMuteOption(player);
        
        this.buttonList.clear();

        int y = 30;
        int x = (width - 200) / 2;
        btn_muteOption = new GuiButtonExt(0, x, y, 200, 20, (MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString()));
        btn_midiVolume = new GuiSliderMX(1, x, y+=25, 200, 20, I18n.format("mxtune.gui.slider.midiVolume"), midiVolume*100F, 0F, 100F, 1F);
        
        btn_cancel = new GuiButtonExt(2, x, y+=25, 200, 20, I18n.format("gui.cancel"));
        btn_done = new GuiButtonExt(3, x, y+=25, 200, 20, I18n.format("gui.done"));
        
        x = (width - 250) / 2;
        lbl_desc = new GuiLabel(this.getFontRenderer(), 4, x, y, 250, 100, 0xD3D3D3);
        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description01"));
        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description02"));
        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description03"));
        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description04"));
        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description05"));
        
        this.buttonList.add(btn_muteOption);
        this.buttonList.add(btn_midiVolume);
        this.buttonList.add(btn_cancel);
        this.buttonList.add(btn_done);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(TITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(TITLE, posX, posY, 0xD3D3D3);
        lbl_desc.drawLabel(mc, mouseX, mouseY);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException
    {
        /** if button is disabled ignore click */
        if (!guibutton.enabled) return;

        /** id 0 = okay; 1 = cancel; 2 = play; 3 = stop */
        switch (guibutton.id)
        {
        case 0:
            /** Increment Mute Option */
            this.muteOption = ((++this.muteOption) % MusicOptionsUtil.EnumMuteOptions.values().length);
            ModLogger.logInfo("muteOption meta: " + muteOption + ", text: " + MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString() +
                    ", enum: " + MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).name());
            btn_muteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString();
          break;

        case 1:
            /** Volume */
            updateGuiElments();
            break;
        case 3:
            /** done */
            this.midiVolume = btn_midiVolume.getValue() / 100;
            sendOptionsToServer(this.midiVolume, this.muteOption);
        case 2:
            /** cancel */
            midiClose();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        default:
        }
    }

    private void updateGuiElments()
    {
        this.midiVolume = btn_midiVolume.getValue() / 100;
        if ((this.midiVolume == 0F) && this.lastMuteOption == -1)
        {
            this.lastMuteOption = this.muteOption;
            this.muteOption = MusicOptionsUtil.EnumMuteOptions.ALL.getMetadata();
            btn_muteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byMetadata(this.muteOption).toString();
            btn_muteOption.enabled = false;
        }
        if ((this.midiVolume != 0F) && this.lastMuteOption != -1)
        {
            btn_muteOption.enabled = true;
            this.muteOption = this.lastMuteOption;
            btn_muteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byMetadata(this.muteOption).toString();
            this.lastMuteOption = -1;
        }   
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        /** capture the ESC key do we close cleanly */
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            actionPerformed((GuiButton) buttonList.get(btn_cancel.id));
        }
        super.keyTyped(typedChar, keyCode);
    }

    @SuppressWarnings("unused")
    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        
        if (btn_midiVolume.isMouseOver()) {
            updateGuiElments();
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}
    
    protected void sendOptionsToServer(float midiVolume, int muteOption)
    {
        PacketDispatcher.sendToServer(new MusicOptionsMessage(midiVolume, muteOption));
    }
    
    /** MIDI generator for testing volume level - the John Cage Machine */
    private Synthesizer synth = null;
    MidiChannel[]   channels;
    MidiChannel channel;
    
    private void midiInit()
    {
        try {synth = MidiSystem.getSynthesizer();}
        catch (MidiUnavailableException e) {e.printStackTrace();}
        try {synth.open();}
        catch (MidiUnavailableException e) {e.printStackTrace();}
        channels = synth.getChannels();
        channel = channels[0];
        channel.programChange(2);
        setWait(2);
    }
    
    private void midiUpdate()
    {
        overControl(btn_midiVolume.isMouseOver());
        nextNote();
    }
    
    private void midiClose() {synth.close();}

    private int tickLen = 1;
    private int noteMidi1, noteMidi2, noteMidi3, noteMidi4, noteMidi5, run1, run2;
    private long tick;
    private long tock;
    private boolean waiting;
    private int noteActive = 0;
    private boolean noteOff = true;
    private double cyc = 0D;
    
    public void nextTick()
    {
        if (waiting)
        {
            if ((++tick % tickLen) == 0)
            {
                if (tock-- <= 0)
                {
                    waiting = false;
                }
            }
        }
    }

    public void setWait(int ms)
    {
        if (ms <= 0) return;
        tock = ms;
        tick = 0;
        waiting = true;
    }

    public void overControl(boolean hasHoover) {if (hasHoover) noteActive = 1; else noteActive = 0;}
    
    public void nextNote() {nextCmd(); cyc =  cyc + 0.16D;}

    public void nextCmd()
    {
        nextTick();

        if (waiting) return;   
        
        if (noteOff) {
            noteMidi1 = (int) Math.round(((((Math.sin(cyc)+1.D)/2D) *30.0D)+50.0D));
            noteMidi2 = noteMidi1 + ((int) Math.round((Math.random()*1.D)) ) + 4;
            //ModLogger.logInfo("diff: " + (noteMidi2 - noteMidi1));
            noteMidi3 = noteMidi1 + 9;
            noteMidi4 = ((run1+=3) %24) + 60;
            noteMidi5 = ((run2-=4) %10) + 40;
            int scaledVolume =  scaleVolume(127*noteActive);
            channel.noteOn(noteMidi1, scaledVolume);
            channel.noteOn(noteMidi2, scaledVolume);
            channel.noteOn(noteMidi3, scaledVolume);
            channel.noteOn(noteMidi4, scaledVolume);
            channel.noteOn(noteMidi5, scaledVolume);
            setWait(2);
            noteOff=false;
        } else {
            channel.noteOff(noteMidi1);
            channel.noteOff(noteMidi2);
            channel.noteOff(noteMidi3);
            channel.noteOff(noteMidi4);
            channel.noteOff(noteMidi5);
            setWait(1);
            noteOff = true;
        }
    }
    
    private int scaleVolume(int volumeIn)
    {
        int temp = (int) Math.round((float)volumeIn * (Math.exp(this.midiVolume)-1)/(Math.E-1));
        //ModLogger.logInfo("volumeIn: " + volumeIn + ", midiVolume: " + midiVolume + ", scaled: " + temp);
        return temp;
    }

    /** Lists - Players, Whitelist, Blacklist */
    
    private List<String> serverPlayerList; 
    public static class GuiPlayerList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final List<String> serverPlayerList;
        
        public GuiPlayerList(GuiMusicOptions parent, ArrayList<String> serverPlayerList, int left, int top, int listWidth, int listHeight, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, listHeight, top, top + listHeight, left, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.serverPlayerList = serverPlayerList;
        }

        @Override
        protected int getSize()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        protected boolean isSelected(int index)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        protected void drawBackground()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            // TODO Auto-generated method stub
            
        }
    }
}

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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.capabilities.IJamPlayer;
import net.aeronica.mods.mxtune.capabilities.JamDefaultImpl;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiMusicOptions extends GuiScreen
{
    public static final int GUI_ID = 8;
    private Minecraft mc;
    private String TITLE;

    private GuiButtonExt btn_muteOption;
    private GuiSliderMX btn_midiVolume;
    
    private EntityPlayer player;
    private IJamPlayer jamPlayerProps;
    
    private float midiVolume;
    private int muteOption;
    
    public GuiMusicOptions() {}
    
    @Override
    public void updateScreen()
    {
        // TODO Auto-generated method stub
        super.updateScreen();
    }

    @Override
    public void initGui()
    {
        this.mc = Minecraft.getMinecraft();
        player = this.mc.thePlayer;
        TITLE = I18n.format("mxtune.gui.GuiMusicOptions.title", new Object[0]);
        jamPlayerProps = player.getCapability(MXTuneMain.JAM_PLAYER, null);
        midiVolume = jamPlayerProps.getMidiVolume();
        muteOption = jamPlayerProps.getMuteOption();
        
        this.buttonList.clear();

        int y = 30;
        int x = (width - 200) / 2;
        btn_muteOption = new GuiButtonExt(0, x, y, 200, 20, I18n.format((JamDefaultImpl.EnumMuteOptions.byMetadata(muteOption).toString()), new Object[0]));
        btn_midiVolume = new GuiSliderMX(1, x, y+25, 200, 20, "MIDI Volume", midiVolume*100F, 0F, 100F, 5F);
        
        this.buttonList.add(btn_muteOption);
        this.buttonList.add(btn_midiVolume);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(TITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(TITLE, posX, posY, 0xD3D3D3);
        
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
            this.muteOption = ((++this.muteOption) % JamDefaultImpl.EnumMuteOptions.values().length);
            ModLogger.logInfo("muteOption meta: " + muteOption + ", text: " + 
            I18n.format((JamDefaultImpl.EnumMuteOptions.byMetadata(muteOption).toString()), new Object[0]) + ", enum: " +
            (JamDefaultImpl.EnumMuteOptions.byMetadata(muteOption).name())  );
            btn_muteOption.displayString = I18n.format(JamDefaultImpl.EnumMuteOptions.byMetadata(muteOption).toString(), new Object[0]);
            break;

        case 1:
            /** Volume */
            break;
            
        default:
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // TODO Auto-generated method stub
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        // TODO Auto-generated method stub
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        // TODO Auto-generated method stub
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}
}

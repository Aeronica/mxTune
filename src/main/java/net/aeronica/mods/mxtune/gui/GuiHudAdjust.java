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

import java.awt.Color;
import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiHudAdjust extends GuiScreen
{

    private static final String TITLE = I18n.format("mxtune.gui.hudAdjust.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    
    private GuiButtonExt btn_cancel, btn_done;

    private boolean midiUnavailable;
    
    public GuiHudAdjust()
    {
        midiUnavailable = MIDISystemUtil.getInstance().midiUnavailable();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        /** if button is disabled ignore click */
        if (!button.enabled) return;

        switch(button.id)
        {
        case 0:
        case 1:
            this.mc.displayGuiScreen(new GuiMusicOptions(this.mc.thePlayer));  
            break;
        default:
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        /** capture the ESC key so we close cleanly */
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            actionPerformed((GuiButton) buttonList.get(btn_cancel.id));
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        
        int y = (height) / 2;
        int buttonWidth = 100;
        y = height - 100;
        int x = (width / 2) - buttonWidth - 2;
        btn_done = new GuiButtonExt(0, x, y, buttonWidth, 20, I18n.format("gui.done"));
        btn_cancel = new GuiButtonExt(1, x + buttonWidth + 1 , y, buttonWidth, 20, I18n.format("gui.cancel"));
        
        this.buttonList.add(btn_cancel);
        this.buttonList.add(btn_done);
    }

    @Override
    public void updateScreen()
    {
        // TODO Do component updates here
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        // drawDefaultBackground();
        guiDrawBackground();
        String localTITLE;
        if (midiUnavailable)
            localTITLE = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            localTITLE = TITLE;
        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(localTITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(localTITLE, posX, posY, 0xD3D3D3);
        // TODO titles, labels, enclosed lists . . .
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void guiDrawBackground()
    {
        for (int i = 0; i < 8; i++)
        {
            HudData hd = GuiJamOverlay.calcHudPositions(i, width, height);
            Color color = new Color(0,255,255);
            Color darkColor = color.darker();
            drawRect(
                    (hd.isDisplayLeft() ? hd.getPosX() + 2 : hd.getPosX() -2),
                    (hd.isDisplayTop()  ? hd.getPosY() + 2 : hd.getPosY() -2),
                    (hd.isDisplayLeft() ? hd.getPosX() + (width/2) -1 : hd.getPosX() - (width/2) +1),
                    (hd.isDisplayTop()  ? hd.getPosY() + (height/4)-1 : hd.getPosY() - (height/4)+1),
                    ((i == mouseOverHudPos(hd, i)) ? color.getRGB() : darkColor.getRGB()) + (128 << 24));
            //System.out.println("db: " + i);
        }
    }

    private int lastHudPos = 0;
    private int mouseOverHudPos(HudData hd, int pos)
    {
        if (
                ((hd.isDisplayLeft() ? hd.getPosX() : hd.getPosX() - (width/2)) < mouseX) &&
                ((hd.isDisplayLeft() ? hd.getPosX() + (width/2) : (width)) > mouseX) &&
                ((hd.isDisplayTop()  ? hd.getPosY() : hd.getPosY() - (height/4)) < mouseY) &&
                ((hd.isDisplayTop()  ? hd.getPosY() + (height/4) : hd.getPosY())) > mouseY)
        {
            return lastHudPos = pos;
        }
        return lastHudPos;
    }
    
    private int mouseX;
    private int mouseY;
    
    @Override
    public void handleMouseInput() throws IOException
    {
        mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;    
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        MusicOptionsUtil.setHudOptions(mc.thePlayer, MusicOptionsUtil.isHudDisabled(mc.thePlayer), lastHudPos);
        System.out.println("mouseClicked positionHud: " + lastHudPos);
    }
    
    @Override
    public void onGuiClosed()
    {
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}

}

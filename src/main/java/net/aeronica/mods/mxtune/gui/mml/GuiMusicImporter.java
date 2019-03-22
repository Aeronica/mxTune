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

package net.aeronica.mods.mxtune.gui.mml;

import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.caches.MXTuneFile;
import net.aeronica.mods.mxtune.caches.MXTunePart;
import net.aeronica.mods.mxtune.caches.MXTuneStaff;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiMusicImporter extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiMusicImporter.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private GuiScreen guiScreenParent;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;
    private boolean midiUnavailable;

    private MXTuneFile mxTuneFile = new MXTuneFile();

    protected int entryHeightImportList;
    protected GuiPartList guiPartList;
    protected GuiStaffList guiStaffList;
    private GuiTextField musicTitle;
    private GuiTextField musicAuthor;
    private GuiTextField musicSource;
    private GuiTextField statusText;
    private GuiButton buttonCancel;
    private List<GuiButton> safeButtonList;

    // Cache across screen resizing
    private String cacheMusicTitle;
    private String cacheMusicAuthor;
    private String cacheMusicSource;
    private String cacheStatusText;
    private int cacheSelectedPart;
    private int cacheSelectedStave;

    public GuiMusicImporter(GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        midiUnavailable = MIDISystemUtil.midiUnavailable();
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        this.guiLeft = 0;
        this.guiTop = 0;
        int guiListWidth = (width - 15) / 2;
        int guiTextWidth = width - 10;
        entryHeightImportList = mc.fontRenderer.FONT_HEIGHT + 2;
        int left = 5;
        int titleTop = 20;
        int authorTop = titleTop + entryHeightImportList + 5;
        int sourceTop = authorTop + entryHeightImportList + 5;
        int listTop = sourceTop + entryHeightImportList + 5;
        int listHeight = height - (entryHeightImportList * 3) - 15 - 10 - 30 - 30;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;

        musicTitle = new GuiTextField(0, fontRenderer, left, titleTop, guiTextWidth, entryHeightImportList);
        musicTitle.setMaxStringLength(80);
        musicAuthor = new GuiTextField(1, fontRenderer, left, authorTop, guiTextWidth, entryHeightImportList);
        musicAuthor.setMaxStringLength(80);
        musicSource = new GuiTextField(2, fontRenderer, left, sourceTop, guiTextWidth, entryHeightImportList);
        musicSource.setMaxStringLength(160);
        guiPartList = new GuiPartList(this, guiListWidth, listHeight, listTop, listBottom, left);
        guiStaffList = new GuiStaffList(this, guiListWidth, listHeight, listTop, listBottom, width - guiListWidth - 5);
        statusText = new GuiTextField(3, fontRenderer, left, statusTop, guiTextWidth, entryHeightImportList);
        statusText.setMaxStringLength(100);
        statusText.setTextColor(0xEEEE00);
        statusText.setFocused(false);
        statusText.setEnabled(false);

        int buttonTop = height - 25;
        int xFiles = (width /2) - 75 * 2;
        int xPaste = xFiles + 75;
        int xSaveDone = xPaste + 75;
        int xCancel = xSaveDone + 75;
        GuiButton buttonSaveDone = new GuiButton(0, xSaveDone, buttonTop, 75, 20, I18n.format("mxtune.gui.button.saveDone"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));
        GuiButton buttonFiles = new GuiButton(2, xFiles, buttonTop, 75, 20, I18n.format("mxtune.gui.button.pickFile"));
        GuiButton buttonPaste = new GuiButton(3, xPaste, buttonTop, 75, 20, I18n.format("mxtune.gui.button.pasteMML"));

        buttonList.add(buttonSaveDone);
        buttonList.add(buttonCancel);
        buttonList.add(buttonPaste);
        buttonList.add(buttonFiles);
        safeButtonList = new CopyOnWriteArrayList<>(buttonList);
        reloadState();
        getSelection();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        musicAuthor.setText(cacheMusicAuthor);
        musicSource.setText(cacheMusicSource);
        musicTitle.setText(cacheMusicTitle);
        statusText.setText(cacheStatusText);
        guiPartList.setTuneParts(mxTuneFile.getParts());
        guiPartList.elementClicked(cacheSelectedPart, false);
        guiStaffList.elementClicked(cacheSelectedStave, false);
    }

    private void updateState()
    {
        cacheMusicAuthor = musicAuthor.getText();
        cacheMusicSource = musicSource.getText();
        cacheMusicTitle = musicTitle.getText();
        cacheStatusText = statusText.getText();
        cacheSelectedPart = guiPartList.getSelectedIndex();
        cacheSelectedStave = guiStaffList.getSelectedIndex();
        isStateCached = true;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen()
    {
        musicTitle.updateCursorCounter();
        musicAuthor.updateCursorCounter();
        musicSource.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        String guiTitle;
        if (midiUnavailable)
            guiTitle = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            guiTitle = TITLE;
        /* draw "TITLE" at the top middle */
        int posX = (this.width - mc.fontRenderer.getStringWidth(guiTitle)) / 2 ;
        int posY = 5;
        mc.fontRenderer.drawStringWithShadow(guiTitle, posX, posY, 0xD3D3D3);

        musicTitle.drawTextBox();
        musicAuthor.drawTextBox();
        musicSource.drawTextBox();
        guiPartList.drawScreen(mouseX, mouseY, partialTicks);
        guiStaffList.drawScreen(mouseX, mouseY, partialTicks);
        statusText.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        ModGuiUtils.INSTANCE.drawHooveringButtonHelp(this, safeButtonList, guiLeft, guiTop, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                // Done
                if (!saveFile()) break;
                mc.displayGuiScreen(guiScreenParent);
                break;
            case 1:
                // Cancel
                ActionGet.INSTANCE.clear();
                mc.displayGuiScreen(guiScreenParent);
                break;
            case 2:
                // Get File
                getFile();
                break;
            case 3:
                // Get Paste
                getPaste();
                break;
            default:
        }
        updateState();
        super.actionPerformed(button);
    }

    private boolean saveFile() throws IOException
    {
        boolean result = false;
        String filename = musicTitle.getText().trim();
        filename = filename.replace(".[^\\.]+$", "");
        musicTitle.setText(filename);
        mxTuneFile.setTitle(filename);
        if (!mxTuneFile.getParts().isEmpty())
        {
            mxTuneFile.setAuthor(musicAuthor.getText().trim());
            mxTuneFile.setSource(musicSource.getText().trim());
            NBTTagCompound compound = new NBTTagCompound();
            mxTuneFile.writeToNBT(compound);
            FileHelper.sendCompoundToFile(FileHelper.getCacheFile(FileHelper.CLIENT_LIB_FOLDER, filename + ".dat", Side.CLIENT), compound);
            result = true;
        }
        else
            statusText.setText("Unable to save! No parts, or title is too short.");
        return result;
    }

    private void getFile()
    {
        ActionGet.INSTANCE.setFile();
        ActionGet.INSTANCE.clear();
        mc.displayGuiScreen(new GuiFileSelector(this));
    }

    private void getPaste()
    {
        ActionGet.INSTANCE.setPaste();
        ActionGet.INSTANCE.clear();
        mc.displayGuiScreen(new GuiMusicPaperParse(this));
    }

    private void getSelection()
    {
        List<MXTuneStaff> staves = new ArrayList<>();
        switch (ActionGet.INSTANCE.getSelector())
        {
            case FILE:
                ModLogger.debug("File: %s", ActionGet.INSTANCE.getFileNameString());
                musicTitle.setText(ActionGet.INSTANCE.getFileNameString());
                musicAuthor.setText(ActionGet.INSTANCE.getAuthor());
                musicSource.setText(ActionGet.INSTANCE.getSource());
                mxTuneFile = new MXTuneFile();
                mxTuneFile.applyUserDateTime(true);
                mxTuneFile.setTitle(musicTitle.getText());
                mxTuneFile.setAuthor(musicAuthor.getText());
                mxTuneFile.setSource(musicSource.getText());
                guiPartList.tuneParts = mxTuneFile.getParts();
                guiStaffList.setTuneStaves(staves);

                break;
            case PASTE:
                ModLogger.debug("Paste: %s", ActionGet.INSTANCE.getTitle());
                musicTitle.setText(ActionGet.INSTANCE.getTitle());
                musicAuthor.setText(ActionGet.INSTANCE.getAuthor());
                musicSource.setText(ActionGet.INSTANCE.getAuthor());
                mxTuneFile.setTitle(ActionGet.INSTANCE.getTitle());
                mxTuneFile.setAuthor(ActionGet.INSTANCE.getAuthor());
                mxTuneFile.setSource(ActionGet.INSTANCE.getAuthor());
                mxTuneFile.applyUserDateTime(true);
                staves = new ArrayList<>();
                int i = 0;
                for (String mml : ActionGet.INSTANCE.getMml().replaceAll("MML@|;", "").split(","))
                {
                    staves.add(new MXTuneStaff(i, mml));
                    i++;
                }
                MXTunePart part = new MXTunePart(ActionGet.INSTANCE.getInstrument(), ActionGet.INSTANCE.getSuggestedInstrument(), ActionGet.INSTANCE.getPackedPatch(), staves);
                mxTuneFile.getParts().add(part);
                guiPartList.setTuneParts(mxTuneFile.getParts());

                break;
            case CANCEL:
                break;
            default:
        }
        ActionGet.INSTANCE.cancel();
        updateState();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // capture the ESC key to close cleanly
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.actionPerformed(buttonCancel);
            return;
        }
        /* add char to GuiTextField */
        musicTitle.textboxKeyTyped(typedChar, keyCode);
        musicAuthor.textboxKeyTyped(typedChar, keyCode);
        musicSource.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_TAB)
        {
            if (musicTitle.isFocused())
            {
                musicAuthor.setFocused(true);
                musicSource.setFocused(false);
                musicTitle.setFocused(false);
            }
            else if (musicAuthor.isFocused())
            {
                musicAuthor.setFocused(false);
                musicSource.setFocused(true);
                musicTitle.setFocused(false);
            }
            else if (musicSource.isFocused())
            {
                musicAuthor.setFocused(false);
                musicSource.setFocused(false);
                musicTitle.setFocused(true);
            }
        }
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        guiPartList.handleMouseInput(mouseX, mouseY);
        guiStaffList.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        musicTitle.mouseClicked(mouseX, mouseY, mouseButton);
        musicAuthor.mouseClicked(mouseX, mouseY, mouseButton);
        musicSource.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateState();
    }
}

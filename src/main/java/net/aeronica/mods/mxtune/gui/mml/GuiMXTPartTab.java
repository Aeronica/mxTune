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

import net.aeronica.libs.mml.core.*;
import net.aeronica.mods.mxtune.caches.MXTunePart;
import net.aeronica.mods.mxtune.caches.MXTuneStaff;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class GuiMXTPartTab extends GuiScreen implements IAudioStatusCallback
{
    // Localization Keys
    private static final String HELPER_ENTER_MML = I18n.format("mxtune.gui.musicPaperParse.enterMML");
    private static final String LABEL_INSTRUMENTS = I18n.format("mxtune.gui.musicPaperParse.labelInstruments");
    private static final String LABEL_TITLE_MML = I18n.format("mxtune.gui.guiMXT.labelBulkPasteMML");

    // Layout
    private GuiMXT guiMXT;
    private int top;
    private int bottom;
    private int childHeight;
    private int entryHeight;
    private static final int PADDING = 4;

    // Content
    private MXTunePart mxTunePart = new MXTunePart();
    // FIXME: private GuiMMLBox textMMLPaste;
    private GuiTextField labelStatus;
    private GuiTextField labelMeta;
    private GuiButtonExt buttonPlay;
    private GuiScrollingListOf<ParseErrorEntry> listBoxMMLError;
    private GuiScrollingListOf<Instrument> listBoxInstruments;
    private GuiScrollingListOf<MXTuneStaff> listBoxStaves;

    // MIDI Channel Settings
//    private GuiCheckBox enableVolume;
//    private boolean cachedEnableVolume;
//    private GuiSlider sliderVolume;
//    private double cachedVolume = 100D;
//
//    private GuiCheckBox enablePan;
//    private boolean cachedEnabledPan;
//    private GuiSlider sliderPan;
//    private double cachedPan = 0D;
//
//    private GuiCheckBox enableReverb;
//    private boolean cachedEnabledReverb;
//    private GuiSlider sliderReverb;
//    private double cachedReverb = 0D;
//
//    private GuiCheckBox enableChorus;
//    private boolean cachedEnabledChorus;
//    private GuiSlider sliderChorus;
//    private double cachedChorus = 0D;

    /* MML Staves: Melody, chord 01, chord 02 ... */
    private static final int MAX_MML_LINES = 10;
    private static final int MIN_MML_LINES = 1;
    private static final int MML_LINE_IDX = 200;
    private GuiMMLTextField[] mmlTextLines = new GuiMMLTextField[MAX_MML_LINES];
    private String[] cachedTextLines = new String[MAX_MML_LINES];
    private int[] cachedCursorPos = new int[MAX_MML_LINES];
    private int activeMMLIndex;
    private int cachedActiveMMLIndex;
    private GuiButtonExt buttonAddLine;
    private GuiButtonExt buttonMinusLine;

    /* MML Line limits - allow limiting the viewable lines */
    private int viewableLineCount = MIN_MML_LINES;
    private int cachedViewableLineCount = MIN_MML_LINES;

    /* MML Parser */
    private ParseErrorListener parseErrorListener = new ParseErrorListener();

    /* MML Player */
    private int playId = PlayIdSupplier.PlayType.INVALID;

    /* Instruments */
    private int instListWidth;
    private boolean isPlaying = false;

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private boolean cachedIsPlaying;
    private String cachedMMLText = "";
    private int cachedSelectedInst;

    // Colored Text Helper
    private int helperTextCounter;
    private int helperTextColor;
    private boolean helperState;

    public GuiMXTPartTab(GuiMXT guiMXT)
    {
        this.guiMXT = guiMXT;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        entryHeight = fontRenderer.FONT_HEIGHT + 2;

        Keyboard.enableRepeatEvents(true);

        Arrays.fill(cachedTextLines, "");

        listBoxInstruments = new GuiScrollingListOf<Instrument>(this)
        {
            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                selectInstrument();
                updateState();
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                Instrument instrument = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (instrument != null)
                {
                    String s = fontRenderer.trimStringToWidth(I18n.format(instrument.getName()), listWidth - 10);
                    int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                    fontRenderer.drawString(s, left + 3, slotTop, color);
                }
            }
        };
        listBoxInstruments.addAll(MIDISystemUtil.getInstrumentCacheCopy());

        listBoxMMLError = new GuiScrollingListOf<ParseErrorEntry>(this)
        {
            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                selectError();
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex)
            {
                selectError();
            }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                ParseErrorEntry errorEntry = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (errorEntry != null)
                {
                    String charAt = String.format("%05d", errorEntry.getCharPositionInLine());
                    String formattedErrorEntry = fontRenderer.trimStringToWidth(charAt + ": " + errorEntry.getMsg(), listWidth - 10);
                    fontRenderer.drawString(formattedErrorEntry, this.left + 3, slotTop, 0xFF2222);
                }
            }
        };
    }

    void setLayout(int top, int bottom, int childHeight)
    {
        this.width = guiMXT.width;
        this.height = guiMXT.height;
        this.top = top;
        this.bottom = bottom;
        this.childHeight = childHeight;
    }

    void setPart(MXTunePart mxTunePart)
    {
        clearPart();
        NBTTagCompound compound = new NBTTagCompound();
        mxTunePart.writeToNBT(compound);
        this.mxTunePart = new MXTunePart(compound);
        this.listBoxInstruments.setSelectedIndex(MIDISystemUtil.getInstrumentCachedIndexFromPackedPreset(mxTunePart.getPackedPatch()));
        listBoxInstruments.resetScroll();
        Iterator<MXTuneStaff> iterator = mxTunePart.getStaves().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            mmlTextLines[i].setText(iterator.next().getMml());
            mmlTextLines[i++].setCursorPositionZero();
            if (iterator.hasNext())
                addLine();
        }
        updateState();
        initGui();
    }

    MXTunePart getPart()
    {
        return mxTunePart;
    }

    void updatePart()
    {
        List<MXTuneStaff> staves = new ArrayList<>();
        for (int i = 0; i < viewableLineCount; i++)
        {
            staves.add(new MXTuneStaff(i, mmlTextLines[i].getText()));
        }
        mxTunePart.setStaves(staves);
        selectInstrument();
    }

    public void clearPart()
    {
        this.mxTunePart = new MXTunePart();
        this.listBoxInstruments.setSelectedIndex(-1);
        listBoxInstruments.resetScroll();
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setText(""));
        viewableLineCount = MIN_MML_LINES;
        updateState();
        initGui();
    }

    @Override
    public void updateScreen()
    {
        updateHelperTextCounter();
        for (int i = 0; i < viewableLineCount; i++)
            mmlTextLines[i].updateCursorCounter();
    }

    @Override
    public void initGui()
    {
        buttonList.clear();

        for (Instrument in : listBoxInstruments)
        {
            int stringWidth = fontRenderer.getStringWidth(I18n.format(in.getName()));
            instListWidth = Math.max(instListWidth, stringWidth + 10);
            //instListWidth = Math.max(instListWidth, stringWidth + 5 + entryHeight);
        }
        instListWidth = Math.min(instListWidth, 150);

        // create Instrument selector, and buttons
        buttonPlay = new GuiButtonExt(2, PADDING, bottom - 20, instListWidth, 20, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_part"));
        buttonPlay.enabled = false;
        buttonList.add(buttonPlay);

        int posY = top + 15;
        int coreHeight = Math.max(bottom - posY, 100);
        int statusHeight = entryHeight;
        int pasteErrorHeight = ((coreHeight / 2) / fontRenderer.FONT_HEIGHT) * fontRenderer.FONT_HEIGHT - statusHeight - 10;
        listBoxInstruments.setLayout(entryHeight, instListWidth, Math.max(buttonPlay.y - PADDING - posY, entryHeight), posY, buttonPlay.y - PADDING, PADDING);
        int posX = listBoxInstruments.getRight() + PADDING;


        // Create add/minus line buttons
        buttonAddLine = new GuiButtonExt(0,posX, posY, 20, 20, I18n.format("mxtune.gui.button.plus"));
        buttonList.add(buttonAddLine);
        buttonMinusLine = new GuiButtonExt(1, posX, buttonAddLine.y + buttonAddLine.height + PADDING, 20, 20, I18n.format("mxtune.gui.button.minus"));
        buttonList.add(buttonMinusLine);

        // Create Channel Controls
//        int sliderHeight = fontRenderer.FONT_HEIGHT + PADDING;
//        enableVolume = new GuiCheckBox(20, posX, posY, "", cachedEnableVolume);
//        sliderVolume = new GuiSlider(21, posX + enableVolume.width + 2, posY, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.Volume") + " ", "%", 0, 100, cachedVolume, false, true);
//        buttonList.add(enableVolume);
//        buttonList.add(sliderVolume);
//        enablePan = new GuiCheckBox(22, posX, sliderVolume.y + sliderVolume.height + 2, "", cachedEnabledPan);
//        sliderPan = new GuiSlider(21, posX + enablePan.width + 2, sliderVolume.y + sliderVolume.height + 2, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.pan.left") + " ", " " + I18n.format("mxtune.gui.guiMXT.pan.Right"), -100, 100, cachedPan, false, true);
//        buttonList.add(enablePan);
//        buttonList.add(sliderPan);
//        enableReverb = new GuiCheckBox(22, posX, sliderPan.y + sliderPan.height + 2, "", cachedEnabledReverb);
//        sliderReverb = new GuiSlider(21, posX + enableReverb.width + 2, sliderPan.y + sliderPan.height + 2, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.reverb") + " ", "%", 0, 100, cachedReverb, false, true);
//        buttonList.add(enableReverb);
//        buttonList.add(sliderReverb);
//        enableChorus = new GuiCheckBox(22, posX, sliderReverb.y + sliderReverb.height + 2, "", cachedEnabledChorus);
//        sliderChorus = new GuiSlider(21, posX + enableChorus.width + 2, sliderReverb.y + sliderReverb.height + 2, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.Chorus") + " ", "%", 0, 100, cachedChorus, false, true);
//        buttonList.add(enableChorus);
//        buttonList.add(sliderChorus);

        /* create MML Paste/Edit field */
        posX = buttonMinusLine.x + buttonMinusLine.width + PADDING;
        int rightSideWidth = Math.max(width - posX - PADDING, 100);
//        textMMLPaste = new GuiMMLBox(1, fontRenderer, posX, posY, rightSideWidth, pasteErrorHeight); // FIXME:
//        textMMLPaste.setFocused(false);
//        textMMLPaste.setCanLoseFocus(true);
//        textMMLPaste.setMaxStringLength(10000);

        /* create Status line */
        // FIXME: labelStatus = new GuiTextField(2, fontRenderer, posX, textMMLPaste.yPosition + textMMLPaste.height + PADDING , rightSideWidth, statusHeight);
        labelStatus = new GuiTextField(2, fontRenderer, posX, posY , rightSideWidth, statusHeight);
        labelStatus.setFocused(false);
        labelStatus.setCanLoseFocus(true);
        labelStatus.setEnabled(false);
        labelStatus.setMaxStringLength(80);

        posY = labelStatus.y + labelStatus.height + PADDING;
        for(int i = 0; i < MAX_MML_LINES; i++)
        {
            mmlTextLines[i] = new GuiMMLTextField( i + MML_LINE_IDX, fontRenderer, posX, posY, rightSideWidth, fontRenderer.FONT_HEIGHT + 2);
            mmlTextLines[i].setFocused(false);
            mmlTextLines[i].setCanLoseFocus(true);
            mmlTextLines[i].setMaxStringLength(10000);
            posY += entryHeight + PADDING;
        }

        setLinesLayout(cachedViewableLineCount);
        reloadState();
    }

    private void setLinesLayout(int viewableLines)
    {
        int posX = buttonMinusLine.x + buttonMinusLine.width + PADDING;
        int rightSideWidth = Math.max(width - posX - PADDING, 100);
        GuiMMLTextField mmlTextField = mmlTextLines[viewableLines - 1];
        listBoxMMLError.setLayout(entryHeight, rightSideWidth, Math.max(bottom - mmlTextField.y - mmlTextField.height - PADDING, entryHeight), mmlTextField.y + mmlTextField.height + PADDING, bottom, posX);

    }
    private void reloadState()
    {
        if (!isStateCached) return;
        // FIXME: textMMLPaste.setText(cachedMMLText);
        listBoxInstruments.setSelectedIndex(cachedSelectedInst);
        isPlaying = cachedIsPlaying;
        // FIXME: parseMML(textMMLPaste.getText());
        // FIXME: labelStatus.setText(String.format("[%05d]", textMMLPaste.getCursorPosition()));
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setText(cachedTextLines[i]));
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setCursorPosition(cachedCursorPos[i]));
        viewableLineCount = cachedViewableLineCount;

        updateButtonState();
        listBoxInstruments.resetScroll();
    }

    private void updateState()
    {
//        if (cachedMMLText.length() != textMMLPaste.getText().length()) // FIXME:
//            updatePart();
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedTextLines[i] = mmlTextLines[i].getText());
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedCursorPos[i] = mmlTextLines[i].getCursorPosition());
        cachedViewableLineCount = viewableLineCount;
        cachedSelectedInst = listBoxInstruments.getSelectedIndex();
        cachedIsPlaying = isPlaying;
        // FIXME: labelStatus.setText(String.format("[%05d]", textMMLPaste.getCursorPosition()));

//        cachedVolume = sliderVolume.getValue(); // FIXME:
//        cachedPan = sliderPan.getValue();
//        cachedReverb = sliderReverb.getValue();
//        cachedChorus = sliderChorus.getValue();
//
//        cachedEnableVolume = enableVolume.isChecked();
//        cachedEnabledPan = enablePan.isChecked();
//        cachedEnabledReverb = enableReverb.isChecked();
//        cachedEnabledChorus = enableChorus.isChecked();

        updateButtonState();
        isStateCached = true;
    }

    private void updateButtonState()
    {
        // enable Play button when MML Parsing Field has greater than 0 characters and passes the MML parsing tests
        // FIXME: boolean isOK = (!textMMLPaste.isEmpty()) && (listBoxMMLError.isEmpty());
        boolean isOK = listBoxMMLError.isEmpty();
        buttonPlay.enabled = isPlaying || isOK;
        buttonPlay.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_part");

        buttonAddLine.enabled = viewableLineCount < MAX_MML_LINES;
        buttonMinusLine.enabled = viewableLineCount > MIN_MML_LINES;
        setLinesLayout(viewableLineCount);
    }

    boolean canPlay()
    {
        return buttonPlay.enabled;
    }

    @Override
    public void onGuiClosed()
    {
        stop();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        /* draw Field names */
        // FIXME: int posX = sliderVolume.x + sliderVolume.width + 4;
        int posX = listBoxInstruments.getRight() + 4;
        int posY = top + 2;
        fontRenderer.drawStringWithShadow(LABEL_TITLE_MML, posX, posY, 0xD3D3D3);

        /* draw the instrument list */
        posX = 5;
        fontRenderer.drawStringWithShadow(LABEL_INSTRUMENTS, posX, posY, 0xD3D3D3);

        listBoxInstruments.drawScreen(mouseX, mouseY, partialTicks);
        listBoxMMLError.drawScreen(mouseX, mouseY, partialTicks);
        labelStatus.drawTextBox();

        /* draw the GuiTextField */
        // FIXME: textMMLPaste.drawTextBox();
        for (int i = 0; i < viewableLineCount; i++)
            mmlTextLines[i].drawTextBox();

        /* draw helpers */
//        if (textMMLPaste.isEmpty()) // FIXME:
//        {
//            int helperWidth = fontRenderer.getStringWidth(HELPER_ENTER_MML);
//            int pasteWidth = textMMLPaste.width - 4;
//            int visibleHelperWidth = Math.min(helperWidth, pasteWidth);
//            String helperText = fontRenderer.trimStringToWidth(HELPER_ENTER_MML, visibleHelperWidth);
//            int fontHeight = fontRenderer.FONT_HEIGHT + 2;
//            fontRenderer.drawString(helperText, textMMLPaste.xPosition + 4, textMMLPaste.yPosition + textMMLPaste.height / 2 - fontHeight / 2,
//                                    getHelperTextColor());
//        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateHelperTextCounter()
    {
        ++helperTextCounter;
    }

    private int getHelperTextColor()
    {
        final int LO = 0x30;
        final int HI = 0xD0;

        if (helperTextCounter % 20 == 0)
        {
            helperState = ((helperTextColor <= LO) && !helperState) != helperState;
            helperState = ((helperTextColor >= HI) && helperState) != helperState;
        }
        helperTextColor = (short) (helperState ? Math.min(HI, ++helperTextColor) : Math.max(LO, --helperTextColor));
        int color = helperTextColor;
        color &= 0xFF;
        return (color << 16) + (color << 8) + -color;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        /* if button is disabled ignore click */
        if (!guibutton.enabled) return;

        /* id 0 = okay; 1 = cancel; 2 = play; 3 = stop */
        switch (guibutton.id)
        {
            case 0:
                addLine();
                break;
            case 1:
                minusLine();
                break;
            case 2:
                /* Play MML */
                play();
                break;
            default:
        }
        updateState();
    }

    private void addLine()
    {
        viewableLineCount = (viewableLineCount + 1) > MAX_MML_LINES ? viewableLineCount : viewableLineCount + 1;
        activeMMLIndex = viewableLineCount;
    }

    private void minusLine()
    {
        viewableLineCount = (viewableLineCount - 1) >= MIN_MML_LINES ? viewableLineCount - 1 : viewableLineCount;
    }

    private boolean canAddLine()
    {
        return (viewableLineCount + 1) < MAX_MML_LINES;
    }

    /*
     * Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e).
     *
     * @throws IOException
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            // Let the parent handle the escape kay!
            return;
        }
        /* add char to GuiTextField */
        // FIXME: textMMLPaste.textboxKeyTyped(typedChar, keyCode);
        for (int i = 0; i < viewableLineCount; i++)
            mmlTextLines[i].textboxKeyTyped(typedChar, keyCode);

        listBoxInstruments.keyTyped(typedChar, keyCode);
        // FIXME: parseMML(textMMLPaste.getText());
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        /*
         * A hack is a hack is a hack - Disabling mouse handling on other
         * controls. In this case to ensure a particular control keeps focus
         * while clicking on the error list.
         **/
        if (!listBoxMMLError.isHovering()) super.handleMouseInput();
        listBoxInstruments.handleMouseInput(mouseX, mouseY);
        listBoxMMLError.handleMouseInput(mouseX, mouseY);
        updateState();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int partialTicks) throws IOException
    {
        // FIXME: textMMLPaste.mouseClicked(mouseX, mouseY, partialTicks);
        for (int i = 0; i < viewableLineCount; i++)
            mmlTextLines[i].mouseClicked(mouseX, mouseY, partialTicks);

        super.mouseClicked(mouseX, mouseY, partialTicks);
        updateState();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleKeyboardInput() throws IOException
    {
        super.handleKeyboardInput();
    }

    @Override
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);
    }

    /* MML Parsing */
    private void parseMML(String mml)
    {
        MMLParser parser;

        try
        {
            parser = MMLParserFactory.getMMLParser(mml);
        }
        catch (IOException e)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser() IOException in %s, Error: %s", GuiMXTPartTab.class.getSimpleName(), e);
            listBoxMMLError.clear();
            listBoxMMLError.add(new ParseErrorEntry(0,0, "MMLParserFactory.getMMLParser(mml) is null", null));
            return;
        }
        parser.removeErrorListeners();
        parser.addErrorListener(parseErrorListener);
        parser.setBuildParseTree(true);
        listBoxMMLError.clear();
        parser.test();
        listBoxMMLError.addAll(parseErrorListener.getParseErrorEntries());
    }

    private void selectError()
    {
        ParseErrorEntry parseErrorEntry = listBoxMMLError.get();
        if (parseErrorEntry != null)
        {
            // FIXME: textMMLPaste.setCursorPosition(parseErrorEntry.getCharPositionInLine());
            // FIXME: textMMLPaste.setFocused(true);
        }
        updateState();
    }

    private void selectInstrument()
    {
        int index = listBoxInstruments.getSelectedIndex() >= 0 ? listBoxInstruments.getSelectedIndex() : 0;
        mxTunePart.setPackedPatch(MIDISystemUtil.getPackedPresetFromInstrumentCacheIndex(index));
        mxTunePart.setInstrumentName(I18n.format(listBoxInstruments.get(index).getName()));
    }

    /** Table Flip!
     * Because of the apparent different interpretations of MIDI and
     * SoundFont specifications and the way Sun implemented
     * {@link Instrument}, soundfont loading, etc.:
     * <br/><br/>
     * A soundfont preset bank:0, program:0 for a piano AND
     * a soundfont preset bank:128, program:0 for a standard percussion set
     * produce identical {@link Patch} objects using
     * {@link Patch javax.sound.midi.Instrument.getPatch()} However
     * percussion sets use a different internal class. It uses the Drumkit class.
     * <br/><br/>
     * If you want to manipulate or test values for bank or program settings
     * you must also check for the existence of "Drumkit:" in Instrument#toString.
     */
    @SuppressWarnings("restriction")
    private boolean mmlPlay(String mmlIn)
    {
        String mml = mmlIn;
        int packedPreset;
        Instrument inst = listBoxInstruments.get();
        if (inst != null)
            packedPreset = MMLUtil.instrument2PackedPreset(inst);
        else
            return false;
        
        mml = mml.replace("MML@", "MML@i" + packedPreset);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay() name: %s, bank %05d, program %03d, packed %08d", inst.getName(), inst.getPatch().getBank() >> 7, inst.getPatch().getProgram(), packedPreset);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay(): %s", mml.substring(0, mml.length() >= 25 ? 25 : mml.length()));

        playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
        ClientAudio.playLocal(playId, mml, this);
        return true;
    }

    private void play()
    {
        if (isPlaying)
        {
            stop();
        }
        else
        {
            if (listBoxInstruments.getSelectedIndex() < 0)
                listBoxInstruments.setSelectedIndex(0);

            StringBuilder lines = new StringBuilder();
            for (int i = 0; i < viewableLineCount; i++)
            {
                lines.append(mmlTextLines[i].getText().replaceAll(",", ""));
                if (i < viewableLineCount) lines.append(",");
            }

            String mml = getTextToParse(lines.toString());
            isPlaying = mmlPlay(mml);
        }
    }

    private String getTextToParse(String text)
    {
        /* ArcheAge Semi-Compatibility Adjustments and fixes for stupid MML */
        String copy = text;

        // remove any remaining "MML@" and ";" tokens
        copy = copy.replaceAll("(MML\\@)|;", "");
        StringBuilder sb = new StringBuilder(copy);
        // Add the required MML BEGIN and END tokens
        if (!copy.regionMatches(true, 0, "MML@", 0, 4) && copy.length() > 0)
            sb.insert(0, "MML@");
        if (!copy.endsWith(";") && copy.length() > 0)
            sb.append(";");
        return sb.toString();
    }

    @Override
    public void statusCallBack(Status status, int playId)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (this.playId == playId && (status == Status.ERROR || status == Status.DONE))
            {
                ModLogger.debug("AudioStatus event received: %s, playId: %s", status, playId);
                stop();
            }
        });
    }

    private void stop()
    {
        ClientAudio.queueAudioDataRemoval(playId);
        isPlaying = false;
        playId = PlayIdSupplier.PlayType.INVALID;
        updateState();
    }
}

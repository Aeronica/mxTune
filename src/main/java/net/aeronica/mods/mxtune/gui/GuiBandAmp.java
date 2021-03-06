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

package net.aeronica.mods.mxtune.gui;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.gui.util.GuiButtonMX;
import net.aeronica.mods.mxtune.gui.util.GuiLockButton;
import net.aeronica.mods.mxtune.gui.util.GuiRedstoneButton;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.BandAmpMessage;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.aeronica.mods.mxtune.gui.util.GuiRedstoneButton.ArrowFaces;

@SideOnly(Side.CLIENT)
public class GuiBandAmp extends GuiContainer
{
    public static final ResourceLocation BG_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/band_amp.png");
    private InventoryPlayer inventoryPlayer;
    private TileBandAmp tileBandAmp;
    private GuiLockButton lockButton;
    private GuiRedstoneButton rearInputButton;
    private GuiRedstoneButton leftOutputButton;
    private GuiRedstoneButton rightOutputButton;
    private GuiButtonMX soundRangeButton;
    private boolean prevLockState;
    private boolean prevRearInputButtonState;
    private boolean prevLeftOutputButtonState;
    private boolean prevRightOutputButtonState;
    private SoundRange soundRange;
    private SoundRange prevSoundRange;

    public GuiBandAmp(Container container, InventoryPlayer inventoryPlayer, TileBandAmp tileBandAmp)
    {
        super(container);
        this.inventoryPlayer = inventoryPlayer;
        this.tileBandAmp = tileBandAmp;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        boolean isEnabled = LockableHelper.canLock(mc.player, tileBandAmp);
        soundRange = tileBandAmp.getSoundRange();
        prevSoundRange = soundRange;

        lockButton =  new GuiLockButton(100, guiLeft + 16, guiTop + 25);
        lockButton.addHooverTexts(I18n.format("mxtune.gui.bandAmp.lockButton.help01"));
        lockButton.addHooverTexts(TextFormatting.GREEN + I18n.format("mxtune.gui.bandAmp.lockButton.help02"));
        lockButton.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.bandAmp.lockButton.help03"));
        buttonList.add(lockButton);
        lockButton.setLocked(tileBandAmp.isLocked());
        lockButton.enabled = isEnabled;
        prevLockState = lockButton.isLocked();

        rearInputButton = new GuiRedstoneButton(101, guiLeft + 139, guiTop + 25, ArrowFaces.DOWN);
        rearInputButton.addHooverTexts(I18n.format("mxtune.gui.bandAmp.rearInputButton.help01"));
        rearInputButton.addHooverTexts(TextFormatting.GREEN + I18n.format("mxtune.gui.bandAmp.rearInputButton.help02"));
        rearInputButton.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.bandAmp.rearInputButton.help03"));
        buttonList.add(rearInputButton);
        rearInputButton.setSignalEnabled(tileBandAmp.isRearRedstoneInputEnabled());
        rearInputButton.enabled = isEnabled;

        prevRearInputButtonState = rearInputButton.isSignalEnabled();

        leftOutputButton = new GuiRedstoneButton(102, guiLeft + 129, guiTop + 45, ArrowFaces.LEFT);
        leftOutputButton.addHooverTexts(I18n.format("mxtune.gui.bandAmp.leftOutputButton.help01"));
        leftOutputButton.addHooverTexts(TextFormatting.GREEN + I18n.format("mxtune.gui.bandAmp.leftOutputButton.help02"));
        leftOutputButton.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.bandAmp.leftOutputButton.help03"));
        buttonList.add(leftOutputButton);
        leftOutputButton.setSignalEnabled(tileBandAmp.isLeftRedstoneOutputEnabled());
        leftOutputButton.enabled = isEnabled;
        prevLeftOutputButtonState = leftOutputButton.isSignalEnabled();

        rightOutputButton = new GuiRedstoneButton(103, guiLeft + 149, guiTop + 45, ArrowFaces.RIGHT);
        rightOutputButton.addHooverTexts(I18n.format("mxtune.gui.bandAmp.rightOutputButton.help01"));
        rightOutputButton.addHooverTexts(TextFormatting.GREEN + I18n.format("mxtune.gui.bandAmp.rightOutputButton.help02"));
        rightOutputButton.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.bandAmp.rightOutputButton.help03"));
        buttonList.add(rightOutputButton);
        rightOutputButton.setSignalEnabled(tileBandAmp.isRightRedstoneOutputEnabled());
        rightOutputButton.enabled = isEnabled;
        prevRightOutputButtonState = rightOutputButton.isSignalEnabled();

        soundRangeButton = new GuiButtonMX(104, guiLeft +6 , guiTop + 45, 40, 20, "");
        soundRangeButton.displayString = I18n.format(soundRange.getLanguageKey());
        soundRangeButton.addHooverTexts(I18n.format("mxtune.gui.bandAmp.soundRangeButton.help01"));
        soundRangeButton.addHooverTexts(TextFormatting.GREEN + I18n.format("mxtune.gui.bandAmp.soundRangeButton.help02"));
        soundRangeButton.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.bandAmp.soundRangeButton.help03"));

        buttonList.add(soundRangeButton);
        soundRangeButton.enabled = isEnabled;
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        sendButtonChanges();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        toggleLockButton(lockButton, button);
        toggleRedstoneButton(rearInputButton, button);
        toggleRedstoneButton(leftOutputButton, button);
        toggleRedstoneButton(rightOutputButton, button);
        nextRangeButton(soundRangeButton, button);
    }

    private void toggleLockButton(GuiLockButton lockIconButton, GuiButton buttonClicked)
    {
        if (buttonClicked.id == lockIconButton.id)
        {
            boolean invertButton = !lockIconButton.isLocked();
            lockIconButton.setLocked(invertButton);
            sendButtonChanges();
        }
    }

    private void toggleRedstoneButton(GuiRedstoneButton guiRedstoneButton, GuiButton buttonClicked)
    {
        if (buttonClicked.id == guiRedstoneButton.id)
        {
            boolean invertButton = !guiRedstoneButton.isSignalEnabled();
            guiRedstoneButton.setSignalEnabled(invertButton);
            sendButtonChanges();
        }
    }

    private void nextRangeButton(GuiButton guiRangeButton, GuiButton buttonClicked)
    {
        if(buttonClicked.id == guiRangeButton.id)
        {
            soundRange = SoundRange.nextRange(soundRange);
            sendButtonChanges();
        }
    }

    private void updateButtonStatus()
    {
        boolean isOwnerManaged = LockableHelper.canLock(mc.player, tileBandAmp);
        String ownerText = isOwnerManaged ? "" : I18n.format("mxtune.gui.button.ownerManaged");

        updateLockButtonStatus();
        boolean state = tileBandAmp.isRearRedstoneInputEnabled();

        lockButton.setStatusText(TextFormatting.AQUA + (prevLockState
                                                        ? I18n.format("mxtune.gui.bandAmp.lockButton.locked")
                                                        : I18n.format("mxtune.gui.bandAmp.lockButton.unLocked")
                                                        + ownerText));

        prevRearInputButtonState = updateButtonStatus(rearInputButton, state, prevRearInputButtonState);
        rearInputButton.setStatusText(TextFormatting.AQUA + (rearInputButton.isSignalEnabled()
                                              ? I18n.format("mxtune.gui.bandAmp.rearInputButton.enabled")
                                              : I18n.format("mxtune.gui.bandAmp.rearInputButton.disabled"))
                                              + ownerText);

        state = tileBandAmp.isLeftRedstoneOutputEnabled();
        prevLeftOutputButtonState = updateButtonStatus(leftOutputButton, state, prevLeftOutputButtonState);
        leftOutputButton.setStatusText(TextFormatting.AQUA + (leftOutputButton.isSignalEnabled()
                                               ? I18n.format("mxtune.gui.bandAmp.leftOutputButton.enabled")
                                               : I18n.format("mxtune.gui.bandAmp.leftOutputButton.disabled"))
                                               + ownerText);

        state = tileBandAmp.isRightRedstoneOutputEnabled();
        prevRightOutputButtonState = updateButtonStatus(rightOutputButton, state, prevRightOutputButtonState);
        rightOutputButton.setStatusText(TextFormatting.AQUA + (rightOutputButton.isSignalEnabled()
                                                ? I18n.format("mxtune.gui.bandAmp.rightOutputButton.enabled")
                                                : I18n.format("mxtune.gui.bandAmp.rightOutputButton.disabled"))
                                                + ownerText);

        soundRangeButton.setStatusText(TextFormatting.AQUA + soundRangeButton.displayString
                                                + ownerText);
        updateSoundRangeButton();
    }

    private void updateLockButtonStatus()
    {
        boolean lockState = this.tileBandAmp.isLocked();
        if (prevLockState != lockState)
        {
            this.lockButton.setLocked(lockState);
            this.prevLockState = lockState;
        }
    }

    private void updateSoundRangeButton()
    {
        soundRange = tileBandAmp.getSoundRange();
        if(prevSoundRange != soundRange)
        {
            soundRangeButton.displayString = I18n.format(soundRange.getLanguageKey());
            prevSoundRange = soundRange;
        }
    }

    private boolean updateButtonStatus(GuiRedstoneButton button, boolean buttonState, boolean prevState)
    {
        if (prevState != buttonState)
            button.setSignalEnabled(buttonState);

        return buttonState;
    }

    private void sendButtonChanges()
    {
        PacketDispatcher.sendToServer(new BandAmpMessage(
                tileBandAmp.getPos(), lockButton.isLocked(),
                rearInputButton.isSignalEnabled(),
                leftOutputButton.isSignalEnabled(),
                rightOutputButton.isSignalEnabled(), soundRange));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        updateButtonStatus();
        String name = I18n.format(tileBandAmp.getName());
        fontRenderer.drawString(name, 8, 6, 0x404040);
        String duration = SheetMusicUtil.formatDuration(tileBandAmp.getDuration());
        int durationLength = fontRenderer.getStringWidth(duration) + 8;
        fontRenderer.drawString(duration, xSize - durationLength, 6, 0x404040);
        fontRenderer.drawString(inventoryPlayer.getDisplayName().getUnformattedText(), 8, ySize - 94, 0x404040);
        String helpText = I18n.format("mxtune.gui.bandAmp.sneakHelp");
        int helpLength = fontRenderer.getStringWidth(helpText) + 8;
        fontRenderer.drawString(helpText, xSize - helpLength, ySize - 94, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(BG_TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        ModGuiUtils.INSTANCE.drawHooveringHelp(this, buttonList, guiLeft, guiTop, mouseX, mouseY);
    }
}

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

import net.aeronica.mods.mxtune.gui.hud.GuiHudAdjust;
import net.aeronica.mods.mxtune.gui.hud.HudData;
import net.aeronica.mods.mxtune.gui.hud.HudDataFactory;
import net.aeronica.mods.mxtune.items.ItemStaffOfMusic;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.ClientPlayManager;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.DebugRendererChunkBorder;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static net.aeronica.mods.mxtune.gui.hud.GuiJamOverlay.HOT_BAR_CLEARANCE;

public class GuiStaffOverlay extends Gui
{
    private Minecraft mc;
    private FontRenderer fontRenderer;
    private float partialTicks;
    private int width;
    private int height;
    private DebugRenderer.IDebugRenderer chunkBorder;
    private boolean holdingStaffOfMusic;


    private static class GuiStaffOverlayHolder { private static final GuiStaffOverlay INSTANCE = new GuiStaffOverlay(); }
    public static GuiStaffOverlay getInstance() { return GuiStaffOverlayHolder.INSTANCE; }

    private GuiStaffOverlay()
    {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = this.mc.fontRenderer;
        this.chunkBorder = new DebugRendererChunkBorder(mc);

    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEvent(RenderGameOverlayEvent.Post event)
    {
        if (mc.gameSettings.showDebugInfo) return;
        RenderGameOverlayEvent.ElementType elementType = event.getType();

        if (event.isCancelable() || (elementType != RenderGameOverlayEvent.ElementType.EXPERIENCE && elementType != RenderGameOverlayEvent.ElementType.TEXT))
            return;

        EntityPlayerSP playerSP = this.mc.player;
        width = event.getResolution().getScaledWidth();
        height = event.getResolution().getScaledHeight() - HOT_BAR_CLEARANCE;
        partialTicks = event.getPartialTicks();
        HudData hudData = HudDataFactory.calcHudPositions(0, width, height);

        holdingStaffOfMusic = playerSP.getHeldItemMainhand().getItem() instanceof ItemStaffOfMusic;

        if (holdingStaffOfMusic)
        {
            renderHud(hudData, playerSP, elementType);
        }
    }

    @SubscribeEvent
    public void onEvent(RenderWorldLastEvent event)
    {
        if (holdingStaffOfMusic && !mc.gameSettings.showDebugInfo)
            chunkBorder.render(event.getPartialTicks(), 0);
    }

    private boolean inGuiHudAdjust() {return (mc.currentScreen instanceof GuiHudAdjust);}

    public void setTexture(ResourceLocation texture) { this.mc.renderEngine.bindTexture(texture);}

    private void renderHud(HudData hd, EntityPlayerSP playerSP, RenderGameOverlayEvent.ElementType elementType)
    {
        int maxWidth = width - 4;
        int maxHeight = (fontRenderer.FONT_HEIGHT + 2) * 6;

       // String musicTitle = getMusicTitle(sheetMusic);

        GL11.glPushMatrix();
        GL11.glTranslatef(hd.getPosX(), hd.getPosY(), 0F);
        if (elementType == RenderGameOverlayEvent.ElementType.TEXT)
            drawAreaInfo(hd, maxWidth, maxHeight);

        int iconX = hd.quadX(maxWidth, 0, 2, maxWidth);
        int iconY = hd.quadY(maxHeight, 0, 2, maxHeight);

        //setTexture(TEXTURE_STATUS);
        if (elementType == RenderGameOverlayEvent.ElementType.EXPERIENCE)
            drawBox(iconX, iconY, maxWidth, maxHeight);

        GL11.glPopMatrix();
    }

    private void drawAreaInfo(HudData hd, int maxWidth, int maxHeight)
    {
        int y = 0;
        int fontHeight = fontRenderer.FONT_HEIGHT + 2;
        GUID chunkAreaGuid = ClientPlayManager.getCurrentAreaGUID();
        Area area = ClientFileManager.getArea(chunkAreaGuid);
        Area selectedArea = ClientFileManager.getArea(MusicOptionsUtil.getSelectedAreaGuid(mc.player));

        String areaName = area != null ? area.getName() : I18n.format("mxtune.error.undefined_area");
        String formattedText = I18n.format("mxtune.gui.guiStaffOverlay.area_name", areaName);
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight);

        y += fontHeight;
        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.help");
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xAAAAAA);

        y += fontHeight;
        formattedText = ClientPlayManager.getLastSongLine01();
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFFFF00);

        y += fontHeight;
        formattedText = ClientPlayManager.getLastSongLine02();
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFFFF00);

        y += fontHeight;
        formattedText = ClientPlayManager.getLastSongLine03();
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFFFF00);

        y += fontHeight;
        areaName = selectedArea != null ? selectedArea.getName() : I18n.format("mxtune.info.null");
        formattedText = I18n.format("mxtune.gui.guiStaffOverlay.selected_area_to_apply",areaName);
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0x00EE00);
    }

    private void renderLine(String formattedText, int y, HudData hd, int maxWidth, int maxHeight, int fontHeight)
    {
        renderLine(formattedText, y, hd, maxWidth, maxHeight, fontHeight, 0xFFFFFF);
    }
    private void renderLine(String formattedText, int y, HudData hd, int maxWidth, int maxHeight, int fontHeight, int fontColor)
    {
        String trimmedText = fontRenderer.trimStringToWidth(formattedText, maxWidth);
        int qX = hd.quadX(maxWidth, 0, 4, maxWidth - 4);
        int qY = hd.quadY(maxHeight, y, 4, fontHeight);
        fontRenderer.drawStringWithShadow(trimmedText, qX, qY, fontColor);
    }

    private void drawBox(int x, int y, int width, int height) {
        drawRect(x - 2, y - 2, x + width + 2, y + height + 2, 0x88222222);
        drawRect(x, y, x + width, y + height,0xCC444444);
    }
}

/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * 2019-Apr-09, Paul Boese a.k.s. Aeronica / Rymor
 *
 * Changed the following to protected fields:
 *   scrollDistance, highlightSelected
 *
 * Renamed and made protected for children of this class to use
 *   was: public func_27258_a(boolean p_27258_1_)
 *   now: protected void setHighlightSelected(boolean state)
 *
 * Removed:
 *   boolean captureMouse
 */
package net.aeronica.mods.mxtune.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public abstract class GuiScrollingListMX
{
    private final Minecraft client;
    protected int listWidth;
    protected int listHeight;
    protected int screenWidth;
    protected int screenHeight;
    protected int top;
    protected int bottom;
    protected int right;
    protected int left;
    protected int slotHeight;
    private int scrollUpActionId;
    private int scrollDownActionId;
    protected int mouseX;
    protected int mouseY;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    protected float scrollDistance;
    protected int selectedIndex = -1;
    private long lastClickTime = 0L;
    protected boolean highlightSelected = true;
    private boolean hasHeader;
    private int headerHeight;

    /**
     * Deprecated since we need to know screen size.
     * @param client Minecraft
     * @param width scroll list width
     * @param height scroll list height
     * @param top of the scroll area
     * @param bottom of the scroll area
     * @param left side of the scroll area
     * @param entryHeight of the displayed data
     */
    @Deprecated // We need to know screen size.
    public GuiScrollingListMX(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight)
    {
       this(client, width, height, top, bottom, left, entryHeight, width, height);
    }
    public GuiScrollingListMX(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight)
    {
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public GuiScrollingListMX(Minecraft client)
    {
        this.client = client;
    }

    public void setLayout(int entryHeight, int width, int height, int top, int bottom, int left, int screenWidth, int screenHeight)
    {
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    protected void setHighlightSelected(boolean state)
    {
        this.highlightSelected = state;
    }

    @Deprecated protected void func_27259_a(boolean hasFooter, int footerHeight){ setHeaderInfo(hasFooter, footerHeight); }
    protected void setHeaderInfo(boolean hasHeader, int headerHeight)
    {
        this.hasHeader = hasHeader;
        this.headerHeight = headerHeight;
        if (!hasHeader) this.headerHeight = 0;
    }

    public int getRight() {return right;}

    public int getLeft() {return left;}

    public int getTop() {return top;}

    public int getBottom() {return bottom;}

    protected abstract int getSize();

    protected abstract void elementClicked(int index, boolean doubleClick);

    public abstract boolean isSelected(int index);

    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.headerHeight;
    }

    public boolean isHovering()
    {
        return mouseX >= left && mouseX <= left + listWidth && mouseY >= top && mouseY <= bottom && getSize() > 0;
    }

    protected abstract void drawBackground();

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected abstract void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess);

    @Deprecated protected void func_27260_a(int entryRight, int relativeY, Tessellator tess) {}
    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess) { func_27260_a(entryRight, relativeY, tess); }

    @Deprecated protected void func_27255_a(int x, int y) {}
    protected void clickHeader(int x, int y) { func_27255_a(x, y); }

    @Deprecated protected void func_27257_b(int mouseX, int mouseY) {}
    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawScreen(int mouseX, int mouseY) { func_27257_b(mouseX, mouseY); }

    @Deprecated // Unused, Remove in 1.9.3?
    public int func_27256_c(int x, int y)
    {
        int left = this.left + 1;
        int right = this.left + this.listWidth - 7;
        int relativeY = y - this.top - this.headerHeight + (int)this.scrollDistance - 4;
        int entryIndex = relativeY / this.slotHeight;
        return x >= left && x <= right && entryIndex >= 0 && relativeY >= 0 && entryIndex < this.getSize() ? entryIndex : -1;
    }

    // FIXME: is this correct/still needed?
    public void registerScrollButtons(List<GuiButton> buttons, int upActionID, int downActionID)
    {
        this.scrollUpActionId = upActionID;
        this.scrollDownActionId = downActionID;
    }

    protected void applyScrollLimits()
    {
        int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);

        if (listHeight < 0)
        {
            listHeight = 0;
        }

        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > (float)listHeight)
        {
            this.scrollDistance = (float)listHeight;
        }
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == this.scrollUpActionId)
            {
                this.scrollDistance -= (float) (this.slotHeight * 2 / (double) 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
            else if (button.id == this.scrollDownActionId)
            {
                this.scrollDistance += (float) (this.slotHeight * 2 / (double) 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
        }
    }

    public void handleMouseInput(int mouseX, int mouseY) throws IOException
    {
        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
                             mouseY >= this.top && mouseY <= this.bottom;
        if (!isHovering)
            return;

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0)
        {
            this.scrollDistance += (float)((-1 * scroll / 120.0F) * this.slotHeight / 2);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (bottom - top < 0) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();

        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
                             mouseY >= this.top && mouseY <= this.bottom;
        int listLength     = this.getSize();
        int scrollBarWidth = 6;
        int scrollBarRight = this.left + this.listWidth;
        int scrollBarLeft  = scrollBarRight - scrollBarWidth;
        int entryLeft      = this.left;
        int entryRight     = scrollBarLeft - 1;
        int viewHeight     = this.bottom - this.top;
        int border         = 4;

        if (Mouse.isButtonDown(0))
        {
            if (this.initialMouseClickY == -1.0F)
            {
                if (isHovering)
                {
                    int mouseListY = mouseY - this.top - this.headerHeight + (int)this.scrollDistance - border;
                    int slotIndex = mouseListY / this.slotHeight;

                    if (mouseX >= entryLeft && mouseX <= entryRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength)
                    {
                        this.elementClicked(slotIndex, slotIndex == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
                        this.selectedIndex = slotIndex;
                        this.lastClickTime = System.currentTimeMillis();
                    }
                    else if (mouseX >= entryLeft && mouseX <= entryRight && mouseListY < 0)
                    {
                        this.clickHeader(mouseX - entryLeft, mouseY - this.top + (int)this.scrollDistance - border);
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight)
                    {
                        this.scrollFactor = -1.0F;
                        int scrollHeight = this.getContentHeight() - viewHeight - border;
                        if (scrollHeight < 1) scrollHeight = 1;

                        int var13 = (int)((float)(viewHeight * viewHeight) / (float)this.getContentHeight());

                        if (var13 < 32) var13 = 32;
                        if (var13 > viewHeight - border*2)
                            var13 = viewHeight - border*2;

                        this.scrollFactor /= (float)(viewHeight - var13) / (float)scrollHeight;
                    }
                    else
                    {
                        this.scrollFactor = 1.0F;
                    }

                    this.initialMouseClickY = mouseY;
                }
                else
                {
                    this.initialMouseClickY = -2.0F;
                }
            }
            else if (this.initialMouseClickY >= 0.0F)
            {
                this.scrollDistance -= ((float)mouseY - this.initialMouseClickY) * this.scrollFactor;
                this.initialMouseClickY = (float)mouseY;
            }
        }
        else
        {
            this.initialMouseClickY = -1.0F;
        }

        this.applyScrollLimits();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();

        ScaledResolution res = new ScaledResolution(client);
        double scaleW = client.displayWidth / res.getScaledWidth_double();
        double scaleH = client.displayHeight / res.getScaledHeight_double();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left      * scaleW), (int)(client.displayHeight - (bottom * scaleH)),
                       (int)(listWidth * scaleW), (int)(viewHeight * scaleH));

        if (this.client.world != null)
        {
            this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
        }
        else // Draw dark dirt background
        {
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            this.client.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            final float scale = 32.0F;
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(this.left,  this.bottom, 0.0D).tex(this.left  / scale, (this.bottom + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            worldr.pos(this.right, this.bottom, 0.0D).tex(this.right / scale, (this.bottom + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            worldr.pos(this.right, this.top,    0.0D).tex(this.right / scale, (this.top    + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            worldr.pos(this.left,  this.top,    0.0D).tex(this.left  / scale, (this.top    + (int)this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
            tess.draw();
        }

        int baseY = this.top + border - (int)this.scrollDistance;

        if (this.hasHeader) {
            this.drawHeader(entryRight, baseY, tess);
        }

        for (int slotIdx = 0; slotIdx < listLength; ++slotIdx)
        {
            int slotTop = baseY + slotIdx * this.slotHeight + this.headerHeight;
            int slotBuffer = this.slotHeight - border;

            if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top)
            {
                if (this.highlightSelected && this.isSelected(slotIdx))
                {
                    int min = this.left;
                    int max = entryRight;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableTexture2D();
                    worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldr.pos(min,     slotTop + slotBuffer + (double) 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max,     slotTop + slotBuffer + (double) 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max,     slotTop              - (double) 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min,     slotTop              - (double) 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min + (double) 1, slotTop + slotBuffer + (double) 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(max - (double) 1, slotTop + slotBuffer + (double) 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(max - (double) 1, slotTop              - (double) 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(min + (double) 1, slotTop              - (double) 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    tess.draw();
                    GlStateManager.enableTexture2D();
                }

                this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess);
            }
        }

        GlStateManager.disableDepth();

        int extraHeight = (this.getContentHeight() + border) - viewHeight;
        if (extraHeight > 0)
        {
            int height;
            // NPE prevention
            if (this.getContentHeight() != 0)
                height = (viewHeight * viewHeight) / this.getContentHeight();
            else
                height = 0;

            if (height < 32) height = 32;

            if (height > viewHeight - border*2)
                height = viewHeight - border*2;

            int barTop = (int)this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
            if (barTop < this.top)
            {
                barTop = this.top;
            }

            GlStateManager.disableTexture2D();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,  this.bottom, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.top,    0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,  this.top,    0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,  barTop + (double) height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight, barTop + (double) height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight, barTop,          0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,  barTop,          0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,      barTop + height - (double) 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarRight - (double) 1, barTop + height - (double) 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarRight - (double) 1, barTop,              0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,      barTop,              0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.draw();
        }

        this.drawScreen(mouseX, mouseY);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2)
    {
        GuiUtils.drawGradientRect(0, left, top, right, bottom, color1, color2);
    }
}
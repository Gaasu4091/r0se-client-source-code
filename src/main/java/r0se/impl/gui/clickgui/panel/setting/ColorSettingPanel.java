/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  org.joml.Matrix4f
 */
package r0se.impl.gui.clickgui.panel.setting;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import r0se.api.render.ColorUtil;
import r0se.api.settings.ColorSetting;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class ColorSettingPanel
extends BasePanel {
    private static final int HEADER_HEIGHT = 13;
    private static final int PICKER_SIZE = 94;
    private static final int BAR_HEIGHT = 7;
    private static final int BUTTON_HEIGHT = 12;
    private static final int PADDING = 3;
    private static final int SPACING = 3;
    private static final int BUTTON_SPACING = 2;
    private static final int PREVIEW_SIZE = 9;
    private final ColorSetting setting;
    private boolean draggingPicker;
    private boolean draggingHue;
    private boolean draggingAlpha;

    public ColorSettingPanel(ColorSetting setting) {
        this.setting = setting;
        this.height = 13;
    }

    @Override
    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        Color textColor = this.getTextColor();
        Objects.requireNonNull(font);
        int textY = this.y + (13 - 9) / 2 + 1;
        int textX = this.x + 2;
        context.drawText(font, this.getName(), textX, textY, ColorSettingPanel.rgba(textColor), true);
        Color preview = Managers.COLORS.resolve(this.setting);
        int previewX = this.x + this.width - 3 - 9;
        int previewY = this.y + 2;
        context.fill(previewX - 1, previewY - 1, previewX + 9 + 1, previewY + 9 + 1, ColorUtil.rgba(28, 28, 34, 255));
        context.fill(previewX, previewY, previewX + 9, previewY + 9, ColorSettingPanel.rgba(preview));
        if (!this.expanded) {
            return;
        }
        int pickerSize = this.getPickerSize();
        int contentX = this.x + (this.width - pickerSize) / 2;
        int pickerY = this.y + 13 + 3;
        this.renderPicker(context, contentX, pickerY, pickerSize, pickerSize);
        int hueY = pickerY + pickerSize + 3;
        this.renderHueBar(context, contentX, hueY, pickerSize, 7);
        int alphaY = hueY + 7 + 3;
        this.renderAlphaBar(context, contentX, alphaY, pickerSize, 7);
        int buttonY = alphaY + 7 + 3;
        this.renderButtons(context, font, contentX, buttonY, pickerSize, 12, mouseX, mouseY);
    }

    @Override
    public int getFullHeight() {
        if (!this.expanded) {
            return 13;
        }
        int pickerSize = this.getPickerSize();
        return 16 + pickerSize + 3 + 7 + 3 + 7 + 3 + 12 + 3;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        int totalHeight = this.expanded ? this.getFullHeight() : 13;
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + totalHeight;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int syncX;
        if (!this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (mouseY >= this.y && mouseY <= this.y + 13) {
            if (button == 0 || button == 1) {
                this.expanded = !this.expanded;
                return true;
            }
            return false;
        }
        if (!this.expanded) {
            return false;
        }
        int pickerSize = this.getPickerSize();
        int contentX = this.x + (this.width - pickerSize) / 2;
        int pickerY = this.y + 13 + 3;
        int hueY = pickerY + pickerSize + 3;
        int alphaY = hueY + 7 + 3;
        int buttonY = alphaY + 7 + 3;
        if (ColorSettingPanel.inside(mouseX, mouseY, contentX, pickerY, pickerSize, pickerSize)) {
            this.draggingPicker = true;
            this.updatePicker(mouseX, mouseY, contentX, pickerY, pickerSize, pickerSize);
            return true;
        }
        if (ColorSettingPanel.inside(mouseX, mouseY, contentX, hueY, pickerSize, 7)) {
            this.draggingHue = true;
            this.updateHue(mouseX, contentX, pickerSize);
            return true;
        }
        if (ColorSettingPanel.inside(mouseX, mouseY, contentX, alphaY, pickerSize, 7)) {
            this.draggingAlpha = true;
            this.updateAlpha(mouseX, contentX, pickerSize);
            return true;
        }
        int buttonCount = this.setting.canSync() ? 3 : 2;
        int totalSpacing = 2 * (buttonCount - 1);
        int buttonWidth = (pickerSize - totalSpacing) / buttonCount;
        if (ColorSettingPanel.inside(mouseX, mouseY, contentX, buttonY, buttonWidth, 12)) {
            this.setting.copy();
            return true;
        }
        if (ColorSettingPanel.inside(mouseX, mouseY, contentX + buttonWidth + 2, buttonY, buttonWidth, 12)) {
            this.setting.paste();
            return true;
        }
        if (this.setting.canSync() && ColorSettingPanel.inside(mouseX, mouseY, syncX = contentX + (buttonWidth + 2) * 2, buttonY, buttonWidth, 12)) {
            this.setting.toggleSync();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.draggingPicker = false;
        this.draggingHue = false;
        this.draggingAlpha = false;
        return false;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (!this.expanded || button != 0) {
            return;
        }
        int pickerSize = this.getPickerSize();
        int contentX = this.x + (this.width - pickerSize) / 2;
        int pickerY = this.y + 13 + 3;
        int hueY = pickerY + pickerSize + 3;
        int alphaY = hueY + 7 + 3;
        if (this.draggingPicker) {
            this.updatePicker(mouseX, mouseY, contentX, pickerY, pickerSize, pickerSize);
        } else if (this.draggingHue) {
            this.updateHue(mouseX, contentX, pickerSize);
        } else if (this.draggingAlpha) {
            this.updateAlpha(mouseX, contentX, pickerSize);
        }
    }

    @Override
    protected String getName() {
        return this.setting.getName();
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    protected Colors getColorFeature() {
        return Managers.MODULES.getFeature(Colors.class);
    }

    @Override
    protected ClickGui getClickGuiFeature() {
        return Managers.MODULES.getFeature(ClickGui.class);
    }

    private int getPickerSize() {
        return Math.max(24, Math.min(94, this.width - 6));
    }

    private void renderPicker(DrawContext context, int x, int y, int width, int height) {
        float[] hsb = ColorSettingPanel.toHsb((Color)this.setting.getValue());
        Color hueColor = Color.getHSBColor(hsb[0], 1.0f, 1.0f);
        this.drawGradientRect(context, x, y, width, height, ColorUtil.rgba(255, 255, 255, 255), ColorUtil.rgba(hueColor.getRed(), hueColor.getGreen(), hueColor.getBlue(), 255), ColorUtil.rgba(90, 90, 90, 255), ColorUtil.rgba(hueColor.getRed() / 3, hueColor.getGreen() / 3, hueColor.getBlue() / 3, 255));
        this.drawGradientRect(context, x, y, width, height, ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 210), ColorUtil.rgba(0, 0, 0, 210));
        this.outline(context, x, y, width, height, ColorUtil.rgba(180, 180, 190, 255));
        int selectorX = x + Math.round(hsb[1] * (float)(width - 1));
        int selectorY = y + Math.round((1.0f - hsb[2]) * (float)(height - 1));
        context.fill(selectorX - 1, selectorY - 1, selectorX + 2, selectorY + 2, ColorUtil.rgba(210, 190, 230, 255));
    }

    private void renderHueBar(DrawContext context, int x, int y, int width, int height) {
        int segments = 6;
        float segmentWidth = (float)width / (float)segments;
        for (int i = 0; i < segments; ++i) {
            float startHue = (float)i / (float)segments;
            float endHue = (float)(i + 1) / (float)segments;
            int segmentX = x + Math.round((float)i * segmentWidth);
            int nextX = i == segments - 1 ? x + width : x + Math.round((float)(i + 1) * segmentWidth);
            Color start = Color.getHSBColor(startHue, 1.0f, 1.0f);
            Color end = Color.getHSBColor(endHue, 1.0f, 1.0f);
            this.drawGradientRect(context, segmentX, y, nextX - segmentX, height, ColorSettingPanel.rgba(start), ColorSettingPanel.rgba(end), ColorSettingPanel.rgba(start), ColorSettingPanel.rgba(end));
        }
        this.outline(context, x, y, width, height, ColorUtil.rgba(180, 180, 190, 255));
        float[] hsb = ColorSettingPanel.toHsb((Color)this.setting.getValue());
        int selectorX = x + Math.round(hsb[0] * (float)(width - 1));
        context.fill(selectorX - 1, y - 1, selectorX + 1, y + height + 1, ColorUtil.rgba(255, 255, 255, 255));
    }

    private void renderAlphaBar(DrawContext context, int x, int y, int width, int height) {
        this.renderCheckerboard(context, x, y, width, height);
        Color base = this.setting.isSync() ? Managers.COLORS.resolve(this.setting) : (Color)this.setting.getValue();
        this.drawGradientRect(context, x, y, width, height, ColorUtil.rgba(base.getRed(), base.getGreen(), base.getBlue(), 0), ColorUtil.rgba(base.getRed(), base.getGreen(), base.getBlue(), 255), ColorUtil.rgba(base.getRed(), base.getGreen(), base.getBlue(), 0), ColorUtil.rgba(base.getRed(), base.getGreen(), base.getBlue(), 255));
        this.outline(context, x, y, width, height, ColorUtil.rgba(180, 180, 190, 255));
        int selectorX = x + Math.round((float)((Color)this.setting.getValue()).getAlpha() / 255.0f * (float)(width - 1));
        context.fill(selectorX - 1, y - 1, selectorX + 1, y + height + 1, ColorUtil.rgba(255, 255, 255, 255));
    }

    private void renderButtons(DrawContext context, TextRenderer font, int x, int y, int width, int height, int mouseX, int mouseY) {
        int buttonCount = this.setting.canSync() ? 3 : 2;
        int totalSpacing = 2 * (buttonCount - 1);
        int buttonWidth = (width - totalSpacing) / buttonCount;
        this.renderButton(context, font, "Copy", x, y, buttonWidth, height, mouseX, mouseY, true);
        this.renderButton(context, font, "Paste", x + buttonWidth + 2, y, buttonWidth, height, mouseX, mouseY, this.setting.canPaste());
        if (this.setting.canSync()) {
            String syncLabel = this.setting.isSync() ? "Sync*" : "Sync";
            this.renderButton(context, font, syncLabel, x + (buttonWidth + 2) * 2, y, buttonWidth, height, mouseX, mouseY, true);
        }
    }

    private void renderButton(DrawContext context, TextRenderer font, String text, int x, int y, int width, int height, int mouseX, int mouseY, boolean active) {
        boolean hovered = ColorSettingPanel.inside(mouseX, mouseY, x, y, width, height);
        int bg = hovered ? ColorUtil.rgba(86, 74, 108, active ? 230 : 150) : ColorUtil.rgba(66, 56, 86, active ? 205 : 120);
        context.fill(x, y, x + width, y + height, bg);
        this.outline(context, x, y, width, height, ColorUtil.rgba(185, 185, 195, 210));
        int textColor = active ? ColorUtil.rgba(240, 240, 245, 255) : ColorUtil.rgba(160, 160, 168, 255);
        int textX = x + (width - font.getWidth(text)) / 2;
        Objects.requireNonNull(font);
        int textY = y + (height - 9) / 2 + 1;
        context.drawText(font, text, textX, textY, textColor, true);
    }

    private void updatePicker(int mouseX, int mouseY, int x, int y, int width, int height) {
        this.setting.setSync(false);
        float[] hsb = ColorSettingPanel.toHsb((Color)this.setting.getValue());
        float saturation = ColorSettingPanel.clamp01((float)(mouseX - x) / (float)Math.max(1, width - 1));
        float brightness = 1.0f - ColorSettingPanel.clamp01((float)(mouseY - y) / (float)Math.max(1, height - 1));
        Color updated = Color.getHSBColor(hsb[0], saturation, brightness);
        this.setting.setValue(ColorUtil.withAlpha(updated, ((Color)this.setting.getValue()).getAlpha()));
    }

    private void updateHue(int mouseX, int x, int width) {
        this.setting.setSync(false);
        float[] hsb = ColorSettingPanel.toHsb((Color)this.setting.getValue());
        float hue = ColorSettingPanel.clamp01((float)(mouseX - x) / (float)Math.max(1, width - 1));
        Color updated = Color.getHSBColor(hue, hsb[1], hsb[2]);
        this.setting.setValue(ColorUtil.withAlpha(updated, ((Color)this.setting.getValue()).getAlpha()));
    }

    private void updateAlpha(int mouseX, int x, int width) {
        float alphaFactor = ColorSettingPanel.clamp01((float)(mouseX - x) / (float)Math.max(1, width - 1));
        Color current = this.setting.isSync() ? Managers.COLORS.resolve(this.setting) : (Color)this.setting.getValue();
        this.setting.setValue(ColorUtil.withAlpha(current, (int)(alphaFactor * 255.0f)));
    }

    private void drawGradientRect(DrawContext context, int x, int y, int width, int height, int topLeft, int topRight, int bottomLeft, int bottomRight) {
        if (width <= 0 || height <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float)x, (float)y, 0.0f).color(topLeft);
        buffer.vertex(matrix, (float)x, (float)(y + height), 0.0f).color(bottomLeft);
        buffer.vertex(matrix, (float)(x + width), (float)(y + height), 0.0f).color(bottomRight);
        buffer.vertex(matrix, (float)(x + width), (float)y, 0.0f).color(topRight);
        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
        }
        RenderSystem.disableBlend();
    }

    private void renderCheckerboard(DrawContext context, int x, int y, int width, int height) {
        int cell = 4;
        int light = ColorUtil.rgba(184, 184, 190, 255);
        int dark = ColorUtil.rgba(124, 124, 130, 255);
        for (int iy = 0; iy < height; iy += cell) {
            for (int ix = 0; ix < width; ix += cell) {
                boolean alt = (ix / cell + iy / cell) % 2 == 0;
                context.fill(x + ix, y + iy, Math.min(x + ix + cell, x + width), Math.min(y + iy + cell, y + height), alt ? light : dark);
            }
        }
    }

    private void outline(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static float[] toHsb(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int rgba(Color color) {
        return ColorUtil.rgba(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}




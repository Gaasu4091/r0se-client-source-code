/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.clickgui.panel.setting;

import java.awt.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.gui.base.GuiAnimation;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public abstract class NumberSettingPanel<T extends Number>
extends BasePanel {
    public static final int HEIGHT = 14;
    protected static final int PADDING = 4;
    protected static final int SLIDER_HEIGHT = 14;
    protected boolean waiting;
    protected String input = "";
    protected boolean dragging;
    protected float displayedPercent;

    public NumberSettingPanel() {
        this.height = 14;
        this.displayedPercent = 0.0f;
    }

    protected abstract String getSettingName();

    protected abstract double getValue();

    protected abstract double getMin();

    protected abstract double getMax();

    protected abstract void setValueFromDouble(double var1);

    protected abstract String formatValue(double var1);

    @Override
    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean hovered = this.isHovered(mouseX, mouseY);
        this.renderSlider(context, hovered);
        Color textColor = this.getTextColor();
        int textY = this.y + 2;
        int textX = this.x + 4 - 2;
        context.drawText(font, this.getSettingName(), textX, textY, NumberSettingPanel.rgba(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha()), true);
        String value = this.waiting ? this.input : this.formatValue(this.getValue());
        int valueX = this.x + this.width - 4 - font.getWidth(value);
        context.drawText(font, value, valueX, textY, NumberSettingPanel.rgba(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha()), true);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            this.dragging = true;
            this.waiting = false;
            this.updateValue(mouseX);
            return true;
        }
        if (button == 1) {
            this.waiting = true;
            this.dragging = false;
            this.input = "";
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) {
            this.dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (!this.waiting && this.dragging && button == 0) {
            this.updateValue(mouseX);
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        if (!this.waiting) {
            return;
        }
        if (keyCode == 257) {
            this.applyValue();
            return;
        }
        if (keyCode == 256) {
            this.waiting = false;
            this.input = "";
            return;
        }
        if (keyCode == 259) {
            if (!this.input.isEmpty()) {
                this.input = this.input.substring(0, this.input.length() - 1);
            }
            return;
        }
        if (keyCode >= 48 && keyCode <= 57) {
            this.input = this.input + Character.toString((char)(48 + (keyCode - 48)));
            return;
        }
        if (keyCode >= 320 && keyCode <= 329) {
            this.input = this.input + Character.toString((char)(48 + (keyCode - 320)));
            return;
        }
        if (!(keyCode != 46 && keyCode != 330 || this.input.contains("."))) {
            this.input = this.input + ".";
            return;
        }
        if (keyCode == 45 && this.input.isEmpty()) {
            this.input = this.input + "-";
            return;
        }
        this.waiting = false;
        this.input = "";
    }

    @Override
    protected String getName() {
        return this.getSettingName();
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

    @Override
    protected Color getTextColor() {
        return this.getColorFeature().getStyledTextColor(255);
    }

    private void renderSlider(DrawContext context, boolean hovered) {
        Colors colorFeature = this.getColorFeature();
        int sliderX = this.x;
        int sliderY = this.y;
        int sliderWidth = this.width;
        double denominator = Math.max(1.0E-4, this.getMax() - this.getMin());
        double percent = (this.getValue() - this.getMin()) / denominator;
        percent = Math.max(0.0, Math.min(1.0, percent));
        this.displayedPercent = GuiAnimation.approachToggle(this.displayedPercent, (float)percent);
        int filled = Math.max(0, Math.round((float)sliderWidth * this.displayedPercent));
        if (filled > 0) {
            Color accent = colorFeature.getStyledGlobalColor();
            context.fill(sliderX, sliderY, sliderX + filled, sliderY + 14, NumberSettingPanel.rgba(accent.getRed(), accent.getGreen(), accent.getBlue(), accent.getAlpha()));
        }
    }

    private void applyValue() {
        try {
            if (this.input.isEmpty() || this.input.equals("-") || this.input.equals(".")) {
                this.waiting = false;
                this.input = "";
                return;
            }
            double value = Double.parseDouble(this.input);
            value = Math.max(this.getMin(), Math.min(this.getMax(), value));
            this.setValueFromDouble(value);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.waiting = false;
        this.input = "";
    }

    private void updateValue(double mouseX) {
        double percent = (mouseX - (double)(this.x + 4)) / Math.max(1.0, (double)(this.width - 8));
        percent = Math.max(0.0, Math.min(1.0, percent));
        double value = this.getMin() + percent * (this.getMax() - this.getMin());
        this.setValueFromDouble(value);
    }

    private static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
}



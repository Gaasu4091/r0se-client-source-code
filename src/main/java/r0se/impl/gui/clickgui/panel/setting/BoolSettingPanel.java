/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.clickgui.panel.setting;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import r0se.api.settings.BoolSetting;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.gui.base.GuiAnimation;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class BoolSettingPanel
extends BasePanel {
    public static final int HEIGHT = 13;
    private static final int PADDING = 4;
    private final BoolSetting setting;
    private float enabledFactor;

    public BoolSettingPanel(BoolSetting setting) {
        this.setting = setting;
        this.height = 13;
        this.enabledFactor = (Boolean)setting.getValue() != false ? 1.0f : 0.0f;
    }

    @Override
    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean hovered = this.isHovered(mouseX, mouseY);
        this.enabledFactor = GuiAnimation.approachToggle(this.enabledFactor, (Boolean)this.setting.getValue() != false ? 1.0f : 0.0f);
        if (this.enabledFactor > 0.015f) {
            Colors colorFeature = this.getColorFeature();
            Color active = colorFeature.getStyledGlobalColor();
            int filledWidth = Math.round((float)this.width * this.enabledFactor);
            context.fill(this.x, this.y, this.x + filledWidth, this.y + this.height, BoolSettingPanel.rgba(active.getRed(), active.getGreen(), active.getBlue(), active.getAlpha()));
        }
        Objects.requireNonNull(font);
        int textY = this.y + (this.height - 9) / 2 + (hovered ? 0 : 1);
        int textX = this.x + 4 - 2;
        Color textColor = this.getTextColor();
        context.drawText(font, this.getName(), textX, textY, BoolSettingPanel.rgba(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha()), true);
    }

    @Override
    public void resetAnimationState() {
        this.enabledFactor = 0.0f;
        super.resetAnimationState();
    }

    @Override
    protected String getName() {
        return this.setting.getName();
    }

    @Override
    protected boolean isEnabled() {
        return (Boolean)this.setting.getValue();
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

    @Override
    public void onLeftClick() {
        this.setting.setValue((Boolean)this.setting.getValue() == false);
    }

    private static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
}



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
import r0se.api.settings.GroupSetting;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.gui.base.GuiAnimation;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class GroupSettingPanel
extends BasePanel {
    public static final int HEIGHT = 13;
    private static final int PADDING = 4;
    private final GroupSetting setting;
    private float enabledFactor;

    public GroupSettingPanel(GroupSetting setting) {
        this.setting = setting;
        this.height = 13;
        this.enabledFactor = setting.isToggled() ? 1.0f : 0.0f;
    }

    @Override
    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean hovered = this.isHovered(mouseX, mouseY);
        boolean hasToggle = this.setting.hasToggleSetting();
        this.enabledFactor = GuiAnimation.approachToggle(this.enabledFactor, hasToggle && this.setting.isToggled() ? 1.0f : 0.0f);
        if (this.enabledFactor > 0.015f) {
            Colors colorFeature = this.getColorFeature();
            Color active = colorFeature.getStyledGlobalColor();
            int filledWidth = Math.round((float)this.width * this.enabledFactor);
            context.fill(this.x, this.y, this.x + filledWidth, this.y + this.height, GroupSettingPanel.rgba(active));
        }
        Color textColor = this.getTextColor();
        Objects.requireNonNull(font);
        int textY = this.y + (this.height - 9) / 2 + (hovered ? 0 : 1);
        int textX = this.x + 4 - 2;
        context.drawText(font, this.getName(), textX, textY, GroupSettingPanel.rgba(textColor), true);
        String state = hasToggle ? (this.setting.isExpanded() ? "-" : "+") : "...";
        int stateX = this.x + this.width - 4 - font.getWidth(state);
        context.drawText(font, state, stateX, textY, GroupSettingPanel.rgba(textColor), true);
    }

    @Override
    public void resetAnimationState() {
        this.enabledFactor = 0.0f;
        super.resetAnimationState();
    }

    @Override
    public void onLeftClick() {
        this.setting.toggleLinkedValue();
    }

    @Override
    public void onRightClick() {
        this.setting.toggleExpanded();
    }

    @Override
    protected String getName() {
        return this.setting.getDisplayName();
    }

    @Override
    protected boolean isEnabled() {
        return this.setting.hasToggleSetting() && this.setting.isToggled();
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

    private static int rgba(Color color) {
        return (color.getAlpha() & 0xFF) << 24 | (color.getRed() & 0xFF) << 16 | (color.getGreen() & 0xFF) << 8 | color.getBlue() & 0xFF;
    }
}



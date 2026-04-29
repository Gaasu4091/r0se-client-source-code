/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.base;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import r0se.impl.gui.base.GuiAnimation;
import r0se.impl.gui.clickgui.panel.FeaturePanel;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public abstract class BasePanel {
    protected int x;
    protected int y;
    protected int width;
    public int height;
    public boolean expanded;
    private static final int PADDING = 3;
    private static final int GEAR_PADDING = 5;
    private static final int CHILD_INDENT = 2;
    private static final int CHILD_SPACING = 1;
    private final List<BasePanel> subPanels = new ArrayList<BasePanel>();
    private float visibleContentHeight;
    private float enabledFactor;

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addSubPanel(BasePanel panel) {
        this.subPanels.add(panel);
    }

    public List<BasePanel> getSubPanels() {
        return this.subPanels;
    }

    protected abstract String getName();

    protected abstract boolean isEnabled();

    protected abstract Colors getColorFeature();

    protected abstract ClickGui getClickGuiFeature();

    protected Color getTextColor() {
        return this.getColorFeature().getStyledTextColor(255);
    }

    protected Color getEnabledColor() {
        return this.getColorFeature().getStyledGlobalColor();
    }

    protected Color getDisabledColor() {
        return this.getColorFeature().getStyledSecondColor(64);
    }

    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean hovered = this.isHovered(mouseX, mouseY);
        boolean enabled = this.isEnabled();
        this.enabledFactor = GuiAnimation.approachToggle(this.enabledFactor, enabled ? 1.0f : 0.0f);
        if (this.enabledFactor > 0.015f) {
            Color enabledColor = this.getEnabledColor();
            int filledWidth = Math.round((float)this.width * this.enabledFactor);
            context.fill(this.x, this.y, this.x + filledWidth, this.y + this.height, BasePanel.rgba(enabledColor.getRed(), enabledColor.getGreen(), enabledColor.getBlue(), enabledColor.getAlpha()));
        }
        Objects.requireNonNull(font);
        int textY = this.y + (this.height - 9) / 2 + (hovered ? 0 : 1);
        int textX = this.x + 3 - 1;
        context.drawText(font, this.getName(), textX, textY, BasePanel.rgba(this.getTextColor().getRed(), this.getTextColor().getGreen(), this.getTextColor().getBlue(), this.getTextColor().getAlpha()), true);
        if (!this.subPanels.isEmpty() && ((Boolean)this.getClickGuiFeature().gear.getValue()).booleanValue()) {
            String gear = this.expanded ? "-" : "+";
            int gearWidth = font.getWidth(gear) + 5;
            context.drawText(font, gear, this.x + this.width - gearWidth, textY, BasePanel.rgba(this.getTextColor().getRed(), this.getTextColor().getGreen(), this.getTextColor().getBlue(), this.getTextColor().getAlpha()), true);
        }
    }

    public void updateLayoutAnimation() {
        BasePanel basePanel = this;
        if (basePanel instanceof FeaturePanel) {
            FeaturePanel featurePanel = (FeaturePanel)basePanel;
            featurePanel.refreshSettings();
        }
        for (BasePanel panel : this.subPanels) {
            panel.updateLayoutAnimation();
        }
        float targetContentHeight = this.expanded ? (float)this.getDirectContentHeight() : 0.0f;
        this.visibleContentHeight = GuiAnimation.approach(this.visibleContentHeight, targetContentHeight);
        if (!this.expanded && this.visibleContentHeight <= 0.5f) {
            this.resetClosedSubPanelAnimations();
        }
    }

    public void renderOverlay(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        if (this.subPanels.isEmpty() || this.visibleContentHeight <= 0.5f) {
            return;
        }
        int revealTop = this.y + this.height;
        int revealBottom = revealTop + Math.round(this.visibleContentHeight);
        this.layoutSubPanels();
        Managers.RENDER.enableScissor(context, this.x + 2, revealTop, this.x + this.width, revealBottom);
        for (BasePanel panel : this.subPanels) {
            panel.render(context, font, mouseX, mouseY);
        }
        for (int i = this.subPanels.size() - 1; i >= 0; --i) {
            this.subPanels.get(i).renderOverlay(context, font, mouseX, mouseY);
        }
        Managers.RENDER.disableScissor(context);
    }

    public void resetAnimationState() {
        this.enabledFactor = 0.0f;
        this.visibleContentHeight = 0.0f;
        for (BasePanel panel : this.subPanels) {
            panel.resetAnimationState();
        }
    }

    private void resetClosedSubPanelAnimations() {
        for (BasePanel panel : this.subPanels) {
            panel.resetAnimationState();
        }
    }

    public int getFullHeight() {
        return this.height + Math.round(this.visibleContentHeight);
    }

    public void onLeftClick() {
    }

    public void onRightClick() {
    }

    public void onMiddleClick() {
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.visibleContentHeight > 0.5f && this.isWithinReveal(mouseX, mouseY)) {
            this.layoutSubPanels();
            for (int i = this.subPanels.size() - 1; i >= 0; --i) {
                BasePanel sub = this.subPanels.get(i);
                if (!sub.mouseClicked(mouseX, mouseY, button)) continue;
                return true;
            }
        }
        if (!this.isHovered(mouseX, mouseY)) {
            return false;
        }
        switch (button) {
            case 0: {
                this.onLeftClick();
                break;
            }
            case 1: {
                this.onRightClick();
                break;
            }
            case 2: {
                this.onMiddleClick();
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (this.visibleContentHeight > 0.5f && this.isWithinReveal(mouseX, mouseY)) {
            this.layoutSubPanels();
            for (int i = this.subPanels.size() - 1; i >= 0; --i) {
                if (!this.subPanels.get(i).mouseReleased(mouseX, mouseY, button)) continue;
                return true;
            }
        }
        return false;
    }

    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (this.visibleContentHeight > 0.5f && this.isWithinReveal(mouseX, mouseY)) {
            this.layoutSubPanels();
            for (int i = this.subPanels.size() - 1; i >= 0; --i) {
                BasePanel sub = this.subPanels.get(i);
                if (!sub.isHovered(mouseX, mouseY) && !sub.isWithinReveal(mouseX, mouseY)) continue;
                sub.mouseDragged(mouseX, mouseY, button);
                return;
            }
        }
    }

    public void keyPressed(int keyCode) {
        if (this.visibleContentHeight > 0.5f) {
            this.layoutSubPanels();
            for (BasePanel sub : this.subPanels) {
                sub.keyPressed(keyCode);
            }
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public boolean isWithinReveal(int mouseX, int mouseY) {
        int revealTop = this.y + this.height;
        int revealBottom = revealTop + Math.round(this.visibleContentHeight);
        return mouseX >= this.x + 2 && mouseX <= this.x + this.width && mouseY >= revealTop && mouseY <= revealBottom;
    }

    private void layoutSubPanels() {
        int currentY = this.y + this.height + 1;
        int childX = this.x + 2;
        int childWidth = Math.max(1, this.width - 2);
        for (BasePanel panel : this.subPanels) {
            panel.setBounds(childX, currentY, childWidth, panel.height);
            currentY += panel.getFullHeight() + 1;
        }
    }

    private int getDirectContentHeight() {
        if (this.subPanels.isEmpty()) {
            return 0;
        }
        int total = 1;
        for (int i = 0; i < this.subPanels.size(); ++i) {
            total += this.subPanels.get(i).getFullHeight();
            if (i >= this.subPanels.size() - 1) continue;
            ++total;
        }
        return total;
    }

    private static Color brighten(Color color, int amount) {
        return new Color(Math.min(255, color.getRed() + amount), Math.min(255, color.getGreen() + amount), Math.min(255, color.getBlue() + amount), color.getAlpha());
    }

    private static void drawHorizontalGradient(DrawContext context, int x, int y, int width, int height, int leftColor, int rightColor) {
        for (int i = 0; i < width; ++i) {
            float delta = width <= 1 ? 0.0f : (float)i / (float)(width - 1);
            context.fill(x + i, y, x + i + 1, y + height, BasePanel.lerpColor(leftColor, rightColor, delta));
        }
    }

    private static int lerpColor(int start, int end, float delta) {
        int sa = start >> 24 & 0xFF;
        int sr = start >> 16 & 0xFF;
        int sg = start >> 8 & 0xFF;
        int sb = start & 0xFF;
        int ea = end >> 24 & 0xFF;
        int er = end >> 16 & 0xFF;
        int eg = end >> 8 & 0xFF;
        int eb = end & 0xFF;
        int a = (int)((float)sa + (float)(ea - sa) * delta);
        int r = (int)((float)sr + (float)(er - sr) * delta);
        int g = (int)((float)sg + (float)(eg - sg) * delta);
        int b = (int)((float)sb + (float)(eb - sb) * delta);
        return BasePanel.rgba(r, g, b, a);
    }

    private static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
}



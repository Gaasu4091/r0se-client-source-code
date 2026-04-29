/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.clickgui.panel;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.gui.base.GuiAnimation;
import r0se.impl.gui.base.PanelRenderer;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class CategoryPanel {
    public static final int WIDTH = 100;
    public static final int HEADER_HEIGHT = 14;
    public static final int BORDER_WIDTH = 1;
    public static final int PADDING = 5;
    public static final int PANEL_SPACING = 1;
    private static final int CONTENT_SIDE_PADDING = 1;
    private static final int CONTENT_TOP_PADDING = 1;
    private static final int CONTENT_BOTTOM_PADDING = 2;
    private final String name;
    private final PanelRenderer renderer;
    private final List<BasePanel> panels = new ArrayList<BasePanel>();
    private double scroll;
    private double velocity;
    private float currentHeight = 14.0f;
    private boolean expanded = true;
    private boolean lastExpanded = true;
    private boolean categoryTransitioning;

    public CategoryPanel(String name) {
        this.name = name;
        this.renderer = new PanelRenderer(Managers.MODULES.getFeature(Colors.class), Managers.MODULES.getFeature(ClickGui.class));
    }

    public void addPanel(BasePanel panel) {
        this.panels.add(panel);
    }

    public List<BasePanel> getPanels() {
        return this.panels;
    }

    private int getStaticContentPadding() {
        return 4;
    }

    private int getActualContentHeight() {
        if (this.panels.isEmpty()) {
            return 0;
        }
        int height = 0;
        for (int i = 0; i < this.panels.size(); ++i) {
            BasePanel panel = this.panels.get(i);
            height += panel.getFullHeight();
            if (i >= this.panels.size() - 1) continue;
            ++height;
        }
        return height;
    }

    private int getContentViewportHeight(int animatedHeight) {
        int contentTopOffset = 16;
        return Math.max(0, animatedHeight - contentTopOffset - 1);
    }

    private int getTargetHeight(int screenHeight, int panelY) {
        int height = 14;
        if (this.expanded) {
            height += this.getStaticContentPadding() + this.getActualContentHeight();
        }
        int maxHeight = Math.max(14, screenHeight - panelY);
        return Math.min(height, maxHeight);
    }

    public void render(DrawContext context, TextRenderer font, int x, int y, int mouseX, int mouseY, float fps) {
        int screenHeight = context.getScaledWindowHeight();
        for (BasePanel panel : this.panels) {
            panel.updateLayoutAnimation();
        }
        int targetHeight = this.getTargetHeight(screenHeight, y);
        if (this.expanded != this.lastExpanded) {
            this.categoryTransitioning = true;
            this.lastExpanded = this.expanded;
        }
        if (this.categoryTransitioning) {
            this.currentHeight = GuiAnimation.approach(this.currentHeight, targetHeight, 0.085f);
            if (Math.abs(this.currentHeight - (float)targetHeight) <= 0.5f) {
                this.currentHeight = targetHeight;
                this.categoryTransitioning = false;
            }
        } else {
            this.currentHeight = targetHeight;
        }
        int animatedHeight = Math.round(this.currentHeight);
        int actualContentHeight = this.getActualContentHeight();
        int visibleHeight = this.getContentViewportHeight(animatedHeight);
        int maxScroll = Math.max(0, actualContentHeight - visibleHeight);
        this.scroll += this.velocity;
        this.velocity *= 0.85;
        if (this.scroll < 0.0) {
            this.scroll = 0.0;
            this.velocity = 0.0;
        }
        if (this.scroll > (double)maxScroll) {
            this.scroll = maxScroll;
            this.velocity = 0.0;
        }
        this.renderer.renderPanel(context, x, y, 100, animatedHeight, 14, true, true);
        this.renderer.renderHeaderText(context, font, this.name, x, y, 14, 5);
        int contentX = x + 1 + 1;
        int contentY = y + 14 + 1 + 1;
        int contentW = 96;
        int contentH = this.getContentViewportHeight(animatedHeight);
        if (contentH <= 0) {
            return;
        }
        Managers.RENDER.enableScissor(context, contentX, contentY, contentX + contentW, contentY + contentH);
        int currentY = contentY - (int)this.scroll;
        for (int i = 0; i < this.panels.size(); ++i) {
            BasePanel panel = this.panels.get(i);
            panel.setBounds(contentX, currentY, contentW, panel.height);
            panel.render(context, font, mouseX, mouseY);
            currentY += panel.getFullHeight();
            if (i >= this.panels.size() - 1) continue;
            ++currentY;
        }
        currentY = contentY - (int)this.scroll;
        for (BasePanel panel : this.panels) {
            panel.setBounds(contentX, currentY, contentW, panel.height);
            currentY += panel.getFullHeight() + 1;
        }
        for (int i = this.panels.size() - 1; i >= 0; --i) {
            this.panels.get(i).renderOverlay(context, font, mouseX, mouseY);
        }
        Managers.RENDER.disableScissor(context);
    }

    public boolean mouseScrolled(int mouseX, int mouseY, double amount, int x, int y) {
        if (!this.isHovered(mouseX, mouseY, x, y)) {
            return false;
        }
        this.velocity += -amount * 5.0;
        return true;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button, int x, int y) {
        if (this.expanded) {
            for (int i = this.panels.size() - 1; i >= 0; --i) {
                if (!this.panels.get(i).mouseClicked(mouseX, mouseY, button)) continue;
                return true;
            }
        }
        if (button == 1 && this.isHeaderHovered(mouseX, mouseY, x, y)) {
            this.expanded = !this.expanded;
            return true;
        }
        return false;
    }

    public void mouseDragged(int mouseX, int mouseY, int button, int x, int y) {
        if (!this.expanded) {
            return;
        }
        for (int i = this.panels.size() - 1; i >= 0; --i) {
            BasePanel panel = this.panels.get(i);
            if (!panel.isHovered(mouseX, mouseY) && !panel.isWithinReveal(mouseX, mouseY)) continue;
            panel.mouseDragged(mouseX, mouseY, button);
            return;
        }
    }

    public boolean mouseReleased(int mouseX, int mouseY, int button, int x, int y) {
        if (!this.expanded) {
            return false;
        }
        for (int i = this.panels.size() - 1; i >= 0; --i) {
            if (!this.panels.get(i).mouseReleased(mouseX, mouseY, button)) continue;
            return true;
        }
        return false;
    }

    public boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= (double)x && mouseX <= (double)(x + 100) && mouseY >= (double)y && mouseY <= (double)(y + Math.round(this.currentHeight));
    }

    public boolean isHeaderHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= (double)x && mouseX <= (double)(x + 100) && mouseY >= (double)y && mouseY <= (double)(y + 14);
    }

    public void keyPressed(int keyCode) {
        if (!this.expanded) {
            return;
        }
        for (BasePanel panel : this.panels) {
            panel.keyPressed(keyCode);
        }
    }
}



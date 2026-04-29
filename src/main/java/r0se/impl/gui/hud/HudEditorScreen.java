/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.hud;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.api.feature.Feature;
import r0se.api.feature.FeatureCategory;
import r0se.impl.gui.base.R0SEScreen;
import r0se.impl.gui.clickgui.panel.CategoryPanel;
import r0se.impl.gui.clickgui.panel.FeaturePanel;
import r0se.impl.module.hud.HUD;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class HudEditorScreen
extends R0SEScreen {
    private static final int PANEL_WIDTH = 200;
    private static final int SNAP_DISTANCE = 6;
    private HUD hudFeature;
    private List<HudModuleFeature> modules = List.of();
    private HudModuleFeature selectedModule;
    private boolean draggingElement;
    private int dragOffsetX;
    private int dragOffsetY;
    private CategoryPanel hudPanel;
    private int panelX;
    private int panelY;
    private boolean draggingPanel;
    private int panelDragOffsetX;
    private int panelDragOffsetY;
    private boolean snapLeftGuide;
    private boolean snapRightGuide;
    private boolean snapTopGuide;
    private boolean snapBottomGuide;

    public HudEditorScreen() {
        super((Text)Text.literal((String)"R0SE HUD Editor"));
    }

    protected void init() {
        this.hudFeature = Managers.MODULES.getFeature(HUD.class);
        this.modules = Managers.MODULES.getFeatures(FeatureCategory.HUD).stream().filter(HudModuleFeature.class::isInstance).map(HudModuleFeature.class::cast).toList();
        this.hudPanel = this.createHudPanel();
        if (this.panelX == 0 && this.panelY == 0) {
            this.panelX = this.width / 2 + 12;
            this.panelY = this.height / 2 - 180;
        }
        this.clampPanel();
    }

    private CategoryPanel createHudPanel() {
        CategoryPanel panel = new CategoryPanel("Hud");
        for (Feature feature : Managers.MODULES.getFeatures(FeatureCategory.HUD)) {
            if (!(feature instanceof HudModuleFeature)) continue;
            panel.addPanel(new FeaturePanel(feature));
        }
        return panel;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        for (HudModuleFeature module : this.modules) {
            module.renderEditorPreview(context);
        }
        this.drawGuides(context);
        this.drawElementBounds(context, mouseX, mouseY);
        this.drawSettingsPanel(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x28000000);
    }

    private void drawGuides(DrawContext context) {
        int centerX = (this.width - 1) / 2;
        int centerY = (this.height - 1) / 2;
        int guideColor = 0x66FFFFFF;
        context.fill(centerX, 0, centerX + 1, this.height, guideColor);
        context.fill(0, centerY, this.width, centerY + 1, guideColor);
        if (this.hudFeature == null || !this.draggingElement) {
            return;
        }
        int safeX = this.hudFeature.getSafeX();
        int safeY = this.hudFeature.getSafeY();
        int snapColor = -521610934;
        if (this.snapLeftGuide) {
            context.fill(safeX, 0, safeX + 1, this.height, snapColor);
        }
        if (this.snapRightGuide) {
            context.fill(this.width - safeX - 1, 0, this.width - safeX, this.height, snapColor);
        }
        if (this.snapTopGuide) {
            context.fill(0, safeY, this.width, safeY + 1, snapColor);
        }
        if (this.snapBottomGuide) {
            context.fill(0, this.height - safeY - 1, this.width, this.height - safeY, snapColor);
        }
    }

    private void drawSettingsPanel(DrawContext context, int mouseX, int mouseY) {
        if (this.hudPanel != null) {
            this.hudPanel.render(context, R0SE.mc.textRenderer, this.panelX, this.panelY, mouseX, mouseY, 60.0f);
        }
    }

    private void drawElementBounds(DrawContext context, int mouseX, int mouseY) {
        for (HudModuleFeature module : this.modules) {
            boolean selected;
            Rectangle bounds = module.getEditorBounds();
            if (bounds == null || !module.isEnabled()) continue;
            boolean hovered = this.isHovered(mouseX, mouseY, bounds.x, bounds.y, bounds.width, bounds.height);
            boolean bl = selected = module == this.selectedModule;
            int fill = selected ? -1777990135 : (hovered ? -2113402868 : 1778911244);
            context.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, fill);
        }
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.hudPanel != null && this.hudPanel.isHeaderHovered(mouseX, mouseY, this.panelX, this.panelY)) {
            this.draggingPanel = true;
            this.panelDragOffsetX = (int)mouseX - this.panelX;
            this.panelDragOffsetY = (int)mouseY - this.panelY;
            return true;
        }
        if (button == 0) {
            for (HudModuleFeature module : new ArrayList<HudModuleFeature>(this.modules)) {
                Rectangle bounds = module.getEditorBounds();
                if (bounds == null || !module.isEnabled() || !this.isHovered((int)mouseX, (int)mouseY, bounds.x, bounds.y, bounds.width, bounds.height)) continue;
                this.selectedModule = module;
                this.draggingElement = true;
                this.dragOffsetX = (int)mouseX - bounds.x;
                this.dragOffsetY = (int)mouseY - bounds.y;
                return true;
            }
        }
        if (this.hudPanel != null && this.hudPanel.mouseClicked((int)mouseX, (int)mouseY, button, this.panelX, this.panelY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && this.draggingPanel) {
            this.panelX = (int)mouseX - this.panelDragOffsetX;
            this.panelY = (int)mouseY - this.panelDragOffsetY;
            this.clampPanel();
            return true;
        }
        if (button == 0 && this.draggingElement && this.selectedModule != null && this.hudFeature != null) {
            Rectangle bounds = this.selectedModule.getEditorBounds();
            int elementWidth = bounds == null ? 0 : bounds.width;
            int elementHeight = bounds == null ? 0 : bounds.height;
            int targetX = (int)mouseX - this.dragOffsetX;
            int targetY = (int)mouseY - this.dragOffsetY;
            int safeX = this.hudFeature.getSafeX();
            int safeY = this.hudFeature.getSafeY();
            int centerX = (this.width - 1) / 2;
            int centerY = (this.height - 1) / 2;
            targetX = Math.max(0, Math.min(this.width - elementWidth, targetX));
            targetY = Math.max(0, Math.min(this.height - elementHeight, targetY));
            this.snapLeftGuide = Math.abs(targetX - safeX) <= 6;
            this.snapTopGuide = Math.abs(targetY - safeY) <= 6;
            this.snapRightGuide = Math.abs(targetX + elementWidth - (this.width - safeX)) <= 6;
            boolean bl = this.snapBottomGuide = Math.abs(targetY + elementHeight - (this.height - safeY)) <= 6;
            if (this.snapLeftGuide) {
                targetX = safeX;
            }
            if (this.snapTopGuide) {
                targetY = safeY;
            }
            if (this.snapRightGuide) {
                targetX = this.width - safeX - elementWidth;
            }
            if (this.snapBottomGuide) {
                targetY = this.height - safeY - elementHeight;
            }
            if (Math.abs(targetX + elementWidth / 2 - centerX) <= 6) {
                targetX = centerX - elementWidth / 2;
            }
            if (Math.abs(targetY + elementHeight / 2 - centerY) <= 6) {
                targetY = centerY - elementHeight / 2;
            }
            this.selectedModule.setEditorBoundsPosition(targetX, targetY);
            this.resolveOverlap(this.selectedModule);
            return true;
        }
        if (this.hudPanel != null) {
            this.hudPanel.mouseDragged((int)mouseX, (int)mouseY, button, this.panelX, this.panelY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void resolveOverlap(HudModuleFeature movingModule) {
        Rectangle moving = movingModule.getEditorBounds();
        if (moving == null) {
            return;
        }
        int attempts = 0;
        while (attempts++ < 20) {
            int score;
            Rectangle[] candidates;
            Rectangle blocker = null;
            for (HudModuleFeature module : this.modules) {
                Rectangle other;
                if (module == movingModule || !module.isEnabled() || (other = module.getEditorBounds()) == null || !moving.intersects(other)) continue;
                blocker = other;
                break;
            }
            if (blocker == null) {
                return;
            }
            Rectangle best = null;
            int bestScore = Integer.MAX_VALUE;
            for (Rectangle candidate : candidates = new Rectangle[]{this.clampRect(new Rectangle(moving.x, blocker.y - moving.height - 2, moving.width, moving.height)), this.clampRect(new Rectangle(moving.x, blocker.y + blocker.height + 2, moving.width, moving.height)), this.clampRect(new Rectangle(blocker.x - moving.width - 2, moving.y, moving.width, moving.height)), this.clampRect(new Rectangle(blocker.x + blocker.width + 2, moving.y, moving.width, moving.height))}) {
                if (candidate == null || this.intersectsAny(candidate, movingModule) || (score = Math.abs(candidate.x - moving.x) + Math.abs(candidate.y - moving.y)) >= bestScore) continue;
                bestScore = score;
                best = candidate;
            }
            if (best == null) {
                for (Rectangle candidate : candidates) {
                    if (candidate == null || (score = this.overlapScore(candidate, movingModule) + Math.abs(candidate.x - moving.x) + Math.abs(candidate.y - moving.y)) >= bestScore) continue;
                    bestScore = score;
                    best = candidate;
                }
            }
            if (best == null) {
                return;
            }
            movingModule.setEditorBoundsPosition(best.x, best.y);
            moving = movingModule.getEditorBounds();
            if (moving != null) continue;
            return;
        }
    }

    private Rectangle clampRect(Rectangle rect) {
        if (rect == null) {
            return null;
        }
        int x = Math.max(0, Math.min(this.width - rect.width, rect.x));
        int y = Math.max(0, Math.min(this.height - rect.height, rect.y));
        return new Rectangle(x, y, rect.width, rect.height);
    }

    private boolean intersectsAny(Rectangle rect, HudModuleFeature movingModule) {
        for (HudModuleFeature module : this.modules) {
            Rectangle other;
            if (module == movingModule || !module.isEnabled() || (other = module.getEditorBounds()) == null || !rect.intersects(other)) continue;
            return true;
        }
        return false;
    }

    private int overlapScore(Rectangle rect, HudModuleFeature movingModule) {
        int score = 0;
        for (HudModuleFeature module : this.modules) {
            Rectangle other;
            if (module == movingModule || !module.isEnabled() || (other = module.getEditorBounds()) == null || !rect.intersects(other)) continue;
            Rectangle intersection = rect.intersection(other);
            score += Math.max(1, intersection.width * intersection.height);
        }
        return score;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggingElement = false;
            this.draggingPanel = false;
            this.snapLeftGuide = false;
            this.snapRightGuide = false;
            this.snapTopGuide = false;
            this.snapBottomGuide = false;
        }
        if (this.hudPanel != null && this.hudPanel.mouseReleased((int)mouseX, (int)mouseY, button, this.panelX, this.panelY)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 344) {
            this.close();
            return true;
        }
        if (this.hudPanel != null) {
            this.hudPanel.keyPressed(keyCode);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void close() {
        R0SE.mc.setScreen(null);
    }

    public boolean shouldPause() {
        return false;
    }

    private void clampPanel() {
        int minX = 4;
        int minY = 4;
        int maxX = Math.max(minX, this.width - 200 - 4);
        int maxY = Math.max(minY, this.height - 20);
        this.panelX = Math.max(minX, Math.min(maxX, this.panelX));
        this.panelY = Math.max(minY, Math.min(maxY, this.panelY));
    }
}



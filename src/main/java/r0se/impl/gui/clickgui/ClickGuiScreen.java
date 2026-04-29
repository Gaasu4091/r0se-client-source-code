/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.text.Text
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.clickgui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.api.feature.Feature;
import r0se.api.feature.FeatureCategory;
import r0se.impl.gui.base.GuiAnimation;
import r0se.impl.gui.base.R0SEScreen;
import r0se.impl.gui.clickgui.panel.CategoryPanel;
import r0se.impl.gui.clickgui.panel.FeaturePanel;
import r0se.impl.module.client.ClickGui;
import r0se.manager.Managers;

public class ClickGuiScreen
extends R0SEScreen {
    private final Map<FeatureCategory, Point> categoryPositions = new HashMap<FeatureCategory, Point>();
    private final Map<FeatureCategory, CategoryPanel> categoryPanels = new HashMap<FeatureCategory, CategoryPanel>();
    private boolean draggingCategory;
    private FeatureCategory draggedCategory;
    private int dragStartX;
    private int dragStartY;
    private int initialCategoryX;
    private int initialCategoryY;
    private ClickGui clickGuiModule;
    private float fadeFactor;
    private boolean closing;
    public float scale = 1.0f;

    public ClickGuiScreen() {
        super((Text)Text.literal((String)"R0SE ClickGUI"));
        this.refreshPanels();
    }

    public void refreshPanels() {
        HashMap<FeatureCategory, Point> oldPositions = new HashMap<FeatureCategory, Point>(this.categoryPositions);
        this.categoryPositions.clear();
        this.categoryPanels.clear();
        this.clickGuiModule = Managers.MODULES.getFeature(ClickGui.class);
        this.scale = this.clickGuiModule != null ? ((Double)this.clickGuiModule.scale.getValue()).floatValue() : 1.0f;
        int startX = 20;
        int startY = 20;
        for (FeatureCategory category : Managers.MODULES.getCategories()) {
            if (category == FeatureCategory.HUD || Managers.MODULES.getFeatures(category).isEmpty()) continue;
            Point pos = oldPositions.getOrDefault((Object)category, new Point(startX, startY));
            this.categoryPositions.put(category, pos);
            CategoryPanel panel = new CategoryPanel(category.getDisplayName());
            for (Feature feature : Managers.MODULES.getFeatures(category)) {
                panel.addPanel(new FeaturePanel(feature));
            }
            this.categoryPanels.put(category, panel);
            startX += 102;
        }
    }

    protected void init() {
        this.refreshPanels();
        this.fadeFactor = 0.0f;
        this.closing = false;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.fadeFactor = GuiAnimation.approach(this.fadeFactor, 1.0f, 0.12f);
        if (this.clickGuiModule != null && ((Boolean)this.clickGuiModule.blur.getValue()).booleanValue() && this.fadeFactor > 0.08f) {
            this.applyGameBlur();
        }
        if (this.clickGuiModule != null && ((Boolean)this.clickGuiModule.background.getValue()).booleanValue()) {
            this.renderBackground(context, mouseX, mouseY, delta);
        }
        int scaledMouseX = (int)((float)mouseX / this.scale);
        int scaledMouseY = (int)((float)mouseY / this.scale);
        context.getMatrices().push();
        context.getMatrices().scale(this.scale, this.scale, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.max(0.0f, Math.min(1.0f, this.fadeFactor)));
        float fps = Math.max(1.0f, (float)R0SE.mc.getCurrentFps());
        for (FeatureCategory category : this.categoryPanels.keySet()) {
            Point pos = this.categoryPositions.get((Object)category);
            if (pos == null) continue;
            CategoryPanel panel = this.categoryPanels.get((Object)category);
            panel.render(context, this.textRenderer, pos.x, pos.y, scaledMouseX, scaledMouseY, fps);
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        context.getMatrices().pop();
        super.render(context, mouseX, mouseY, delta);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        int alpha = Math.round(88.0f * this.fadeFactor);
        int topAlpha = Math.round(18.0f * this.fadeFactor);
        int bottomAlpha = Math.round(24.0f * this.fadeFactor);
        context.fill(0, 0, this.width, this.height, alpha << 24 | 0x9090D);
        context.fill(0, 0, this.width, this.height / 3, topAlpha << 24);
        context.fill(0, this.height * 2 / 3, this.width, this.height, bottomAlpha << 24);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        CategoryPanel panel;
        Point pos;
        int scaledMouseX = (int)(mouseX / (double)this.scale);
        int scaledMouseY = (int)(mouseY / (double)this.scale);
        for (FeatureCategory category : this.categoryPanels.keySet()) {
            pos = this.categoryPositions.get((Object)category);
            if (pos == null || !(panel = this.categoryPanels.get((Object)category)).isHeaderHovered(scaledMouseX, scaledMouseY, pos.x, pos.y) || button != 0) continue;
            this.draggingCategory = true;
            this.draggedCategory = category;
            this.dragStartX = scaledMouseX;
            this.dragStartY = scaledMouseY;
            this.initialCategoryX = pos.x;
            this.initialCategoryY = pos.y;
            return true;
        }
        if (!this.draggingCategory) {
            for (FeatureCategory category : this.categoryPanels.keySet()) {
                pos = this.categoryPositions.get((Object)category);
                if (pos == null || !(panel = this.categoryPanels.get((Object)category)).mouseClicked(scaledMouseX, scaledMouseY, button, pos.x, pos.y)) continue;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Point pos;
        int scaledMouseX = (int)(mouseX / (double)this.scale);
        int scaledMouseY = (int)(mouseY / (double)this.scale);
        if (this.draggingCategory && this.draggedCategory != null && (pos = this.categoryPositions.get((Object)this.draggedCategory)) != null) {
            pos.x = this.initialCategoryX + (scaledMouseX - this.dragStartX);
            pos.y = this.initialCategoryY + (scaledMouseY - this.dragStartY);
            return true;
        }
        boolean handled = false;
        for (FeatureCategory category : this.categoryPanels.keySet()) {
            Point pos2 = this.categoryPositions.get((Object)category);
            if (pos2 == null) continue;
            CategoryPanel panel = this.categoryPanels.get((Object)category);
            panel.mouseDragged(scaledMouseX, scaledMouseY, button, pos2.x, pos2.y);
            handled = true;
        }
        if (handled) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int scaledMouseX = (int)(mouseX / (double)this.scale);
        int scaledMouseY = (int)(mouseY / (double)this.scale);
        this.draggingCategory = false;
        this.draggedCategory = null;
        for (FeatureCategory category : this.categoryPanels.keySet()) {
            Point pos = this.categoryPositions.get((Object)category);
            if (pos == null) continue;
            CategoryPanel panel = this.categoryPanels.get((Object)category);
            panel.mouseReleased(scaledMouseX, scaledMouseY, button, pos.x, pos.y);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int scaledMouseX = (int)(mouseX / (double)this.scale);
        int scaledMouseY = (int)(mouseY / (double)this.scale);
        for (FeatureCategory category : this.categoryPanels.keySet()) {
            CategoryPanel panel;
            Point pos = this.categoryPositions.get((Object)category);
            if (pos == null || !(panel = this.categoryPanels.get((Object)category)).mouseScrolled(scaledMouseX, scaledMouseY, verticalAmount, pos.x, pos.y)) continue;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == this.getClickGuiBind() || keyCode == 256) {
            this.close();
            return true;
        }
        for (CategoryPanel panel : this.categoryPanels.values()) {
            panel.keyPressed(keyCode);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void close() {
        R0SE.mc.setScreen(null);
        this.closing = true;
        this.draggingCategory = false;
        this.draggedCategory = null;
    }

    public boolean shouldPause() {
        return false;
    }

    private int getClickGuiBind() {
        return (Integer)Managers.MODULES.getFeature(ClickGui.class).getKeyBind().getValue();
    }
}



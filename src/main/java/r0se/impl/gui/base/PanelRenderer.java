/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.gui.base;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import r0se.api.render.ColorUtil;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class PanelRenderer {
    private final Colors colorsModule;
    private final ClickGui clickGuiModule;

    public PanelRenderer(Colors colorsModule, ClickGui clickGuiModule) {
        this.colorsModule = colorsModule;
        this.clickGuiModule = clickGuiModule;
    }

    public void renderPanel(DrawContext context, int x, int y, int width, int height, int headerHeight, boolean renderHeader, boolean renderBackground) {
        int lineColor;
        Color primary = this.colorsModule.getStyledGlobalColor();
        Color secondary = this.colorsModule.getStyledSecondColor();
        if (renderBackground) {
            int bgAlpha = (Integer)this.clickGuiModule.guiAlpha.getValue();
            Managers.RENDER.rect(context, x, y, x + width, y + height, ColorUtil.rgba(18, 18, 24, bgAlpha));
        }
        int n = lineColor = (Boolean)this.clickGuiModule.lines.getValue() != false ? ColorUtil.rgba(primary.getRed(), primary.getGreen(), primary.getBlue(), 255) : ColorUtil.rgba(22, 22, 28, 0);
        if (!renderHeader) {
            Managers.RENDER.rect(context, x, y, x + width, y + 1, lineColor);
        }
        if (renderHeader && headerHeight > 0) {
            if (((Boolean)this.clickGuiModule.gradientFill.getValue()).booleanValue()) {
                Managers.RENDER.horizontalGradient(context, x, y, width, headerHeight, ColorUtil.rgba(primary.getRed(), primary.getGreen(), primary.getBlue(), 255), ColorUtil.rgba(secondary.getRed(), secondary.getGreen(), secondary.getBlue(), 255));
            } else {
                Managers.RENDER.rect(context, x, y, x + width, y + headerHeight, ColorUtil.rgba(primary.getRed(), primary.getGreen(), primary.getBlue(), 255));
            }
            Managers.RENDER.rect(context, x, y + headerHeight, x + width, y + headerHeight + 1, lineColor);
        }
        Managers.RENDER.rect(context, x, y + height - 1, x + width, y + height, lineColor);
        int topOffset = renderHeader && headerHeight > 0 ? headerHeight + 1 : 1;
        Managers.RENDER.rect(context, x, y + topOffset, x + 1, y + height - 1, lineColor);
        Managers.RENDER.rect(context, x + width - 1, y + topOffset, x + width, y + height - 1, lineColor);
    }

    public void renderHeaderText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int headerHeight, int padding) {
        Objects.requireNonNull(textRenderer);
        int textY = y + (headerHeight - 9) / 2 + 1;
        Managers.RENDER.text(context, textRenderer, text, x + padding, textY, ColorUtil.rgba(255, 255, 255, 255), true);
    }
}



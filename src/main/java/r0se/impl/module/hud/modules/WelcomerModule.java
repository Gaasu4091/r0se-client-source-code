/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Formatting
 *  net.minecraft.client.gui.DrawContext
 */
package r0se.impl.module.hud.modules;

import java.awt.Point;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import r0se.R0SE;
import r0se.impl.module.hud.modules.HudModuleFeature;
import r0se.manager.Managers;

public class WelcomerModule
extends HudModuleFeature {
    public WelcomerModule() {
        super("Welcomer", "Renders a welcome message", new String[0]);
    }

    @Override
    protected void renderHud(DrawContext context, boolean editor) {
        if (R0SE.mc == null || R0SE.mc.player == null) {
            this.clearBounds();
            return;
        }
        String text = "Hello " + String.valueOf(Formatting.WHITE) + R0SE.mc.player.getName().getString() + String.valueOf(Formatting.RESET) + " :^)";
        int defaultX = Math.round((float)(R0SE.mc.getWindow().getScaledWidth() - R0SE.mc.textRenderer.getWidth(text)) * 0.5f);
        Point point = this.resolvePosition(defaultX, this.safeY() + 4);
        context.drawText(R0SE.mc.textRenderer, text, point.x, point.y, Managers.COLORS.getAccent().getRGB(), true);
        int n = point.x;
        int n2 = point.y;
        int n3 = R0SE.mc.textRenderer.getWidth(text);
        Objects.requireNonNull(R0SE.mc.textRenderer);
        this.setBounds(n, n2, n3, 9);
    }
}



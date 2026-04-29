/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.client.util.math.MatrixStack
 */
package r0se.api.render;

import java.awt.Color;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.client.util.math.MatrixStack;
import r0se.manager.impl.RenderManager;

public enum BoxRenderMode {
    FILL(true, false),
    OUTLINE(false, true),
    BOTH(true, true);

    private final boolean fill;
    private final boolean outline;

    private BoxRenderMode(boolean fill, boolean outline) {
        this.fill = fill;
        this.outline = outline;
    }

    public boolean isFill() {
        return this.fill;
    }

    public boolean isOutline() {
        return this.outline;
    }

    public void render(RenderManager renderManager, MatrixStack matrices, BlockPos pos, Color color, float lineWidth) {
        this.render(renderManager, matrices, new Box(pos), color, lineWidth);
    }

    public void render(RenderManager renderManager, MatrixStack matrices, Box box, Color color, float lineWidth) {
        renderManager.renderBox(matrices, box, this, color, RenderLayer.getLines(), lineWidth);
    }

    public void renderScaled(RenderManager renderManager, MatrixStack matrices, BlockPos pos, float scale, Color color, float lineWidth) {
        this.renderScaled(renderManager, matrices, new Box(pos), scale, color, lineWidth);
    }

    public void renderScaled(RenderManager renderManager, MatrixStack matrices, Box box, float scale, Color color, float lineWidth) {
        renderManager.renderScaledBox(matrices, box, scale, this, color, RenderLayer.getLines(), lineWidth);
    }
}



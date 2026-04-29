/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 */
package r0se.api.event.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import r0se.api.event.Event;

public class RenderOverlayEvent
extends Event {
    private final DrawContext context;
    private final float tickDelta;

    public RenderOverlayEvent(DrawContext context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }

    public DrawContext getContext() {
        return this.context;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }

    public MatrixStack getMatrices() {
        return this.context.getMatrices();
    }
}



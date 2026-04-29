/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 */
package r0se.api.event.render;

import net.minecraft.client.util.math.MatrixStack;
import r0se.api.event.Event;

public class RenderWorldEvent
extends Event {
    private final MatrixStack matrices;
    private final float tickDelta;

    public RenderWorldEvent(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    public MatrixStack getMatrices() {
        return this.matrices;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }
}



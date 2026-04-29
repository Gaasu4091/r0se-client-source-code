/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.render.state;

import r0se.api.render.Easing;
import r0se.api.render.animation.Animation;

public class BlockRenderState {
    private final Animation fadeAnimation;
    private long lastSeenTick;

    public BlockRenderState(boolean visible, float length, Easing easing, long tick) {
        this.fadeAnimation = new Animation(visible, length, easing);
        this.lastSeenTick = tick;
        this.fadeAnimation.setStateHard(visible);
    }

    public void markVisible(long tick) {
        this.lastSeenTick = tick;
        this.fadeAnimation.setState(true);
    }

    public void markHidden() {
        this.fadeAnimation.setState(false);
    }

    public void setLength(float length) {
        this.fadeAnimation.setLength(length);
    }

    public void setStateHard(boolean visible, long tick) {
        this.lastSeenTick = tick;
        this.fadeAnimation.setStateHard(visible);
    }

    public double getFactor() {
        return this.fadeAnimation.getFactor();
    }

    public boolean isFinished() {
        return this.fadeAnimation.isFinished();
    }

    public long getLastSeenTick() {
        return this.lastSeenTick;
    }
}


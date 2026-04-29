/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.render.animation;

import r0se.api.render.Easing;

public class Animation {
    private float length;
    private long last;
    private boolean state;
    private Easing easing;

    public Animation(float length) {
        this(false, length, Easing.LINEAR);
    }

    public Animation(boolean initial, float length) {
        this(initial, length, Easing.LINEAR);
    }

    public Animation(boolean initial, float length, Easing easing) {
        this.length = length;
        this.state = initial;
        this.easing = easing;
    }

    public void setState(boolean state) {
        this.last = (long)(!state ? (double)System.currentTimeMillis() - (1.0 - this.getLinearFactor()) * (double)this.length : (double)System.currentTimeMillis() - this.getLinearFactor() * (double)this.length);
        this.state = state;
    }

    public void setStateHard(boolean state) {
        this.state = state;
        this.last = state ? System.currentTimeMillis() - (long)(this.getLinearFactor() * (double)this.length) : (long)((double)System.currentTimeMillis() - (1.0 - this.getLinearFactor()) * (double)this.length);
    }

    public boolean getState() {
        return this.state;
    }

    public double getFactor() {
        return this.easing.ease(this.getLinearFactor());
    }

    public double getLinearFactor() {
        double factor = this.state ? (double)((float)(System.currentTimeMillis() - this.last) / this.length) : 1.0 - (double)((float)(System.currentTimeMillis() - this.last) / this.length);
        return Math.max(0.0, Math.min(1.0, factor));
    }

    public boolean isFinished() {
        return !this.state && this.getFactor() == 0.0 || this.state && this.getFactor() == 1.0;
    }

    public void reset() {
        this.last = System.currentTimeMillis();
    }

    public void setLength(float length) {
        this.length = length;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.render.animation;

import r0se.api.render.Easing;

public class ValueAnimation {
    private float previous;
    private float current;
    private long startTime;
    private float duration;
    private Easing easing;

    public ValueAnimation(float initial, float duration, Easing easing) {
        this.previous = initial;
        this.current = initial;
        this.duration = duration;
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
    }

    public float get() {
        return this.lerpFactor();
    }

    public float get(float target) {
        float value = this.lerpFactor();
        if (Float.compare(this.current, target) != 0) {
            this.previous = value;
            this.current = target;
            this.startTime = System.currentTimeMillis();
        }
        return value;
    }

    public boolean isFinished() {
        return Math.abs(this.get() - this.current) < 0.001f;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    private float lerpFactor() {
        if (this.duration <= 0.0f) {
            return this.current;
        }
        double linear = Math.max(0.0, Math.min(1.0, (double)(System.currentTimeMillis() - this.startTime) / (double)this.duration));
        double factor = this.easing.ease(linear);
        return (float)((double)this.previous + (double)(this.current - this.previous) * factor);
    }
}


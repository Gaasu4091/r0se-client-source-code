/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.gui.base;

public final class GuiAnimation {
    public static final float SPEED = 0.1f;
    public static final float TOGGLE_SPEED = 0.055f;

    private GuiAnimation() {
    }

    public static float approach(float current, float target) {
        return GuiAnimation.approach(current, target, 0.1f);
    }

    public static float approachToggle(float current, float target) {
        return GuiAnimation.approach(current, target, 0.055f);
    }

    public static float approach(float current, float target, float speed) {
        float next = current + (target - current) * speed;
        if (Math.abs(target - next) < 0.001f) {
            return target;
        }
        return next;
    }
}


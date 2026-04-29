/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.event.network;

import r0se.api.event.Event;

public class RotationUpdateEvent
extends Event {
    private final float yaw;
    private final float pitch;

    public RotationUpdateEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public static class PrePacket
    extends RotationUpdateEvent {
        public PrePacket() {
            super(0.0f, 0.0f);
        }
    }

    public static class Pre
    extends RotationUpdateEvent {
        public Pre() {
            super(0.0f, 0.0f);
        }
    }
}


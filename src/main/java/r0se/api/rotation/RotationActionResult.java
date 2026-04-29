/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.rotation;

import r0se.api.rotation.Rotation;

public class RotationActionResult {
    private final RotationActionState state;
    private final Rotation rotation;
    private final long tick;

    private RotationActionResult(RotationActionState state, Rotation rotation, long tick) {
        this.state = state;
        this.rotation = rotation;
        this.tick = tick;
    }

    public static RotationActionResult ready(Rotation rotation, long tick) {
        return new RotationActionResult(RotationActionState.READY, rotation, tick);
    }

    public static RotationActionResult waiting(Rotation rotation, long tick) {
        return new RotationActionResult(RotationActionState.WAITING, rotation, tick);
    }

    public static RotationActionResult rejected(Rotation rotation, long tick) {
        return new RotationActionResult(RotationActionState.REJECTED, rotation, tick);
    }

    public RotationActionState getState() {
        return this.state;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public long getTick() {
        return this.tick;
    }

    public boolean isReady() {
        return this.state == RotationActionState.READY;
    }

    public boolean isWaiting() {
        return this.state == RotationActionState.WAITING;
    }

    public boolean isRejected() {
        return this.state == RotationActionState.REJECTED;
    }

    public static enum RotationActionState {
        READY,
        WAITING,
        REJECTED;

    }
}


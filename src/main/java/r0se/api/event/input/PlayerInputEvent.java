/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.event.input;

import r0se.api.event.Event;

public class PlayerInputEvent
extends Event {
    private float movementForward;
    private float movementSideways;

    public PlayerInputEvent(float movementForward, float movementSideways) {
        this.movementForward = movementForward;
        this.movementSideways = movementSideways;
    }

    public float getMovementForward() {
        return this.movementForward;
    }

    public void setMovementForward(float movementForward) {
        this.movementForward = movementForward;
    }

    public float getMovementSideways() {
        return this.movementSideways;
    }

    public void setMovementSideways(float movementSideways) {
        this.movementSideways = movementSideways;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.event.rotation;

import r0se.api.event.Event;
import r0se.api.rotation.Rotation;

public class ClientRotationEvent
extends Event {
    private Rotation rotation;

    public ClientRotationEvent(Rotation rotation) {
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }
}


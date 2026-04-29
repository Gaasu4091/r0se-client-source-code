/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.event.game;

import r0se.api.event.Event;

public class KeyEvent
extends Event {
    private final int key;
    private final int action;

    public KeyEvent(int key, int action) {
        this.key = key;
        this.action = action;
    }

    public int getKey() {
        return this.key;
    }

    public int getAction() {
        return this.action;
    }

    public boolean isPressed() {
        return this.action == 1;
    }

    public boolean isReleased() {
        return this.action == 0;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.feature;

public enum BindMode {
    TOGGLE("Toggle"),
    HOLD("Hold"),
    REVERSE_HOLD("ReverseHold");

    private final String displayName;

    private BindMode(String displayName) {
        this.displayName = displayName;
    }

    public String toString() {
        return this.displayName;
    }
}


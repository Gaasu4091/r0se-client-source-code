/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.settings;

import r0se.api.settings.IntSetting;

public class KeyBindSetting
extends IntSetting {
    public static final int KEY_NONE = -1;

    public KeyBindSetting(String name, int defaultValue) {
        super(name, "Keyboard key binding", defaultValue, -1, 348);
    }

    @Override
    protected Integer sanitize(Integer value) {
        if (value == null || value == -1) {
            return -1;
        }
        if (value >= 0 && value <= 7) {
            return value;
        }
        if (value >= 32 && value <= 348) {
            return value;
        }
        return -1;
    }
}


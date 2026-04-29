/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.event.feature;

import r0se.api.event.Event;
import r0se.api.feature.ToggleableFeature;

public class ModuleToggleEvent
extends Event {
    private final ToggleableFeature feature;
    private final boolean enabled;

    public ModuleToggleEvent(ToggleableFeature feature, boolean enabled) {
        this.feature = feature;
        this.enabled = enabled;
    }

    public ToggleableFeature getFeature() {
        return this.feature;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}


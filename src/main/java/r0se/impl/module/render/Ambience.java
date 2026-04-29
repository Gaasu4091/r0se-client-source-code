/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.module.render;

import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.settings.BoolSetting;

public class Ambience
extends ToggleableFeature {
    private final BoolSetting fullBright = this.addSetting(new BoolSetting("FullBright", true));

    public Ambience() {
        super("Ambience", "Modifies visual ambiance including FullBright", FeatureCategory.RENDER, new String[0]);
    }

    public boolean getFullbright() {
        return this.isEnabled() && (Boolean)this.fullBright.getValue() != false;
    }
}


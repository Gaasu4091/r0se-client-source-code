/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.feature;

import r0se.R0SE;
import r0se.api.feature.Feature;
import r0se.api.feature.FeatureCategory;

public abstract class ConcurrentFeature
extends Feature {
    protected ConcurrentFeature(String name, String description, FeatureCategory category, String ... aliases) {
        this(name, ConcurrentFeature.normalizeIdentifier(name), description, category, aliases);
    }

    protected ConcurrentFeature(String name, String identifier, String description, FeatureCategory category, String ... aliases) {
        super(name, identifier, description, category, aliases);
    }

    @Override
    public void onRegistered() {
        R0SE.eventHandler.subscribe(this);
    }

    public boolean isEnabled() {
        return true;
    }
}


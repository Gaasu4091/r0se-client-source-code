/*
 * Decompiled with CFR 0.152.
 */
package r0se.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import r0se.api.feature.Feature;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.impl.module.client.AntiCheat;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.impl.module.client.Inventory;
import r0se.impl.module.client.Rotations;
import r0se.impl.module.client.Targeting;
import r0se.impl.module.combat.FeetPlace;
import r0se.impl.module.hud.HUD;
import r0se.impl.module.hud.modules.DirectionModule;
import r0se.impl.module.hud.modules.EffectsModule;
import r0se.impl.module.hud.modules.MetricsModule;
import r0se.impl.module.hud.modules.ModuleListModule;
import r0se.impl.module.hud.modules.PositionModule;
import r0se.impl.module.hud.modules.TextRadarModule;
import r0se.impl.module.hud.modules.TotemsModule;
import r0se.impl.module.hud.modules.WatermarkModule;
import r0se.impl.module.hud.modules.WelcomerModule;
import r0se.impl.module.movement.NoSlow;
import r0se.impl.module.render.Ambience;
import r0se.impl.module.render.BlockHighlight;
import r0se.impl.module.render.NoRender;
import r0se.impl.module.world.SpeedMine;
import r0se.manager.Manager;

public class ModuleManager
implements Manager {
    private final List<Feature> features = new ArrayList<Feature>();

    @Override
    public void init() {
        this.register(new AntiCheat(), new ClickGui(), new Colors(), new Inventory(), new Rotations(), new Targeting(), new FeetPlace(), new HUD(), new DirectionModule(), new EffectsModule(), new MetricsModule(), new ModuleListModule(), new PositionModule(), new TextRadarModule(), new TotemsModule(), new WatermarkModule(), new WelcomerModule(), new NoSlow(), new Ambience(), new BlockHighlight(), new NoRender(), new SpeedMine());
    }

    public void register(Feature ... features) {
        for (Feature feature : features) {
            if (feature == null || this.getFeature(feature.getIdentifier()) != null) continue;
            this.features.add(feature);
            feature.onRegistered();
        }
        this.features.sort((first, second) -> Integer.compare(first.getCategory().ordinal(), second.getCategory().ordinal()));
    }

    public List<Feature> getFeatures() {
        return List.copyOf(this.features);
    }

    public List<ToggleableFeature> getToggleableFeatures() {
        ArrayList<ToggleableFeature> filtered = new ArrayList<ToggleableFeature>();
        for (Feature feature : this.features) {
            if (!(feature instanceof ToggleableFeature)) continue;
            ToggleableFeature toggleableFeature = (ToggleableFeature)feature;
            filtered.add(toggleableFeature);
        }
        return filtered;
    }

    public List<Feature> getFeatures(FeatureCategory category) {
        ArrayList<Feature> filtered = new ArrayList<Feature>();
        for (Feature feature : this.features) {
            if (feature.getCategory() != category) continue;
            filtered.add(feature);
        }
        return filtered;
    }

    public List<FeatureCategory> getCategories() {
        return List.of(FeatureCategory.values());
    }

    public Feature getFeature(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.toLowerCase(Locale.ROOT);
        for (Feature feature : this.features) {
            if (!feature.getIdentifier().equalsIgnoreCase(normalized) && !feature.matches(normalized)) continue;
            return feature;
        }
        return null;
    }

    public <T extends Feature> T getFeature(Class<T> featureClass) {
        for (Feature feature : this.features) {
            if (!featureClass.isInstance(feature)) continue;
            return (T)((Feature)featureClass.cast(feature));
        }
        return null;
    }
}


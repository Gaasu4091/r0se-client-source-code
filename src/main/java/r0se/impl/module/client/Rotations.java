/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.module.client;

import r0se.api.feature.ConcurrentFeature;
import r0se.api.feature.FeatureCategory;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.EnumSetting;

public class Rotations
extends ConcurrentFeature {
    private final EnumSetting<MoveFix> moveFix = this.addSetting(new EnumSetting<MoveFix>("MoveFix", MoveFix.NORMAL));
    private final BoolSetting normalizeMovement = this.addSetting(new BoolSetting("NormalizeMovement", true));
    private final BoolSetting noServerRotate = this.addSetting(new BoolSetting("NoServerRotate", false));
    private final BoolSetting renderRotations = this.addSetting(new BoolSetting("RenderRotations", false));

    public Rotations() {
        super("Rotations", "Controls rotation sync and movement correction", FeatureCategory.CLIENT, "rotation", "rots");
    }

    public EnumSetting<MoveFix> getMoveFix() {
        return this.moveFix;
    }

    public BoolSetting getNormalizeMovement() {
        return this.normalizeMovement;
    }

    public BoolSetting getNoServerRotate() {
        return this.noServerRotate;
    }

    public BoolSetting getRenderRotations() {
        return this.renderRotations;
    }

    public static enum MoveFix {
        OFF,
        NORMAL,
        GRIM;

    }
}


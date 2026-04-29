/*
 * Decompiled with CFR 0.152.
 */
package r0se.manager.impl;

import java.awt.Color;
import r0se.api.render.ColorUtil;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.ColorSyncMode;
import r0se.impl.module.client.Colors;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class ColorManager
implements Manager {
    public Color getAccent() {
        Colors feature = Managers.MODULES.getFeature(Colors.class);
        return feature == null ? new Color(83, 90, 196, 255) : feature.getStyledGlobalColor();
    }

    public Color getAccent(int alpha) {
        return ColorUtil.withAlpha(this.getAccent(), alpha);
    }

    public Color getSecondary() {
        Colors feature = Managers.MODULES.getFeature(Colors.class);
        return feature == null ? new Color(56, 61, 133, 255) : feature.getStyledSecondColor();
    }

    public Color getSecondary(int alpha) {
        return ColorUtil.withAlpha(this.getSecondary(), alpha);
    }

    public Color getFriend() {
        Colors feature = Managers.MODULES.getFeature(Colors.class);
        return feature == null ? new Color(85, 255, 255, 255) : (Color)feature.friendColor.getValue();
    }

    public Color getEnemy() {
        Colors feature = Managers.MODULES.getFeature(Colors.class);
        return feature == null ? new Color(255, 85, 85, 255) : (Color)feature.enemyColor.getValue();
    }

    public Color resolve(ColorSetting setting) {
        Color base = (Color)setting.getValue();
        if (!setting.canSync() || !setting.isSync()) {
            return base;
        }
        Color synced = switch (setting.getSyncMode()) {
            default -> throw new MatchException(null, null);
            case ColorSyncMode.PRIMARY -> this.getAccent();
            case ColorSyncMode.SECONDARY -> this.getSecondary();
            case ColorSyncMode.FRIEND -> this.getFriend();
            case ColorSyncMode.ENEMY -> this.getEnemy();
        };
        return ColorUtil.withAlpha(synced, base.getAlpha());
    }
}


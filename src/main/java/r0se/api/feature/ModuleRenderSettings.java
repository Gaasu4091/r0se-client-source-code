/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.feature;

import java.awt.Color;
import r0se.api.feature.Feature;
import r0se.api.render.BoxRenderMode;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.ColorSyncMode;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.EnumSetting;
import r0se.api.settings.GroupSetting;

public class ModuleRenderSettings {
    private final GroupSetting group;
    private final BoolSetting enabled;
    private final EnumSetting<BoxRenderMode> mode;
    private final ColorSetting fillColor;
    private final ColorSetting outlineColor;
    private final DoubleSetting lineWidth;
    private final BoolSetting fade;
    private final DoubleSetting fadeTime;

    public ModuleRenderSettings(GroupSetting group, BoolSetting enabled, EnumSetting<BoxRenderMode> mode, ColorSetting fillColor, ColorSetting outlineColor, DoubleSetting lineWidth, BoolSetting fade, DoubleSetting fadeTime) {
        this.group = group;
        this.enabled = enabled;
        this.mode = mode;
        this.fillColor = fillColor;
        this.outlineColor = outlineColor;
        this.lineWidth = lineWidth;
        this.fade = fade;
        this.fadeTime = fadeTime;
    }

    public static ModuleRenderSettings create(Feature feature, Color fill, Color outline) {
        GroupSetting group = feature.group("Render", true);
        BoolSetting enabled = (BoolSetting)feature.bool("Render", true).hide();
        group.linkToggle(enabled);
        EnumSetting<BoxRenderMode> mode = feature.grouped(group, feature.mode("RenderMode", BoxRenderMode.BOTH));
        ColorSetting fillColor = feature.grouped(group, feature.syncedColor("Fill", fill, ColorSyncMode.SECONDARY));
        ColorSetting outlineColor = feature.grouped(group, feature.syncedColor("Outline", outline, ColorSyncMode.PRIMARY));
        DoubleSetting lineWidth = feature.grouped(group, feature.decimal("LineWidth", 1.0, 0.1, 5.0));
        BoolSetting fade = feature.grouped(group, feature.bool("Fade", true));
        DoubleSetting fadeTime = feature.grouped(group, feature.decimal("FadeTime", 1.0, 0.1, 3.0));
        return new ModuleRenderSettings(group, enabled, mode, fillColor, outlineColor, lineWidth, fade, fadeTime);
    }

    public boolean isEnabled() {
        return (Boolean)this.enabled.getValue();
    }

    public GroupSetting getGroup() {
        return this.group;
    }

    public EnumSetting<BoxRenderMode> getMode() {
        return this.mode;
    }

    public ColorSetting getFillColor() {
        return this.fillColor;
    }

    public ColorSetting getOutlineColor() {
        return this.outlineColor;
    }

    public DoubleSetting getLineWidth() {
        return this.lineWidth;
    }

    public BoolSetting getFade() {
        return this.fade;
    }

    public DoubleSetting getFadeTime() {
        return this.fadeTime;
    }
}


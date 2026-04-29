/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 */
package r0se.impl.module.hud;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.screen.Screen;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.world.TickEvent;
import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.ColorSyncMode;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.EnumSetting;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.Setting;
import r0se.impl.gui.hud.HudEditorScreen;

public class HUD
extends ToggleableFeature {
    private final BoolSetting hudEditor = this.addSetting(new BoolSetting("HudEditor", false));
    private final EnumSetting<IconMode> icons = this.addSetting(new EnumSetting<IconMode>("Icons", IconMode.HIDE));
    private final DoubleSetting textScale = this.addSetting(new DoubleSetting("TextScale", 1.0, 0.5, 2.0).precision(1));
    private final DoubleSetting smoothWidth = this.addSetting(new DoubleSetting("SmoothWidth", 1.0, 0.0, 4.0).precision(1));
    private final BoolSetting parallax = this.addSetting(new BoolSetting("Parallax", false));
    private final BoolSetting safeZonesEnabled = this.addSetting((BoolSetting)new BoolSetting("Enabled", true).hide());
    private final GroupSetting safeZones = this.addSetting(new GroupSetting("SafeZones", false).linkToggle(this.safeZonesEnabled));
    private final DoubleSetting safeX = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("SafeX", 2.0, 0.0, 40.0).precision(1).insideGroup(this.safeZones)).visibleWhen(this.safeZones::isExpanded));
    private final DoubleSetting safeY = this.addSetting((DoubleSetting)((Setting)new DoubleSetting("SafeY", 1.0, 0.0, 40.0).precision(1).insideGroup(this.safeZones)).visibleWhen(this.safeZones::isExpanded));
    private final BoolSetting colorsEnabled = this.addSetting((BoolSetting)new BoolSetting("Enabled", true).hide());
    private final GroupSetting colors = this.addSetting(new GroupSetting("Colors", false).linkToggle(this.colorsEnabled));
    private final EnumSetting<FadeMode> fade = this.addSetting((EnumSetting)((Setting)new EnumSetting<FadeMode>("Fade", FadeMode.NONE).insideGroup(this.colors)).visibleWhen(this.colors::isExpanded));
    private final ColorSetting color = this.addSetting((ColorSetting)((Setting)new ColorSetting("Color", new Color(210, 180, 255, 255)).enableSync(ColorSyncMode.PRIMARY).insideGroup(this.colors)).visibleWhen(this.colors::isExpanded));

    public HUD() {
        super("HUD", "Shared HUD settings and editor access", FeatureCategory.CLIENT, "overlay");
        this.getDrawn().setValue(false);
        this.getNotify().setValue(false);
    }

    @Override
    public void onRegistered() {
        this.enable();
    }

    @Override
    public void onKeybind() {
        this.openEditor();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (((Boolean)this.hudEditor.getValue()).booleanValue()) {
            this.hudEditor.setValue(false);
            this.openEditor();
        }
    }

    private void openEditor() {
        if (R0SE.mc.currentScreen instanceof HudEditorScreen) {
            R0SE.mc.setScreen(null);
        } else {
            R0SE.mc.setScreen((Screen)new HudEditorScreen());
        }
    }

    public int getSafeX() {
        return (int)Math.round((Double)this.safeX.getValue());
    }

    public int getSafeY() {
        return (int)Math.round((Double)this.safeY.getValue());
    }

    public int getLineStep() {
        Objects.requireNonNull(R0SE.mc.textRenderer);
        return Math.max(9, (int)Math.round(9.0 * (Double)this.textScale.getValue()));
    }

    public int getBottomOffset(boolean chatOffset) {
        return 2;
    }

    public double getTextScale() {
        return (Double)this.textScale.getValue();
    }

    public double getSmoothWidth() {
        return (Double)this.smoothWidth.getValue();
    }

    public boolean isParallax() {
        return (Boolean)this.parallax.getValue();
    }

    public IconMode getIcons() {
        return (IconMode)((Object)this.icons.getValue());
    }

    public FadeMode getFadeMode() {
        return (FadeMode)((Object)this.fade.getValue());
    }

    public ColorSetting getColorSetting() {
        return this.color;
    }

    public static enum IconMode {
        HIDE,
        SHOW;

    }

    public static enum FadeMode {
        NONE;

    }
}



/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.module.render;

import r0se.api.feature.FeatureCategory;
import r0se.api.feature.ToggleableFeature;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.Setting;

public class NoRender
extends ToggleableFeature {
    private final GroupSetting overlayGroup = this.addSetting(new GroupSetting("OverlayGroup", "overlay", false));
    private final BoolSetting darkness = this.addSetting((BoolSetting)((Setting)new BoolSetting("Darkness", false).insideGroup(this.overlayGroup)).visibleWhen(this.overlayGroup::isExpanded));
    private final BoolSetting blindness = this.addSetting((BoolSetting)((Setting)new BoolSetting("Blindness", false).insideGroup(this.overlayGroup)).visibleWhen(this.overlayGroup::isExpanded));
    private final BoolSetting totem = this.addSetting((BoolSetting)((Setting)new BoolSetting("Totem", false).insideGroup(this.overlayGroup)).visibleWhen(this.overlayGroup::isExpanded));
    private final BoolSetting fire = this.addSetting((BoolSetting)((Setting)new BoolSetting("Fire", false).insideGroup(this.overlayGroup)).visibleWhen(this.overlayGroup::isExpanded));
    private final BoolSetting liquid = this.addSetting((BoolSetting)((Setting)new BoolSetting("Liquid", false).insideGroup(this.overlayGroup)).visibleWhen(this.overlayGroup::isExpanded));
    private final BoolSetting wall = this.addSetting((BoolSetting)((Setting)new BoolSetting("Wall", false).insideGroup(this.overlayGroup)).visibleWhen(this.overlayGroup::isExpanded));
    private final BoolSetting blockBreak = this.addSetting((BoolSetting)((Setting)new BoolSetting("BlockBreak", false).insideGroup(this.overlayGroup)).visibleWhen(this.overlayGroup::isExpanded));
    private final GroupSetting hudGroup = this.addSetting(new GroupSetting("HUDGroup", "hud", false));
    private final BoolSetting messageIndicator = this.addSetting((BoolSetting)((Setting)new BoolSetting("MessageIndicator", false).insideGroup(this.hudGroup)).visibleWhen(this.hudGroup::isExpanded));
    private final BoolSetting heldTooltip = this.addSetting((BoolSetting)((Setting)new BoolSetting("HeldTooltip", false).insideGroup(this.hudGroup)).visibleWhen(this.hudGroup::isExpanded));
    private final GroupSetting worldGroup = this.addSetting(new GroupSetting("WorldGroup", "world", false));
    private final BoolSetting fog = this.addSetting((BoolSetting)((Setting)new BoolSetting("Fog", false).insideGroup(this.worldGroup)).visibleWhen(this.worldGroup::isExpanded));
    private final BoolSetting explosions = this.addSetting((BoolSetting)((Setting)new BoolSetting("Explosions", false).insideGroup(this.worldGroup)).visibleWhen(this.worldGroup::isExpanded));
    private final BoolSetting worldBorder = this.addSetting((BoolSetting)((Setting)new BoolSetting("WorldBorder", false).insideGroup(this.worldGroup)).visibleWhen(this.worldGroup::isExpanded));
    private final GroupSetting misc = this.addSetting(new GroupSetting("MiscSetting", "misc", false));
    private final BoolSetting hurt = this.addSetting((BoolSetting)((Setting)new BoolSetting("Hurt", false).insideGroup(this.misc)).visibleWhen(this.misc::isExpanded));
    private final BoolSetting nametags = this.addSetting((BoolSetting)((Setting)new BoolSetting("Nametags", false).insideGroup(this.misc)).visibleWhen(this.misc::isExpanded));
    private final BoolSetting armor = this.addSetting((BoolSetting)((Setting)new BoolSetting("Armor", false).insideGroup(this.misc)).visibleWhen(this.misc::isExpanded));
    private final BoolSetting fireworks = this.addSetting((BoolSetting)((Setting)new BoolSetting("Fireworks", false).insideGroup(this.misc)).visibleWhen(this.misc::isExpanded));

    public NoRender() {
        super("NoRender", "disable render something", FeatureCategory.RENDER, new String[0]);
    }

    private boolean enabledModule() {
        return this.isEnabled();
    }

    public boolean getDarkness() {
        return this.enabledModule() && (Boolean)this.darkness.getValue() != false;
    }

    public boolean getBlindness() {
        return this.enabledModule() && (Boolean)this.blindness.getValue() != false;
    }

    public boolean getTotem() {
        return this.enabledModule() && (Boolean)this.totem.getValue() != false;
    }

    public boolean getFire() {
        return this.enabledModule() && (Boolean)this.fire.getValue() != false;
    }

    public boolean getLiquid() {
        return this.enabledModule() && (Boolean)this.liquid.getValue() != false;
    }

    public boolean getWall() {
        return this.enabledModule() && (Boolean)this.wall.getValue() != false;
    }

    public boolean getBlockBreak() {
        return this.enabledModule() && (Boolean)this.blockBreak.getValue() != false;
    }

    public boolean getMessageIndicator() {
        return this.enabledModule() && (Boolean)this.messageIndicator.getValue() != false;
    }

    public boolean getHeldTooltip() {
        return this.enabledModule() && (Boolean)this.heldTooltip.getValue() != false;
    }

    public boolean getFog() {
        return this.enabledModule() && (Boolean)this.fog.getValue() != false;
    }

    public boolean getExplosions() {
        return this.enabledModule() && (Boolean)this.explosions.getValue() != false;
    }

    public boolean getWorldBorder() {
        return this.enabledModule() && (Boolean)this.worldBorder.getValue() != false;
    }

    public boolean getHurt() {
        return this.enabledModule() && (Boolean)this.hurt.getValue() != false;
    }

    public boolean getNametags() {
        return this.enabledModule() && (Boolean)this.nametags.getValue() != false;
    }

    public boolean getArmor() {
        return this.enabledModule() && (Boolean)this.armor.getValue() != false;
    }
}


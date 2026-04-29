/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.gui.screen.Screen
 */
package r0se.api.feature;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.screen.Screen;
import r0se.R0SE;
import r0se.api.event.feature.ModuleToggleEvent;
import r0se.api.feature.BindMode;
import r0se.api.feature.Feature;
import r0se.api.feature.FeatureCategory;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.EnumSetting;
import r0se.api.settings.KeyBindSetting;
import r0se.impl.gui.clickgui.ClickGuiScreen;
import r0se.manager.Managers;

public abstract class ToggleableFeature
extends Feature {
    protected final KeyBindSetting keyBind = this.addSetting((KeyBindSetting)new KeyBindSetting("Bind", -1).hide());
    protected final EnumSetting<BindMode> bindMode = this.addSetting((EnumSetting)new EnumSetting<BindMode>("BindMode", BindMode.TOGGLE).hide());
    protected final BoolSetting drawn = this.addSetting((BoolSetting)new BoolSetting("Drawn", true).hide());
    protected final BoolSetting notify = this.addSetting((BoolSetting)new BoolSetting("Notify", true).hide());
    private boolean enabled;

    protected ToggleableFeature(String name, String description, FeatureCategory category, String ... aliases) {
        this(name, ToggleableFeature.normalizeIdentifier(name), description, category, aliases);
    }

    protected ToggleableFeature(String name, String identifier, String description, FeatureCategory category, String ... aliases) {
        super(name, identifier, description, category, aliases);
    }

    public void toggle() {
        if (this.enabled) {
            this.disable();
        } else {
            this.enable();
        }
    }

    public void enable() {
        this.setEnabled(true);
    }

    public void disable() {
        this.setEnabled(false);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            R0SE.eventHandler.subscribe(this);
            this.onEnable();
        } else {
            R0SE.eventHandler.unsubscribe(this);
            this.onDisable();
        }
        this.onToggle();
        R0SE.eventHandler.post(new ModuleToggleEvent(this, enabled));
        Managers.CONFIG.markDirty();
    }

    public void onKeybind() {
        this.toggle();
    }

    public void onBindPressed() {
        Screen current = R0SE.mc.currentScreen;
        if (!(current instanceof ClickGuiScreen)) {
            switch ((BindMode)((Object)this.bindMode.getValue())) {
                case TOGGLE: {
                    this.onKeybind();
                    break;
                }
                case HOLD: {
                    this.enable();
                    break;
                }
                case REVERSE_HOLD: {
                    this.disable();
                }
            }
        }
    }

    public void onBindReleased() {
        switch ((BindMode)((Object)this.bindMode.getValue())) {
            case TOGGLE: {
                break;
            }
            case HOLD: {
                this.disable();
                break;
            }
            case REVERSE_HOLD: {
                this.enable();
            }
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = super.toJson();
        object.addProperty("enabled", Boolean.valueOf(this.enabled));
        return object;
    }

    @Override
    public void fromJson(JsonObject object) {
        super.fromJson(object);
        if (object != null && object.has("enabled")) {
            this.setEnabled(object.get("enabled").getAsBoolean());
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public KeyBindSetting getKeyBind() {
        return this.keyBind;
    }

    public EnumSetting<BindMode> getBindMode() {
        return this.bindMode;
    }

    public BoolSetting getDrawn() {
        return this.drawn;
    }

    public BoolSetting getNotify() {
        return this.notify;
    }

    public boolean isDrawn() {
        return (Boolean)this.drawn.getValue();
    }

    public boolean shouldNotify() {
        return (Boolean)this.notify.getValue();
    }

    protected boolean shouldRun() {
        return this.enabled && !this.checkNull();
    }

    protected boolean shouldRunWithScreen() {
        return this.enabled && ToggleableFeature.mc.player != null && ToggleableFeature.mc.world != null;
    }

    protected void setNotifyEnabled(boolean value) {
        this.notify.setValue(value);
    }

    protected void setDrawnEnabled(boolean value) {
        this.drawn.setValue(value);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected void onToggle() {
    }
}



/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package r0se.api.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;
import r0se.api.settings.ColorSyncMode;
import r0se.api.settings.Setting;
import r0se.manager.Managers;

public class ColorSetting
extends Setting<Color> {
    private static Color clipboardColor;
    private boolean allowSync;
    private boolean sync;
    private ColorSyncMode syncMode = ColorSyncMode.PRIMARY;

    public ColorSetting(String name, Color defaultValue) {
        super(name, defaultValue);
    }

    public ColorSetting(String name, String description, Color defaultValue) {
        super(name, description, defaultValue);
    }

    public ColorSetting enableSync(ColorSyncMode mode) {
        this.allowSync = true;
        this.syncMode = mode == null ? ColorSyncMode.PRIMARY : mode;
        return this;
    }

    public boolean canSync() {
        return this.allowSync;
    }

    public boolean isSync() {
        return this.allowSync && this.sync;
    }

    public void setSync(boolean sync) {
        if (!this.allowSync || this.sync == sync) {
            return;
        }
        this.sync = sync;
        Managers.CONFIG.markDirty();
    }

    public void toggleSync() {
        this.setSync(!this.isSync());
    }

    public ColorSyncMode getSyncMode() {
        return this.syncMode;
    }

    public void copy() {
        clipboardColor = (Color)this.getValue();
    }

    public boolean canPaste() {
        return clipboardColor != null;
    }

    public void paste() {
        if (clipboardColor != null) {
            this.setValue(new Color(clipboardColor.getRed(), clipboardColor.getGreen(), clipboardColor.getBlue(), clipboardColor.getAlpha()));
        }
    }

    @Override
    protected JsonElement serializeValue() {
        JsonObject object = new JsonObject();
        Color color = (Color)this.getValue();
        object.addProperty("r", (Number)color.getRed());
        object.addProperty("g", (Number)color.getGreen());
        object.addProperty("b", (Number)color.getBlue());
        object.addProperty("a", (Number)color.getAlpha());
        if (this.allowSync) {
            object.addProperty("sync", Boolean.valueOf(this.sync));
        }
        return object;
    }

    @Override
    protected Color deserializeValue(JsonElement element) {
        if (!element.isJsonObject()) {
            return (Color)this.getDefaultValue();
        }
        JsonObject object = element.getAsJsonObject();
        Color color = new Color(object.has("r") ? object.get("r").getAsInt() : ((Color)this.getDefaultValue()).getRed(), object.has("g") ? object.get("g").getAsInt() : ((Color)this.getDefaultValue()).getGreen(), object.has("b") ? object.get("b").getAsInt() : ((Color)this.getDefaultValue()).getBlue(), object.has("a") ? object.get("a").getAsInt() : ((Color)this.getDefaultValue()).getAlpha());
        if (this.allowSync && object.has("sync")) {
            this.sync = object.get("sync").getAsBoolean();
        }
        return color;
    }
}


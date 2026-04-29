/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 */
package r0se.api.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import r0se.api.settings.Setting;

public class BoolSetting
extends Setting<Boolean> {
    public BoolSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public BoolSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    protected JsonElement serializeValue() {
        return new JsonPrimitive((Boolean)this.getValue());
    }

    @Override
    protected Boolean deserializeValue(JsonElement element) {
        return element.getAsBoolean();
    }
}


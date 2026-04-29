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

public class IntSetting
extends Setting<Integer> {
    private final int min;
    private final int max;

    public IntSetting(String name, int defaultValue, int min, int max) {
        this(name, null, defaultValue, min, max);
    }

    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.setValue(defaultValue);
    }

    @Override
    protected Integer sanitize(Integer value) {
        if (value == null) {
            return (Integer)this.getDefaultValue();
        }
        return Math.max(this.min, Math.min(this.max, value));
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    @Override
    protected JsonElement serializeValue() {
        return new JsonPrimitive((Number)this.getValue());
    }

    @Override
    protected Integer deserializeValue(JsonElement element) {
        return element.getAsInt();
    }
}


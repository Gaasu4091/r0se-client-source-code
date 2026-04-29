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

public class DoubleSetting
extends Setting<Double> {
    private final double min;
    private final double max;
    private int precision = 2;
    private String suffix = "";

    public DoubleSetting(String name, double defaultValue, double min, double max) {
        this(name, null, defaultValue, min, max);
    }

    public DoubleSetting(String name, String description, double defaultValue, double min, double max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.setValue(defaultValue);
    }

    @Override
    protected Double sanitize(Double value) {
        if (value == null) {
            return (Double)this.getDefaultValue();
        }
        double clamped = Math.max(this.min, Math.min(this.max, value));
        if (this.precision <= 0) {
            return (double)Math.round(clamped);
        }
        double scale = Math.pow(10.0, this.precision);
        return (double)Math.round(clamped * scale) / scale;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public DoubleSetting precision(int precision) {
        this.precision = Math.max(0, precision);
        return this;
    }

    public DoubleSetting suffix(String suffix) {
        this.suffix = suffix == null ? "" : suffix;
        return this;
    }

    public int getPrecision() {
        return this.precision;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public String format(double value) {
        return "%." + this.precision + "f" + this.suffix;
    }

    @Override
    protected JsonElement serializeValue() {
        return new JsonPrimitive((Number)this.getValue());
    }

    @Override
    protected Double deserializeValue(JsonElement element) {
        return element.getAsDouble();
    }
}

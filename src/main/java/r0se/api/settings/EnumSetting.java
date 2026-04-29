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

public class EnumSetting<E extends Enum<E>>
extends Setting<E> {
    private final Class<E> enumClass;

    public EnumSetting(String name, E defaultValue) {
        this(name, (String)null, defaultValue);
    }

    public EnumSetting(String name, String description, E defaultValue) {
        super(name, description, defaultValue);
        this.enumClass = ((Enum)defaultValue).getDeclaringClass();
    }

    @SuppressWarnings(value={"unchecked"})
    public E[] getValues() {
        return (E[])this.enumClass.getEnumConstants();
    }

    @Override
    protected JsonElement serializeValue() {
        return new JsonPrimitive(((Enum)this.getValue()).name());
    }

    @Override
    protected E deserializeValue(JsonElement element) {
        String name = element.getAsString();
        try {
            return Enum.valueOf(this.enumClass, name);
        }
        catch (IllegalArgumentException ignored) {
            return (E)((Enum)this.getDefaultValue());
        }
    }
}

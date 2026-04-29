/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 */
package r0se.api.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.Setting;

public class GroupSetting
extends Setting<Boolean> {
    private BoolSetting toggleSetting;
    private String displayName;

    public GroupSetting(String name) {
        this(name, false);
    }

    public GroupSetting(String name, boolean expanded) {
        super(name, "Expandable setting group", expanded);
        this.displayName = name;
    }

    public GroupSetting(String name, String displayName, boolean expanded) {
        super(name, "Expandable setting group", expanded);
        this.displayName = displayName == null || displayName.isBlank() ? name : displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public GroupSetting displayName(String displayName) {
        this.displayName = displayName == null || displayName.isBlank() ? this.getName() : displayName;
        return this;
    }

    public boolean isExpanded() {
        return (Boolean)this.getValue();
    }

    public boolean hasToggleSetting() {
        return this.toggleSetting != null;
    }

    public BoolSetting getToggleSetting() {
        return this.toggleSetting;
    }

    public GroupSetting linkToggle(BoolSetting toggleSetting) {
        this.toggleSetting = toggleSetting;
        return this;
    }

    public boolean isToggled() {
        return this.toggleSetting == null || (Boolean)this.toggleSetting.getValue() != false;
    }

    public void toggleLinkedValue() {
        if (this.toggleSetting != null) {
            this.toggleSetting.setValue((Boolean)this.toggleSetting.getValue() == false);
        }
    }

    public void toggleExpanded() {
        this.setValue((Boolean)this.getValue() == false);
    }

    @Override
    protected JsonElement serializeValue() {
        if (this.toggleSetting == null) {
            return new JsonPrimitive((Boolean)this.getValue());
        }
        JsonObject object = new JsonObject();
        object.addProperty("expanded", (Boolean)this.getValue());
        object.addProperty("enabled", (Boolean)this.toggleSetting.getValue());
        return object;
    }

    @Override
    protected Boolean deserializeValue(JsonElement element) {
        if (element == null) {
            return (Boolean)this.getDefaultValue();
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (this.toggleSetting != null && object.has("enabled") && object.get("enabled").isJsonPrimitive()) {
                this.toggleSetting.setValue(object.get("enabled").getAsBoolean());
            }
            if (object.has("expanded") && object.get("expanded").isJsonPrimitive()) {
                return object.get("expanded").getAsBoolean();
            }
            return (Boolean)this.getDefaultValue();
        }
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean() ? element.getAsBoolean() : ((Boolean)this.getDefaultValue()).booleanValue();
    }
}


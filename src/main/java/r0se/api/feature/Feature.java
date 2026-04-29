/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package r0se.api.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;
import r0se.api.feature.FeatureCategory;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.ColorSyncMode;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.EnumSetting;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.IntSetting;
import r0se.api.settings.Setting;
import r0se.api.settings.container.SettingContainer;
import r0se.manager.Managers;

public abstract class Feature
extends SettingContainer {
    private final String description;
    private final FeatureCategory category;
    private boolean expanded;

    protected Feature(String name, String description, FeatureCategory category, String ... aliases) {
        this(name, Feature.normalizeIdentifier(name), description, category, aliases);
    }

    protected Feature(String name, String identifier, String description, FeatureCategory category, String ... aliases) {
        super(name, identifier, aliases);
        this.description = description;
        this.category = category;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("expanded", Boolean.valueOf(this.expanded));
        JsonObject settingsObject = new JsonObject();
        for (Setting<?> setting : this.getSettings()) {
            JsonElement element = setting.toJson();
            if (element == null) continue;
            settingsObject.add(setting.getName(), element);
        }
        object.add("settings", (JsonElement)settingsObject);
        return object;
    }

    @Override
    public void fromJson(JsonObject object) {
        if (object == null) {
            return;
        }
        if (object.has("expanded")) {
            this.expanded = object.get("expanded").getAsBoolean();
        }
        if (object.has("settings") && object.get("settings").isJsonObject()) {
            JsonObject settingsObject = object.getAsJsonObject("settings");
            for (Setting<?> setting : this.getSettings()) {
                if (!settingsObject.has(setting.getName())) continue;
                setting.fromJson(settingsObject.get(setting.getName()));
            }
        }
    }

    public String getDescription() {
        return this.description;
    }

    public FeatureCategory getCategory() {
        return this.category;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) {
            return;
        }
        this.expanded = expanded;
        Managers.CONFIG.markDirty();
    }

    public void onRegistered() {
    }

    public String getMetaData() {
        return "";
    }

    protected BoolSetting bool(String name, boolean defaultValue) {
        return this.addSetting(new BoolSetting(name, defaultValue));
    }

    protected BoolSetting bool(String name, String description, boolean defaultValue) {
        return this.addSetting(new BoolSetting(name, description, defaultValue));
    }

    protected IntSetting integer(String name, int defaultValue, int min, int max) {
        return this.addSetting(new IntSetting(name, defaultValue, min, max));
    }

    protected DoubleSetting decimal(String name, double defaultValue, double min, double max) {
        return this.addSetting(new DoubleSetting(name, defaultValue, min, max));
    }

    protected <E extends Enum<E>> EnumSetting<E> mode(String name, E defaultValue) {
        return this.addSetting(new EnumSetting<E>(name, defaultValue));
    }

    protected ColorSetting color(String name, Color defaultValue) {
        return this.addSetting(new ColorSetting(name, defaultValue));
    }

    protected ColorSetting syncedColor(String name, Color defaultValue, ColorSyncMode syncMode) {
        return this.addSetting(new ColorSetting(name, defaultValue).enableSync(syncMode));
    }

    protected GroupSetting group(String name) {
        return this.addSetting(new GroupSetting(name));
    }

    protected GroupSetting group(String name, boolean expanded) {
        return this.addSetting(new GroupSetting(name, expanded));
    }

    protected <T extends Setting<?>> T grouped(GroupSetting group, T setting) {
        setting.insideGroup(group);
        if (setting.getParent() == null) {
            this.addSetting(setting);
        }
        return setting;
    }

    protected void markDirty() {
        Managers.CONFIG.markDirty();
    }

    protected boolean isNull() {
        return Feature.mc.player == null || Feature.mc.world == null;
    }

    protected static String normalizeIdentifier(String name) {
        return name.toLowerCase().replace(" ", "_");
    }
}



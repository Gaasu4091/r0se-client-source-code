/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  org.jetbrains.annotations.Nullable
 */
package r0se.api.settings;

import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.container.SettingContainer;
import r0se.manager.Managers;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;
    private Supplier<Boolean> visible = () -> true;
    private SettingContainer parent;
    private GroupSetting group;

    protected Setting(String name, T defaultValue) {
        this(name, null, defaultValue);
    }

    protected Setting(String name, @Nullable String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        T sanitized = this.sanitize(value);
        if (Objects.equals(this.value, sanitized)) {
            return;
        }
        this.value = sanitized;
        Managers.CONFIG.markDirty();
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isVisible() {
        return this.visible.get();
    }

    public <S extends Setting<T>> S visibleWhen(Supplier<Boolean> visible) {
        this.visible = visible == null ? () -> true : visible;
        return (S)this;
    }

    public <S extends Setting<T>> S hide() {
        this.visible = () -> false;
        return (S)this;
    }

    public SettingContainer getParent() {
        return this.parent;
    }

    public void setParent(SettingContainer parent) {
        this.parent = parent;
    }

    public GroupSetting getGroup() {
        return this.group;
    }

    public <S extends Setting<T>> S insideGroup(GroupSetting group) {
        this.group = group;
        return (S)this;
    }

    public JsonElement toJson() {
        return this.serializeValue();
    }

    public void fromJson(JsonElement element) {
        if (element != null) {
            this.setValue(this.deserializeValue(element));
        }
    }

    protected T sanitize(T value) {
        return value;
    }

    protected abstract JsonElement serializeValue();

    protected abstract T deserializeValue(JsonElement var1);
}


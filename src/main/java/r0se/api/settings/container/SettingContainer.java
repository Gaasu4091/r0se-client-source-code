/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package r0se.api.settings.container;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import r0se.api.GenericFeature;
import r0se.api.JsonSerializable;
import r0se.api.settings.Setting;

public abstract class SettingContainer
extends GenericFeature
implements JsonSerializable {
    protected final List<Setting<?>> settings = new ArrayList();

    protected SettingContainer(String name, String ... aliases) {
        super(name, aliases);
    }

    protected SettingContainer(String name, String identifier, String ... aliases) {
        super(name, identifier, aliases);
    }

    public <T extends Setting<?>> T addSetting(T setting) {
        setting.setParent(this);
        this.settings.add(setting);
        return setting;
    }

    @Nullable
    public Setting<?> getSetting(String name) {
        if (name == null) {
            return null;
        }
        for (Setting<?> setting : this.settings) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }
        return null;
    }

    public List<Setting<?>> getSettings() {
        return this.settings;
    }
}


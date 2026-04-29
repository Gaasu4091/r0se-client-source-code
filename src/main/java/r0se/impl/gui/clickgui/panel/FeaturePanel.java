/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.gui.clickgui.panel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import r0se.api.feature.Feature;
import r0se.api.feature.ToggleableFeature;
import r0se.api.settings.BoolSetting;
import r0se.api.settings.ColorSetting;
import r0se.api.settings.DoubleSetting;
import r0se.api.settings.EnumSetting;
import r0se.api.settings.GroupSetting;
import r0se.api.settings.IntSetting;
import r0se.api.settings.KeyBindSetting;
import r0se.api.settings.Setting;
import r0se.impl.gui.base.BasePanel;
import r0se.impl.gui.clickgui.panel.setting.BoolSettingPanel;
import r0se.impl.gui.clickgui.panel.setting.ColorSettingPanel;
import r0se.impl.gui.clickgui.panel.setting.DoubleSettingPanel;
import r0se.impl.gui.clickgui.panel.setting.EnumSettingPanel;
import r0se.impl.gui.clickgui.panel.setting.GroupSettingPanel;
import r0se.impl.gui.clickgui.panel.setting.IntSettingPanel;
import r0se.impl.gui.clickgui.panel.setting.ModuleBindPanel;
import r0se.impl.module.client.ClickGui;
import r0se.impl.module.client.Colors;
import r0se.manager.Managers;

public class FeaturePanel
extends BasePanel {
    public static final int HEIGHT = 13;
    private final Feature feature;
    private final ToggleableFeature toggleableFeature;
    private final List<Setting<?>> allSettings = new ArrayList();
    private final Map<Setting<?>, BasePanel> settingPanels = new HashMap();

    public FeaturePanel(Feature feature) {
        ToggleableFeature toggleable;
        this.feature = feature;
        this.toggleableFeature = feature instanceof ToggleableFeature ? (toggleable = (ToggleableFeature)feature) : null;
        this.height = 13;
        this.expanded = feature.isExpanded();
        for (Setting<?> setting : feature.getSettings()) {
            if (setting == null) continue;
            this.allSettings.add(setting);
        }
        this.refreshSettings();
    }

    @Override
    protected String getName() {
        return this.feature.getName();
    }

    @Override
    protected boolean isEnabled() {
        return this.toggleableFeature == null || this.toggleableFeature.isEnabled();
    }

    @Override
    protected Colors getColorFeature() {
        return Managers.MODULES.getFeature(Colors.class);
    }

    @Override
    protected ClickGui getClickGuiFeature() {
        return Managers.MODULES.getFeature(ClickGui.class);
    }

    @Override
    public void onLeftClick() {
        if (this.toggleableFeature != null) {
            this.toggleableFeature.toggle();
        }
    }

    @Override
    public void onMiddleClick() {
        if (this.toggleableFeature != null) {
            this.toggleableFeature.getDrawn().setValue(!this.toggleableFeature.isDrawn());
        }
    }

    @Override
    public void onRightClick() {
        this.expanded = !this.expanded;
        this.feature.setExpanded(this.expanded);
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void refreshSettings() {
        Iterator<Map.Entry<Setting<?>, BasePanel>> it = this.settingPanels.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Setting<?>, BasePanel> entry = it.next();
            if (entry.getKey() == null || entry.getKey().isVisible()) continue;
            this.getSubPanels().remove(entry.getValue());
            it.remove();
        }
        this.getSubPanels().clear();
        List<Setting<?>> orderedSettings = new ArrayList<Setting<?>>(this.allSettings);
        orderedSettings.sort(Comparator.comparingInt(this::settingOrder));
        for (Setting<?> setting : orderedSettings) {
            if (setting == null || !setting.isVisible() || this.toggleableFeature != null && (setting == this.toggleableFeature.getKeyBind() || setting == this.toggleableFeature.getBindMode())) continue;
            BasePanel panel = this.settingPanels.get(setting);
            if (panel == null) {
                if (setting instanceof BoolSetting) {
                    BoolSetting boolSetting = (BoolSetting)setting;
                    panel = new BoolSettingPanel(boolSetting);
                } else if (setting instanceof GroupSetting) {
                    GroupSetting groupSetting = (GroupSetting)setting;
                    panel = new GroupSettingPanel(groupSetting);
                } else if (setting instanceof ColorSetting) {
                    ColorSetting colorSetting = (ColorSetting)setting;
                    panel = new ColorSettingPanel(colorSetting);
                } else if (setting instanceof IntSetting) {
                    IntSetting intSetting = (IntSetting)setting;
                    panel = new IntSettingPanel(intSetting);
                } else if (setting instanceof DoubleSetting) {
                    DoubleSetting doubleSetting = (DoubleSetting)setting;
                    panel = new DoubleSettingPanel(doubleSetting);
                } else if (setting instanceof EnumSetting) {
                    EnumSetting enumSetting = (EnumSetting)setting;
                    panel = new EnumSettingPanel(enumSetting);
                }
            }
            if (panel != null) {
                this.settingPanels.put(setting, panel);
            }
            if (panel == null) continue;
            this.addSubPanel(panel);
        }
        if (this.toggleableFeature != null) {
            KeyBindSetting bindKey = this.toggleableFeature.getKeyBind();
            BasePanel bindPanel = this.settingPanels.get(bindKey);
            if (!(bindPanel instanceof ModuleBindPanel)) {
                bindPanel = new ModuleBindPanel(this.toggleableFeature);
                this.settingPanels.put(bindKey, bindPanel);
            }
            this.addSubPanel(bindPanel);
        }
    }

    private int settingOrder(Setting<?> setting) {
        if (this.toggleableFeature != null) {
            if (setting == this.toggleableFeature.getKeyBind()) {
                return 10000;
            }
            if (setting == this.toggleableFeature.getBindMode()) {
                return 9999;
            }
        }
        return this.allSettings.indexOf(setting);
    }
}

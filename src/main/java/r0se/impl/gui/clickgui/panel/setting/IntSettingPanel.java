/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.gui.clickgui.panel.setting;

import r0se.api.settings.IntSetting;
import r0se.impl.gui.clickgui.panel.setting.NumberSettingPanel;

public class IntSettingPanel
extends NumberSettingPanel<Integer> {
    private final IntSetting setting;

    public IntSettingPanel(IntSetting setting) {
        this.setting = setting;
    }

    @Override
    protected String getSettingName() {
        return this.setting.getName();
    }

    @Override
    protected double getValue() {
        return ((Integer)this.setting.getValue()).intValue();
    }

    @Override
    protected double getMin() {
        return this.setting.getMin();
    }

    @Override
    protected double getMax() {
        return this.setting.getMax();
    }

    @Override
    protected void setValueFromDouble(double value) {
        this.setting.setValue((int)Math.round(value));
    }

    @Override
    protected String formatValue(double value) {
        return String.valueOf((int)Math.round(value));
    }
}


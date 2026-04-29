/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.gui.clickgui.panel.setting;

import java.util.Locale;
import r0se.api.settings.DoubleSetting;
import r0se.impl.gui.clickgui.panel.setting.NumberSettingPanel;

public class DoubleSettingPanel
extends NumberSettingPanel<Double> {
    private final DoubleSetting setting;

    public DoubleSettingPanel(DoubleSetting setting) {
        this.setting = setting;
    }

    @Override
    protected String getSettingName() {
        return this.setting.getName();
    }

    @Override
    protected double getValue() {
        return (Double)this.setting.getValue();
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
        this.setting.setValue(value);
    }

    @Override
    protected String formatValue(double value) {
        return String.format(Locale.ROOT, this.setting.format(value), value);
    }
}


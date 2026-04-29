/*
 * Decompiled with CFR 0.152.
 */
package r0se.impl.module.client;

import r0se.api.feature.ConcurrentFeature;
import r0se.api.feature.FeatureCategory;
import r0se.api.inventory.SilentSwapType;
import r0se.api.inventory.SwapMode;
import r0se.api.settings.EnumSetting;

public class Inventory
extends ConcurrentFeature {
    private final EnumSetting<SwapMode> swapMode = this.addSetting(new EnumSetting<SwapMode>("SwapMode", SwapMode.SILENT));
    private final EnumSetting<SilentSwapType> silentSwapType = this.addSetting(new EnumSetting<SilentSwapType>("SilentSwapType", SilentSwapType.HOTBAR));

    public Inventory() {
        super("Inventory", "Client inventory settings and swap behavior.", FeatureCategory.CLIENT, new String[0]);
    }

    public EnumSetting<SwapMode> getSwapMode() {
        return this.swapMode;
    }

    public EnumSetting<SilentSwapType> getSilentSwapType() {
        return this.silentSwapType;
    }
}


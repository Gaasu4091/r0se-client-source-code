/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.feature;

public enum FeatureCategory {
    COMBAT("Combat"),
    EXPLOIT("Exploit"),
    MISCELLANEOUS("Miscellaneous"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    CLIENT("Client"),
    HUD("HUD");

    private final String displayName;

    private FeatureCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}


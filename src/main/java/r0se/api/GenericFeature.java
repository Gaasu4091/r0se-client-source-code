/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 */
package r0se.api;

import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import r0se.api.Identifiable;

public abstract class GenericFeature
implements Identifiable {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    private final String identifier;
    private final String name;
    private final String[] aliases;

    protected GenericFeature(String name, String ... aliases) {
        this(name, GenericFeature.normalizeIdentifier(name), aliases);
    }

    protected GenericFeature(String name, String identifier, String ... aliases) {
        this.name = name;
        this.identifier = identifier;
        this.aliases = aliases == null ? new String[]{} : aliases;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String[] getAliases() {
        return (String[])this.aliases.clone();
    }

    public boolean matches(String input) {
        if (input == null) {
            return false;
        }
        String normalized = input.toLowerCase(Locale.ROOT);
        if (this.name.toLowerCase(Locale.ROOT).equals(normalized) || this.identifier.toLowerCase(Locale.ROOT).equals(normalized)) {
            return true;
        }
        for (String alias : this.aliases) {
            if (alias == null || !alias.toLowerCase(Locale.ROOT).equals(normalized)) continue;
            return true;
        }
        return false;
    }

    protected boolean checkNull() {
        return GenericFeature.mc.player == null || GenericFeature.mc.world == null;
    }

    private static String normalizeIdentifier(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "_");
    }
}



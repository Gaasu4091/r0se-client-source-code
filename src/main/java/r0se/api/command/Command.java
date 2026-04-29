/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 */
package r0se.api.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.LinkedHashSet;
import java.util.Set;
import r0se.api.GenericFeature;

public abstract class Command
extends GenericFeature {
    private final String description;
    private final Set<LiteralArgumentBuilder<Object>> argumentBuilders = new LinkedHashSet<LiteralArgumentBuilder<Object>>();

    protected Command(String name, String description, String ... aliases) {
        super(name, aliases);
        this.description = description;
        this.argumentBuilders.add((LiteralArgumentBuilder<Object>)LiteralArgumentBuilder.literal((String)name));
        if (aliases != null) {
            for (String alias : aliases) {
                if (alias == null || alias.isBlank()) continue;
                this.argumentBuilders.add((LiteralArgumentBuilder<Object>)LiteralArgumentBuilder.literal((String)alias));
            }
        }
    }

    public String getDescription() {
        return this.description;
    }

    public Set<LiteralArgumentBuilder<Object>> getArgumentBuilders() {
        return this.argumentBuilders;
    }

    public abstract void build(LiteralArgumentBuilder<Object> var1);

    protected <T> RequiredArgumentBuilder<Object, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument((String)name, type);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package r0se.api.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import r0se.api.command.Command;
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class PrefixCommand
extends Command {
    public PrefixCommand() {
        super("prefix", "Changes the chat command prefix", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.executes(context -> {
            ChatUtil.info("Prefix", "Current prefix: " + Managers.COMMANDS.getPrefix());
            return 1;
        });
        builder.then(this.argument("value", StringArgumentType.greedyString()).executes(context -> {
            String value = StringArgumentType.getString((CommandContext)context, (String)"value").trim();
            if (value.isEmpty()) {
                ChatUtil.error("Prefix", "Prefix cannot be empty");
                return 0;
            }
            Managers.COMMANDS.setPrefix(value);
            ChatUtil.info("Prefix", "Updated prefix to: " + value);
            return 1;
        }));
    }
}


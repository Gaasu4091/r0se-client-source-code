/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.text.Text
 */
package r0se.api.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.text.Text;
import r0se.api.command.Command;
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super("help", "Lists available commands", new String[]{"h"});
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.executes(context -> {
            ChatUtil.info("Commands", "Prefix: " + Managers.COMMANDS.getPrefix());
            Managers.COMMANDS.getCommands().forEach(command -> ChatUtil.send((Text)Text.literal((String)(Managers.COMMANDS.getPrefix() + command.getName() + " - " + command.getDescription()))));
            return 1;
        });
    }
}



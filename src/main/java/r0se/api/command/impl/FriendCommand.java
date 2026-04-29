/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package r0se.api.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Set;
import r0se.api.command.Command;
import r0se.api.social.SocialType;
import r0se.manager.Managers;
import r0se.util.chat.ChatUtil;

public class FriendCommand
extends Command {
    public FriendCommand() {
        super("friend", "Manages friend socials", new String[]{"friends", "f"});
    }

    @Override
    public void build(LiteralArgumentBuilder<Object> builder) {
        builder.executes(context -> {
            this.listFriends();
            return 1;
        });
        builder.then(((RequiredArgumentBuilder)this.argument("action", StringArgumentType.word()).executes(context -> {
            String action = StringArgumentType.getString((CommandContext)context, (String)"action");
            if (action.equalsIgnoreCase("list")) {
                this.listFriends();
                return 1;
            }
            ChatUtil.error("Friend", "Usage: friend <add|remove|toggle|list> [name]");
            return 0;
        })).then(this.argument("name", StringArgumentType.greedyString()).executes(context -> {
            String action = StringArgumentType.getString((CommandContext)context, (String)"action");
            String name = StringArgumentType.getString((CommandContext)context, (String)"name").trim();
            if (name.isEmpty()) {
                ChatUtil.error("Friend", "Name cannot be empty");
                return 0;
            }
            switch (action.toLowerCase()) {
                case "add": {
                    Managers.SOCIAL.addFriend(name);
                    ChatUtil.info("Friend", "Added " + name);
                    return 1;
                }
                case "remove": 
                case "del": 
                case "delete": {
                    boolean removed = Managers.SOCIAL.removeFriend(name);
                    ChatUtil.info("Friend", removed ? "Removed " + name : name + " was not a friend");
                    return removed ? 1 : 0;
                }
                case "toggle": {
                    boolean enabled = Managers.SOCIAL.toggleFriend(name);
                    ChatUtil.info("Friend", (enabled ? "Added " : "Removed ") + name);
                    return 1;
                }
            }
            ChatUtil.error("Friend", "Unknown action: " + action);
            return 0;
        })));
    }

    private void listFriends() {
        Set<String> friends = Managers.SOCIAL.getByType(SocialType.FRIEND);
        if (friends.isEmpty()) {
            ChatUtil.info("Friend", "No friends added");
            return;
        }
        ChatUtil.info("Friend", "Friends: " + String.join((CharSequence)", ", friends));
    }
}


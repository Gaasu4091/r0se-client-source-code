/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package r0se.manager.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import r0se.R0SE;
import r0se.api.command.Command;
import r0se.api.command.impl.BindCommand;
import r0se.api.command.impl.ConfigCommand;
import r0se.api.command.impl.DrawnCommand;
import r0se.api.command.impl.EnemyCommand;
import r0se.api.command.impl.FriendCommand;
import r0se.api.command.impl.HelpCommand;
import r0se.api.command.impl.PrefixCommand;
import r0se.api.command.impl.ToggleCommand;
import r0se.manager.Manager;
import r0se.util.chat.ChatUtil;

public class CommandManager
implements Manager {
    private static final String DEFAULT_PREFIX = ".";
    private final List<Command> commands = new ArrayList<Command>();
    private final CommandDispatcher<Object> dispatcher = new CommandDispatcher();
    private final Object source = new Object();
    private Path clientDirectory;
    private Path commandConfig;
    private String prefix = ".";

    @Override
    public void init() {
        this.clientDirectory = R0SE.mc.runDirectory.toPath().resolve("r0se");
        this.commandConfig = this.clientDirectory.resolve("commands.json");
        this.registerCommands(new HelpCommand(), new ToggleCommand(), new BindCommand(), new DrawnCommand(), new PrefixCommand(), new ConfigCommand(), new FriendCommand(), new EnemyCommand());
        for (Command command : this.commands) {
            command.getArgumentBuilders().forEach(builder -> {
                command.build((LiteralArgumentBuilder<Object>)builder);
                this.dispatcher.register(builder);
            });
        }
        this.load();
    }

    public boolean handleChatMessage(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim();
        if (trimmed.isEmpty() || !trimmed.startsWith(this.prefix)) {
            return false;
        }
        String command = trimmed.substring(this.prefix.length()).trim();
        if (command.isEmpty()) {
            return true;
        }
        try {
            this.dispatcher.execute(command, this.source);
        }
        catch (CommandSyntaxException exception) {
            ChatUtil.error("Command", exception.getMessage());
        }
        catch (Exception exception) {
            ChatUtil.error("Command", "Unexpected error while executing command");
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to execute command: {}", (Object)command, (Object)exception);
        }
        return true;
    }

    public void registerCommand(Command command) {
        if (command != null) {
            this.commands.add(command);
        }
    }

    public void registerCommands(Command ... commands) {
        Arrays.stream(commands).forEach(this::registerCommand);
    }

    public List<Command> getCommands() {
        return List.copyOf(this.commands);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        this.prefix = prefix;
        this.save();
    }

    public void save() {
        if (this.commandConfig == null) {
            return;
        }
        JsonObject root = new JsonObject();
        root.addProperty("prefix", this.prefix);
        try {
            Files.createDirectories(this.clientDirectory, new FileAttribute[0]);
            Files.writeString(this.commandConfig, (CharSequence)root.toString(), new OpenOption[0]);
        }
        catch (IOException exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to save command config", (Throwable)exception);
        }
    }

    public void load() {
        if (this.commandConfig == null || !Files.exists(this.commandConfig, new LinkOption[0])) {
            return;
        }
        try {
            String loadedPrefix;
            JsonObject root = JsonParser.parseString((String)Files.readString(this.commandConfig)).getAsJsonObject();
            if (root.has("prefix") && !(loadedPrefix = root.get("prefix").getAsString()).isBlank()) {
                this.prefix = loadedPrefix;
            }
        }
        catch (Exception exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to load command config", (Throwable)exception);
        }
    }
}



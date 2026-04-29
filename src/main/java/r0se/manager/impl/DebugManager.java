/*
 * Decompiled with CFR 0.152.
 */
package r0se.manager.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import r0se.R0SE;
import r0se.impl.module.client.AntiCheat;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class DebugManager
implements Manager {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private Path logPath;

    @Override
    public void init() {
        this.logPath = Path.of("r0se", "debug.log");
        try {
            Files.createDirectories(this.logPath.getParent(), new FileAttribute[0]);
            Files.writeString(this.logPath, (CharSequence)("==== R0SE debug session " + LocalDateTime.now().format(TIME_FORMAT) + " version=" + R0SE.CLIENT_VERSION + " hash=" + R0SE.GIT_HASH + " ====" + System.lineSeparator()), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException exception) {
            R0SE.LOGGER.warn("{} Failed to initialize debug log", (Object)R0SE.LOG_PREFIX, (Object)exception);
        }
    }

    public void log(String channel, String message) {
        R0SE.LOGGER.info("{} [{}] {}", new Object[]{R0SE.LOG_PREFIX, channel, message});
        if (!this.isEnabled() || this.logPath == null) {
            return;
        }
        String line = LocalDateTime.now().format(TIME_FORMAT) + " [" + channel + "] " + message + System.lineSeparator();
        try {
            Files.writeString(this.logPath, (CharSequence)line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException exception) {
            R0SE.LOGGER.warn("{} Failed to write debug log", (Object)R0SE.LOG_PREFIX, (Object)exception);
        }
    }

    public Path getLogPath() {
        return this.logPath;
    }

    private boolean isEnabled() {
        AntiCheat config = Managers.MODULES.getFeature(AntiCheat.class);
        return config != null && (Boolean)config.getDebug().getValue() != false;
    }
}


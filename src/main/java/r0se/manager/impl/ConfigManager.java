/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package r0se.manager.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.world.TickEvent;
import r0se.api.feature.Feature;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class ConfigManager
implements Manager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int AUTO_SAVE_DELAY_TICKS = 40;
    private Path clientDirectory;
    private Path featureConfig;
    private boolean dirty;
    private boolean loading;
    private int dirtyTicks;

    @Override
    public void init() {
        this.clientDirectory = R0SE.mc.runDirectory.toPath().resolve("r0se");
        this.featureConfig = this.clientDirectory.resolve("features.json");
        try {
            Files.createDirectories(this.clientDirectory, new FileAttribute[0]);
            this.loadFeatures();
            this.dirty = false;
            this.dirtyTicks = 0;
            R0SE.eventHandler.subscribe(this);
        }
        catch (IOException exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to initialize config manager", (Throwable)exception);
        }
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.saveFeatures();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (!this.dirty || this.loading) {
            return;
        }
        ++this.dirtyTicks;
        if (this.dirtyTicks >= 40) {
            this.saveFeatures();
        }
    }

    public void markDirty() {
        if (this.loading) {
            return;
        }
        this.dirty = true;
        this.dirtyTicks = 0;
    }

    public void saveFeatures() {
        if (this.featureConfig == null) {
            return;
        }
        JsonObject root = new JsonObject();
        for (Feature feature : Managers.MODULES.getFeatures()) {
            root.add(feature.getIdentifier(), (JsonElement)feature.toJson());
        }
        try {
            Files.createDirectories(this.clientDirectory, new FileAttribute[0]);
            Files.writeString(this.featureConfig, (CharSequence)GSON.toJson((JsonElement)root), new OpenOption[0]);
            this.dirty = false;
            this.dirtyTicks = 0;
        }
        catch (IOException exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to save feature config", (Throwable)exception);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadFeatures() {
        if (this.featureConfig == null || !Files.exists(this.featureConfig, new LinkOption[0])) {
            return;
        }
        this.loading = true;
        try {
            String json = Files.readString(this.featureConfig);
            JsonObject root = JsonParser.parseString((String)json).getAsJsonObject();
            for (Feature feature : Managers.MODULES.getFeatures()) {
                if (!root.has(feature.getIdentifier()) || !root.get(feature.getIdentifier()).isJsonObject()) continue;
                feature.fromJson(root.getAsJsonObject(feature.getIdentifier()));
            }
        }
        catch (Exception exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to load feature config", (Throwable)exception);
        }
        finally {
            this.loading = false;
            this.dirty = false;
            this.dirtyTicks = 0;
        }
    }
}



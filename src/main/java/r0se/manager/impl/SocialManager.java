/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.entity.Entity
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
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.world.TickEvent;
import r0se.api.social.SocialType;
import r0se.manager.Manager;

public class SocialManager
implements Manager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int AUTO_SAVE_DELAY_TICKS = 40;
    private final Map<String, SocialType> socials = new LinkedHashMap<String, SocialType>();
    private Path clientDirectory;
    private Path socialConfig;
    private boolean dirty;
    private boolean loading;
    private int dirtyTicks;

    @Override
    public void init() {
        this.clientDirectory = R0SE.mc.runDirectory.toPath().resolve("r0se");
        this.socialConfig = this.clientDirectory.resolve("socials.json");
        try {
            Files.createDirectories(this.clientDirectory, new FileAttribute[0]);
            this.load();
            this.dirty = false;
            this.dirtyTicks = 0;
            R0SE.eventHandler.subscribe(this);
        }
        catch (IOException exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to initialize social manager", (Throwable)exception);
        }
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.save();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (!this.dirty || this.loading) {
            return;
        }
        ++this.dirtyTicks;
        if (this.dirtyTicks >= 40) {
            this.save();
        }
    }

    public void markDirty() {
        if (this.loading) {
            return;
        }
        this.dirty = true;
        this.dirtyTicks = 0;
    }

    public void addFriend(String name) {
        this.addSocial(name, SocialType.FRIEND);
    }

    public void addEnemy(String name) {
        this.addSocial(name, SocialType.ENEMY);
    }

    public boolean removeFriend(String name) {
        return this.removeSocial(name, SocialType.FRIEND);
    }

    public boolean removeEnemy(String name) {
        return this.removeSocial(name, SocialType.ENEMY);
    }

    public boolean toggleFriend(String name) {
        String normalized = SocialManager.normalizeName(name);
        if (normalized == null) {
            return false;
        }
        if (this.isFriendInternal(normalized)) {
            this.socials.remove(normalized);
            this.markDirty();
            return false;
        }
        this.socials.put(normalized, SocialType.FRIEND);
        this.markDirty();
        return true;
    }

    public boolean toggleEnemy(String name) {
        String normalized = SocialManager.normalizeName(name);
        if (normalized == null) {
            return false;
        }
        if (this.isEnemyInternal(normalized)) {
            this.socials.remove(normalized);
            this.markDirty();
            return false;
        }
        this.socials.put(normalized, SocialType.ENEMY);
        this.markDirty();
        return true;
    }

    public boolean isFriend(Entity entity) {
        return entity != null && this.isFriend(entity.getName().getString());
    }

    public boolean isEnemy(Entity entity) {
        return entity != null && this.isEnemy(entity.getName().getString());
    }

    public boolean isFriend(String name) {
        return this.isFriendInternal(SocialManager.normalizeName(name));
    }

    public boolean isEnemy(String name) {
        return this.isEnemyInternal(SocialManager.normalizeName(name));
    }

    public boolean isFriendInternal(String normalizedName) {
        return this.socials.get(normalizedName) == SocialType.FRIEND;
    }

    public boolean isEnemyInternal(String normalizedName) {
        return this.socials.get(normalizedName) == SocialType.ENEMY;
    }

    public SocialType getType(String name) {
        return this.socials.get(SocialManager.normalizeName(name));
    }

    public boolean isType(String name, SocialType type) {
        return this.getType(name) == type;
    }

    public void addSocial(String name, SocialType type) {
        String normalized = SocialManager.normalizeName(name);
        if (normalized == null || type == null) {
            return;
        }
        this.socials.put(normalized, type);
        this.markDirty();
    }

    public boolean removeSocial(String name) {
        boolean removed;
        String normalized = SocialManager.normalizeName(name);
        if (normalized == null) {
            return false;
        }
        boolean bl = removed = this.socials.remove(normalized) != null;
        if (removed) {
            this.markDirty();
        }
        return removed;
    }

    public boolean removeSocial(String name, SocialType expectedType) {
        String normalized = SocialManager.normalizeName(name);
        if (normalized == null) {
            return false;
        }
        if (this.socials.get(normalized) != expectedType) {
            return false;
        }
        this.socials.remove(normalized);
        this.markDirty();
        return true;
    }

    public Set<String> getByType(SocialType type) {
        return this.socials.entrySet().stream().filter(entry -> entry.getValue() == type).map(Map.Entry::getKey).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<String, SocialType> getSocials() {
        return Collections.unmodifiableMap(this.socials);
    }

    public Map<SocialType, Set<String>> getGroupedSocials() {
        EnumMap<SocialType, Set<String>> grouped = new EnumMap<SocialType, Set<String>>(SocialType.class);
        for (SocialType type : SocialType.values()) {
            grouped.put(type, this.getByType(type));
        }
        return grouped;
    }

    public void clear() {
        if (this.socials.isEmpty()) {
            return;
        }
        this.socials.clear();
        this.markDirty();
    }

    public void save() {
        if (this.socialConfig == null) {
            return;
        }
        JsonObject root = new JsonObject();
        for (Map.Entry<String, SocialType> entry : this.socials.entrySet()) {
            root.addProperty(entry.getKey(), entry.getValue().name());
        }
        try {
            Files.createDirectories(this.clientDirectory, new FileAttribute[0]);
            Files.writeString(this.socialConfig, (CharSequence)GSON.toJson((JsonElement)root), new OpenOption[0]);
            this.dirty = false;
            this.dirtyTicks = 0;
        }
        catch (IOException exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to save socials", (Throwable)exception);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load() {
        if (this.socialConfig == null || !Files.exists(this.socialConfig, new LinkOption[0])) {
            return;
        }
        this.loading = true;
        try {
            this.socials.clear();
            JsonObject root = JsonParser.parseString((String)Files.readString(this.socialConfig)).getAsJsonObject();
            for (Map.Entry entry : root.entrySet()) {
                String normalized = SocialManager.normalizeName((String)entry.getKey());
                if (normalized == null || !((JsonElement)entry.getValue()).isJsonPrimitive()) continue;
                try {
                    SocialType type = SocialType.valueOf(((JsonElement)entry.getValue()).getAsString().toUpperCase(Locale.ROOT));
                    this.socials.put(normalized, type);
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
        }
        catch (Exception exception) {
            R0SE.LOGGER.error(R0SE.LOG_PREFIX + " Failed to load socials", (Throwable)exception);
        }
        finally {
            this.loading = false;
            this.dirty = false;
            this.dirtyTicks = 0;
        }
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}



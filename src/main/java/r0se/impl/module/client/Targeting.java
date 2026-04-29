/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.mob.Monster
 *  net.minecraft.entity.player.PlayerEntity
 */
package r0se.impl.module.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import r0se.api.feature.ConcurrentFeature;
import r0se.api.feature.FeatureCategory;
import r0se.api.settings.BoolSetting;

public class Targeting
extends ConcurrentFeature {
    private final BoolSetting targetPlayers = this.addSetting(new BoolSetting("Players", true));
    private final BoolSetting targetHostiles = this.addSetting(new BoolSetting("Hostiles", false));
    private final BoolSetting targetPassives = this.addSetting(new BoolSetting("Passives", false));
    private final BoolSetting ignoreFriends = this.addSetting(new BoolSetting("IgnoreFriends", true));
    private final BoolSetting ignoreInvisible = this.addSetting(new BoolSetting("IgnoreInvisible", true));
    private final BoolSetting ignoreDead = this.addSetting(new BoolSetting("IgnoreDead", true));

    public Targeting() {
        super("Targeting", "Controls shared combat targeting filters", FeatureCategory.CLIENT, "targets", "target");
    }

    public BoolSetting getTargetPlayers() {
        return this.targetPlayers;
    }

    public BoolSetting getTargetHostiles() {
        return this.targetHostiles;
    }

    public BoolSetting getTargetPassives() {
        return this.targetPassives;
    }

    public BoolSetting getIgnoreFriends() {
        return this.ignoreFriends;
    }

    public BoolSetting getIgnoreInvisible() {
        return this.ignoreInvisible;
    }

    public BoolSetting getIgnoreDead() {
        return this.ignoreDead;
    }

    public boolean isValid(Entity entity) {
        if (entity == null || entity == Targeting.mc.player) {
            return false;
        }
        if (entity instanceof PlayerEntity) {
            return ((Boolean)this.targetPlayers.getValue()).booleanValue();
        }
        if (entity instanceof Monster) {
            return ((Boolean)this.targetHostiles.getValue()).booleanValue();
        }
        if (entity instanceof AnimalEntity) {
            return ((Boolean)this.targetPassives.getValue()).booleanValue();
        }
        return false;
    }
}



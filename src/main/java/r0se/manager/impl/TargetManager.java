/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.client.network.ClientPlayerEntity
 */
package r0se.manager.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import r0se.R0SE;
import r0se.api.event.Subscribe;
import r0se.api.event.world.TickEvent;
import r0se.impl.module.client.Targeting;
import r0se.manager.Manager;
import r0se.manager.Managers;

public class TargetManager
implements Manager {
    private LivingEntity target;

    @Override
    public void init() {
        R0SE.eventHandler.subscribe(this);
    }

    @Override
    public void shutdown() {
        R0SE.eventHandler.unsubscribe(this);
        this.clearTarget();
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (this.target != null && !this.isValidTarget((Entity)this.target, Float.MAX_VALUE)) {
            this.target = null;
        }
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public boolean hasTarget() {
        return this.target != null;
    }

    public void clearTarget() {
        this.target = null;
    }

    public LivingEntity setClosestTarget(float range) {
        this.target = this.getClosestTarget(range);
        return this.target;
    }

    public LivingEntity getClosestTarget(float range) {
        return this.getTargets(range).stream().findFirst().orElse(null);
    }

    public PlayerEntity getClosestPlayer(float range) {
        for (LivingEntity entity : this.getTargets(range)) {
            if (!(entity instanceof PlayerEntity)) continue;
            PlayerEntity player = (PlayerEntity)entity;
            return player;
        }
        return null;
    }

    public List<LivingEntity> getTargets(float range) {
        if (R0SE.mc.world == null || R0SE.mc.player == null) {
            return List.of();
        }
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        for (Entity entity : R0SE.mc.world.getEntities()) {
            LivingEntity living;
            if (!(entity instanceof LivingEntity) || !this.isValidTarget((Entity)(living = (LivingEntity)entity), range)) continue;
            targets.add(living);
        }
        targets.sort(Comparator.comparingDouble(arg_0 -> ((ClientPlayerEntity)R0SE.mc.player).squaredDistanceTo(arg_0)));
        return targets;
    }

    public boolean isValidTarget(Entity entity, float range) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity living = (LivingEntity)entity;
        if (R0SE.mc.player == null || R0SE.mc.world == null) {
            return false;
        }
        Targeting targeting = Managers.MODULES.getFeature(Targeting.class);
        if (targeting == null || !targeting.isValid((Entity)living)) {
            return false;
        }
        if (((Boolean)targeting.getIgnoreDead().getValue()).booleanValue() && (!living.isAlive() || living.isRemoved())) {
            return false;
        }
        if (((Boolean)targeting.getIgnoreInvisible().getValue()).booleanValue() && living.isInvisible()) {
            return false;
        }
        if (((Boolean)targeting.getIgnoreFriends().getValue()).booleanValue() && Managers.SOCIAL.isFriend((Entity)living)) {
            return false;
        }
        return range == Float.MAX_VALUE || !(R0SE.mc.player.squaredDistanceTo((Entity)living) > (double)MathHelper.square((float)range));
    }
}



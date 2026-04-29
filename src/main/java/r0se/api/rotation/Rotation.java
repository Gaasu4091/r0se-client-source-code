/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.util.math.MathHelper
 */
package r0se.api.rotation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class Rotation {
    private float yaw;
    private float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(Entity entity) {
        this(entity.getYaw(), entity.getPitch());
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Rotation add(Rotation other) {
        return new Rotation(this.yaw + other.yaw, this.pitch + other.pitch);
    }

    public Rotation subtract(Rotation other) {
        return new Rotation(this.yaw - other.yaw, this.pitch - other.pitch);
    }

    public Rotation multiply(float scale) {
        return new Rotation(this.yaw * scale, this.pitch * scale);
    }

    public void apply(Entity entity) {
        entity.setYaw(this.yaw);
        entity.setPitch(this.pitch);
    }

    public void applyToPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            this.apply((Entity)client.player);
        }
    }

    public float[] getComponents() {
        return new float[]{this.yaw, this.pitch};
    }

    public Rotation correctSensitivity(Rotation previous) {
        Rotation delta = this.closestDelta(previous);
        List<Rotation> options = Rotation.approximateCursorDeltas(delta);
        return options.stream().min(Comparator.comparingDouble(this::fov)).orElse(this);
    }

    public Rotation smoothedTurn(Rotation target, double smoothness) {
        Rotation delta = target.closestDelta(this).multiply((float)smoothness);
        return this.add(delta);
    }

    public float fov(Rotation other) {
        Rotation delta = this.closestDelta(other);
        return (float)Math.sqrt(delta.yaw * delta.yaw + delta.pitch * delta.pitch);
    }

    public Rotation closestDelta(Rotation other) {
        float dyaw = MathHelper.wrapDegrees((float)(other.yaw - this.yaw));
        float dpitch = other.pitch - this.pitch;
        return new Rotation(dyaw, dpitch);
    }

    public Vec3d toForwardVector() {
        double yawRad = Math.toRadians(this.yaw);
        double pitchRad = Math.toRadians(this.pitch);
        return new Vec3d(Math.sin(-yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(-yawRad) * Math.cos(pitchRad));
    }

    public Rotation withYaw(float newYaw) {
        return new Rotation(newYaw, this.pitch);
    }

    public Rotation withPitch(float newPitch) {
        return new Rotation(this.yaw, newPitch);
    }

    public static Rotation calculateNewRotation(Rotation previous, double dx, double dy) {
        double gcd = Rotation.getGcd();
        Rotation delta = new Rotation((float)(dx * gcd * (double)0.15f), (float)(dy * gcd * (double)0.15f));
        Rotation next = previous.add(delta);
        return next.withPitch(MathHelper.clamp((float)next.pitch, (float)-90.0f, (float)90.0f));
    }

    public static List<Rotation> approximateCursorDeltas(Rotation deltaRotation) {
        double gcd = Rotation.getGcd() * (double)0.15f;
        double tx = (double)(-deltaRotation.getYaw()) / gcd;
        double ty = (double)(-deltaRotation.getPitch()) / gcd;
        ArrayList<Rotation> possibilities = new ArrayList<Rotation>();
        possibilities.add(Rotation.calculateNewRotation(new Rotation(0.0f, 0.0f), Math.floor(tx), Math.floor(ty)));
        possibilities.add(Rotation.calculateNewRotation(new Rotation(0.0f, 0.0f), Math.ceil(tx), Math.floor(ty)));
        possibilities.add(Rotation.calculateNewRotation(new Rotation(0.0f, 0.0f), Math.ceil(tx), Math.ceil(ty)));
        possibilities.add(Rotation.calculateNewRotation(new Rotation(0.0f, 0.0f), Math.floor(tx), Math.ceil(ty)));
        return possibilities;
    }

    private static double getGcd() {
        MinecraftClient client = MinecraftClient.getInstance();
        double sensitivity = (Double)client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        double scaled = sensitivity * sensitivity * sensitivity;
        return client.options.getPerspective().isFirstPerson() && client.player != null && client.player.isUsingSpyglass() ? scaled : scaled * 8.0;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Rotation)) {
            return false;
        }
        Rotation rotation = (Rotation)object;
        return Float.compare(rotation.yaw, this.yaw) == 0 && Float.compare(rotation.pitch, this.pitch) == 0;
    }

    public int hashCode() {
        return Float.hashCode(this.yaw) * 31 + Float.hashCode(this.pitch);
    }
}



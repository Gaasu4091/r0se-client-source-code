/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.client.network.SequencedPacketCreator
 */
package r0se.manager.impl;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.network.SequencedPacketCreator;
import r0se.R0SE;
import r0se.manager.Manager;
import r0se.manager.Managers;
import r0se.mixin.ClientPlayerInteractionManagerAccessor;

public class NetworkManager
implements Manager {
    private final Set<Packet<?>> sentPackets = Collections.newSetFromMap(new IdentityHashMap());
    private final Set<Packet<?>> quietPackets = Collections.newSetFromMap(new IdentityHashMap());

    @Override
    public void shutdown() {
        this.clearCache();
    }

    public boolean sendPacket(Packet<?> packet) {
        return this.sendPacket(packet, "network");
    }

    public boolean sendPacket(Packet<?> packet, String source) {
        if (packet == null || R0SE.mc.getNetworkHandler() == null) {
            return false;
        }
        this.sentPackets.add(packet);
        Managers.TRACE.mark(packet, source, false);
        R0SE.mc.getNetworkHandler().sendPacket(packet);
        return true;
    }

    public boolean sendQuietPacket(Packet<?> packet) {
        return this.sendQuietPacket(packet, "network-quiet");
    }

    public boolean sendQuietPacket(Packet<?> packet, String source) {
        if (packet == null || R0SE.mc.getNetworkHandler() == null) {
            return false;
        }
        this.sentPackets.add(packet);
        this.quietPackets.add(packet);
        Managers.TRACE.mark(packet, source, true);
        R0SE.mc.getNetworkHandler().getConnection().send(packet);
        return true;
    }

    public boolean sendSequencedPacket(SequencedPacketCreator creator) {
        return this.sendSequencedPacket(creator, "sequenced");
    }

    public boolean sendSequencedPacket(SequencedPacketCreator creator, String source) {
        if (creator == null || R0SE.mc.world == null || R0SE.mc.interactionManager == null) {
            return false;
        }
        ((ClientPlayerInteractionManagerAccessor)R0SE.mc.interactionManager).r0se$sendSequencedPacket(R0SE.mc.world, sequence -> {
            Packet packet = creator.predict(sequence);
            this.sentPackets.add(packet);
            Managers.TRACE.mark(packet, source, false);
            return packet;
        });
        return true;
    }

    public boolean isManagedPacket(Packet<?> packet) {
        return this.sentPackets.contains(packet);
    }

    public boolean consumeQuietPacket(Packet<?> packet) {
        boolean quiet = this.quietPackets.remove(packet);
        if (quiet) {
            Managers.TRACE.logQuiet(packet, "quiet-consume");
        }
        return quiet;
    }

    public void clearCache() {
        this.sentPackets.clear();
        this.quietPackets.clear();
    }
}



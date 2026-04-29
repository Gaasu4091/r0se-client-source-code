/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 */
package r0se.api.event.network;

import net.minecraft.network.packet.Packet;
import r0se.api.event.Event;

public class PacketOutboundEvent
extends Event {
    private final Packet<?> packet;

    public PacketOutboundEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }
}



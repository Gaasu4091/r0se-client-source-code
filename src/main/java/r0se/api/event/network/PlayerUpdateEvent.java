/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.event.network;

import r0se.api.event.Event;

public class PlayerUpdateEvent
extends Event {

    public static class Post
    extends PlayerUpdateEvent {
    }

    public static class PrePacket
    extends PlayerUpdateEvent {
    }

    public static class Pre
    extends PlayerUpdateEvent {
    }
}


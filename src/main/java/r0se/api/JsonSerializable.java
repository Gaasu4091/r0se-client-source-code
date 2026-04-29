/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package r0se.api;

import com.google.gson.JsonObject;

public interface JsonSerializable {
    public JsonObject toJson();

    public void fromJson(JsonObject var1);
}


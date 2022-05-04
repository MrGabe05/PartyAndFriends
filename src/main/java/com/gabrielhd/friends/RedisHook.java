package com.gabrielhd.friends;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;

import java.util.UUID;

public class RedisHook {

    @Getter private static RedisHook redis;

    public static boolean isEnabled() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null;
    }

    public RedisHook() {
        redis = this;

        this.registerChannels();
    }

    public void registerChannels() {
        RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels("friends", "party");
    }

    public void unregisterChannels() {
        RedisBungeeAPI.getRedisBungeeApi().unregisterPubSubChannels("friends", "party");
    }

    public void sendMessage(String channel, String message) {
        RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(channel, message);
    }

    public boolean isOnline(UUID uuid) {
        return RedisBungeeAPI.getRedisBungeeApi().isPlayerOnline(uuid);
    }

    public String getServer(UUID uuid) {
        return RedisBungeeAPI.getRedisBungeeApi().getServerFor(uuid).getName();
    }

    public String getName(UUID uuid) {
        return RedisBungeeAPI.getRedisBungeeApi().getNameFromUuid(uuid);
    }
}

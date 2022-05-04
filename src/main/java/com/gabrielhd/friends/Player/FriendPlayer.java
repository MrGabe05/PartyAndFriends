package com.gabrielhd.friends.Player;

import com.gabrielhd.friends.Database.DataHandler;
import com.gabrielhd.friends.Database.Database;
import com.gabrielhd.friends.Main;
import com.gabrielhd.friends.RedisHook;
import com.gabrielhd.friends.Utilities.IDFetcher;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Getter @Setter
public class FriendPlayer {

    private static final Map<UUID, FriendPlayer> friendPlayers = new HashMap<>();

    private final UUID uuid;

    public FriendPlayer(UUID uuid) {
        this.uuid = uuid;

        friendPlayers.put(uuid, this);

        Database.getStorage().createPlayerIfNotExists(uuid);
    }

    public String getServer() {
        if(!isOnline()) return "";

        if(RedisHook.isEnabled()) {
            return RedisHook.getRedis().getServer(this.uuid);
        }

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.uuid);
        return player.getServer().getInfo().getName();
    }

    public String getName() {
        if(RedisHook.isEnabled()) {
            return RedisHook.getRedis().getName(this.uuid);
        }

        return Database.getStorage().getString(this.uuid, DataHandler.TABLE_FRIENDS, "playerName").toCompletableFuture().join();
    }

    public boolean isOnline() {
        if(RedisHook.isEnabled()) {
            return RedisHook.getRedis().isOnline(this.uuid);
        }

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.uuid);
        return (player != null && player.isConnected());
    }

    public CompletionStage<Void> addFriend(UUID uuid) {
        return CompletableFuture.runAsync(() -> getFriendsUuid().thenAcceptAsync(friends -> {
            if(!friends.contains(uuid)) friends.add(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_FRIENDS, "friends", friends);
        }));
    }

    public CompletionStage<Void> removeFriend(UUID uuid) {
        return CompletableFuture.runAsync(() -> getFriendsUuid().thenAcceptAsync(friends -> {
            friends.remove(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_FRIENDS, "friends", friends);
        }));
    }

    public CompletionStage<Void> addRequest(UUID uuid) {
        return CompletableFuture.runAsync(() -> getRequestsUuid().thenAcceptAsync(requests -> {
            if(!requests.contains(uuid)) requests.add(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_FRIENDS, "requests", requests);
        }));
    }

    public CompletionStage<Void> removeRequest(UUID uuid) {
        return CompletableFuture.runAsync(() -> getRequestsUuid().thenAcceptAsync(requests -> {
            requests.remove(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_FRIENDS, "requests", requests);
        }));
    }

    public void setLastSeen(String lastSeen) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> Database.getStorage().update(this.uuid, DataHandler.TABLE_FRIENDS, "last_seen", lastSeen));
    }

    public String getLastSeen() {
        return Database.getStorage().getString(this.uuid, DataHandler.TABLE_FRIENDS, "last_seen").toCompletableFuture().join();
    }

    public boolean isFriend(UUID uuid) {
        return getFriendsUuid().toCompletableFuture().join().contains(uuid);
    }

    public boolean hasRequest(UUID uuid) {
        return getRequestsUuid().toCompletableFuture().join().contains(uuid);
    }

    public int getFriendsAmount() {
        return getFriendsUuid().toCompletableFuture().join().size();
    }

    public int getRequestsAmount() {
        return getRequestsUuid().toCompletableFuture().join().size();
    }

    public void sendMessage(String msg) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.uuid);
        if(player.isConnected()) {
            player.sendMessage(msg);
            return;
        }

        if(RedisHook.isEnabled()) {
            RedisHook.getRedis().sendMessage("friends", this.uuid.toString() + ":"+msg);
        }
    }

    public CompletionStage<List<UUID>> getFriendsUuid() {
        return CompletableFuture.supplyAsync(() -> Database.getStorage().getList(this.uuid, DataHandler.TABLE_FRIENDS, "friends").toCompletableFuture().join());
    }

    public CompletionStage<List<UUID>> getRequestsUuid() {
        return CompletableFuture.supplyAsync(() -> Database.getStorage().getList(this.uuid, DataHandler.TABLE_FRIENDS, "requests").toCompletableFuture().join());
    }

    public CompletionStage<List<FriendPlayer>> getFriends() {
        return CompletableFuture.supplyAsync(() -> {
            List<FriendPlayer> friends = new ArrayList<>();

            for(UUID uuid : this.getFriendsUuid().toCompletableFuture().join()) {
                friends.add(of(uuid).toCompletableFuture().join());
            }

            friends.sort(Comparator.comparing(FriendPlayer::isOnline));
            Collections.reverse(friends);

            return friends;
        });
    }

    public CompletionStage<List<FriendPlayer>> getRequests() {
        return CompletableFuture.supplyAsync(() -> {
            List<FriendPlayer> requests = new ArrayList<>();

            for(UUID uuid : this.getRequestsUuid().toCompletableFuture().join()) {
                requests.add(of(uuid).toCompletableFuture().join());
            }

            requests.sort(Comparator.comparing(FriendPlayer::isOnline));
            Collections.reverse(requests);

            return requests;
        });
    }

    public static CompletionStage<FriendPlayer> of(String name) {
        return CompletableFuture.supplyAsync(() -> {
            IDFetcher fetcher = IDFetcher.getIDFetcher(name);
            if (fetcher == null) return null;

            return of(fetcher.getUuid()).toCompletableFuture().join();
        });
    }

    public static CompletionStage<FriendPlayer> of(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if(friendPlayers.containsKey(uuid)) {
                return friendPlayers.get(uuid);
            }

            return new FriendPlayer(uuid);
        });
    }
}

package com.gabrielhd.friends.Party;

import com.gabrielhd.friends.Configuration.ConfigData;
import com.gabrielhd.friends.Database.DataHandler;
import com.gabrielhd.friends.Database.Database;
import com.gabrielhd.friends.Main;
import com.gabrielhd.friends.Party.Events.PartyInviteEvent;
import com.gabrielhd.friends.Party.Events.PartyJoinEvent;
import com.gabrielhd.friends.Party.Events.PartyLeftEvent;
import com.gabrielhd.friends.Utilities.Utils;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Getter @Setter
public class Party {

    private static final List<Party> parties = new ArrayList<>();

    private UUID uuid;
    private ProxiedPlayer leader;

    public Party(UUID uuid) {
        this.uuid = uuid;

        this.leader = ProxyServer.getInstance().getPlayer(uuid);

        parties.add(this);

        Database.getStorage().createPartyIfNotExists(uuid);
    }

    public void disbandParty() {
        parties.remove(this);

        Database.getStorage().deletePartyIfExists(this.uuid);
    }

    public CompletionStage<Void> addMember(UUID uuid) {
        return CompletableFuture.runAsync(() -> getMembers().thenAccept(members -> {
            if(!members.contains(uuid)) members.add(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_PARTY, "members", members);
        }));
    }

    public CompletionStage<Void> removeMember(UUID uuid) {
        return CompletableFuture.runAsync(() -> getMembers().thenAccept(members -> {
            members.remove(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_PARTY, "members", members);
        }));
    }

    public CompletionStage<Void> addRequest(UUID uuid) {
        return CompletableFuture.runAsync(() -> getRequests().thenAccept(requests -> {
            if(!requests.contains(uuid)) requests.add(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_PARTY, "requests", requests);
        }));
    }

    public CompletionStage<Void> removeRequest(UUID uuid) {
        return CompletableFuture.runAsync(() -> getRequests().thenAccept(requests -> {
            requests.remove(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_PARTY, "requests", requests);
        }));
    }

    public CompletionStage<Void> addModerator(UUID uuid) {
        return CompletableFuture.runAsync(() -> getModerators().thenAccept(mods -> {
            if(!mods.contains(uuid)) mods.add(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_PARTY, "moderators", mods);
        }));
    }

    public CompletionStage<Void> removeModerator(UUID uuid) {
        return CompletableFuture.runAsync(() -> getModerators().thenAccept(mods -> {
            mods.remove(uuid);

            Database.getStorage().updateList(uuid, DataHandler.TABLE_PARTY, "moderators", mods);
        }));
    }

    public CompletionStage<List<UUID>> getMembers() {
        return CompletableFuture.supplyAsync(() -> Database.getStorage().getList(this.uuid, DataHandler.TABLE_PARTY, "members").toCompletableFuture().join());
    }

    public CompletionStage<List<UUID>> getRequests() {
        return CompletableFuture.supplyAsync(() -> Database.getStorage().getList(this.uuid, DataHandler.TABLE_PARTY, "requests").toCompletableFuture().join());
    }

    public CompletionStage<List<UUID>> getModerators() {
        return CompletableFuture.supplyAsync(() -> Database.getStorage().getList(this.uuid, DataHandler.TABLE_PARTY, "moderators").toCompletableFuture().join());
    }

    public int getSize() {
        return this.getMembers().toCompletableFuture().join().size();
    }

    public boolean isPartyMember(UUID uuid) {
        return this.getMembers().toCompletableFuture().join().contains(uuid);
    }

    public boolean isInRequest(UUID uuid) {
        return this.getRequests().toCompletableFuture().join().contains(uuid);
    }

    public boolean isModerator(UUID uuid) {
        return this.getModerators().toCompletableFuture().join().contains(uuid);
    }

    public boolean isPartyLeader(UUID uuid) {
        return this.leader.toString().equals(uuid.toString());
    }

    public boolean promotePlayer(ProxiedPlayer player) {
        if(isPartyLeader(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getCantYourSelf());
            return false;
        }

        if(!isPartyMember(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getNotInTheParty().replace("%player%", player.getName()));
            return false;
        }

        if(isModerator(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getAlreadyMod().replace("%player%", player.getName()));
            return false;
        }

        this.addModerator(player.getUniqueId());
        return true;
    }

    public boolean demotePlayer(ProxiedPlayer player) {
        if (isPartyLeader(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getCantYourSelf());
            return false;
        }

        if (!isPartyMember(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getNotInTheParty().replace("%player%", player.getName()));
            return false;
        }

        if (!isModerator(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getIsntMod().replace("%player%", player.getName()));
            return false;
        }

        this.removeModerator(player.getUniqueId());
        return true;
    }

    public boolean addPlayer(ProxiedPlayer player) {
        if (isPartyLeader(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getCantInviteSelf());
            return false;
        }

        if (isPartyMember(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getAlreadyAtTheParty().replace("%player%", player.getName()));
            return false;
        }

        this.addMember(player.getUniqueId());
        this.removeRequest(player.getUniqueId());

        ProxyServer.getInstance().getPluginManager().callEvent(new PartyJoinEvent(this, player));
        return true;
    }

    public boolean removePlayer(ProxiedPlayer player) {
        if (isPartyLeader(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getCantKickSelf());
            return false;
        }

        if (!isPartyMember(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getNotInTheParty().replace("%player%", player.getName()));
            return false;
        }

        this.removeMember(player.getUniqueId());
        this.removeModerator(player.getUniqueId());

        ProxyServer.getInstance().getPluginManager().callEvent(new PartyLeftEvent(this, player));
        return true;
    }

    public boolean sendRequest(ProxiedPlayer player) {
        if (isPartyLeader(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getCantInviteSelf());
            return false;
        }

        if (isPartyMember(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getAlreadyAtTheParty().replace("%player%", player.getName()));
            return false;
        }

        if (getParty(player.getUniqueId()) != null) {
            this.leader.sendMessage(ConfigData.getAlreadyInAParty().replace("%player%", player.getName()));
            return false;
        }

        if (isInRequest(player.getUniqueId())) {
            this.leader.sendMessage(ConfigData.getAlreadyInvite().replace("%player%", player.getName()));
            return false;
        }

        String permission = Utils.getPermissionsLimits(player.getPermissions().toArray(new String[0]), ConfigData.getPartyLimits());
        if (Utils.isInt(permission) && Integer.parseInt(permission) <= this.getSize()) {
            this.leader.sendMessage(ConfigData.getPartyMaxMembers());
            return false;
        }

        this.addRequest(player.getUniqueId());
        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> this.removeRequest(player.getUniqueId()), ConfigData.getRequestClear(), TimeUnit.MINUTES);

        ProxyServer.getInstance().getPluginManager().callEvent(new PartyInviteEvent(this, player));
        return true;
    }

    public CompletionStage<Void> sendMessage(String... msg) {
        return CompletableFuture.runAsync(() -> this.getMembers().thenAccept(members -> members.stream().map(uuid -> ProxyServer.getInstance().getPlayer(uuid)).forEach(player -> Utils.Color(msg).forEach(player::sendMessage))));
    }

    public static CompletionStage<Party> getParty(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> parties.stream().filter(party -> party.isPartyMember(uuid)).findFirst().orElse(null));
    }

    public static CompletionStage<Party> createParty(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Party party = getParty(uuid).toCompletableFuture().join();

            if(party != null) return party;

            return new Party(uuid);
        });
    }
}

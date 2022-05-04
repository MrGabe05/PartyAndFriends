package com.gabrielhd.friends.Listeners;

import com.gabrielhd.friends.Configuration.ConfigData;
import com.gabrielhd.friends.Main;
import com.gabrielhd.friends.Party.Party;
import com.gabrielhd.friends.Player.FriendPlayer;
import com.gabrielhd.friends.RedisHook;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NormalListeners implements Listener {

	private final Map<UUID, ScheduledTask> disbandParty = new HashMap<>();

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> FriendPlayer.of(player.getUniqueId()).thenAccept(friendPlayer -> {
			int requests = friendPlayer.getRequestsAmount();

			if(requests > 0) {
				player.sendMessage(ConfigData.getNewRequests().replace("%requests%", String.valueOf(requests)));
			}

			Main.debug("Logged "+player.getName());
		}));

		if(!RedisHook.isEnabled()) {
			if (disbandParty.containsKey(player.getUniqueId())) {
				disbandParty.get(player.getUniqueId()).cancel();

				disbandParty.remove(player.getUniqueId());
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerDisconnectEvent e) {
		ProxiedPlayer player = e.getPlayer();

		FriendPlayer.of(player.getUniqueId()).thenAccept(friendPlayer -> friendPlayer.setLastSeen(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

		if(!RedisHook.isEnabled()) {
			Party.getParty(player.getUniqueId()).thenAcceptAsync(party -> {
				if(party == null) return;

				if (party.isPartyLeader(player.getUniqueId())) {
					disbandParty.put(player.getUniqueId(), ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
						party.disbandParty();

						disbandParty.remove(player.getUniqueId());
					}, ConfigData.getDisbandTime(), TimeUnit.MINUTES));
					return;
				}

				party.removePlayer(player);
			});
		}
	}

	@EventHandler
	public void onChangeServer(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServerInfo info = player.getServer().getInfo();

		if(!RedisHook.isEnabled()) {
			Party.getParty(player.getUniqueId()).thenAcceptAsync(party -> {
				if (party == null) return;

				if (party.isPartyLeader(player.getUniqueId())) {
					if (ConfigData.getBlacklistServers().contains(info.getName().toLowerCase())) {
						party.disbandParty();
						return;
					}

					party.getMembers().toCompletableFuture().join().stream().map(uuid -> ProxyServer.getInstance().getPlayer(uuid)).forEach(member -> member.connect(info));
					return;
				}

				if (ConfigData.getBlacklistServers().contains(info.getName().toLowerCase())) {
					party.removePlayer(player);
				}
			});
		}
	}
}

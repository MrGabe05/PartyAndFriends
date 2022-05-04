package com.gabrielhd.friends.Listeners;

import com.gabrielhd.friends.Configuration.ConfigData;
import com.gabrielhd.friends.Player.FriendPlayer;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class RedisListeners implements Listener {

    @EventHandler
    public void onPubMessage(PubSubMessageEvent event) {
        if(event.getChannel().equalsIgnoreCase("friends")) {
            String[] message = event.getMessage().split(":");

            if(message.length == 3) {
                if(message[0].equalsIgnoreCase("request")) {
                    UUID uuid = UUID.fromString(message[1]);
                    String playerName = message[2];

                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
                    if(target.isConnected()) {
                        TextComponent accept = new TextComponent(ConfigData.getP_accept());
                        TextComponent decline = new TextComponent(ConfigData.getP_decline());

                        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends accept " + playerName));
                        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ConfigData.getD_accept()).create()));
                        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends decline " + playerName));
                        decline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ConfigData.getD_decline()).create()));

                        TextComponent current = new TextComponent(ConfigData.getRequestReceived().replace("%player%", playerName).replace("%action%", ""));

                        current.addExtra(accept);
                        current.addExtra(decline);

                        target.sendMessage(current);
                    }
                    return;
                }

                FriendPlayer.of(UUID.fromString(message[1])).thenAcceptAsync(friendPlayer -> friendPlayer.getFriendsUuid().thenAcceptAsync(list -> {
                    if(list.isEmpty()) return;

                    String msg = (message[0].equalsIgnoreCase("joined") ? ConfigData.getPlayerOnline() : ConfigData.getPlayerOffline()).replace("%player%", message[2]);
                    list.stream().map(uuidF -> ProxyServer.getInstance().getPlayer(uuidF)).filter(ProxiedPlayer::isConnected).forEach(online -> online.sendMessage(msg));
                }));
                return;
            }

            if(message.length == 2) {
                if (message[0].equalsIgnoreCase("socialspy")) {
                    ProxyServer.getInstance().getPlayers().stream().filter(ProxiedPlayer::isConnected).filter(player -> player.hasPermission("friends.socialspy")).forEach(player -> player.sendMessage(message[1]));
                    return;
                }

                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(UUID.fromString(message[0]));
                if (target.isConnected()) target.sendMessage(message[1]);
            }
            return;
        }

        if(event.getChannel().equalsIgnoreCase("party")) {

        }
    }
}

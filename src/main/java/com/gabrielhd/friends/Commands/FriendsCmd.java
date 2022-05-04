package com.gabrielhd.friends.Commands;

import com.gabrielhd.friends.Configuration.ConfigData;
import com.gabrielhd.friends.Main;
import com.gabrielhd.friends.Player.FriendPlayer;
import com.gabrielhd.friends.RedisHook;
import com.gabrielhd.friends.Utilities.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FriendsCmd extends Command{

	public FriendsCmd() {
		super("friends", "", "f");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
				help(player);
				return;
			}

			if(args[0].equalsIgnoreCase("reload")) {
				if(!player.hasPermission("friends.admin")) {
					player.sendMessage(Utils.Color("&cYou do not have permissions to run this!"));
					return;
				}

				Main.getInstance().reload();
				player.sendMessages(Utils.Color("&aPlugin reloaded!"));
				return;
			}

			FriendPlayer.of(player.getUniqueId()).thenAcceptAsync(friendPlayer -> {
				if(args[0].equalsIgnoreCase("msg")) {
					if(args.length < 3) {
						player.sendMessage(ConfigData.getValue());
						help(player);
						return;
					}

					FriendPlayer.of(args[1]).thenAcceptAsync(friendTarget -> {
						if(friendTarget == null) {
							player.sendMessage(ConfigData.getPlayerNotRegistered());
							return;
						}

						if(!friendTarget.isFriend(friendPlayer.getUuid())) {
							player.sendMessage(ConfigData.getPlayerNotFriend().replace("%player%", friendTarget.getName()));
							return;
						}

						if(!friendTarget.isOnline()) {
							player.sendMessage(ConfigData.getPlayerOffline().replace("%player%", friendTarget.getName()));
							return;
						}

						StringBuilder msg = new StringBuilder();
						for(int i = 2; i < args.length; i++) {
							msg.append(args[i]);
						}

						String finalMsg = Utils.Color(ConfigData.getMsgFormat().replace("%msg%", msg.toString()).replace("%player%", player.getName()).replace("%server%", player.getServer().getInfo().getName()));

						player.sendMessage(finalMsg);
						friendTarget.sendMessage(finalMsg);

						String socialSpyMsg = ConfigData.getSpyMsgFormat().replace("%msg%", msg.toString()).replace("%sender%", player.getName()).replace("%sender-server%", player.getServer().getInfo().getName()).replace("%receiver%", friendTarget.getName()).replace("%receiver-server%", friendTarget.getServer());
						if(RedisHook.isEnabled()) {
							RedisHook.getRedis().sendMessage("friends", "socialspy:" + socialSpyMsg);
							return;
						}

						for(ProxiedPlayer staff : ProxyServer.getInstance().getPlayers()) {
							if(staff != null && staff.isConnected() && staff.hasPermission("friends.socialspy")) {
								staff.sendMessage(socialSpyMsg);
							}
						}
					});
					return;
				}

				if(args[0].equalsIgnoreCase("list")) {
					int page = 1;
					if(args.length == 2) {
						if (!Utils.isInt(args[1])) {
							player.sendMessage(ConfigData.getNumericalValue());
							return;
						}

						page = Integer.parseInt(args[1]);
					}
					int index = (page - 1) * ConfigData.getResultsPerPage();

					int finalPage = page;
					friendPlayer.getFriends().thenAcceptAsync(list -> {
						if(list.isEmpty()) {
							player.sendMessage(ConfigData.getNoFriends());
							return;
						}

						if(index > list.size()) {
							player.sendMessage(ConfigData.getPageNotFound());
							return;
						}

						for(String msg : ConfigData.getFriendListMessage()) {
							msg = msg.replaceAll("%current%", String.valueOf(finalPage));
							msg = msg.replaceAll("%max%", String.valueOf((int) Math.ceil((double) list.size() / ConfigData.getResultsPerPage())));
							msg = msg.replaceAll("%total%", String.valueOf(friendPlayer.getFriendsAmount()));

							msg = Utils.Color(msg);

							if(msg.contains("%friends%")) {
								for(int i = index; i < list.size(); i++) {
									FriendPlayer friendTarget = list.get(i);

									String fmsg = msg;
									fmsg = fmsg.replaceAll("%friends%", (friendTarget.isOnline() ? ConfigData.getOnline() : ConfigData.getOffline()));

									fmsg = fmsg.replaceAll("%name%", friendTarget.getName());
									fmsg = fmsg.replaceAll("%server%", (friendTarget.isOnline() ? friendTarget.getServer() : ""));
									fmsg = fmsg.replaceAll("%online%", (friendTarget.isOnline() ? "&aonline" : "&coffline"));
									fmsg = fmsg.replaceAll("%last-seen%", (friendTarget.isOnline() ? "&anow" : "&7" + friendTarget.getLastSeen()));

									fmsg = Utils.Color(fmsg);

									player.sendMessage(fmsg);
								}
								continue;
							}
							player.sendMessage(msg);
						}
					});
					return;
				}

				if(args[0].equalsIgnoreCase("requests")) {
					int page = 1;
					if(args.length == 2) {
						if (!Utils.isInt(args[1])) {
							player.sendMessage(ConfigData.getNumericalValue());
							return;
						}

						page = Integer.parseInt(args[1]);
					}
					int index = (page - 1) * ConfigData.getResultsPerPage();

					int finalPage = page;
					friendPlayer.getRequests().thenAcceptAsync(list -> {
						if(list.isEmpty()) {
							player.sendMessage(ConfigData.getNotNewRequests());
							return;
						}

						if(index > list.size()) {
							player.sendMessage(ConfigData.getPageNotFound());
							return;
						}

						for(String msg : ConfigData.getRequestListMessage()) {
							msg = msg.replaceAll("%current%", String.valueOf(finalPage));
							msg = msg.replaceAll("%max%", String.valueOf((int) Math.ceil((double) list.size() / ConfigData.getResultsPerPage())));

							if(msg.contains("%requests%")) {
								for(int i = index; i < list.size(); i++) {
									FriendPlayer friendTarget = list.get(i);

									String rmsg = msg;
									rmsg = rmsg.replaceAll("%requests%", ConfigData.getRequest());

									rmsg = rmsg.replaceAll("%name%", friendTarget.getName());
									rmsg = rmsg.replaceAll("%online%", (friendTarget.isOnline() ? "&aOnline" : "&cOffline"));
									rmsg = rmsg.replaceAll("%last-seen%", (friendTarget.isOnline() ? "&aNow" : "&7" + friendTarget.getLastSeen()));

									player.sendMessage(rmsg);
								}
								continue;
							}
							player.sendMessage(msg);
						}
					});
					return;
				}

				if (args.length != 2) {
					player.sendMessage(ConfigData.getValue());
					help(player);
					return;
				}

				if (player.getName().equalsIgnoreCase(args[1])) {
					player.sendMessage(ConfigData.getOwnRequest());
					return;
				}

				FriendPlayer.of(args[1]).thenAccept(friendTarget -> {
					if(friendTarget == null) {
						player.sendMessage(ConfigData.getPlayerNotRegistered());
						return;
					}

					switch (args[0].toLowerCase()) {
						case "seen": {
							if (!friendTarget.isFriend(player.getUniqueId())) {
								player.sendMessage(ConfigData.getPlayerNotFriend().replace("%player%", friendTarget.getName()));
								return;
							}

							if (!friendTarget.isOnline()) {
								DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
								player.sendMessage(ConfigData.formattedDate(ConfigData.getPlayerLastSeen(), ConfigData.formatDate(LocalDateTime.parse(friendTarget.getLastSeen(), df), LocalDateTime.now())));
								return;
							}

							if (player.getUniqueId().equals(friendTarget.getUuid())) {
								player.sendMessage(ConfigData.getOwnSeen());
								return;
							}

							player.sendMessage(ConfigData.getPlayerOnline().replace("%player%", friendTarget.getName()));
							return;
						}
						case "add": {
							String permission = Utils.getPermissionsLimits(player.getPermissions().toArray(new String[0]), ConfigData.getFriendsLimits());
							if (Utils.isInt(permission) && Integer.parseInt(permission) <= friendPlayer.getFriendsAmount()) {
								player.sendMessage(ConfigData.getFriendsLimitReached());
								return;
							}

							if (friendTarget.isFriend(player.getUniqueId())) {
								player.sendMessage(ConfigData.getPlayerIsFriend().replace("%player%", friendTarget.getName()));
								return;
							}

							if(!friendTarget.isOnline()) {
								player.sendMessage(ConfigData.getPlayerOffline().replace("%player%", friendTarget.getName()));
								return;
							}

							if (friendPlayer.hasRequest(friendTarget.getUuid())) {
								friendPlayer.addFriend(friendTarget.getUuid());
								friendTarget.addFriend(friendPlayer.getUuid());
								friendPlayer.removeRequest(friendTarget.getUuid());
								friendTarget.removeRequest(friendPlayer.getUuid());

								player.sendMessage(ConfigData.getNewFriend().replace("%player%", friendTarget.getName()));
								friendTarget.sendMessage(ConfigData.getNewFriend().replace("%player%", player.getName()));
								return;
							}

							friendTarget.addRequest(friendPlayer.getUuid());
							ProxiedPlayer target = ProxyServer.getInstance().getPlayer(friendTarget.getUuid());
							if(target.isConnected()) {
								TextComponent accept = new TextComponent(ConfigData.getP_accept());
								TextComponent decline = new TextComponent(ConfigData.getP_decline());

								accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends accept " + player.getName()));
								accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ConfigData.getD_accept()).create()));
								decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends decline " + player.getName()));
								decline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ConfigData.getD_decline()).create()));

								TextComponent current = new TextComponent(ConfigData.getRequestReceived().replace("%player%", player.getName()).replace("%action%", ""));

								current.addExtra(accept);
								current.addExtra(decline);

								target.sendMessage(current);
							} else {
								if(RedisHook.isEnabled()) {
									RedisHook.getRedis().sendMessage("friends", "request:" + friendTarget.getUuid() + ":" + player.getName());
								}
							}
							player.sendMessage(ConfigData.getRequestSend());
							return;
						}
						case "remove": {
							if (!friendTarget.isFriend(friendPlayer.getUuid())) {
								player.sendMessage(ConfigData.getPlayerNotFriend().replace("%player%", friendTarget.getName()));
								return;
							}

							friendPlayer.removeFriend(friendTarget.getUuid());
							friendTarget.removeFriend(friendPlayer.getUuid());

							player.sendMessage(ConfigData.getNoLongerFriends().replace("%player%", friendTarget.getName()));
							friendTarget.sendMessage(ConfigData.getNoLongerFriends().replace("%player%", player.getName()));
							return;
						}
						case "accept": {
							if (friendTarget.isFriend(player.getUniqueId())) {
								player.sendMessage(ConfigData.getPlayerIsFriend().replace("%player%", friendTarget.getName()));
								return;
							}

							String permission = Utils.getPermissionsLimits(player.getPermissions().toArray(new String[0]), ConfigData.getFriendsLimits());
							if (Utils.isInt(permission) && Integer.parseInt(permission) <= friendPlayer.getFriendsAmount()) {
								player.sendMessage(ConfigData.getFriendsLimitReached());
								return;
							}

							if(!friendPlayer.hasRequest(friendTarget.getUuid())) {
								player.sendMessage(ConfigData.getRequestNotSend().replace("%player%", friendTarget.getName()));
								return;
							}

							friendPlayer.addFriend(friendTarget.getUuid());
							friendTarget.addFriend(friendPlayer.getUuid());
							friendPlayer.removeRequest(friendTarget.getUuid());
							friendTarget.removeRequest(friendPlayer.getUuid());

							player.sendMessage(ConfigData.getNewFriend().replace("%player%", friendTarget.getName()));
							friendTarget.sendMessage(ConfigData.getNewFriend().replace("%player%", player.getName()));
							return;
						}
						case "decline": {
							if(!friendPlayer.hasRequest(friendTarget.getUuid())) {
								player.sendMessage(ConfigData.getNotNewRequests());
								return;
							}

							if(friendPlayer.isFriend(friendTarget.getUuid())) {
								player.sendMessage(ConfigData.getPlayerIsFriend().replace("%player%", friendTarget.getName()));
								return;
							}

							friendPlayer.removeRequest(friendTarget.getUuid());

							friendTarget.sendMessage(ConfigData.getRequestDeclined().replace("%player%", player.getName()));
							player.sendMessage(ConfigData.getOwnRequestDeclined().replace("%player%", friendTarget.getName()));
						}
					}
				});
			});
			return;
		}

		if(args[0].equalsIgnoreCase("reload")) {
			Main.getInstance().reload();

			sender.sendMessages(Utils.Color("&aPlugin reloaded!"));
		}
	}

	private void help(CommandSender sender) {
		ConfigData.getHelpFriendsMessage().forEach(msg -> {
			if(sender instanceof ProxiedPlayer && msg.contains("/friends reload")) {
				ProxiedPlayer player = (ProxiedPlayer) sender;
				if(player.hasPermission("friends.admin")) {
					sender.sendMessage(msg);
				}
			} else {
				sender.sendMessage(msg);
			}
		});
	}
}

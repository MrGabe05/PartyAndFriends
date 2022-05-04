package com.gabrielhd.friends;

import com.gabrielhd.friends.Commands.FriendsCmd;
import com.gabrielhd.friends.Commands.PartyCmd;
import com.gabrielhd.friends.Configuration.Config;
import com.gabrielhd.friends.Configuration.ConfigData;
import com.gabrielhd.friends.Database.Database;
import com.gabrielhd.friends.Listeners.NormalListeners;
import com.gabrielhd.friends.Listeners.RedisListeners;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.logging.Level;

public class Main extends Plugin {

	@Getter
	private static Main instance;

	@Override
	public void onEnable() {
		instance = this;

		reload();

		new Database(this);

		this.getProxy().getPluginManager().registerCommand(this, new PartyCmd());
		this.getProxy().getPluginManager().registerCommand(this, new FriendsCmd());
		this.getProxy().getPluginManager().registerListener(this, new NormalListeners());

		if(RedisHook.isEnabled()) {
			new RedisHook();

			this.getProxy().getPluginManager().registerListener(this, new RedisListeners());
		}
	}

	@Override
	public void onDisable() {
		if(RedisHook.isEnabled()) {
			RedisHook.getRedis().unregisterChannels();
		}
	}

	public void reload() {
		Config.createFiles("Settings");
		Config.createFiles("Messages");

		Configuration settings = Config.getConfiguration("Settings");
		Configuration messages = Config.getConfiguration("Messages");

		new ConfigData(settings, messages);
	}

	public static void debug(String msg) {
		if(ConfigData.isDebugEnabled()) Main.getInstance().getLogger().log(Level.INFO, msg);
	}
}

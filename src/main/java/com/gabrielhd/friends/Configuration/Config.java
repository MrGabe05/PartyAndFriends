package com.gabrielhd.friends.Configuration;

import com.gabrielhd.friends.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Config{

	public static void saveConfiguration(Configuration configuration, String file) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(Main.getInstance().getDataFolder(), file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFiles(String file) {
        File fileconfig = new File(Main.getInstance().getDataFolder(), file+".yml");
        if (fileconfig.exists()) {
            fileconfig.delete();
        }
    }

    public static void createFiles(String file) {
        if (!Main.getInstance().getDataFolder().exists()) {
            Main.getInstance().getDataFolder().mkdir();
        }
        File fileconfig = new File(Main.getInstance().getDataFolder(), file+".yml");
        if (!fileconfig.exists()) {
            try {
                InputStream in = Main.getInstance().getResourceAsStream(file+".yml");
                Files.copy(in, fileconfig.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Configuration getConfiguration(String file) {
        File configFile = new File(Main.getInstance().getDataFolder(), file+".yml");
        if (!configFile.exists()) {
            return null;
        }
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
}

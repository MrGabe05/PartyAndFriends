package com.gabrielhd.friends.Database;

import com.gabrielhd.friends.Configuration.ConfigData;
import com.gabrielhd.friends.Database.Types.MySQL;
import com.gabrielhd.friends.Database.Types.SQLite;
import com.gabrielhd.friends.Main;
import lombok.Getter;

public class Database {

    @Getter
    private static DataHandler storage;
    
    public Database(Main plugin) {
        if (ConfigData.getType().equalsIgnoreCase("mysql")) {
            storage = new MySQL(plugin, ConfigData.getHost(), ConfigData.getPort(), ConfigData.getDatabase(), ConfigData.getUsername(), ConfigData.getPassword());
        }
        else {
            storage = new SQLite(plugin);
        }
    }
}

package com.gabrielhd.friends.Database.Types;

import com.gabrielhd.friends.Database.DataHandler;
import com.gabrielhd.friends.Main;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SQLite extends DataHandler {

    private final Main plugin;
    private Connection connection;
    
    public SQLite(Main plugin) {
        this.plugin = plugin;

        this.connect();
        this.setup();

        this.plugin.getLogger().log(Level.INFO, "SQLite Setup finished");
    }
    
    private synchronized void connect() {
        try {
            new org.sqlite.JDBC();

            File database = new File(this.plugin.getDataFolder(), "Database.db");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Can't initialize database connection! Please check your configuration!");
            this.plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

            ex.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }
}

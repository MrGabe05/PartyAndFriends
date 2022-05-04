package com.gabrielhd.friends.Database.Types;

import com.gabrielhd.friends.Database.DataHandler;
import com.gabrielhd.friends.Main;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class MySQL extends DataHandler {

    private final String url;
    private final String username;
    private final String password;
    private Connection connection;
    private HikariDataSource ds;

    private final Main plugin;
    
    public MySQL(Main plugin, String host, String port, String database, String username, String password) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
        this.plugin = plugin;
        try {
            this.setConnectionArguments();
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                plugin.getLogger().log(Level.SEVERE, "Invalid database arguments! Please check your configuration!");
                plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

                throw new IllegalArgumentException(e);
            }
            if (e instanceof HikariPool.PoolInitializationException) {
                plugin.getLogger().log(Level.SEVERE, "Can't initialize database connection! Please check your configuration!");
                plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");
                throw new HikariPool.PoolInitializationException(e);
            }
            plugin.getLogger().log(Level.SEVERE, "Can't use the Hikari Connection Pool! Please, report this error to the developer!");
            throw e;
        }
        this.setup();

        this.plugin.getLogger().log(Level.INFO, "MySQL Setup finished");
    }
    
    private synchronized void setConnectionArguments() throws RuntimeException {
        (this.ds = new HikariDataSource()).setPoolName("Friends MySQL");

        this.ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.ds.setJdbcUrl(this.url);
        this.ds.addDataSourceProperty("cachePrepStmts", "true");
        this.ds.addDataSourceProperty("prepStmtCacheSize", "250");
        this.ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.ds.addDataSourceProperty("characterEncoding", "utf8");
        this.ds.addDataSourceProperty("encoding", "UTF-8");
        this.ds.addDataSourceProperty("useUnicode", "true");
        this.ds.addDataSourceProperty("useSSL", "false");
        this.ds.setUsername(this.username);
        this.ds.setPassword(this.password);
        this.ds.setMaxLifetime(180000L);
        this.ds.setIdleTimeout(60000L);
        this.ds.setMinimumIdle(1);
        this.ds.setMaximumPoolSize(8);
        try {
            this.connection = this.ds.getConnection();
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Error on setting connection!");
        }

        this.plugin.getLogger().log(Level.INFO, "Connection arguments loaded, Hikari ConnectionPool ready!");
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }
}

package com.gabrielhd.friends.Database;

import com.gabrielhd.friends.Main;
import com.gabrielhd.friends.Player.FriendPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;

public abstract class DataHandler {

    public abstract Connection getConnection();

    public static final String TABLE_PARTY = "party_data";
    public static final String TABLE_FRIENDS = "friends_data";

    private final String CREATE_PARTY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PARTY + " (uuid VARCHAR(100), members TEXT, requests TEXT, PRIMARY KEY (`uuid`))";
    private final String CREATE_FRIENDS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FRIENDS + " (uuid VARCHAR(100), playerName VARCHAR(100), last_seen VARCHAR(50), friends TEXT, requests TEXT, PRIMARY KEY (`uuid`))";

    private final String INSERT_PARTY_DATA = "INSERT INTO " + TABLE_PARTY + " (uuid, members, requests) VALUES ('%s', '%s', 'empty');";
    private final String INSERT_FRIENDS_DATA = "INSERT INTO " + TABLE_FRIENDS + " (uuid, playerName, last_seen, friends, requests) VALUES ('%s', 'null', 'none', 'empty', 'empty');";

    private final String DELETE_PARTY = "DELETE FROM " + TABLE_PARTY + " WHERE uuid=%s";

    private final String UPDATE_DATA = "UPDATE %s SET %s='%s' WHERE uuid='%s';";
    private final String SELECT_PLAYER = "SELECT * FROM %s WHERE uuid='%s'";

    private void execute(String sql, Object... replacements) throws SQLException {
        Main.debug("Executing: " + String.format(sql, replacements));

        Connection connection = this.getConnection();
        try(PreparedStatement statement = connection.prepareStatement(String.format(sql, replacements))) {
            statement.execute();
        }
    }

    public synchronized void setup() {
        try {
            this.execute(CREATE_PARTY_TABLE);
            this.execute(CREATE_FRIENDS_TABLE);
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error inserting columns! Please check your configuration!");
            Main.getInstance().getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

            e.printStackTrace();
        }
    }

    private boolean isClosed() {
        Connection connection = this.getConnection();
        try {
            if(connection != null && !connection.isClosed() && !connection.isValid(5000)) {
                return false;
            }
        } catch (SQLException ignored) {}
        return false;
    }

    private List<UUID> getUUIDs(String result) {
        List<UUID> list = new ArrayList<>();

        if(!result.isEmpty() && !result.equalsIgnoreCase("empty")) {
            String[] friends = result.split(":");
            Arrays.stream(friends).map(UUID::fromString).forEach(list::add);
        }

        return list;
    }

    private StringBuilder getResult(List<UUID> uuids) {
        StringBuilder result = new StringBuilder();
        uuids.forEach(uuid -> {
            if(result.length() > 0) {
                result.append(":");
            }

            result.append(uuid.toString());
        });

        return result;
    }

    public void deletePartyIfExists(UUID uuid) {
        try {
            this.execute(DELETE_PARTY, uuid.toString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void createPartyIfNotExists(UUID uuid) {
        Connection connection = this.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, TABLE_PARTY, uuid.toString()))) {
            ResultSet rs = statement.executeQuery();

            if(rs != null && rs.next()) return;

            this.execute(INSERT_PARTY_DATA, uuid.toString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void createPlayerIfNotExists(UUID uuid) {
        Connection connection = this.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, TABLE_FRIENDS, uuid.toString()))) {
            ResultSet rs = statement.executeQuery();

            if(rs != null && rs.next()) return;

            this.execute(INSERT_FRIENDS_DATA, uuid.toString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public CompletionStage<Void> updateList(UUID uuid, String table, String column, List<UUID> result) {
        return CompletableFuture.runAsync(() -> {
            if(isClosed()) return;

            Connection connection = this.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, table, uuid.toString())); ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    this.execute(UPDATE_DATA, table, column, getResult(result), uuid.toString());
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public CompletionStage<Void> update(UUID uuid, String table, String column, String update) {
        return CompletableFuture.runAsync(() -> {
            if (isClosed()) return;

            Connection connection = this.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, table, uuid.toString())); ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) this.execute(UPDATE_DATA, table, column, update, uuid.toString());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public CompletionStage<List<UUID>> getList(UUID uuid, String table, String column) {
        return CompletableFuture.supplyAsync(() -> {
            if(isClosed()) return new ArrayList<>();

            Connection connection = this.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, table, uuid.toString())); ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return getUUIDs(resultSet.getString(column));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            return new ArrayList<>();
        });
    }

    public CompletionStage<String> getString(UUID uuid, String table, String column) {
        return CompletableFuture.supplyAsync(() -> {
            if (isClosed()) return "";

            Connection connection = this.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, table, uuid.toString())); ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getString(column);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            return "";
        });
    }
}

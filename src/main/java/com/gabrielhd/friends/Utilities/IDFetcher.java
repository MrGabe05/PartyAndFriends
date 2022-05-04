package com.gabrielhd.friends.Utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class IDFetcher {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";
    private static final Map<String, IDFetcher> uuidCache = new HashMap<>();
    private static final Gson gson = new GsonBuilder().create();

    private final UUID uuid;
    private final String name;

    public IDFetcher(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        uuidCache.put(name.toLowerCase(), this);
    }

    public static IDFetcher getIDFetcher(String name) {
        name = name.toLowerCase();
        if (uuidCache.containsKey(name)) {
            return uuidCache.get(name);
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name)).openConnection();
            connection.setReadTimeout(5000);

            return gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), IDFetcher.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static UUID fromString(String input) {
        return UUID.fromString(input.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}

package com.gabrielhd.friends.Configuration;

import com.gabrielhd.friends.Utilities.Utils;
import lombok.Getter;
import net.md_5.bungee.config.Configuration;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigData {

	@Getter
	private static String
		type,
		host,
		port,
		username,
		password,
		database;

	@Getter
	private static String
		friendsLimitReached,
		newFriend,
		newRequests,
		notNewRequests,
		noLongerFriends,
		noFriends,
		numericalValue,
		value,
		ownRequest,
		ownRequestDeclined,
		ownSeen,
		pageNotFound,
		playerLastSeen,
		playerOnline,
		playerOffline,
		playerIsFriend,
		playerLogOut,
		playerNotFriend,
		playerNotRegistered,
		requestDeclined,
		requestLimit,
		requestNotSend,
		requestReceived,
		msgFormat,
		spyMsgFormat,
		requestSend;

	@Getter
	private static String
	    p_accept,
	    p_decline,
	    d_accept,
	    d_decline,
	    requestAction,
	    online,
		offline,
		request,
	    year,
	    years,
	    month,
	    months,
	    week,
	    weeks,
	    day,
	    days,
	    hour,
	    hours,
	    minute,
	    minutes,
	    seconds,
	    few;

	@Getter
	private static String
		cantYourSelf,
		cantInviteSelf,
		alreadyAtTheParty,
		alreadyInAParty,
		alreadyInvite,
		alreadyMod,
		isntMod,
		partyMaxMembers,
		cantKickSelf,
		notInTheParty;

	@Getter private static boolean debugEnabled;
	@Getter private static String[] friendsLimits, partyLimits;
	@Getter private static int resultsPerPage, purgeTime, requestClear, disbandTime;
	@Getter private static HashMap<String,String> stringsInClass;

	@Getter private static List<String> helpFriendsMessage, friendListMessage, requestListMessage, helpPartyMessage, blacklistServers;
	
	public ConfigData(Configuration settings, Configuration messages) {
		debugEnabled = settings.getBoolean("Debug", false);

		type = settings.getString("StorageType", "sqlite");
		host = settings.getString("MySQL.Host", "localhost");
		port = settings.getString("MySQL.Port", "3306");
		username = settings.getString("MySQL.Username");
		password = settings.getString("MySQL.Password");
		database = settings.getString("MySQL.Database");

		//messages strings
		friendsLimitReached = Utils.Color(messages.getString("Messages.FriendsLimitReached"));
		newFriend = Utils.Color(messages.getString("Messages.NewFriend"));
		newRequests = Utils.Color(messages.getString("Messages.Requests"));
		notNewRequests = Utils.Color(messages.getString("Messages.NotRequests"));
		noFriends = Utils.Color(messages.getString("Messages.NoFriends"));
		noLongerFriends = Utils.Color(messages.getString("Messages.NoLongerFriends"));
		numericalValue = Utils.Color(messages.getString("Messages.InsertNumericalValue"));
		value = Utils.Color(messages.getString("Messages.InsertValue"));
		ownRequest = Utils.Color(messages.getString("Messages.OwnRequest"));
		ownRequestDeclined = Utils.Color(messages.getString("Messages.OwnRequestDeclined"));
		ownSeen = Utils.Color(messages.getString("Messages.OwnSeen"));
		pageNotFound = Utils.Color(messages.getString("Messages.PageNotFound"));
		playerLastSeen = Utils.Color(messages.getString("Messages.PlayerLastSeen"));
		playerLogOut = Utils.Color(messages.getString("Messages.PlayerLogOut"));
		playerNotFriend = Utils.Color(messages.getString("Messages.PlayerNotFriend"));
		playerNotRegistered = Utils.Color(messages.getString("Messages.PlayerNotRegistered"));
		playerOnline = Utils.Color(messages.getString("Messages.PlayerIsOnline"));
		playerOffline = Utils.Color(messages.getString("Messages.PlayerIsOffline"));
		playerIsFriend = Utils.Color(messages.getString("Messages.PlayerIsFriend"));
		requestDeclined = Utils.Color(messages.getString("Messages.RequestDeclined"));
		requestLimit = Utils.Color(messages.getString("Messages.RequestLimit"));
		requestNotSend = Utils.Color(messages.getString("Messages.RequestNotSended"));
		requestReceived = Utils.Color(messages.getString("Messages.RequestReceived"));
		requestSend = Utils.Color(messages.getString("Messages.RequestSend"));
		msgFormat = Utils.Color(messages.getString("Messages.MsgFormat"));
		spyMsgFormat = Utils.Color(messages.getString("Messages.SpyMsgFormat"));

		//placeholder strings
		p_accept = Utils.Color(messages.getString("Placeholders.Accept"));
		p_decline = Utils.Color(messages.getString("Placeholders.Decline"));
		d_accept = Utils.Color(messages.getString("Placeholders.AcceptDescription"));
		d_decline = Utils.Color(messages.getString("Placeholders.DeclineDescription"));
		online = Utils.Color(messages.getString("Placeholders.Online"));
		offline = Utils.Color(messages.getString("Placeholders.Offline"));
		request = Utils.Color(messages.getString("Placeholders.Request"));
		year = Utils.Color(messages.getString("Placeholders.Year"));
		years = Utils.Color(messages.getString("Placeholders.Years"));
		month = Utils.Color(messages.getString("Placeholders.Month"));
		months = Utils.Color(messages.getString("Placeholders.Months"));
		week = Utils.Color(messages.getString("Placeholders.Week"));
		weeks = Utils.Color(messages.getString("Placeholders.Weeks"));
		day = Utils.Color(messages.getString("Placeholders.Day"));
		days = Utils.Color(messages.getString("Placeholders.Days"));
		hour = Utils.Color(messages.getString("Placeholders.Hour"));
		hours = Utils.Color(messages.getString("Placeholders.Hours"));
		minute = Utils.Color(messages.getString("Placeholders.Minute"));
		minutes = Utils.Color(messages.getString("Placeholders.Minutes"));
		seconds = Utils.Color(messages.getString("Placeholders.Seconds"));
		few = Utils.Color(messages.getString("Placeholders.Few"));

		helpFriendsMessage = messages.getStringList("Messages.HelpFriends").stream().map(Utils::Color).collect(Collectors.toList());
		friendListMessage = messages.getStringList("Messages.FriendList").stream().map(Utils::Color).collect(Collectors.toList());
		requestListMessage = messages.getStringList("Messages.RequestList").stream().map(Utils::Color).collect(Collectors.toList());
		helpPartyMessage = messages.getStringList("Messages.HelpParty").stream().map(Utils::Color).collect(Collectors.toList());
		
		friendsLimits = settings.getStringList("Friends.Permissions").toArray(new String[0]);
		resultsPerPage = settings.getInt("Friends.MaxResultsPerPage");

		cantInviteSelf = Utils.Color(messages.getString("Messages.CantInviteSelf"));
		alreadyAtTheParty = Utils.Color(messages.getString("Messages.AlreadyAtTheParty"));
		alreadyInAParty = Utils.Color(messages.getString("Messages.AlreadyInAParty"));
		alreadyInvite = Utils.Color(messages.getString("Messages.AlreadyInvite"));
		partyMaxMembers = Utils.Color(messages.getString("Messages.PartyMaxMembers"));

		partyLimits = settings.getStringList("Party.Permissions").toArray(new String[0]);
		
		try {
			stringsInClass = setStrings();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static String[] formatDate(LocalDateTime date, LocalDateTime reference) {
		long until = date.until(reference, ChronoUnit.SECONDS);
		
		if(until < 60) {
			return new String[] {few, seconds};
		}
		if(until > 60 && until < 3600) {
			return new String[] {String.valueOf(until / 60), until / 60 > 1 ? minutes : minute};
		}
		if(until > 3600 && until < 86400) {
			return new String[] {String.valueOf(until / 3600),until / 3600 > 1 ?  hours : hour};
		}
		if(until > 86400 && until < 604800) {
			return new String[] {String.valueOf(until / 86400), until / 86400 > 1 ? days : day};
		}
		if(until > 604800 && Period.between(LocalDate.now(), date.toLocalDate()).getMonths() < 1) {
			return new String[] {String.valueOf(until / 604800),until / 604800 > 1 ? weeks : week};
		}
		if(Period.between(LocalDate.now(), date.toLocalDate()).getMonths() >= 1 && Period.between(LocalDate.now(), date.toLocalDate()).getYears() < 1) {
			return new String[] {String.valueOf(Period.between(LocalDate.now(), date.toLocalDate()).getMonths()), Period.between(LocalDate.now(), date.toLocalDate()).getMonths() > 1 ? months : month};
		}
		if(Period.between(LocalDate.now(), date.toLocalDate()).getYears() >= 1) {
			return new String[] {String.valueOf(until / 604800),(Period.between(LocalDate.now(), date.toLocalDate()).getYears() > 1 ? years : year)};
		}

		return null;
	}

	public static String formattedDate(String msg, String[] formattedDate) {
		return msg.replace("%time%", formattedDate[0]).replace("%time_unit%", formattedDate[1]);
	}
	
	public static String getStringInClass(String key) 
	{
		return stringsInClass.get(key);
	}
	
	public static String getStringInClassKey(String value) {
		for(Map.Entry<String, String> pair : stringsInClass.entrySet()) {
			if(pair.getValue().equalsIgnoreCase(value)) return pair.getKey();
		}
		
		return "";
	}
	
	private static HashMap<String,String> setStrings() throws IllegalArgumentException, IllegalAccessException {
		HashMap<String, String> strings = new HashMap<>();
		
		Class<ConfigData> clazz = ConfigData.class;
        Field[] arr = clazz.getFields(); // Get all public fields of your class
        
        for (Field f : arr) {
            if (f.getType().equals(String.class)) {
                String s = (String)f.get(null);
                
                strings.put(f.getName(), s);
            }
        }
        return strings;
	}
}

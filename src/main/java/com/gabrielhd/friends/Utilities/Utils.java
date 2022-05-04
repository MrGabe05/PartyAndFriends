package com.gabrielhd.friends.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
	
	public static String Color(String string) {
        return string.replaceAll("&", "§");
    }

    public static List<String> Color(String... strings) {
		return Arrays.stream(strings).map(Utils::Color).collect(Collectors.toList());
	}
	
	public static boolean isInt(String s) {
        try {
        	Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

	public static String getPermissionsLimits(String[] permissions, String[] otherPermissions) {
		ArrayList<String> limits = new ArrayList<>();
				
		if(otherPermissions.length == 0) {
			return "*";
		}
		
		for(String p : permissions) {
			for(String fp : otherPermissions) {
				if(fp.equalsIgnoreCase(p)) {
					limits.add(fp.split("\\.")[2]);
				}
			}
		}
		
		if(limits.size() == 1) {
			return limits.get(0);
		}

		ArrayList<Integer> IntegerLimits = new ArrayList<>();
		if(limits.size() > 1) {
			for(String s : limits) {
				if(!isInt(s)) {
					return "*";
				} else {
					IntegerLimits.add(Integer.valueOf(s));
				}
			}
		} else {
			for(String s : otherPermissions) {
				if(isInt(s.split("\\.")[2])) {
					IntegerLimits.add(Integer.valueOf(s.split("\\.")[2]));
				}
			}
		}
		return getMax(IntegerLimits.toArray(new Integer[0])).toString();
	}
	
	public static Integer getMax(Integer[] integers) {
		int max = 0;
		
		for(int i: integers) {
			if(i > max) max = i;
		}
		
		return max;
	}
	
	public static Integer getMin(Integer[] values) {
		int min = getMax(values);
		
		for(int i : values) {
			if(i < min) min = i;
		}
		
		return min;
	}
}

package me.dustin.jex.friend;

import java.util.ArrayList;

public class Friend {

    private static ArrayList<Friend> friendsList = new ArrayList<Friend>();

    private String name;
    private String alias;

    public Friend(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public static void addFriend(Friend friend) {
        friendsList.add(friend);
    }

    public static void addFriend(String name, String alias) {
        addFriend(new Friend(name, alias));
    }

    public static void removeFriend(String name) {
        friendsList.remove(getFriendViaName(name));
    }

    public static boolean isFriend(String name) {
        return getFriendViaName(name) != null;
    }

    public static Friend getFriendViaName(String name) {
        for (Friend friend : friendsList) {
            if (friend.getName().equalsIgnoreCase(name))
                return friend;
        }
        return null;
    }

    public static Friend getFriendViaAlias(String alias) {
        for (Friend friend : friendsList) {
            if (friend.getAlias().equalsIgnoreCase(alias))
                return friend;
        }
        return null;
    }

    public static ArrayList<Friend> getFriendsList() {
        return friendsList;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }
}

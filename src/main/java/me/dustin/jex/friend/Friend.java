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
        return friendsList.stream().filter(friend -> friend.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Friend getFriendViaAlias(String alias) {
        return friendsList.stream().filter(friend -> friend.getAlias().equalsIgnoreCase(alias)).findFirst().orElse(null);
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

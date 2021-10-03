package me.dustin.jex.helper.player;

import java.util.ArrayList;

public enum FriendHelper {
    INSTANCE;
    private final ArrayList<Friend> friendsList = new ArrayList<Friend>();

    public void addFriend(Friend friend) {
        friendsList.add(friend);
        friendsList.sort((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()));
    }

    public void addFriend(String name, String alias) {
        addFriend(new Friend(name, alias));
    }

    public void removeFriend(String name) {
        friendsList.remove(getFriendViaName(name));
    }

    public boolean isFriend(String name) {
        return getFriendViaName(name) != null;
    }

    public Friend getFriendViaName(String name) {
        for (Friend friend : friendsList) {
            if (friend.name().equalsIgnoreCase(name))
                return friend;
        }
        return null;
    }

    public Friend getFriendViaAlias(String alias) {
        for (Friend friend : friendsList) {
            if (friend.alias().equalsIgnoreCase(alias))
                return friend;
        }
        return null;
    }

    public ArrayList<Friend> getFriendsList() {
        return friendsList;
    }

    public record Friend(String name, String alias){}
}

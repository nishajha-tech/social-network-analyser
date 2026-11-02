package backend;

import java.util.ArrayList;

/**
 * User Class
 * Represents a person in the Social Network.
 * 
 * Beginner DSA concepts used:
 * - Object-Oriented Programming (encapsulation)
 * - ArrayList to store friends as strings
 * - Linear loops to check for existing friends
 */
public class User {
    private String name;
    private ArrayList<String> friends;

    public User(String name) {
        this.name = name.trim();
        this.friends = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public int getFriendCount() {
        return friends.size();
    }

    /**
     * Checks if a person is already in the friends list.
     * Uses a simple linear loop.
     */
    public boolean isFriend(String friendName) {
        if (friendName == null) return false;
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).equalsIgnoreCase(friendName.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a friend to the friend list if not already added and not self.
     * Returns true if added, false otherwise.
     */
    public boolean addFriend(String friendName) {
        if (friendName == null || friendName.trim().isEmpty()) {
            return false;
        }
        String cleanName = friendName.trim();
        if (cleanName.equalsIgnoreCase(this.name)) {
            return false; // Cannot befriend oneself
        }
        if (isFriend(cleanName)) {
            return false; // Already friends
        }
        friends.add(cleanName);
        return true;
    }

    /**
     * Removes a friend from the friend list.
     */
    public boolean removeFriend(String friendName) {
        if (friendName == null) return false;
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).equalsIgnoreCase(friendName.trim())) {
                friends.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Simple JSON converter for easy web communication without external libraries.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(escapeJson(name)).append("\",");
        sb.append("\"friendCount\":").append(friends.size()).append(",");
        sb.append("\"friends\":[");
        for (int i = 0; i < friends.size(); i++) {
            sb.append("\"").append(escapeJson(friends.get(i))).append("\"");
            if (i < friends.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return name + " (" + friends.size() + " friends: " + String.join(", ", friends) + ")";
    }
}

package backend;

import java.util.ArrayList;

/**
 * Suggestion Class
 * Holds a friend recommendation for a user, along with the mutual friends connecting them.
 */
public class Suggestion {
    private String candidateName;
    private int mutualCount;
    private ArrayList<String> mutualFriends;

    public Suggestion(String candidateName) {
        this.candidateName = candidateName;
        this.mutualCount = 0;
        this.mutualFriends = new ArrayList<>();
    }

    public String getCandidateName() {
        return candidateName;
    }

    public int getMutualCount() {
        return mutualCount;
    }

    public ArrayList<String> getMutualFriends() {
        return mutualFriends;
    }

    public void addMutualFriend(String friendName) {
        // Prevent duplicate entries in mutual friends list
        for (int i = 0; i < mutualFriends.size(); i++) {
            if (mutualFriends.get(i).equalsIgnoreCase(friendName.trim())) {
                return;
            }
        }
        mutualFriends.add(friendName.trim());
        mutualCount = mutualFriends.size();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"candidateName\":\"").append(escapeJson(candidateName)).append("\",");
        sb.append("\"mutualCount\":").append(mutualCount).append(",");
        sb.append("\"mutualFriends\":[");
        for (int i = 0; i < mutualFriends.size(); i++) {
            sb.append("\"").append(escapeJson(mutualFriends.get(i))).append("\"");
            if (i < mutualFriends.size() - 1) {
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
}

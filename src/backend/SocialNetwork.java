package backend;

import java.util.ArrayList;

/**
 * SocialNetwork Class
 * The core brain of the Social Network Analyser.
 * 
 * STRICT DSA CONSTRAINTS HONORED:
 * - NO Graphs, NO Adjacency Matrices, NO Adjacency Lists.
 * - NO BFS, NO DFS, NO Dijkstra, NO Complex Graph Traversals.
 * - Uses only:
 *    1. Classes & Objects (User, Suggestion, NetworkStats)
 *    2. ArrayLists for simple list storage
 *    3. For-loops & Nested for-loops for iteration and comparison
 *    4. Simple Selection/Bubble Sort for ranking
 *    5. Linear Search for finding users
 */
public class SocialNetwork {
    // Single list of all registered users
    private ArrayList<User> users;

    public SocialNetwork() {
        this.users = new ArrayList<>();
    }

    // ==========================================
    // A. ADD USER
    // ==========================================
    /**
     * Adds a new user to the network.
     * Validates that the name is not empty and not already taken.
     */
    public synchronized boolean addUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String cleanName = name.trim();

        // Check for duplicates using a simple linear search loop
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getName().equalsIgnoreCase(cleanName)) {
                return false; // Name already exists (duplicate prevention)
            }
        }

        // Add new user object to our ArrayList
        users.add(new User(cleanName));
        return true;
    }

    // ==========================================
    // HELPER: FIND USER (Linear Search)
    // ==========================================
    /**
     * Searches for a user object by name using a simple linear search.
     * Returns the User object if found, or null otherwise.
     */
    public User findUser(String name) {
        if (name == null) return null;
        String cleanName = name.trim();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getName().equalsIgnoreCase(cleanName)) {
                return u;
            }
        }
        return null;
    }

    // ==========================================
    // B. ADD FRIENDS (Mutual Friendship)
    // ==========================================
    /**
     * Connects two users as mutual friends.
     * Validates:
     * - Both users must exist
     * - Cannot befriend oneself (self-friendship prevention)
     * - Cannot add duplicate friendship
     */
    public synchronized boolean addFriendship(String name1, String name2) {
        if (name1 == null || name2 == null) return false;
        String u1Name = name1.trim();
        String u2Name = name2.trim();

        // 1. Prevent self-friendship
        if (u1Name.equalsIgnoreCase(u2Name)) {
            return false;
        }

        // 2. Locate both users using simple linear search
        User user1 = findUser(u1Name);
        User user2 = findUser(u2Name);

        if (user1 == null || user2 == null) {
            return false; // One or both users do not exist
        }

        // 3. Prevent duplicate friendship
        if (user1.isFriend(user2.getName())) {
            return false; // Already friends
        }

        // 4. Mutual friendship: Add each other to their respective friend lists
        boolean added1 = user1.addFriend(user2.getName());
        boolean added2 = user2.addFriend(user1.getName());

        return added1 && added2;
    }

    // ==========================================
    // C. DISPLAY USERS
    // ==========================================
    /**
     * Returns the complete list of registered users.
     */
    public ArrayList<User> getAllUsers() {
        return users;
    }

    // ==========================================
    // D. FIND MUTUAL FRIENDS
    // ==========================================
    /**
     * Compares two users' friend lists using simple nested loops.
     * Returns an ArrayList containing all mutual friend names.
     */
    public ArrayList<String> getMutualFriends(String name1, String name2) {
        ArrayList<String> mutual = new ArrayList<>();

        User u1 = findUser(name1);
        User u2 = findUser(name2);

        if (u1 == null || u2 == null) {
            return mutual;
        }

        ArrayList<String> friends1 = u1.getFriends();
        ArrayList<String> friends2 = u2.getFriends();

        // Simple nested loop to compare elements
        for (int i = 0; i < friends1.size(); i++) {
            String f1 = friends1.get(i);
            for (int j = 0; j < friends2.size(); j++) {
                String f2 = friends2.get(j);
                if (f1.equalsIgnoreCase(f2)) {
                    mutual.add(f1);
                    break;
                }
            }
        }

        return mutual;
    }

    // ==========================================
    // E. FRIEND SUGGESTIONS (Friends of Friends)
    // ==========================================
    /**
     * Recommends users who are friends of the selected user's friends,
     * but not yet directly connected to the selected user.
     * 
     * Uses strictly nested loops and simple conditions:
     * - Loop 1: Iterate over direct friends of target user
     * - Loop 2: Iterate over friends of those direct friends (FoF)
     * - Conditional checks:
     *     1. FoF is NOT the target user (no self-suggestion)
     *     2. FoF is NOT already a direct friend of the target user
     */
    public ArrayList<Suggestion> getFriendSuggestions(String targetName) {
        ArrayList<Suggestion> suggestions = new ArrayList<>();
        User target = findUser(targetName);

        if (target == null) {
            return suggestions;
        }

        ArrayList<String> directFriends = target.getFriends();

        // Loop 1: Go through each direct friend
        for (int i = 0; i < directFriends.size(); i++) {
            String friendName = directFriends.get(i);
            User friendUser = findUser(friendName);

            if (friendUser == null) continue;

            ArrayList<String> friendsOfFriend = friendUser.getFriends();

            // Loop 2: Go through each friend of that friend
            for (int j = 0; j < friendsOfFriend.size(); j++) {
                String candidateName = friendsOfFriend.get(j);

                // Condition 1: Cannot suggest the user themselves
                if (candidateName.equalsIgnoreCase(target.getName())) {
                    continue;
                }

                // Condition 2: Cannot suggest someone who is ALREADY a direct friend
                if (target.isFriend(candidateName)) {
                    continue;
                }

                // Find or add to suggestions list
                Suggestion foundSuggestion = null;
                for (int k = 0; k < suggestions.size(); k++) {
                    if (suggestions.get(k).getCandidateName().equalsIgnoreCase(candidateName)) {
                        foundSuggestion = suggestions.get(k);
                        break;
                    }
                }

                if (foundSuggestion == null) {
                    foundSuggestion = new Suggestion(candidateName);
                    suggestions.add(foundSuggestion);
                }

                // Record the mutual friend bridging them (friendName)
                foundSuggestion.addMutualFriend(friendName);
            }
        }

        // Simple Bubble Sort to order suggestions by highest mutual friend count
        for (int i = 0; i < suggestions.size() - 1; i++) {
            for (int j = 0; j < suggestions.size() - i - 1; j++) {
                if (suggestions.get(j).getMutualCount() < suggestions.get(j + 1).getMutualCount()) {
                    Suggestion temp = suggestions.get(j);
                    suggestions.set(j, suggestions.get(j + 1));
                    suggestions.set(j + 1, temp);
                }
            }
        }

        return suggestions;
    }

    // ==========================================
    // F. BASIC NETWORK ANALYSIS
    // ==========================================
    /**
     * Calculates overall statistics using simple loops.
     */
    public NetworkStats getNetworkStatistics() {
        int totalUsers = users.size();

        if (totalUsers == 0) {
            return new NetworkStats(0, 0, "None", 0, 0.0, 0);
        }

        int totalDegrees = 0;
        int maxFriends = -1;
        User mostPopular = null;
        int isolatedCount = 0;

        // Loop through all users to collect stats
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            int count = u.getFriendCount();
            totalDegrees += count;

            if (count == 0) {
                isolatedCount++;
            }

            // Find user with highest friend count
            if (count > maxFriends) {
                maxFriends = count;
                mostPopular = u;
            }
        }

        // Because every mutual friendship is stored once in user1 and once in user2,
        // the total unique friendships is the sum of all friend lists divided by 2.
        int totalFriendships = totalDegrees / 2;

        double averageFriends = (double) totalDegrees / totalUsers;

        String popularName = (mostPopular != null && maxFriends > 0) ? mostPopular.getName() : "None";

        return new NetworkStats(totalUsers, totalFriendships, popularName, 
                                Math.max(0, maxFriends), averageFriends, isolatedCount);
    }

    // ==========================================
    // G. SEARCH USER
    // ==========================================
    /**
     * Searches for users by name (supports partial matching, case-insensitive).
     */
    public ArrayList<User> searchUsers(String query) {
        ArrayList<User> matches = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return matches;
        }

        String q = query.trim().toLowerCase();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getName().toLowerCase().contains(q)) {
                matches.add(u);
            }
        }
        return matches;
    }

    // ==========================================
    // SAMPLE DEMO DATA
    // ==========================================
    /**
     * Loads the sample data specified in the project requirements:
     * Users: Nisha, Priya, Rahul, Aman, Karan
     * Friendships:
     * - Nisha and Priya
     * - Nisha and Aman
     * - Priya and Rahul
     * - Priya and Karan
     */
    public synchronized void loadDemoData() {
        resetNetwork();

        // 1. Add users
        addUser("Nisha");
        addUser("Priya");
        addUser("Rahul");
        addUser("Aman");
        addUser("Karan");

        // 2. Add friendships
        addFriendship("Nisha", "Priya");
        addFriendship("Nisha", "Aman");
        addFriendship("Priya", "Rahul");
        addFriendship("Priya", "Karan");
    }

    /**
     * Clears all users and friendships from the network.
     */
    public synchronized void resetNetwork() {
        users.clear();
    }

    /**
     * Serializes the entire network into a JSON payload for the frontend.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Stats
        NetworkStats stats = getNetworkStatistics();
        sb.append("\"stats\":").append(stats.toJson()).append(",");

        // Users
        sb.append("\"users\":[");
        for (int i = 0; i < users.size(); i++) {
            sb.append(users.get(i).toJson());
            if (i < users.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("],");

        // Unique friendships list (useful for drawing network graph lines)
        sb.append("\"friendships\":[");
        ArrayList<String> recordedEdges = new ArrayList<>();
        boolean first = true;
        for (int i = 0; i < users.size(); i++) {
            User u1 = users.get(i);
            for (int j = 0; j < u1.getFriends().size(); j++) {
                String fName = u1.getFriends().get(j);
                String edgeKey1 = u1.getName() + "---" + fName;
                String edgeKey2 = fName + "---" + u1.getName();

                // Check if already output
                boolean alreadyRecorded = false;
                for (int k = 0; k < recordedEdges.size(); k++) {
                    if (recordedEdges.get(k).equalsIgnoreCase(edgeKey1) || 
                        recordedEdges.get(k).equalsIgnoreCase(edgeKey2)) {
                        alreadyRecorded = true;
                        break;
                    }
                }

                if (!alreadyRecorded) {
                    recordedEdges.add(edgeKey1);
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append("{\"source\":\"").append(escapeJson(u1.getName()))
                      .append("\",\"target\":\"").append(escapeJson(fName)).append("\"}");
                    first = false;
                }
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

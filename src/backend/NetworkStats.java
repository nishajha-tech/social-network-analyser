package backend;

/**
 * NetworkStats Class
 * Encapsulates overall metrics and analytics for the social network.
 */
public class NetworkStats {
    private int totalUsers;
    private int totalFriendships;
    private String mostPopularUser;
    private int maxFriends;
    private double averageFriends;
    private int isolatedUsersCount;

    public NetworkStats(int totalUsers, int totalFriendships, String mostPopularUser, 
                        int maxFriends, double averageFriends, int isolatedUsersCount) {
        this.totalUsers = totalUsers;
        this.totalFriendships = totalFriendships;
        this.mostPopularUser = mostPopularUser != null ? mostPopularUser : "None";
        this.maxFriends = maxFriends;
        this.averageFriends = averageFriends;
        this.isolatedUsersCount = isolatedUsersCount;
    }

    public int getTotalUsers() { return totalUsers; }
    public int getTotalFriendships() { return totalFriendships; }
    public String getMostPopularUser() { return mostPopularUser; }
    public int getMaxFriends() { return maxFriends; }
    public double getAverageFriends() { return averageFriends; }
    public int getIsolatedUsersCount() { return isolatedUsersCount; }

    public String toJson() {
        return String.format(java.util.Locale.US,
            "{\"totalUsers\":%d,\"totalFriendships\":%d,\"mostPopularUser\":\"%s\",\"maxFriends\":%d,\"averageFriends\":%.2f,\"isolatedUsersCount\":%d}",
            totalUsers, totalFriendships, escapeJson(mostPopularUser), maxFriends, averageFriends, isolatedUsersCount
        );
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

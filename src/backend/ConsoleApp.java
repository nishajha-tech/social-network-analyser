package backend;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * ConsoleApp
 * Interactive Terminal / CLI runner for the Social Network Analyser.
 * Perfect for college lab demonstrations, viva questions, and command-line testing.
 * 
 * Strict DSA: Pure ArrayLists, Loops, and OOP.
 */
public class ConsoleApp {
    public static void main(String[] args) {
        SocialNetwork network = new SocialNetwork();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("  SOCIAL NETWORK ANALYSER - TERMINAL MODE");
        System.out.println("  (Strict Beginner DSA: Arrays, ArrayLists, Loops)");
        System.out.println("=================================================");

        // Automatically load sample data initially
        network.loadDemoData();
        System.out.println("[*] Demo data loaded (Nisha, Priya, Rahul, Aman, Karan)\n");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice (1-9): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": // Add User
                    System.out.print("Enter new user name: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("[Error] Name cannot be empty.");
                    } else if (network.addUser(name)) {
                        System.out.println("[Success] User '" + name + "' added successfully!");
                    } else {
                        System.out.println("[Error] User '" + name + "' already exists.");
                    }
                    break;

                case "2": // Add Friends
                    System.out.print("Enter first user name: ");
                    String u1 = scanner.nextLine().trim();
                    System.out.print("Enter second user name: ");
                    String u2 = scanner.nextLine().trim();
                    if (network.addFriendship(u1, u2)) {
                        System.out.println("[Success] Mutual friendship added between " + u1 + " and " + u2 + "!");
                    } else {
                        System.out.println("[Error] Friendship could not be added. (Check names or duplicate friendship)");
                    }
                    break;

                case "3": // Display Users
                    ArrayList<User> allUsers = network.getAllUsers();
                    System.out.println("\n--- REGISTERED USERS & FRIENDSHIPS ---");
                    if (allUsers.isEmpty()) {
                        System.out.println("No users registered yet.");
                    } else {
                        for (int i = 0; i < allUsers.size(); i++) {
                            User u = allUsers.get(i);
                            System.out.println((i + 1) + ". " + u.getName() + " (Friends: " + u.getFriendCount() + ")");
                            if (u.getFriendCount() > 0) {
                                System.out.println("   Friends list: " + String.join(", ", u.getFriends()));
                            } else {
                                System.out.println("   Friends list: [No friends yet]");
                            }
                        }
                    }
                    break;

                case "4": // Find Mutual Friends
                    System.out.print("Enter first user name: ");
                    String m1 = scanner.nextLine().trim();
                    System.out.print("Enter second user name: ");
                    String m2 = scanner.nextLine().trim();
                    ArrayList<String> mutual = network.getMutualFriends(m1, m2);
                    System.out.println("\n--- MUTUAL FRIENDS BETWEEN " + m1.toUpperCase() + " & " + m2.toUpperCase() + " ---");
                    if (mutual.isEmpty()) {
                        System.out.println("No mutual friends found.");
                    } else {
                        System.out.println("Found " + mutual.size() + " mutual friend(s): " + String.join(", ", mutual));
                    }
                    break;

                case "5": // Friend Suggestions
                    System.out.print("Enter user name to get suggestions for: ");
                    String targetUser = scanner.nextLine().trim();
                    ArrayList<Suggestion> suggestions = network.getFriendSuggestions(targetUser);
                    System.out.println("\n--- FRIEND SUGGESTIONS FOR " + targetUser.toUpperCase() + " ---");
                    if (suggestions.isEmpty()) {
                        System.out.println("No suggestions available for " + targetUser + ".");
                    } else {
                        for (int i = 0; i < suggestions.size(); i++) {
                            Suggestion s = suggestions.get(i);
                            System.out.println((i + 1) + ". " + s.getCandidateName() + 
                                " (" + s.getMutualCount() + " mutual friends: " + 
                                String.join(", ", s.getMutualFriends()) + ")");
                        }
                    }
                    break;

                case "6": // Basic Network Analysis
                    NetworkStats stats = network.getNetworkStatistics();
                    System.out.println("\n--- NETWORK ANALYSIS REPORT ---");
                    System.out.println("Total Registered Users:    " + stats.getTotalUsers());
                    System.out.println("Total Unique Friendships:  " + stats.getTotalFriendships());
                    System.out.println("Most Popular User:         " + stats.getMostPopularUser() + " (" + stats.getMaxFriends() + " friends)");
                    System.out.printf("Average Friends per User:  %.2f\n", stats.getAverageFriends());
                    System.out.println("Isolated Users (0 friends): " + stats.getIsolatedUsersCount());
                    break;

                case "7": // Search User
                    System.out.print("Enter search term (name): ");
                    String query = scanner.nextLine().trim();
                    ArrayList<User> matches = network.searchUsers(query);
                    System.out.println("\n--- SEARCH RESULTS FOR '" + query + "' ---");
                    if (matches.isEmpty()) {
                        System.out.println("No users found matching '" + query + "'.");
                    } else {
                        for (int i = 0; i < matches.size(); i++) {
                            User u = matches.get(i);
                            System.out.println((i + 1) + ". " + u.getName() + " | Connections: " + u.getFriendCount() + 
                                               " | Friends: " + String.join(", ", u.getFriends()));
                        }
                    }
                    break;

                case "8": // Reload Demo Data
                    network.loadDemoData();
                    System.out.println("[Success] Demo data reloaded (Nisha, Priya, Rahul, Aman, Karan)!");
                    break;

                case "9": // Exit
                    System.out.println("Exiting Social Network Analyser. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("[Error] Invalid option. Please enter a number between 1 and 9.");
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("---------------- MENU ----------------");
        System.out.println("1. Add User");
        System.out.println("2. Add Friendship");
        System.out.println("3. Display All Users & Friends");
        System.out.println("4. Find Mutual Friends");
        System.out.println("5. Get Friend Suggestions (Friends of Friends)");
        System.out.println("6. Basic Network Analysis");
        System.out.println("7. Search User");
        System.out.println("8. Reload Demo Data");
        System.out.println("9. Exit");
        System.out.println("--------------------------------------");
    }
}

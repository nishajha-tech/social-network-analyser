package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Main Application Runner
 * 
 * Starts the Social Network Analyser embedded server and serves the Web Dashboard.
 * Can be run directly from VS Code or terminal with:
 *   javac backend/*.java
 *   java backend.Main
 */
public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Starting Social Network Analyser...");

        int port = DEFAULT_PORT;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (Exception ignored) {}
        }
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception ignored) {}
        }

        // 1. Initialize Social Network logic
        SocialNetwork network = new SocialNetwork();

        // 2. Pre-load sample data as requested
        network.loadDemoData();

        // 3. Locate frontend files directory
        String frontendPath = resolveFrontendPath();
        System.out.println("Serving frontend from: " + frontendPath);

        // 4. Start HTTP Server
        try {
            NetworkServer server = new NetworkServer(port, network, frontendPath);
            server.start();

            printBanner(port);

            // Keep main thread alive
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down Social Network Analyser server...");
                server.stop();
            }));

        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
            System.err.println("Make sure port " + port + " is not already in use by another application.");
        }
    }

    private static String resolveFrontendPath() {
        // Check relative paths based on where it's launched
        String[] candidatePaths = {
            "src/frontend",
            "frontend",
            "../frontend",
            "social-network-analyser/src/frontend",
            "C:/Users/Akhil/.gemini/antigravity-ide/scratch/social-network-analyser/src/frontend"
        };

        for (String p : candidatePaths) {
            File f = new File(p);
            if (f.exists() && f.isDirectory() && new File(f, "index.html").exists()) {
                return f.getAbsolutePath();
            }
        }

        // Default fallback to standard path
        return Paths.get("src", "frontend").toAbsolutePath().toString();
    }

    private static void printBanner(int port) {
        System.out.println("\n" +
            "╔═══════════════════════════════════════════════════════════════════════════╗\n" +
            "║                  🌐 SOCIAL NETWORK ANALYSER                              ║\n" +
            "║             Beginner DSA Edition (Classes, ArrayLists, Loops)             ║\n" +
            "╠═══════════════════════════════════════════════════════════════════════════╣\n" +
            "║  [✓] Web Dashboard:   http://localhost:" + port + "                          ║\n" +
            "║  [✓] REST API:         http://localhost:" + port + "/api/network                  ║\n" +
            "║  [✓] Graph Constraint: ZERO Graph/BFS/DFS - Pure ArrayLists & Loops       ║\n" +
            "║  [✓] Demo Data:        Nisha, Priya, Rahul, Aman, Karan                   ║\n" +
            "╚═══════════════════════════════════════════════════════════════════════════╝\n" +
            "  Open http://localhost:" + port + " in your browser to interact with the UI!\n"
        );
    }
}

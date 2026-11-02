package backend;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NetworkServer
 * Lightweight embedded HTTP server using Java's built-in HttpServer.
 * 
 * Provides:
 * 1. Static file serving for Frontend (HTML, CSS, JS)
 * 2. REST API endpoints connecting Frontend UI directly to SocialNetwork logic
 * 
 * No third-party frameworks, no Spring Boot, no external JARs needed!
 */
public class NetworkServer {
    private int port;
    private HttpServer server;
    private SocialNetwork socialNetwork;
    private String frontendPath;

    public NetworkServer(int port, SocialNetwork network, String frontendPath) {
        this.port = port;
        this.socialNetwork = network;
        this.frontendPath = frontendPath;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Contexts
        server.createContext("/api/network", this::handleGetNetwork);
        server.createContext("/api/user", this::handleAddUser);
        server.createContext("/api/friendship", this::handleAddFriendship);
        server.createContext("/api/mutual", this::handleGetMutualFriends);
        server.createContext("/api/suggestions", this::handleGetSuggestions);
        server.createContext("/api/search", this::handleSearchUsers);
        server.createContext("/api/demo", this::handleLoadDemo);
        server.createContext("/api/reset", this::handleResetNetwork);

        // Static Frontend Files Context
        server.createContext("/", this::handleStaticFiles);

        server.setExecutor(null); // Default single/multithread executor
        server.start();
        System.out.println("=================================================");
        System.out.println(" Social Network Analyser Server Started!");
        System.out.println(" Web UI URL: http://localhost:" + port);
        System.out.println(" REST API available at: http://localhost:" + port + "/api/network");
        System.out.println("=================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ==========================================
    // API HANDLERS
    // ==========================================

    private void handleGetNetwork(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String json = socialNetwork.toJson();
        sendJsonResponse(exchange, 200, json);
    }

    private void handleAddUser(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        String body = readRequestBody(exchange);
        String name = extractJsonField(body, "name");

        if (name == null || name.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"User name cannot be empty!\"}");
            return;
        }

        boolean success = socialNetwork.addUser(name.trim());
        if (success) {
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"User '" + escapeJson(name) + "' added successfully!\"}");
        } else {
            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"User '" + escapeJson(name) + "' already exists!\"}");
        }
    }

    private void handleAddFriendship(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        String body = readRequestBody(exchange);
        String user1 = extractJsonField(body, "user1");
        String user2 = extractJsonField(body, "user2");

        if (user1 == null || user2 == null || user1.trim().isEmpty() || user2.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Both usernames must be selected!\"}");
            return;
        }

        if (user1.trim().equalsIgnoreCase(user2.trim())) {
            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"A user cannot befriend themselves!\"}");
            return;
        }

        boolean success = socialNetwork.addFriendship(user1.trim(), user2.trim());
        if (success) {
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Mutual friendship established between '" + escapeJson(user1) + "' and '" + escapeJson(user2) + "'!\"}");
        } else {
            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Could not add friendship. Users might already be friends or do not exist.\"}");
        }
    }

    private void handleGetMutualFriends(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String user1 = queryParams.get("user1");
        String user2 = queryParams.get("user2");

        if (user1 == null || user2 == null) {
            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Please specify both user1 and user2 query parameters.\"}");
            return;
        }

        ArrayList<String> mutual = socialNetwork.getMutualFriends(user1, user2);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"user1\":\"").append(escapeJson(user1)).append("\",");
        sb.append("\"user2\":\"").append(escapeJson(user2)).append("\",");
        sb.append("\"count\":").append(mutual.size()).append(",");
        sb.append("\"mutualFriends\":[");
        for (int i = 0; i < mutual.size(); i++) {
            sb.append("\"").append(escapeJson(mutual.get(i))).append("\"");
            if (i < mutual.size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");

        sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleGetSuggestions(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String user = queryParams.get("user");

        if (user == null || user.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Please specify the user parameter.\"}");
            return;
        }

        ArrayList<Suggestion> suggestions = socialNetwork.getFriendSuggestions(user.trim());
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"user\":\"").append(escapeJson(user)).append("\",");
        sb.append("\"count\":").append(suggestions.size()).append(",");
        sb.append("\"suggestions\":[");
        for (int i = 0; i < suggestions.size(); i++) {
            sb.append(suggestions.get(i).toJson());
            if (i < suggestions.size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");

        sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleSearchUsers(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String q = queryParams.get("q");

        ArrayList<User> results = socialNetwork.searchUsers(q != null ? q : "");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"query\":\"").append(escapeJson(q != null ? q : "")).append("\",");
        sb.append("\"count\":").append(results.size()).append(",");
        sb.append("\"results\":[");
        for (int i = 0; i < results.size(); i++) {
            sb.append(results.get(i).toJson());
            if (i < results.size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");

        sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleLoadDemo(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        socialNetwork.loadDemoData();
        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Demo data loaded successfully with Nisha, Priya, Rahul, Aman, and Karan!\"}");
    }

    private void handleResetNetwork(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        socialNetwork.resetNetwork();
        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Social network has been reset!\"}");
    }

    // ==========================================
    // STATIC FILE HANDLER
    // ==========================================
    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String uriPath = exchange.getRequestURI().getPath();
        if (uriPath == null || uriPath.equals("/") || uriPath.isEmpty()) {
            uriPath = "/index.html";
        }

        // Prevent directory traversal
        uriPath = uriPath.replaceAll("\\.\\.", "");

        Path filePath = Paths.get(frontendPath, uriPath.replace('/', File.separatorChar));

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            String notFound = "<h1>404 Not Found</h1><p>File not found: " + uriPath + "</p>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
            return;
        }

        String mimeType = getMimeType(filePath.toString());
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        byte[] fileBytes = Files.readAllBytes(filePath);
        exchange.sendResponseHeaders(200, fileBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(fileBytes);
        os.close();
    }

    // ==========================================
    // UTILITIES
    // ==========================================
    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private String extractJsonField(String json, String field) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0 && idx < pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    params.put(key, value);
                } else if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    params.put(key, "");
                }
            } catch (Exception ignored) {}
        }
        return params;
    }

    private String getMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

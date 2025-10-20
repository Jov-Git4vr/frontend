package com.bidverse.frontend;

public class AuthService {

    // 1. Static field to hold the logged-in user's role
    private static String currentUserRole = null;

    // 2. Public Getter for the LoginPage to retrieve the role
    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    /**
     * POST /login with {"email":"...","password":"..."}
     * Returns true if backend returned 2xx and login was successful, false
     * otherwise.
     */
    public static boolean login(String email, String password) {
        currentUserRole = null; // Reset state before attempting a new login

        if (email == null)
            email = "";
        if (password == null)
            password = "";

        String json = "{\"email\":\"" + escapeJson(email) + "\",\"password\":\"" + escapeJson(password) + "\"}";

        String resp = ApiClient.postJson("/login", json);

        if (resp != null && !resp.trim().isEmpty()) {
            try {
                // 3. MANUAL PARSING: Find the "role" field and extract its value
                String roleKey = "\"role\":\"";
                int roleStartIndex = resp.indexOf(roleKey);

                if (roleStartIndex != -1) {
                    // Start of the role value
                    roleStartIndex += roleKey.length();

                    // End of the role value (first double quote after the start)
                    int roleEndIndex = resp.indexOf("\"", roleStartIndex);

                    if (roleEndIndex != -1) {
                        // Extract the role string
                        String role = resp.substring(roleStartIndex, roleEndIndex);

                        // 4. Store the role in the static field
                        currentUserRole = role;
                        return true; // Login succeeded
                    }
                }

                // If parsing fails (role not found or JSON structure is unexpected)
                System.err.println("JSON parsing failed: Could not find/extract the 'role' field.");
                return false;

            } catch (Exception e) {
                // Unexpected error during parsing
                System.err.println("Error during manual role extraction: " + e.getMessage());
                return false;
            }
        }

        // Login failed (resp was null/empty)
        return false;
    }

    private static String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
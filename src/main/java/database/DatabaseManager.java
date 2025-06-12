package database;

import data.MatchData;
import data.PasswordEncrypt;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all database interactions for the Snooker application using the Singleton pattern.
 * This class handles the database connection, table initialization, and all CRUD
 * (Create, Read, Update, Delete) operations for users, matches, breaks, and reports.
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class DatabaseManager {
    /** The single instance of the DatabaseManager. */
    public static final DatabaseManager INSTANCE = new DatabaseManager();

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/snooker_db?createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection conn;

    private int currentUserId = -1;

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private DatabaseManager() {}

    /**
     * Sets the ID of the currently logged-in user.
     * @param userId The user's ID from the database.
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    /**
     * Gets the ID of the currently logged-in user.
     * @return The current user's ID, or -1 if no user is logged in.
     */
    public int getCurrentUserId() {
        return this.currentUserId;
    }

    /**
     * Establishes a connection to the MySQL database.
     * If a connection is already open, this method does nothing.
     */
    public final void connect() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            conn = null;
        }
    }

    /**
     * Closes the active database connection.
     */
    public void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing the connection: " + e.getMessage());
        }
    }

    /**
     * Initializes the database schema. It creates the necessary tables (users,
     * matches, breaks, reports) if they do not already exist.
     *
     * @throws RuntimeException if the database connection cannot be established or
     * if table creation fails.
     */
    public void initialize() {
        connect();
        if (conn == null) {
            throw new RuntimeException("Fatal Error: Could not establish a database connection.");
        }

        try (Statement stmt = conn.createStatement()) {
            String createUserTable = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL);";
            stmt.execute(createUserTable);

            String createMatchTable = "CREATE TABLE IF NOT EXISTS matches (id INT AUTO_INCREMENT PRIMARY KEY, player1_name VARCHAR(255) NOT NULL, player2_name VARCHAR(255) NOT NULL, score VARCHAR(50), match_date DATE NOT NULL);";
            stmt.execute(createMatchTable);

            String createBreaksTable = "CREATE TABLE IF NOT EXISTS breaks (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, player_name VARCHAR(255), break_score INT NOT NULL, match_id INT, FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL);";
            stmt.execute(createBreaksTable);

            String createReportsTable = "CREATE TABLE IF NOT EXISTS reports (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, report_message TEXT NOT NULL, report_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);";
            stmt.execute(createReportsTable);

        } catch (SQLException e) {
            throw new RuntimeException("Fatal Error: Table initialization failed. Error: " + e.getMessage(), e);
        }
    }

    /**
     * Saves the result of a completed match to the database.
     *
     * @param player1 The name of the first player.
     * @param player2 The name of the second player.
     * @param score1 The final score of the first player.
     * @param score2 The final score of the second player.
     * @return The auto-generated ID of the new match record, or -1 on failure.
     */
    public int saveMatchResult(String player1, String player2, int score1, int score2) {
        connect();
        if (conn == null) return -1;

        String sql = "INSERT INTO matches(player1_name, player2_name, score, match_date) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, player1);
            pstmt.setString(2, player2);
            pstmt.setString(3, score1 + " : " + score2);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving match result: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Saves the highest break of a match, linking it to the player and the specific match.
     *
     * @param matchId The ID of the match where the break was made.
     * @param playerName The name of the player who made the break.
     * @param breakScore The score of the highest break.
     * @return {@code true} if the break was saved successfully, {@code false} otherwise.
     */
    public boolean saveHighestBreak(int matchId, String playerName, int breakScore) {
        connect();
        if (conn == null) return false;

        Integer userId = getUserIdByName(playerName);

        String sql = "INSERT INTO breaks(user_id, player_name, break_score, match_id) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                pstmt.setInt(1, userId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, playerName);
            pstmt.setInt(3, breakScore);
            pstmt.setInt(4, matchId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving highest break: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves a bug report submitted by a logged-in user.
     *
     * @param userId The ID of the user submitting the report.
     * @param message The content of the bug report.
     * @return {@code true} if the report was saved successfully, {@code false} otherwise.
     */
    public boolean saveReport(int userId, String message) {
        connect();
        if (conn == null) return false;
        String sql = "INSERT INTO reports(user_id, report_message) VALUES(?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, message);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving report: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a list of all matches from the database, ordered by date.
     *
     * @return A list of {@link MatchData} objects representing all matches.
     */
    public List<MatchData> getAllMatches() {
        connect();
        List<MatchData> matches = new ArrayList<>();
        if (conn == null) return matches;

        String sql = "SELECT id, player1_name, player2_name, score, match_date FROM matches ORDER BY match_date DESC, id DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                matches.add(new MatchData(
                        rs.getInt("id"),
                        rs.getString("player1_name"),
                        rs.getString("player2_name"),
                        rs.getString("score"),
                        rs.getDate("match_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all matches: " + e.getMessage());
        }
        return matches;
    }

    /**
     * Registers a new user in the database with a hashed password.
     *
     * @param username The desired username.
     * @param password The plain-text password.
     * @return {@code true} if registration is successful, {@code false} otherwise.
     */
    public boolean registerUser(String username, String password) {
        connect();
        if (conn == null) return false;
        String encryptedPassword = PasswordEncrypt.hashPassword(password);
        String sql = "INSERT INTO users(username, password) VALUES(?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, encryptedPassword);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a user's ID based on their username.
     *
     * @param username The username to search for.
     * @return The user's ID as an {@link Integer}, or {@code null} if not found.
     */
    public Integer getUserIdByName(String username) {
        connect();
        if (conn == null) return null;
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID by name: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validates a user's credentials against the database.
     * If validation is successful, the user's ID is stored for the session.
     *
     * @param username The username to validate.
     * @param password The plain-text password to check.
     * @return {@code true} if the credentials are correct, {@code false} otherwise.
     */
    public boolean validateUser(String username, String password) {
        connect();
        if (conn == null) return false;
        Integer userId = getUserIdByName(username);
        if(userId == null) return false;

        String sql = "SELECT password FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (PasswordEncrypt.checkPassword(password, rs.getString("password"))) {
                        setCurrentUserId(userId);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during user validation: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a match record from the database using its ID.
     * This also cascades to delete related break records.
     *
     * @param matchId The ID of the match to delete.
     */
    public void deleteMatch(int matchId) {
        connect();
        if (conn == null) return;
        String sql = "DELETE FROM matches WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting match: " + e.getMessage());
        }
    }
}

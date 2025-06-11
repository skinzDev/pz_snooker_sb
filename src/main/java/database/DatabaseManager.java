package database;

import data.MatchData;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton klasa za upravljanje konekcijom i operacijama sa MySQL bazom podataka.
 * Enkapsulira sve SQL upite i konekcione detalje.
 */
public class DatabaseManager {
    public static final DatabaseManager INSTANCE = new DatabaseManager();
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/snooker_db?createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // <-- UNESITE VAŠU MySQL LOZINKU OVDE

    private Connection conn;

    private DatabaseManager() {
        // Poveži se jednom pri inicijalizaciji instance
        connect();
    }

    /**
     * Uspostavlja konekciju sa bazom podataka. Ako konekcija već postoji, ne radi ništa.
     */
    public final void connect() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Greška prilikom konekcije na bazu: " + e.getMessage());
        }
    }

    /**
     * Prekida konekciju sa bazom podataka.
     */
    public void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Greška prilikom zatvaranja konekcije: " + e.getMessage());
        }
    }

    /**
     * Inicijalizuje tabele 'users' i 'matches' ako ne postoje.
     * Dodaje kolonu 'highest_break' u tabelu 'matches'.
     */
    public void initialize() {
        connect(); // Osiguraj da postoji konekcija
        String createUserTable = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL);";
        String createMatchTable = "CREATE TABLE IF NOT EXISTS matches (id INT AUTO_INCREMENT PRIMARY KEY, player1_name VARCHAR(255) NOT NULL, player2_name VARCHAR(255) NOT NULL, score VARCHAR(50), highest_break INT DEFAULT 0, match_date DATE NOT NULL);";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createMatchTable);
        } catch (SQLException e) {
            System.err.println("Greška prilikom inicijalizacije tabela: " + e.getMessage());
        }
    }

    /**
     * Registruje novog korisnika.
     * @param username Korisničko ime
     * @param password Lozinka
     * @return true ako je uspeh, false inače
     */
    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?,?)";
        connect();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Greška prilikom registracije: " + e.getMessage());
            return false;
        }
    }

    /**
     * Proverava validnost korisničkih podataka.
     * @param username Korisničko ime
     * @param password Lozinka
     * @return true ako je validan, false inače
     */
    public boolean validateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        connect();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password").equals(password);
                }
            }
        } catch (SQLException e) {
            System.err.println("Greška prilikom validacije korisnika: " + e.getMessage());
        }
        return false;
    }

    /**
     * Čuva rezultat meča, uključujući i najveći brejk.
     * @param player1 Ime igrača 1
     * @param player2 Ime igrača 2
     * @param score1 Poeni igrača 1
     * @param score2 Poeni igrača 2
     * @param highestBreak Najveći brejk u meču
     */
    public void saveMatchResult(String player1, String player2, int score1, int score2, int highestBreak) {
        String sql = "INSERT INTO matches(player1_name, player2_name, score, highest_break, match_date) VALUES(?,?,?,?,?)";
        connect();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player1);
            pstmt.setString(2, player2);
            pstmt.setString(3, score1 + " : " + score2);
            pstmt.setInt(4, highestBreak);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Greška prilikom čuvanja rezultata meča: " + e.getMessage());
        }
    }

    /**
     * Dohvata sve sačuvane mečeve iz baze.
     * @return Lista MatchData objekata
     */
    public List<MatchData> getAllMatches() {
        List<MatchData> matches = new ArrayList<>();
        String sql = "SELECT id, player1_name, player2_name, score, highest_break, match_date FROM matches ORDER BY match_date DESC, id DESC";
        connect();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                matches.add(new MatchData(
                        rs.getInt("id"),
                        rs.getString("player1_name"),
                        rs.getString("player2_name"),
                        rs.getString("score"),
                        rs.getInt("highest_break"),
                        rs.getDate("match_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Greška prilikom dohvatanja istorije mečeva: " + e.getMessage());
        }
        return matches;
    }

    /**
     * Briše meč iz baze na osnovu ID-a.
     * @param matchId ID meča za brisanje
     */
    public void deleteMatch(int matchId) {
        String sql = "DELETE FROM matches WHERE id = ?";
        connect();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Greška prilikom brisanja meča: " + e.getMessage());
        }
    }
}
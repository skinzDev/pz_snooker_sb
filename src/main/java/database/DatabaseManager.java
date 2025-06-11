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
    private static final String PASSWORD = "";
    private Connection conn;

    private DatabaseManager() {
        // Prazan konstruktor
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
            // Postavljamo conn na null da bi ostatak aplikacije znao da je neuspešno
            conn = null;
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
     * Inicijalizuje bazu, kreira tabele i automatski dodaje kolone koje nedostaju.
     * Ova metoda je sada ključna za rešavanje problema.
     */
    public void initialize() {
        connect();
        if (conn == null) {
            throw new RuntimeException("Fatalna greška: Nije moguće uspostaviti konekciju sa bazom podataka.");
        }

        try (Statement stmt = conn.createStatement()) {
            // 1. Kreiraj tabele ako ne postoje
            String createUserTable = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL);";
            stmt.execute(createUserTable);

            String createMatchTable = "CREATE TABLE IF NOT EXISTS matches (id INT AUTO_INCREMENT PRIMARY KEY, player1_name VARCHAR(255) NOT NULL, player2_name VARCHAR(255) NOT NULL, score VARCHAR(50), match_date DATE);";
            stmt.execute(createMatchTable);

            // 2. Proveri i dodaj kolonu 'highest_break' ako ne postoji (KLJUČNA ISPRAVKA)
            try {
                // Pokušaj da dodaš kolonu. Ako već postoji, baciće SQLException.
                stmt.executeUpdate("ALTER TABLE matches ADD COLUMN highest_break INT DEFAULT 0");
                System.out.println("INFO: Kolona 'highest_break' je uspešno dodata u tabelu 'matches'.");
            } catch (SQLException e) {
                // Greška "Duplicate column name" je očekivana ako kolona već postoji i nju ignorišemo.
                if (!e.getMessage().contains("Duplicate column name")) {
                    // Ako je greška nešto drugo, onda je problem.
                    throw e;
                }
            }

            // 3. Proveri i osiguraj da 'match_date' nije NULL za buduće unose
            try {
                stmt.executeUpdate("ALTER TABLE matches MODIFY COLUMN match_date DATE NOT NULL");
            } catch (SQLException e) {
                // Ako ne uspe, verovatno postoje redovi sa NULL datumom. To je ok za stare podatke.
                System.err.println("Upozorenje: Nije bilo moguće postaviti 'match_date' na NOT NULL. Moguće da postoje stari mečevi bez datuma.");
            }

        } catch (SQLException e) {
            // Ako bilo šta od ovoga pukne, aplikacija ne može da radi.
            throw new RuntimeException("Fatalna greška: Inicijalizacija tabela nije uspela. Greška: " + e.getMessage(), e);
        }
    }

    public boolean saveMatchResult(String player1, String player2, int score1, int score2, int highestBreak) {
        connect();
        if (conn == null) {
            System.err.println("Database connection is null when saving match result");
            return false;
        }

        String sql = "INSERT INTO matches(player1_name, player2_name, score, highest_break, match_date) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player1);
            pstmt.setString(2, player2);
            pstmt.setString(3, score1 + " : " + score2);
            pstmt.setInt(4, highestBreak);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows inserted: " + rowsAffected); // Debug line
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Greška prilikom čuvanja rezultata meča: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for more details
            return false;
        }
    }

    public List<MatchData> getAllMatches() {
        connect();
        List<MatchData> matches = new ArrayList<>();

        if (conn == null) {
            System.err.println("DEBUG: Connection is null in getAllMatches()");
            return matches;
        }

        System.out.println("DEBUG: Connection established, executing query...");

        String sql = "SELECT id, player1_name, player2_name, score, highest_break, match_date FROM matches ORDER BY match_date DESC, id DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("DEBUG: Query executed successfully");

            int rowCount = 0;
            while (rs.next()) {
                // Debug each field
                int id = rs.getInt("id");
                String player1 = rs.getString("player1_name");
                String player2 = rs.getString("player2_name");
                String score = rs.getString("score");
                int highestBreak = rs.getInt("highest_break");
                Date sqlDate = rs.getDate("match_date");

                // FIXED: Handle null dates properly - use current date as fallback
                LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.now();

                // FIXED: Handle null/empty strings
                if (player1 == null) player1 = "N/A";
                if (player2 == null) player2 = "N/A";
                if (score == null) score = "0 : 0";

                System.out.println("DEBUG: Row " + (rowCount + 1) + " - ID: " + id + ", Player1: " + player1 +
                        ", Player2: " + player2 + ", Score: " + score +
                        ", Break: " + highestBreak + ", Date: " + localDate);

                matches.add(new MatchData(id, player1, player2, score, highestBreak, localDate));
                rowCount++;
            }

            System.out.println("DEBUG: Total rows retrieved: " + rowCount);
            System.out.println("DEBUG: List size: " + matches.size());

        } catch (SQLException e) {
            System.err.println("DEBUG: SQL Exception in getAllMatches: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    // Ostale metode ostaju iste kao u prethodnom odgovoru...
    public boolean registerUser(String username, String password) {
        connect();
        if (conn == null) return false;
        String sql = "INSERT INTO users(username, password) VALUES(?,?)";
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

    public boolean validateUser(String username, String password) {
        connect();
        if (conn == null) return false;
        String sql = "SELECT password FROM users WHERE username = ?";
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

    public void deleteMatch(int matchId) {
        connect();
        if (conn == null) return;
        String sql = "DELETE FROM matches WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Greška prilikom brisanja meča: " + e.getMessage());
        }
    }
}
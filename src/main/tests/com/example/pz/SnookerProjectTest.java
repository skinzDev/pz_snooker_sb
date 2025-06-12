package com.example.pz;

import database.DatabaseManager;
import logika.Snooker;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for the Snooker project.
 * This class contains unit tests for the core game logic in the {@link Snooker} class
 * and integration tests for the {@link DatabaseManager}.
 * <p>
 * NOTE: For DatabaseManager tests to run, a local MySQL instance must be
 * available and configured with the details in {@link DatabaseManager}.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SnookerProjectTest {

    private Snooker snookerGame;
    private DatabaseManager dbManager;

    /**
     * Initializes the database schema once before any tests in this class are run.
     */
    @BeforeAll
    public void setupAll() {
        dbManager = DatabaseManager.INSTANCE;
        dbManager.initialize();
    }

    /**
     * Sets up a fresh game instance and database connection before each test.
     * This ensures test isolation.
     */
    @BeforeEach
    public void setUp() {
        dbManager.connect();
        snookerGame = new Snooker(15);
    }

    /**
     * Closes the database connection after each test to release resources.
     */
    @AfterEach
    public void tearDown() {
        dbManager.disconnect();
    }


    /**
     * Tests that points are correctly added to the current player and the break score is updated.
     */
    @Test
    public void testPointScoringAndBreakUpdate() {
        assertTrue(snookerGame.jeIgrac1NaRedu(), "Player 1 should be on turn at the start.");

        snookerGame.klikNaBoju(1);
        assertEquals(1, snookerGame.getPoeni1(), "Player 1 score should be 1.");
        assertEquals(1, snookerGame.getCurrentBreak(), "Current break should be 1.");

        snookerGame.klikNaBoju(7);
        assertEquals(8, snookerGame.getPoeni1(), "Player 1 score should be 8.");
        assertEquals(8, snookerGame.getCurrentBreak(), "Current break should be 8.");
        assertEquals(0, snookerGame.getPoeni2(), "Player 2 score should be 0.");
    }

    /**
     * Tests that a miss correctly ends the turn, resets the break, and switches players.
     */
    @Test
    public void testMissResetsBreakAndSwitchesPlayer() {
        snookerGame.klikNaBoju(1);
        assertEquals(1, snookerGame.getCurrentBreak(), "Break should be 1 before miss.");
        assertTrue(snookerGame.jeIgrac1NaRedu(), "Player 1 should be on turn.");

        snookerGame.promasaj();

        assertEquals(0, snookerGame.getCurrentBreak(), "Break should reset to 0 after a miss.");
        assertFalse(snookerGame.jeIgrac1NaRedu(), "It should be Player 2's turn after Player 1 misses.");
    }

    /**
     * Tests that the highest break of the match is correctly tracked across both players' turns.
     */
    @Test
    public void testHighestBreakTracking() {
        snookerGame.klikNaBoju(1);
        snookerGame.klikNaBoju(7);
        assertEquals(8, snookerGame.getHighestBreakInMatch(), "Highest break should be 8.");
        assertEquals(1, snookerGame.getPlayerWithHighestBreak(), "Player 1 should have the highest break.");

        snookerGame.promasaj();
        assertFalse(snookerGame.jeIgrac1NaRedu(), "Should be Player 2's turn.");

        snookerGame.klikNaBoju(1);
        snookerGame.klikNaBoju(7);
        snookerGame.klikNaBoju(1);
        snookerGame.klikNaBoju(7);

        assertEquals(16, snookerGame.getCurrentBreak(), "Player 2's current break should be 16.");
        assertEquals(16, snookerGame.getHighestBreakInMatch(), "Highest break should now be 16.");
        assertEquals(2, snookerGame.getPlayerWithHighestBreak(), "Player 2 should now have the highest break.");
    }

    /**
     * Tests the endgame logic, ensuring colored balls must be potted in the correct sequence.
     */
    @Test
    public void testEndgameLogic() {
        Snooker endgameSnooker = new Snooker(1);
        endgameSnooker.klikNaBoju(1); // Pot the last red
        endgameSnooker.klikNaBoju(7); // Pot a color

        assertTrue(endgameSnooker.isEndgame(), "Game should be in endgame state.");
        assertFalse(endgameSnooker.jeIgrac1NaRedu(), "It should be Player 2's turn to start the clearance.");
        assertEquals(2, endgameSnooker.getNextColorValue(), "The next required ball must be yellow (2).");

        assertTrue(endgameSnooker.klikNaBoju(2), "Player 2 potting yellow should be a valid move.");
        assertEquals(3, endgameSnooker.getNextColorValue(), "Next ball should be green (3).");
        assertEquals(2, endgameSnooker.getPoeni2(), "Player 2 score should be 2.");

        assertFalse(endgameSnooker.klikNaBoju(5), "Potting blue out of order should be an invalid move.");
        assertEquals(3, endgameSnooker.getNextColorValue(), "Next ball should still be green (3).");

        assertTrue(endgameSnooker.jeIgrac1NaRedu(), "It should be player 1's turn after Player 2 misses.");
    }

    /**
     * Tests successful user registration in the database.
     */
    @Test
    public void testUserRegistration_Success() {
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        String password = "password123";

        boolean registered = dbManager.registerUser(uniqueUsername, password);
        assertTrue(registered, "User should be successfully registered.");

        // Cleanup: remove the test user
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/snooker_db", "root", "");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users WHERE username = '" + uniqueUsername + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests that a registered user can log in with correct credentials.
     */
    @Test
    public void testUserLogin_ValidCredentials() {
        String uniqueUsername = "validuser_" + System.currentTimeMillis();
        String password = "password123";

        dbManager.registerUser(uniqueUsername, password);

        boolean valid = dbManager.validateUser(uniqueUsername, password);
        assertTrue(valid, "Validation should succeed with correct credentials.");
        assertNotEquals(-1, dbManager.getCurrentUserId(), "A valid user ID should be set after login.");

        // Cleanup: remove the test user
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/snooker_db", "root", "");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users WHERE username = '" + uniqueUsername + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests that user login fails with incorrect credentials.
     */
    @Test
    public void testUserLogin_InvalidCredentials() {
        String username = "user_does_not_exist";
        String password = "wrong_password";

        boolean valid = dbManager.validateUser(username, password);
        assertFalse(valid, "Validation should fail for a non-existent user.");
    }
}

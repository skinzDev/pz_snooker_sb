package data;

import java.time.LocalDate;

/**
 * Match Data Model
 * <p>
 * This class represents the data model for a single snooker match record.
 * It is a Plain Old Java Object (POJO) used to encapsulate match details
 * such as player names, score, and date, primarily for display in the match history table.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class MatchData {
    private final int id;
    private final String player1;
    private final String player2;
    private final String score;
    private final LocalDate date;

    /**
     * Constructs a new MatchData object.
     *
     * @param id      The unique identifier for the match.
     * @param player1 The name of the first player.
     * @param player2 The name of the second player.
     * @param score   The final score of the match (e.g., "3-1").
     * @param date    The date the match was played.
     */
    public MatchData(int id, String player1, String player2, String score, LocalDate date) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.score = score;
        this.date = date;
    }

    /**
     * Gets the unique ID of the match.
     * @return The match ID.
     */
    public int getId() { return id; }

    /**
     * Gets the name of player 1.
     * @return The name of player 1.
     */
    public String getPlayer1() { return player1; }

    /**
     * Gets the name of player 2.
     * @return The name of player 2.
     */
    public String getPlayer2() { return player2; }

    /**
     * Gets the final score of the match.
     * @return The score as a string.
     */
    public String getScore() { return score; }

    /**
     * Gets the date the match was played.
     * @return The match date.
     */
    public LocalDate getDate() { return date; }
}

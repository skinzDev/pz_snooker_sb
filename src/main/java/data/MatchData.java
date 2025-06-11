package data;

import java.time.LocalDate;

/**
 * Model class (POJO) representing one row in the match history table.
 * The highest break is now stored in a separate table.
 */
public class MatchData {
    private final int id;
    private final String player1;
    private final String player2;
    private final String score;
    private final LocalDate date;

    public MatchData(int id, String player1, String player2, String score, LocalDate date) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.score = score;
        this.date = date;
    }

    // Getters are necessary for the PropertyValueFactory in TableView to work
    public int getId() { return id; }
    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getScore() { return score; }
    public LocalDate getDate() { return date; }
}

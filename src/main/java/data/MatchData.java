package data;

import java.time.LocalDate;

/**
 * Model klasa (POJO) koja predstavlja jedan red u tabeli sa istorijom mečeva.
 * Sadrži nepromenljive (final) podatke o jednom meču.
 */
public class MatchData {
    private final int id;
    private final String player1;
    private final String player2;
    private final String score;
    private final int highestBreak;
    private final LocalDate date;

    public MatchData(int id, String player1, String player2, String score, int highestBreak, LocalDate date) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.score = score;
        this.highestBreak = highestBreak;
        this.date = date;
    }

    // Getteri su neophodni da bi PropertyValueFactory u TableView-u radio
    public int getId() { return id; }
    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getScore() { return score; }
    public int getHighestBreak() { return highestBreak; }
    public LocalDate getDate() { return date; }
}
package logika;

/**
 * Implements the complete logic of a Snooker game.
 * This class manages the game's state, including scores for two players,
 * turn management, break tracking, and game progression from potting reds
 * and colors to the final endgame sequence. It also tracks the highest break
 * achieved in the match and which player holds it.
 *
 * @author Andrija Milovanovic
 * @version 1.1
 */
public class Snooker {
    private int crvenePreostale;
    private int poeni1 = 0, poeni2 = 0;
    private boolean igrac1NaRedu = true;
    private boolean trebaCrvena = true;

    private int currentBreak = 0;
    private int highestBreakInMatch = 0;
    private int playerWithHighestBreak = 0;

    private boolean endgame = false;
    private int nextColorValue = 2;
    private boolean gameOver = false;

    /**
     * Constructs a new Snooker game with a specified number of red balls.
     *
     * @param brojCrvenih The number of red balls to start the game with.
     */
    public Snooker(int brojCrvenih) {
        this.crvenePreostale = brojCrvenih;
    }

    /**
     * Adds points to the current player's score and updates the current break.
     * If the current break exceeds the match's highest break, it updates that as well.
     *
     * @param poeni The point value of the pocketed ball.
     */
    private void dodajPoene(int poeni) {
        if (igrac1NaRedu) {
            poeni1 += poeni;
        } else {
            poeni2 += poeni;
        }
        currentBreak += poeni;
        if (currentBreak > highestBreakInMatch) {
            highestBreakInMatch = currentBreak;
            playerWithHighestBreak = igrac1NaRedu ? 1 : 2;
        }
    }

    /**
     * Handles a miss or foul, ending the current player's turn.
     * This method resets the current break to zero, switches the active player,
     * and sets the next required ball to red if not in the endgame phase.
     */
    public void promasaj() {
        currentBreak = 0;
        igrac1NaRedu = !igrac1NaRedu;
        if (!endgame) {
            trebaCrvena = true;
        }
    }

    /**
     * Processes a player's move by evaluating the pocketed ball.
     * This is the main logic method for game progression.
     *
     * @param boja The point value of the ball that was clicked/pocketed.
     * @return {@code true} if the move was valid and successful, {@code false} otherwise.
     */
    public boolean klikNaBoju(int boja) {
        if (gameOver) return false;

        if (endgame) {
            return handleEndgame(boja);
        }

        if (trebaCrvena) {
            if (boja == 1) {
                dodajPoene(1);
                crvenePreostale--;
                trebaCrvena = false;
                if (crvenePreostale == 0) {
                    endgame = true;
                }
                return true;
            }
        } else { // Must be a color
            if (boja > 1) {
                dodajPoene(boja);
                trebaCrvena = true;
                return true;
            }
        }

        promasaj();
        return false;
    }

    /**
     * Handles a foul where 4 points are awarded to the opponent.
     * This action adds 4 points to the non-active player's score and then
     * switches the turn, equivalent to a miss. This would be triggered
     * by a "Foul +4" button.
     */
    public void foulPlusFour() {
        if (gameOver) {
            return;
        }
        if (igrac1NaRedu) {
            poeni2 += 4;
        } else {
            poeni1 += 4;
        }
        promasaj();
    }

    /**
     * Handles the logic for the endgame phase, where colored balls must be
     * potted in ascending order of their value (from yellow to black).
     *
     * @param boja The value of the pocketed colored ball.
     * @return {@code true} if the correct colored ball was potted, {@code false} otherwise.
     */
    private boolean handleEndgame(int boja) {
        if (boja == nextColorValue) {
            dodajPoene(boja);
            if (boja == 7) {
                gameOver = true;
            } else {
                nextColorValue++;
            }
            return true;
        } else {
            promasaj();
            return false;
        }
    }

    /**
     * @return The score of player 1.
     */
    public int getPoeni1() { return poeni1; }

    /**
     * @return The score of player 2.
     */
    public int getPoeni2() { return poeni2; }

    /**
     * @return {@code true} if it is player 1's turn, {@code false} otherwise.
     */
    public boolean jeIgrac1NaRedu() { return igrac1NaRedu; }

    /**
     * @return The number of remaining red balls on the table.
     */
    public int getCrvenePreostale() { return crvenePreostale; }

    /**
     * @return {@code true} if the next required ball is a red, {@code false} otherwise.
     */
    public boolean daLiTrebaCrvena() { return trebaCrvena; }

    /**
     * @return {@code true} if the game is over, {@code false} otherwise.
     */
    public boolean isGameOver() { return gameOver; }

    /**
     * @return The highest break score achieved in the current match.
     */
    public int getHighestBreakInMatch() { return highestBreakInMatch; }

    /**
     * @return The player number (1 or 2) who achieved the highest break.
     */
    public int getPlayerWithHighestBreak() { return playerWithHighestBreak; }

    /**
     * @return The current break score for the active player.
     */
    public int getCurrentBreak() { return currentBreak; }

    /**
     * @return The value of the next colored ball to be potted in the endgame.
     */
    public int getNextColorValue() { return nextColorValue; }

    /**
     * @return {@code true} if the game is in the endgame phase, {@code false} otherwise.
     */
    public boolean isEndgame() { return endgame; }
}
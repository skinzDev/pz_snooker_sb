package logika;

/**
 * Implements the complete logic of the Snooker game.
 * Manages game state, points, turn order, breaks, and game end.
 * Now tracks which player made the highest break.
 */
public class Snooker {
    private int crvenePreostale;
    private int poeni1 = 0, poeni2 = 0;
    private boolean igrac1NaRedu = true;
    private boolean trebaCrvena = true;

    // State for breaks and game end
    private int currentBreak = 0;
    private int highestBreakInMatch = 0;
    private int playerWithHighestBreak = 0; // 1 for player 1, 2 for player 2

    private boolean endgame = false; // Phase for clearing colored balls
    private int nextColorValue = 2; // Starts with yellow (2)
    private boolean gameOver = false;

    public Snooker(int brojCrvenih) {
        this.crvenePreostale = brojCrvenih;
    }

    /**
     * Adds points to the current player and updates the break.
     * @param poeni Value of the pocketed ball
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
     * Handles a miss or end of turn.
     * Resets the break and changes the player.
     */
    public void promasaj() {
        currentBreak = 0;
        igrac1NaRedu = !igrac1NaRedu;
        if (!endgame) {
            trebaCrvena = true;
        }
    }

    /**
     * Main method for processing a move.
     * @param boja Value of the clicked ball
     * @return true if the move was successful, false otherwise
     */
    public boolean klikNaBoju(int boja) {
        if (gameOver) return false;

        if (endgame) {
            return handleEndgame(boja);
        }

        if (trebaCrvena) {
            if (boja == 1) { // Successfully pocketed a red
                dodajPoene(1);
                crvenePreostale--;
                trebaCrvena = false; // Next must be a color
                if (crvenePreostale == 0) {
                    endgame = true; // Enter endgame phase
                }
                return true;
            }
        } else { // Must be a color
            if (boja > 1) {
                dodajPoene(boja);
                trebaCrvena = true; // Next must be a red
                return true;
            }
        }

        promasaj();
        return false;
    }

    /**
     * Logic for the endgame phase (clearing colors).
     */
    private boolean handleEndgame(int boja) {
        if (boja == nextColorValue) {
            dodajPoene(boja);
            if (boja == 7) { // Last ball, black, is pocketed
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

    // --- Getters for UI ---
    public int getPoeni1() { return poeni1; }
    public int getPoeni2() { return poeni2; }
    public boolean jeIgrac1NaRedu() { return igrac1NaRedu; }
    public int getCrvenePreostale() { return crvenePreostale; }
    public boolean daLiTrebaCrvena() { return trebaCrvena; }
    public boolean isGameOver() { return gameOver; }
    public int getHighestBreakInMatch() { return highestBreakInMatch; }
    public int getPlayerWithHighestBreak() { return playerWithHighestBreak; }
    public int getCurrentBreak() { return currentBreak; }
    public int getNextColorValue() { return nextColorValue; }
    public boolean isEndgame() { return endgame; }
}

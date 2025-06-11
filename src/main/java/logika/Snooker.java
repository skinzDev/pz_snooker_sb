package logika;

/**
 * Klasa koja implementira kompletnu logiku Snooker igre.
 * Upravlja stanjem igre, poenima, redosledom poteza, brejkovima i krajem igre.
 */
public class Snooker {
    private int crvenePreostale;
    private int poeni1 = 0, poeni2 = 0;
    private boolean igrac1NaRedu = true;
    private boolean trebaCrvena = true;

    // Stanja za brejk i kraj igre
    private int currentBreak = 0;
    private int highestBreakInMatch = 0;
    private boolean endgame = false; // Faza kada se čiste obojene kugle
    private int nextColorValue = 2; // Počinje se sa žutom (2)
    private boolean gameOver = false;

    public Snooker(int brojCrvenih) {
        this.crvenePreostale = brojCrvenih;
    }

    /**
     * Dodaje poene igraču koji je na potezu i ažurira brejk.
     * @param poeni Vrednost ubačene kugle
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
        }
    }

    /**
     * Obrađuje promašaj ili kraj poteza.
     * Resetuje brejk i menja igrača.
     */
    public void promasaj() {
        currentBreak = 0;
        igrac1NaRedu = !igrac1NaRedu;
        // Ako nismo u endgame fazi, sledeća je uvek crvena
        if (!endgame) {
            trebaCrvena = true;
        }
    }

    /**
     * Glavna metoda za obradu poteza.
     * @param boja Vrednost kliknute kugle
     * @return true ako je potez uspešan, false inače
     */
    public boolean klikNaBoju(int boja) {
        if (gameOver) return false;

        // Faza 2: Nema više crvenih, čiste se obojene po redu
        if (endgame) {
            return handleEndgame(boja);
        }

        // Faza 1: Igra sa crvenim kuglama
        if (trebaCrvena) {
            if (boja == 1) { // Uspešno ubačena crvena
                dodajPoene(1);
                crvenePreostale--;
                trebaCrvena = false; // Sledeća mora biti obojena
                if (crvenePreostale == 0) {
                    endgame = true; // Ulazak u endgame fazu
                }
                return true;
            }
        } else { // Mora biti obojena
            if (boja > 1) {
                dodajPoene(boja);
                trebaCrvena = true; // Sledeća mora biti crvena
                return true;
            }
        }

        // Svaki neispravan klik je promašaj
        promasaj();
        return false;
    }

    /**
     * Logika za endgame fazu (čišćenje obojenih).
     * @param boja Vrednost kliknute kugle
     * @return true ako je potez uspešan
     */
    private boolean handleEndgame(int boja) {
        if (boja == nextColorValue) {
            dodajPoene(boja);
            if (boja == 7) { // Ubačena poslednja, crna kugla
                gameOver = true;
            } else {
                nextColorValue++;
            }
            return true;
        } else {
            // Promašaj ako je ubačena pogrešna obojena
            promasaj();
            return false;
        }
    }

    // --- Getteri za UI ---
    public int getPoeni1() { return poeni1; }
    public int getPoeni2() { return poeni2; }
    public boolean jeIgrac1NaRedu() { return igrac1NaRedu; }
    public int getCrvenePreostale() { return crvenePreostale; }
    public boolean daLiTrebaCrvena() { return trebaCrvena; }
    public boolean isGameOver() { return gameOver; }
    public int getHighestBreakInMatch() { return highestBreakInMatch; }
    public int getCurrentBreak() { return currentBreak; }
    public int getNextColorValue() { return nextColorValue; }
    public boolean isEndgame() { return endgame; }
}
import java.util.ArrayList;
import java.util.Collections; // I could also use TriSimples but we can't import it

/**
 * Grundy game with AI for the machine.
 * - It implements implements optimizations by storing both losing and winning positions.
 * Version 2
 * @version 2.0
 * @since 2025/16/01
 * @author ... and ...
 */
class GrundyRecPerdEtGagn {

    /** The reset color for the console. */
    public static final String RESET = "\u001B[0m";

    /** The green color for the console. */
    public static final String GREEN = "\u001B[32m";

    /** The red color for the console. */
    public static final String RED = "\u001B[31m";

    /** The blue color for the console. */
    public static final String BLUE = "\u001B[34m";

    /** Operation counter. */
    long cpt = 0;

    /**
     * ArrayList to store losing positions.
     * Each element is an ArrayList representing a losing configuration.
     */
    ArrayList<ArrayList<Integer>> posPerdantes = new ArrayList<ArrayList<Integer>>();

    /**
     * ArrayList to store winning positions.
     * Each element is an ArrayList representing a winning configuration.
     */
    ArrayList<ArrayList<Integer>> posGagnantes = new ArrayList<ArrayList<Integer>>();

    /**
     * Main method, entry point of the program.
     */
    void principal() {
        // Launch the game
        //leJeu();
        // Launch the tests
        launchTests();
    }

    /**
     * Launches the tests.
     */
    void launchTests() {
        testEstGagnanteEfficacite();
        testListerCoupsPossibles();
        testPremier();
        testSuivant();
        testEstConnuePerdante();
        testEstConnueGagnante();
    }

    /**
     * Main game loop allowing human vs AI play.
     */
    void leJeu() {
        // Initialize game
        ArrayList<Integer> jeu = demandeNombreAllumettes();
        String nomJoueur = "Joueur";
        String nomMachine = "IA";
        String[] nomsJoueurs = {nomJoueur, nomMachine};
        int indexJoueur = 0; // Player starts

        // Game loop
        boolean finPartie = false;
        while (!finPartie) {
            System.out.println("\nEtat du jeu : ");
            afficherJeu(jeu); // Improved display of the game

            if (indexJoueur == 0) { // Human player's turn
                
                System.out.println("Tour de " + nomsJoueurs[indexJoueur]);

                // Show available moves
                ArrayList<int[]> coupsPossibles = listerCoupsPossibles(jeu);

                int choix = -1;

                System.out.println("Coups possibles :");
                for (int i = 0; i < coupsPossibles.size(); i++) {
                    System.out.println((i + 1) + ": Tas " + coupsPossibles.get(i)[0] + " -> " + coupsPossibles.get(i)[1] + " et " + (jeu.get(coupsPossibles.get(i)[0]) - coupsPossibles.get(i)[1]));
                }

                // Get valid move from player
                do {
                    System.out.print("Choisir un coup (1-" + coupsPossibles.size() + ") : ");
                    choix = SimpleInput.getInt("") - 1;
                } while (choix < 0 || choix >= coupsPossibles.size());

                int tasADiviser = coupsPossibles.get(choix)[0];
                int nbAllumettes = coupsPossibles.get(choix)[1];

                
                enlever(jeu, tasADiviser, nbAllumettes);

            } else { 
                System.out.println("Tour de " + nomsJoueurs[indexJoueur]);
                if (!jouerGagnant(jeu)) {
                    // If no winning move, play random valid move
                    ArrayList<int[]> coupsPossibles = listerCoupsPossibles(jeu);
                    int choix = (int) (Math.random() * coupsPossibles.size());

                    int tasADiviser = coupsPossibles.get(choix)[0];
                    int nbAllumettes = coupsPossibles.get(choix)[1];

                    System.out.println("IA choisit : Tas " + tasADiviser + " -> " + nbAllumettes + " et " + (jeu.get(tasADiviser) - nbAllumettes));
                    enlever(jeu, tasADiviser, nbAllumettes);
                }
            }

            // Check if game is over
            if (!estPossible(jeu)) {
                finPartie = true;
                System.out.println("\nEtat final : ");
                afficherJeu(jeu);
                System.out.println(nomsJoueurs[indexJoueur] + " a gagnee !");
            }

            // Switch player
            indexJoueur = (indexJoueur + 1) % 2;
        }
    }

    /**
     * Lists all possible moves for a given game state, avoiding duplicates.
     *
     * @param jeu The current game state.
     * @return An ArrayList of int[], where each int[] represents a move: {heap index, number of matches to remove}.
     */
    ArrayList<int[]> listerCoupsPossibles(ArrayList<Integer> jeu) {
        ArrayList<int[]> coups = new ArrayList<>();
        for (int i = 0; i < jeu.size(); i++) {
            if (jeu.get(i) > 2) {
                for (int j = 1; j < jeu.get(i) / 2 + 1; j++) { // Iterate only up to half to avoid duplicates
                    if (2 * j != jeu.get(i)) {
                        coups.add(new int[] { i, j });
                    }
                }
            }
        }
        return coups;
    }

    /**
     * Test the efficiency of estGagnante method.
     */
    void testEstGagnanteEfficacite() {
        System.out.println(GREEN + "\n*** Test d'efficacite de estGagnante (Version 2) ***" + RESET);
        System.out.println("n\tNombre d'operations\tTemps (ms)");

        for (int n = 3; n <= 45; n++) {
            ArrayList<Integer> jeu = new ArrayList<Integer>();
            jeu.add(n);

            posPerdantes.clear(); // Reset losing and winning positions for each test
            posGagnantes.clear();

            long startTime = System.currentTimeMillis();
            cpt = 0;

            // Appel de estGagnante et comptage des operations
            estGagnante(jeu);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println(n + "\t" + cpt + "\t\t" + duration);
        }
    }

    /**
     * Prints the different matches arrays.
     *
     * @param tab game board.
     */
    void afficherJeu(ArrayList<Integer> tab) {
        for (int i = 0; i < tab.size(); i++) {
            System.out.print("Tas " + i + " : ");
            for (int j = 0; j < tab.get(i); j++) {
                System.out.print("|");
            }
            System.out.println(" (" + tab.get(i) + ")");
        }
    }

    /**
     * Asks the player for the starting number of matches as long as the number of matches entered is incorrect.
     *
     * @return an array of integers with index 0 initialized.
     */
    ArrayList<Integer> demandeNombreAllumettes() {
        int initialisation = SimpleInput.getInt("Nombre d'allumettes (>= 3) : ");

        while (initialisation <= 2) {
            initialisation = SimpleInput.getInt("<!> Nombre d'allumettes (>= 3): ");
        }
        ArrayList<Integer> ret = new ArrayList<Integer>();
        ret.add(0, initialisation);
        return ret;
    }

    /**
     * Play the winning move if it exists.
     *
     * @param jeu game board.
     * @return true if there is a winning move, false otherwise.
     */
    boolean jouerGagnant(ArrayList<Integer> jeu) {
        boolean gagnant = false;
        boolean erreur = false;

        if (jeu == null) {
            System.err.println("jouerGagnant(): le parametre jeu est null");
            erreur = true;
        } else {
            ArrayList<Integer> essai = new ArrayList<Integer>();
            int ligne = premier(jeu, essai);

            while (ligne != -1 && !gagnant) {
                if (estPerdante(essai)) {
                    jeu.clear();
                    gagnant = true;
                    for (int i = 0; i < essai.size(); i++) {
                        jeu.add(essai.get(i));
                    }
                } else {
                    ligne = suivant(jeu, essai, ligne);
                }
            }
        }

        return gagnant && !erreur;
    }

    /**
     * RECURSIVE method which indicates whether the configuration (of the current game or trial game) is losing.
     * This method is used by the machine to know if the opponent can lose (at 100%).
     *
     * @param jeu current game board (the state of the game at a certain point in the game).
     * @return true if (game) configuration is losing, false otherwise.
     */
    boolean estPerdante(ArrayList<Integer> jeu) {
        boolean ret = true;
        boolean erreur = false;

        if (jeu == null) {
            System.err.println("estPerdante(): le parametre jeu est null");
            erreur = true;
        } else {
            // Check if the current game state is a known losing or winning position
            if (estConnuePerdante(jeu)) {
                ret = true;
            } else if (estConnueGagnante(jeu)) {
                ret = false;
            } else if (!estPossible(jeu)) {
                ret = true;
            } else {
                ArrayList<Integer> essai = new ArrayList<Integer>();
                int ligne = premier(jeu, essai);

                while ((ligne != -1) && ret) {
                    cpt++;
                    if (estPerdante(essai)) {
                        ret = false;
                    } else {
                        ligne = suivant(jeu, essai, ligne);
                    }
                }

                // If the position is losing, add it to the list of known losing positions
                // Otherwise, add it to the list of known winning positions
                ArrayList<Integer> jeuNormalise = normaliser(jeu);
                if (ret) {
                    if (!estConnuePerdante(jeuNormalise)) {
                        posPerdantes.add(jeuNormalise);
                    }
                } else {
                    if (!estConnueGagnante(jeuNormalise)) {
                        posGagnantes.add(jeuNormalise);
                    }
                }
            }
        }

        return ret && !erreur;
    }

    /**
     * Indicates whether the configuration is a winner.
     * Methode that simply calls 'isLosing'.
     *
     * @param jeu game board.
     * @return true if the configuration is a winner, false otherwise.
     */
    boolean estGagnante(ArrayList<Integer> jeu) {
        boolean ret = false;
        if (jeu == null) {
            System.err.println("estGagnante(): le parametre jeu est null");
        } else {
            ret = !estPerdante(jeu);
        }
        return ret;
    }

    /**
     * Normalizes a game state by removing heaps of size 1 and 2 and sorting the remaining heaps.
     *
     * @param jeu The game state to normalize.
     * @return The normalized game state.
     */
    ArrayList<Integer> normaliser(ArrayList<Integer> jeu) {
        ArrayList<Integer> jeuNormalise = new ArrayList<Integer>();
        for (int tas : jeu) {
            if (tas > 2) {
                jeuNormalise.add(tas);
            }
        }
        Collections.sort(jeuNormalise);
        return jeuNormalise;
    }

    /**
     * Checks if a game state is a known losing position.
     *
     * @param jeu The game state to check.
     * @return true if the game state is a known losing position, false otherwise.
     */
    boolean estConnuePerdante(ArrayList<Integer> jeu) {
        boolean ret = false;
        boolean erreur = false;

        if (jeu == null) {
            System.err.println("estConnuePerdante(): le parametre jeu est null");
            erreur = true;
        } else {
            ArrayList<Integer> jeuNormalise = normaliser(jeu);
            int i = 0;
            while (i < posPerdantes.size() && !ret) {
                if (posPerdantes.get(i).equals(jeuNormalise)) {
                    ret = true;
                }
                i++;
            }
        }

        return ret && !erreur;
    }

    /**
     * Checks if a game state is a known winning position.
     *
     * @param jeu The game state to check.
     * @return true if the game state is a known winning position, false otherwise.
     */
    boolean estConnueGagnante(ArrayList<Integer> jeu) {
        boolean ret = false;
        boolean erreur = false;

        if (jeu == null) {
            System.err.println("estConnueGagnante(): le parametre jeu est null");
            erreur = true;
        } else {
            ArrayList<Integer> jeuNormalise = normaliser(jeu);
            int i = 0;
            while (i < posGagnantes.size() && !ret) {
                if (posGagnantes.get(i).equals(jeuNormalise)) {
                    ret = true;
                }
                i++;
            }
        }

        return ret && !erreur;
    }

    /**
     * Brief tests of the playerWinner() method.
     */
    void testJouerGagnant() {
        System.out.println();
        System.out.println(BLUE + "*** testJouerGagnant() ***" + RESET);

        System.out.println("Test des cas normaux");
        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(6);
        ArrayList<Integer> resJeu1 = new ArrayList<Integer>();
        resJeu1.add(4);
        resJeu1.add(2);

        testCasJouerGagnant(jeu1, resJeu1, true);

    }

    /**
     * Test a case of the playWinner() method.
     *
     * @param jeu   the game board.
     * @param resJeu the game board after playWinner.
     * @param res    the result expected by playWinner.
     */
    void testCasJouerGagnant(ArrayList<Integer> jeu, ArrayList<Integer> resJeu, boolean res) {
        // Arrange
        System.out.print("jouerGagnant (" + jeu.toString() + ") : ");

        
        boolean resExec = jouerGagnant(jeu);

        
        System.out.print(jeu.toString() + " " + resExec + " : ");
        boolean egaliteJeux = jeu.equals(resJeu);
        if (egaliteJeux && (res == resExec)) {
            System.out.println(GREEN + "OK" + RESET);
        } else {
            System.err.println(RED + "ERREUR" + RESET);
        }
    }

    /**
     * Divide the matches in a line into two piles (1 line = 1 pile).
     * The new heap is necessarily placed at the end of the board.
     * The heap that is divided decreases by the number of matches removed.
     *
     * @param jeu   array of matches per line.
     * @param ligne heap for which matches must be separated.
     * @param nb    number of matches REMOVED from the heap (ligne) during separation.
     */
    void enlever(ArrayList<Integer> jeu, int ligne, int nb) {
        // traitement des erreurs
        if (jeu == null) {
            System.err.println("enlever() : le parametre jeu est null");
        } else if (ligne >= jeu.size()) {
            System.err.println("enlever() : le numero de ligne est trop grand");
        } else if (nb >= jeu.get(ligne)) {
            System.err.println("enlever() : le nb d'allumettes a retirer est trop grand");
        } else if (nb <= 0) {
            System.err.println("enlever() : le nb d'allumettes a retirer est trop petit");
        } else if (2 * nb == jeu.get(ligne)) {
            System.err.println("enlever() : le nb d'allumettes a retirer est la moitie");
        } else {
            // nouveau tas ajoute au jeu (necessairement en fin de tableau)
            // ce nouveau tas contient le nbre d'allumettes retirees (nb) du tas a separer
            jeu.add(nb);
            // le tas restant possede "nb" allumettes en moins
            jeu.set(ligne, (jeu.get(ligne) - nb));
        }
    }

    /**
     * Tests whether it is possible to separate one of the heaps.
     *
     * @param jeu game board.
     * @return true if there is at least one heap of 3 or more matches, false otherwise.
     */
    boolean estPossible(ArrayList<Integer> jeu) {
        boolean ret = false;
        if (jeu == null) {
            System.err.println("estPossible(): le parametre jeu est null");
        } else {
            int i = 0;
            while (i < jeu.size() && !ret) {
                if (jeu.get(i) > 2) {
                    ret = true;
                }
                i = i + 1;
            }
        }
        return ret;
    }

    /**
     * Create a very first test configuration from the game.
     *
     * @param jeu      game board.
     * @param jeuEssai new game configuration.
     * @return the number of the heap divided in two or (-1) if there is no heap of at least 3 matches.
     */
    int premier(ArrayList<Integer> jeu, ArrayList<Integer> jeuEssai) {
        int numTas = -1;
        boolean erreur = false;

        if (jeu == null) {
            System.err.println("premier(): le parametre jeu est null");
            erreur = true;
        } else if (!estPossible((jeu))) {
            System.err.println("premier(): aucun tas n'est divisible");
            erreur = true;
        } else if (jeuEssai == null) {
            System.err.println("premier(): le parametre jeuEssai est null");
            erreur = true;
        } else {
            jeuEssai.clear();
            for (int val : jeu) {
                jeuEssai.add(val);
            }

            boolean trouve = false;
            int i = 0;
            while (i < jeu.size() && !trouve) {
                if (jeuEssai.get(i) >= 3) {
                    trouve = true;
                    numTas = i;
                }
                i++;
            }

            if (numTas != -1) {
                enlever(jeuEssai, numTas, 1);
            }
        }

        return erreur ? -1 : numTas;
    }

    /**
     * Brief tests of the premier() method.
     */
    void testPremier() {
        System.out.println();
        System.out.println(BLUE + "*** testPremier()" + RESET);

        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(10);
        jeu1.add(11);
        int ligne1 = 0;
        ArrayList<Integer> res1 = new ArrayList<Integer>();
        res1.add(9);
        res1.add(11);
        res1.add(1);
        testCasPremier(jeu1, ligne1, res1);
    }

    /**
     * Test a case of the testPremier method.
     *
     * @param jeu    the game board.
     * @param ligne  the number of the heap separated first.
     * @param res    the game board after a first separation.
     */
    void testCasPremier(ArrayList<Integer> jeu, int ligne, ArrayList<Integer> res) {
        // Arrange
        System.out.print("premier (" + jeu.toString() + ") : ");
        ArrayList<Integer> jeuEssai = new ArrayList<Integer>();
        
        int noLigne = premier(jeu, jeuEssai);
        
        System.out.println("\nnoLigne = " + noLigne + " jeuEssai = " + jeuEssai.toString());
        boolean egaliteJeux = jeuEssai.equals(res);
        if (egaliteJeux && noLigne == ligne) {
            System.out.println(GREEN + "OK\n" + RESET);
        } else {
            System.err.println(GREEN + "ERREUR\n" + RESET);
        }
    }

    /**
     * Generates the following test setup (i.e. ONE possible decomposition).
     *
     * @param jeu      game board.
     * @param jeuEssai play test configuration after separation.
     * @param ligne    the number of the heap which is the last to have been separated.
     * @return the number of the heap divided in two for the new configuration, -1 if no more decomposition is possible.
     */
    int suivant(ArrayList<Integer> jeu, ArrayList<Integer> jeuEssai, int ligne) {
        int numTas = -1;
        boolean erreur = false;

        if (jeu == null) {
            System.err.println("suivant(): le parametre jeu est null");
            erreur = true;
        } else if (jeuEssai == null) {
            System.err.println("suivant() : le parametre jeuEssai est null");
            erreur = true;
        } else if (ligne >= jeu.size()) {
            System.err.println("suivant(): le parametre ligne est trop grand");
            erreur = true;
        } else {
            int nbAllumEnLigne = jeuEssai.get(ligne);
            int nbAllDernCase = jeuEssai.get(jeuEssai.size() - 1);

            if ((nbAllumEnLigne - nbAllDernCase) > 2) {
                jeuEssai.set(ligne, (nbAllumEnLigne - 1));
                jeuEssai.set(jeuEssai.size() - 1, (nbAllDernCase + 1));
                numTas = ligne;
            } else {
                jeuEssai.clear();
                for (int i = 0; i < jeu.size(); i++) {
                    jeuEssai.add(jeu.get(i));
                }

                boolean separation = false;
                int i = ligne + 1;
                while (i < jeuEssai.size() && !separation) {
                    if (jeu.get(i) > 2) {
                        separation = true;
                        enlever(jeuEssai, i, 1);
                        numTas = i;
                    } else {
                        i++;
                    }
                }
            }
        }

        return erreur ? -1 : numTas;
    }

    /**
     * Tests of the listerCoupsPossibles() method.
     */
    void testListerCoupsPossibles() {
        System.out.println();
        System.out.println(BLUE + "*** testListerCoupsPossibles()" + RESET);

        // Cas 1: Un seul tas de 3 allumettes
        ArrayList<Integer> jeu1 = new ArrayList<>();
        jeu1.add(3);
        ArrayList<int[]> res1 = new ArrayList<>();
        res1.add(new int[] { 0, 1 });
        testCasListerCoupsPossibles(jeu1, res1);

        // Cas 2: Un seul tas de 4 allumettes
        ArrayList<Integer> jeu2 = new ArrayList<>();
        jeu2.add(4);
        ArrayList<int[]> res2 = new ArrayList<>();
        res2.add(new int[] { 0, 1 });
        testCasListerCoupsPossibles(jeu2, res2);

        // Cas 3: Un seul tas de 5 allumettes
        ArrayList<Integer> jeu3 = new ArrayList<>();
        jeu3.add(5);
        ArrayList<int[]> res3 = new ArrayList<>();
        res3.add(new int[] { 0, 1 });
        res3.add(new int[] { 0, 2 });
        testCasListerCoupsPossibles(jeu3, res3);

        // Cas 4: Deux tas, un de 3 et un de 4 allumettes
        ArrayList<Integer> jeu4 = new ArrayList<>();
        jeu4.add(3);
        jeu4.add(4);
        ArrayList<int[]> res4 = new ArrayList<>();
        res4.add(new int[] { 0, 1 });
        res4.add(new int[] { 1, 1 });
        testCasListerCoupsPossibles(jeu4, res4);

        // Cas 5: Un tas de 10 allumettes
        ArrayList<Integer> jeu5 = new ArrayList<>();
        jeu5.add(10);
        ArrayList<int[]> res5 = new ArrayList<>();
        res5.add(new int[] { 0, 1 });
        res5.add(new int[] { 0, 2 });
        res5.add(new int[] { 0, 3 });
        res5.add(new int[] { 0, 4 });
        testCasListerCoupsPossibles(jeu5, res5);

        // Cas 6: Aucun tas divisible
        ArrayList<Integer> jeu6 = new ArrayList<>();
        jeu6.add(1);
        jeu6.add(2);
        ArrayList<int[]> res6 = new ArrayList<>();
        testCasListerCoupsPossibles(jeu6, res6);
    }

    
    /**
     * Tests a case of the listerCoupsPossibles() method.
     *
     * @param jeu The game board.
     * @param res The expected list of possible moves.
     */
    void testCasListerCoupsPossibles(ArrayList<Integer> jeu, ArrayList<int[]> res) {
        // Arrange
        System.out.print("listerCoupsPossibles(" + jeu.toString() + ") : ");
    
        ArrayList<int[]> resExec = listerCoupsPossibles(jeu);
    
        System.out.print("[");
        for (int i = 0; i < resExec.size(); i++) {
            System.out.print("[");
            for (int j = 0; j < resExec.get(i).length; j++) {
                System.out.print(resExec.get(i)[j]);
                if (j < resExec.get(i).length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print("]");
            if (i < resExec.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("] : ");
    
        boolean resultatTest = false;
    
        // Check if the two lists have the same size and the same elements
        if (resExec.size() != res.size()) {
            resultatTest = false;
        } else {
            resultatTest = true;
            int i = 0;
            while (i < resExec.size() && resultatTest) {
                if (resExec.get(i).length != res.get(i).length) {
                    resultatTest = false;
                } else {
                    int j = 0;
                    while (j < resExec.get(i).length && resultatTest) {
                        if (resExec.get(i)[j] != res.get(i)[j]) {
                            resultatTest = false;
                        }
                        j++;
                    }
                }
                i++;
            }
        }
    
        if (resultatTest) {
            System.out.println(GREEN + "OK" + RESET);
        } else {
            System.err.println(RED + "ERREUR" + RESET);
        }
    }

    /**
     * Brief tests of the following() method.
     */
    void testSuivant() {
        System.out.println();
        System.out.println(BLUE + "*** testSuivant()" + RESET);

        int ligne1 = 0;
        int resLigne1 = 0;
        ArrayList<Integer> jeu1 = new ArrayList<Integer>();
        jeu1.add(10);
        ArrayList<Integer> jeuEssai1 = new ArrayList<Integer>();
        jeuEssai1.add(9);
        jeuEssai1.add(1);
        ArrayList<Integer> res1 = new ArrayList<Integer>();
        res1.add(8);
        res1.add(2);
        testCasSuivant(jeu1, jeuEssai1, ligne1, res1, resLigne1);

        int ligne2 = 0;
        int resLigne2 = -1;
        ArrayList<Integer> jeu2 = new ArrayList<Integer>();
        jeu2.add(10);
        ArrayList<Integer> jeuEssai2 = new ArrayList<Integer>();
        jeuEssai2.add(6);
        jeuEssai2.add(4);
        ArrayList<Integer> res2 = new ArrayList<Integer>();
        res2.add(10);
        testCasSuivant(jeu2, jeuEssai2, ligne2, res2, resLigne2);

        int ligne3 = 1;
        int resLigne3 = 1;
        ArrayList<Integer> jeu3 = new ArrayList<Integer>();
        jeu3.add(4);
        jeu3.add(6);
        jeu3.add(3);
        ArrayList<Integer> jeuEssai3 = new ArrayList<Integer>();
        jeuEssai3.add(4);
        jeuEssai3.add(5);
        jeuEssai3.add(3);
        jeuEssai3.add(1);
        ArrayList<Integer> res3 = new ArrayList<Integer>();
        res3.add(4);
        res3.add(4);
        res3.add(3);
        res3.add(2);
        testCasSuivant(jeu3, jeuEssai3, ligne3, res3, resLigne3);
    }

    /**
     * Test a case of the following method.
     *
     * @param jeu      the game board.
     * @param jeuEssai the game board obtained after separating a heap.
     * @param ligne    the number of the heap that was last separated.
     * @param resJeu   is the TestGame expected after separation.
     * @param resLigne is the expected number of the heap that is separated.
     */
    void testCasSuivant(ArrayList<Integer> jeu, ArrayList<Integer> jeuEssai, int ligne, ArrayList<Integer> resJeu, int resLigne) {
        // Arrange
        System.out.print("suivant (" + jeu.toString() + ", " + jeuEssai.toString() + ", " + ligne + ") : ");
        
        int noLigne = suivant(jeu, jeuEssai, ligne);
        
        System.out.println("\nnoLigne = " + noLigne + " jeuEssai = " + jeuEssai.toString());
        boolean egaliteJeux = jeuEssai.equals(resJeu);
        if (egaliteJeux && noLigne == resLigne) {
            System.out.println(GREEN + "OK\n" + RESET);
        } else {
            System.err.println(GREEN + "ERREUR\n" + RESET);
        }
    }

    /**
     * Tests of the estConnuePerdante() method.
     */
    void testEstConnuePerdante() {
        System.out.println();
        System.out.println(BLUE + "*** testEstConnuePerdante()" + RESET);

        // Cas 1: Losing position already known
        ArrayList<Integer> jeu1 = new ArrayList<>();
        jeu1.add(3);
        jeu1.add(4);
        ArrayList<Integer> jeu1Normalise = normaliser(jeu1);
        posPerdantes.add(jeu1Normalise);
        testCasEstConnuePerdante(jeu1, true);

        // Cas 2: Losing position not known
        ArrayList<Integer> jeu2 = new ArrayList<>();
        jeu2.add(5);
        jeu2.add(6);
        testCasEstConnuePerdante(jeu2, false);

        // Cas 3: Empty game state
        ArrayList<Integer> jeu3 = new ArrayList<>();
        testCasEstConnuePerdante(jeu3, false);

        // Cas 4: Game state with heaps of size 1 and 2 only
        ArrayList<Integer> jeu4 = new ArrayList<>();
        jeu4.add(1);
        jeu4.add(2);
        testCasEstConnuePerdante(jeu4, false);
    }

    /**
     * Tests a case of the estConnuePerdante() method.
     *
     * @param jeu The game state.
     * @param res The expected result.
     */
    void testCasEstConnuePerdante(ArrayList<Integer> jeu, boolean res) {
        // Arrange
        System.out.print("estConnuePerdante(" + jeu.toString() + ") : ");

        
        boolean resExec = estConnuePerdante(jeu);

        
        System.out.print(resExec + " : ");
        if (resExec == res) {
            System.out.println(GREEN + "OK" + RESET);
        } else {
            System.err.println(RED + "ERREUR" + RESET);
        }
    }

    /**
     * Tests of the estConnueGagnante() method.
     */
    void testEstConnueGagnante() {
        System.out.println();
        System.out.println(BLUE + "*** testEstConnueGagnante()" + RESET);

        posGagnantes.clear(); // Reset winning positions for each test

        // Cas 1: Winning position already known
        ArrayList<Integer> jeu1 = new ArrayList<>();
        jeu1.add(3);
        jeu1.add(5);
        ArrayList<Integer> jeu1Normalise = normaliser(jeu1);
        posGagnantes.add(jeu1Normalise);
        testCasEstConnueGagnante(jeu1, true);

        // Cas 2
        ArrayList<Integer> jeu2 = new ArrayList<>();
        jeu2.add(4);
        jeu2.add(6);
        testCasEstConnueGagnante(jeu2, false);

        // Cas 3: Empty game state
        ArrayList<Integer> jeu3 = new ArrayList<>();
        testCasEstConnueGagnante(jeu3, false);

        // Cas 4: Game state with heaps of size 1 and 2 only
        ArrayList<Integer> jeu4 = new ArrayList<>();
        jeu4.add(1);
        jeu4.add(2);
        testCasEstConnueGagnante(jeu4, false);
    }

    /**
     * Tests a case of the estConnueGagnante() method.
     *
     * @param jeu The game state.
     * @param res The expected result.
     */
    void testCasEstConnueGagnante(ArrayList<Integer> jeu, boolean res) {
        System.out.print("estConnueGagnante(" + jeu.toString() + ") : ");

        boolean resExec = estConnueGagnante(jeu);
        
        System.out.print(resExec + " : ");
        if (resExec == res) {
            System.out.println(GREEN + "OK" + RESET);
        } else {
            System.err.println(RED + "ERREUR" + RESET);
        }
    }
}
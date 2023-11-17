import java.util.Random;
import java.util.Scanner;


public class PlateauText extends Plateau{

    protected Modele modele;
    protected String scoreLabel;
    protected Joueur joueur;


    public PlateauText(Joueur joueur) {
        super(joueur);
        this.joueur = joueur;
        scoreLabel = "SCORE : " + this.joueur.getScore();
        this.color = new int[][]
                { { 0, 1, 0, 0, 0, 1, 0 },
                        { 2, 2, 3, 3, 3, 4, 4 },
                        { 2, 2, 5, 5, 5, 4, 4 },
                        { 3, 3, 5, 5, 5, 6, 6 },
                        { 3, 3, 5, 5, 5, 6, 6 },
                        { 2, 4, 4, 3, 2, 2, 3 },
                        { 2, 4, 4, 3, 2, 2, 3 } };
                        /*   0: case invisible
                        1: case animaux
                        2: vert
                        3: violet
                        4: jaune
                        5: rouge
                        6: bleu
                        */
    }

    public void miseAJour() {

    }

    public void afficher() {
        supprimeAnimaux();

        System.out.print("********************" + "\n* SCORE: " + joueur.getScore()
                + "        *\n********************\n   ");

        for (int j = 1; j < color[0].length+1; j++ ) {
            System.out.print(" "+ j);
        }
        System.out.print("\n    ");
        for (int j = 1; j < color[0].length+1; j++ ) {
            System.out.print("- ");
        }
        System.out.print("\n ");
        for (int i = 0, alph = 65; i < color.length; i++, alph ++) {
            System.out.print((char)alph+"|");
            for (int j = 0; j < color[i].length; j++) {
                System.out.print(" "+color[i][j]);
            }
            System.out.print("\n ");
        }
    }

    public void setModele(Modele c) {
        this.modele = c;
    }

    public void jouer() {//clique
        int[] cord = new int[2];
        cord = joueur.demanderCoordonnes();
        int x = cord[0]; int y = cord[1];
        int currentColors = getNbColors();
        if(caseSeul(x, y)==false) {
            modele.memoriser();
            caseAdja(x, y);
            rearrangement();
        }
        int newColors = getNbColors();
        int points = (int) (Math.pow(currentColors - newColors, 2) * 10);
        joueur.addScore(points);
        modele.memoriserScore(points);

        scoreLabel ="SCORE : " + joueur.getScore();
        System.out.println(joueur);


    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub

        Scanner scanReponse = new Scanner(System.in);
        System.out.println("Votre nom?");
        String pseudo = scanReponse.next();
        Joueur joueur = new Joueur(pseudo);
        PlateauText p = new PlateauText(joueur);
        Modele m = new Modele();
        System.out.print("Pokemon Rescue Saga Niveau 1 \n");

        p.setModele(m);
        m.setPlateau(p);
        m.newColorReculer();
        p.afficher();

        boolean veutj = joueur.veutJouer();
        while(veutj) {
            p.afficher();
            if(m.jeuPerdu()) {
                System.out.print("Perdu!");
                joueur.finir();
                break;
            }
            else if (m.jeuGagne()) {
                System.out.print("Gagne!");
                joueur.finir();
                break;
            }
            p.jouer();
        }
    }
}

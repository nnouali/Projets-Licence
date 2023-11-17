import java.util.Scanner;

public class Joueur {

    // champs
    private String pseudo;
    private int score;
    private Scanner sc;


    // constructeur
    public Joueur(String pseudo) {
        this.pseudo = pseudo;
        this.score = 0;
        sc = new Scanner(System.in);
    }

    // getter / setter
    public String getPseudo() {
        return this.pseudo;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score = this.score + points;
    }

    public void supprimeScore(int points) {
        this.score = this.score - points;
    }

    public void setRobotName() {
        this.pseudo="Robot";
    }

    //pour le plateauText
    public int[] demanderCoordonnes() {
        int[] t = new int[2];
        System.out.println("Votre coordonnés(en forme de B6 par exemple)?");
        String s = sc.next();
        char h = s.charAt(0);
        int l = (int)s.charAt(1);
        t[0] = (int)h-65;
        t[1] = l-49;
        return t ;
    }

    public void finir() {
        sc.close();
    }

    public boolean veutJouer() {
        System.out.println("Voulez-vous jouer (oui/non)?");
        String i = sc.next();
        if(i.equals("oui"))
            return true;
        return false;
    }

    // toString
    @Override
    public String toString() {
        return "Joueur [pseudo=" + pseudo + ", score=" + score + "]";
    }

}

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class Plateau extends JFrame {

    //CHAMPS
    protected Modele modele;
    protected Color COLOR_BG = new Color(240, 255, 255);// couleur du fond
    protected Dimension SIZE = new Dimension(70, 70);// la taille des cases
    protected JLabel scoreLabel;
    protected JButton[][] game;
    protected int[][] color;
    protected JPanel panelPlateau = new JPanel();// plateau
    protected Joueur joueur;
    protected boolean[][] bord;//concerne a la methode aide
    protected JButton reculer;


    //CONSTRUCTEUR
    public Plateau(Joueur joueur) {
        this.joueur = joueur;
        scoreLabel = new JLabel("SCORE : " + this.joueur.getScore());
        scoreLabel.setBounds(20, 0, 200, 30);
        this.add(this.scoreLabel);
    }



    //METHODES

    public int getNbColors() {//permet le calcul du score
        int nbColors = 0;
        for (int i = 0; i < color.length; i++) {
            for (int j = 0; j < color[0].length; j++) {
                if (color[i][j] == 2 || color[i][j] == 3 || color[i][j] == 4 || color[i][j] == 5 || color[i][j] == 6) {
                    nbColors += 1;
                }
            }
        }
        return nbColors;
    }


    public void miseAJour() {
        for (int i = 0; i < game.length; i++) {
            for (int j = 0; j < game[i].length; j++) {
                if (color[i][j] == 0) { //case invisible
                    game[i][j].setBackground(COLOR_BG);
                    game[i][j].setBorderPainted(false);
                    game[i][j].setEnabled(false);
                    continue;
                }
                if (color[i][j] == 1) {//case animaux : pikachu
                    game[i][j].setIcon(new ImageIcon("pikachu.png"));
                }
                if (color[i][j] == 2) {
                    game[i][j].setBackground(Color.GREEN);;
                }
                if (color[i][j] == 3) {
                    game[i][j].setBackground(Color.MAGENTA);
                }
                if (color[i][j] == 4) {
                    game[i][j].setBackground(Color.YELLOW);
                }
                if (color[i][j] == 5) {
                    game[i][j].setBackground(Color.RED);
                }
                if (color[i][j] == 6) {
                    game[i][j].setBackground(Color.BLUE);
                }
                if (color[i][j] == 7) {//case fixe
                    game[i][j].setIcon(new ImageIcon("caillou.png"));
                }
                if (color[i][j] == 8) {//case animaux : aquali
                    game[i][j].setIcon(new ImageIcon("aquali.png"));
                }
                if (color[i][j] == 9) {//case animaux : mentali
                    game[i][j].setIcon(new ImageIcon("mentali.png"));
                }
            }
        }
    }

    protected boolean caseOp(int x, int y) {
        if (x < 0 || x > this.color.length - 1 || y < 0 || y > this.color[x].length - 1 || this.color[x][y] == 0
                || this.color[x][y] == 1) {
            return false;
        }
        return true;
    }

    public boolean caseSeul(int x, int y) {
        int c = color[x][y];
        for (int i = x - 1; i <= x + 1 && i < this.color.length; i++) { // x - 1, x, x + 1
            for (int j = y - 1; j <= y + 1 && j < this.color.length; j++) { // y - 1, y y + 1
                if ((i == x - 1 && j == y - 1) || (i == x - 1 && j == y + 1) || (i == x + 1 && j == y - 1)
                        || (i == x + 1 && j == y + 1) || (i == x && j == y))
                    continue;
                if (caseOp(i, j)) {
                    if (color[i][j] == c)
                        return false;
                    continue;
                }
                continue;
            }
        }
        return true;
    }

    public void caseAdja(int x, int y) {
        int c = color[x][y];
        color[x][y] = 0;
        for (int i = x - 1; i <= x + 1 && i < this.color.length; i++) { // x - 1, x, x + 1
            for (int j = y - 1; j <= y + 1 && j < this.color.length; j++) { // y - 1, y y + 1
                if ((i == x - 1 && j == y - 1) || (i == x - 1 && j == y + 1) || (i == x + 1 && j == y - 1) //sans les diagonales
                        || (i == x + 1 && j == y + 1))
                    continue;
                if (caseOp(i, j)) {
                    if (color[i][j] == c) {
                        color[x][y] = 0;
                        caseAdja(i, j);
                    }
                    continue;
                }
                continue;
            }
        }
        miseAJour();
    }

    public void supprimeAnimaux() {
        boolean animauxEnBas = true; // verifier si les animaux sont tous supprime
        while (animauxEnBas) {
            for (int k = 0; k < this.color[0].length; k++) {
                if (this.color[this.color.length - 1][k] == 1|| this.color[this.color.length - 1][k] == 8|| this.color[this.color.length - 1][k] == 9) {
                    animauxEnBas = true;
                    break;
                } else {
                    animauxEnBas = false;
                }
            }
            for (int j = 0; j < this.color[0].length; j++) {
                if (this.color[this.color.length - 1][j] == 1|| this.color[this.color.length - 1][j] == 8|| this.color[this.color.length - 1][j] == 9) {
                    this.color[this.color.length - 1][j] = 0;
                    rearrangement();
                    continue;
                }
            }
        }
        rearrangement();
    }

    public void rearrangement() {
        // Pas de colonne vide
        for (int j = this.color[0].length - 1; j >= 0; j--) {

            // colonne j est vide
            boolean colonneEstVide = true;
            for (int i = 0; i < this.color.length && this.color[i][j] != 7; i++) {
                colonneEstVide = colonneEstVide && (this.color[i][j] == 0);
            }
            if (colonneEstVide) {
                // Je supprime la colonne
                for (int k = j; k < this.color[0].length - 1; k++) { // 3 4 5
                    for (int i = 0; i < this.color.length; i++) { // 0 1 2 3 4 5 6
                        if (this.color[i][k+1] == 7)
                            continue;// non pour case fixe
                        this.color[i][k] = this.color[i][k + 1];
                        this.color[i][k+1] = 0;// Je met la dernire colonne à 0
                    }

                }


            }
        }

        // Pas de "0" dans une ligne
        for (int i = 0; i < this.color.length; i++) {
            int[] ligneCourante = this.color[i];
            for (int j = 0; j < ligneCourante.length; j++) {
                if (ligneCourante[j] == 0) {
                    int k = i;
                    while (k >= 0) {
                        if (k == 0) {
                            this.color[k][j] = 0;
                        } else {
                            this.color[k][j] = this.color[k - 1][j];
                        }
                        k--;
                    }
                }
            }
        }

    }

    public void afficher() {
        panelPlateau.removeAll();
        supprimeAnimaux();
        for (int i = 0; i < game.length; i++) {
            for (int j = 0; j < game[i].length; j++) {
                game[i][j] = new JButton();
                game[i][j].setPreferredSize(SIZE);
                game[i][j].setBorder(BorderFactory.createRaisedBevelBorder());
                if(bord[i][j]) {//permet d'aider l'utilisateur
                    game[i][j].setIcon(new ImageIcon("flower.png"));
                    bord[i][j]=false;
                }

                if (color[i][j] == 0) {
                    game[i][j].setBackground(COLOR_BG);
                    game[i][j].setBorderPainted(false);
                    game[i][j].setEnabled(false);
                }
                if (color[i][j] == 1) {
                    game[i][j].setIcon(new ImageIcon("pikachu.png"));
                }
                if (color[i][j] == 2) {
                    game[i][j].setBackground(Color.GREEN);
                    game[i][j].addActionListener(new CliqueListener(i, j));
                }
                if (color[i][j] == 3) {
                    game[i][j].setBackground(Color.MAGENTA);
                    game[i][j].addActionListener(new CliqueListener(i, j));
                }
                if (color[i][j] == 4) {
                    game[i][j].setBackground(Color.YELLOW);
                    game[i][j].addActionListener(new CliqueListener(i, j));
                }
                if (color[i][j] == 5) {
                    game[i][j].setBackground(Color.RED);
                    game[i][j].addActionListener(new CliqueListener(i, j));
                }
                if (color[i][j] == 6) {
                    game[i][j].setBackground(Color.BLUE);
                    game[i][j].addActionListener(new CliqueListener(i, j));
                }
                if (color[i][j] == 7) {
                    game[i][j].setIcon(new ImageIcon("caillou.png"));
                }
                if (color[i][j] == 8) {
                    game[i][j].setIcon(new ImageIcon("aquali.png"));
                }
                if (color[i][j] == 9) {
                    game[i][j].setIcon(new ImageIcon("mentali.png"));
                }
                panelPlateau.add(game[i][j]);
            }
        }
    }

    public void reculer() {
        modele.reculer();
        joueur.supprimeScore(modele.point);
        afficher();
        scoreLabel.setText("SCORE : " + joueur.getScore());
        System.out.println(joueur);
        panelPlateau.updateUI();//sert à rafraichir la fenetre
        reculer.setEnabled(false);
    }

    public void aide() {
        Random rd = new Random();
        int x = rd.nextInt(color.length);
        int y = rd.nextInt(color[0].length);
        if(!caseOp(x,y)||caseSeul(x, y)) aide();
        else {
            setBorder(x, y);
            afficher();
            panelPlateau.updateUI();
        }
    }

    public void setBorder(int x, int y) {
        bord[x][y]=true;
        for (int i = x - 1; i <= x + 1 && i < this.color.length; i++) { // x - 1, x, x + 1
            for (int j = y - 1; j <= y + 1 && j < this.color.length; j++) { // y - 1, y y + 1
                if ((i == x - 1 && j == y - 1) || (i == x - 1 && j == y + 1) || (i == x + 1 && j == y - 1)
                        || (i == x + 1 && j == y + 1)||(i == x && j==y))
                    continue;
                if (caseOp(i, j)) {
                    if (color[i][j] == color[x][y]&&!bord[i][j]) {
                        bord[i][j]=true;
                        setBorder(i, j);
                    }
                    continue;
                }
                continue;
            }
        }
    }

    public void restart() {
        joueur.setScore(0);
    }

    public void nextLev() {
        joueur.setScore(0);
    }

    public void setModele(Modele c) {
        this.modele = c;
    }

    class CliqueListener implements ActionListener {

        private int x;
        private int y;


        public CliqueListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        //eccoute les actions liés à la souris sur les bouttons
        public void actionPerformed(ActionEvent arg0) {
            int currentColors = getNbColors();
            if(caseSeul(x, y)==false) {
                modele.memoriser();
                caseAdja(x, y);
                rearrangement();
                afficher();
                reculer.setEnabled(true);
            }

            int newColors = getNbColors();
            int points = (int) (Math.pow(currentColors - newColors, 2) * 10);//calcul des points
            joueur.addScore(points);
            modele.memoriserScore(points);
            scoreLabel.setText("SCORE : " + joueur.getScore());
            System.out.println(joueur);//verification sur la console

            if(modele.jeuPerdu()) {
                JOptionPane perdu = new JOptionPane();
                int n = perdu.showConfirmDialog(null, "Vous avez perdu :( "
                                + "Voulez-vous recommencer?", "AH! Perdu",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if(n == 0) {restart();}

            }
            else if (modele.jeuGagne()) {
                JOptionPane gagne = new JOptionPane();
                int n = gagne.showConfirmDialog(null, "Vous avez gagné :) "
                                + "Next Level?", "OH! Gagné!",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if(n == 0) {nextLev();}
            }
        }
    }

}

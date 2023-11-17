import java.awt.*;
import javax.swing.*;

public class PlateauNiveau1 extends Plateau {
    // la taille de chaque case est 70*70 pix, dans niv1 on a 7*7 de cases. donc la
    // taille de Frame est tjs (lengths totale des cases+100)*(hauteur totale des
    // cases + 250)

    //CHAMPS
    private JPanel panelGoal = new JPanel(); // contient le nb d'animaux reste à sauver
    private JPanel panelHelp = new JPanel();// contient le button help, exit, restart et next level


    //ONSTRUCTEUR
    public PlateauNiveau1(Joueur joueur) {
        super(joueur);
        /**0: case invisible
        1: case animaux: Pikachu
        2: vert
        3: violet
        4: jaune
        5: rouge
        6: bleu
        */
        this.color = new int[][]
                { { 0, 1, 0, 0, 0, 1, 0 },
                        { 2, 2, 3, 3, 3, 4, 4 },
                        { 2, 2, 5, 5, 5, 4, 4 },
                        { 3, 3, 5, 5, 5, 6, 6 },
                        { 3, 3, 5, 5, 5, 6, 6 },
                        { 2, 4, 4, 3, 2, 2, 3 },
                        { 2, 4, 4, 3, 2, 2, 3 } };

        this.bord = new boolean[color.length][color[0].length];
        for(int i = 0; i< color.length;i++) {
            for(int j = 0;j< color[i].length;j++) {
                this.bord[i][j] = false;
            }
        }

        this.setTitle("Pokemon Rescue Saga Niveau 1");
        this.setSize(590, 740);
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        // Layout: organisation de la fenetre
        this.getContentPane().setLayout(new BorderLayout());
        this.add(panelGoal, BorderLayout.NORTH);
        panelGoal.setPreferredSize(new Dimension(590, 90));
        this.add(panelPlateau, BorderLayout.CENTER);
        panelPlateau.setPreferredSize(new Dimension(590, 590)); // la taille ttl des cases + 100
        this.add(panelHelp, BorderLayout.SOUTH);
        panelHelp.setPreferredSize(new Dimension(590, 60));

//		setting panelGoal

        panelGoal.setBackground(COLOR_BG);

//		setting panelPlateau
        panelPlateau.setBackground(COLOR_BG);
        game = new JButton[7][7];//taille de notre jeu

        afficher();


//		setting panelHelp
        JButton help = new JButton("Help me");
        help.addActionListener(event -> {
            super.aide();
        });



        JButton exit = new JButton("Exit");
        exit.addActionListener((event) -> {
            System.exit(0);
        });

        JButton restart = new JButton("Restart");
        restart.addActionListener(event ->{
            restart();
        });

        JButton nextLevel = new JButton("Next Level");
        nextLevel.addActionListener(event -> {
            nextLev();
        });

        reculer = new JButton("Undo");
        reculer.addActionListener(event ->{
            super.reculer();

        });

        panelHelp.setLayout(new GridLayout(1, 5));
        panelHelp.add(help);
        panelHelp.add(exit);
        panelHelp.add(restart);
        panelHelp.add(nextLevel);
        panelHelp.add(reculer);

    }

    //METHODES

    @Override
    public void restart() {
        super.restart();
        this.setVisible(false);
        PlateauNiveau1 lev1 = new PlateauNiveau1(this.joueur);
        lev1.setVisible(true);
        lev1.setModele(modele);
        modele.setPlateau(lev1);

    }

    @Override
    public void nextLev() {
        super.nextLev();
        PlateauNiveau2 lev2 = new PlateauNiveau2(this.joueur);
        this.setVisible(false);
        lev2.setVisible(true);
        lev2.setModele(modele);
        modele.setPlateau(lev2);
        modele.newColorReculer();
    }

}


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PlateauRobot extends Plateau{

    //champs
    private JPanel panelGoal = new JPanel(); // contient le nb d'animaux reste à sauver

    private JPanel panelHelp = new JPanel();// contient le button help, exit, restart et next level

    private Timer timer;

    public PlateauRobot(Joueur joueur) {
        super(joueur);
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

        this.setTitle("Pokemon Rescue Saga Robot");
        this.setSize(590, 740);
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);//pour arreter le programme lorsque la fenetre est fermée


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
        help.setEnabled(false);

        JButton exit = new JButton("Exit");
        exit.addActionListener((event) -> {
            System.exit(0);
        });

        JButton restart = new JButton("Restart");
        restart.addActionListener(event ->{
            restart();
        });


        JButton nextLevel = new JButton("Next Level");
        nextLevel.setEnabled(false);

        reculer = new JButton("Undo");
        reculer.setEnabled(false);

        panelHelp.setLayout(new GridLayout(1, 5));
        panelHelp.add(help);
        panelHelp.add(exit);
        panelHelp.add(restart);
        panelHelp.add(nextLevel);
        panelHelp.add(reculer);

        ActionListener robot=new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                Random rd = new Random(); // cliquer des cases au hasard
                int x = rd.nextInt(color.length);
                int y = rd.nextInt(color[0].length);
                while(!caseOp(x,y)||caseSeul(x, y)) {
                    x = rd.nextInt(color.length);
                    y = rd.nextInt(color[0].length);
                }
                int currentColors = getNbColors();
                if(caseSeul(x, y)==false) {
                    modele.memoriser();
                    caseAdja(x, y);
                    rearrangement();
                    afficher();
                }

                int newColors = getNbColors();
                int points = (int) (Math.pow(currentColors - newColors, 2) * 10);
                joueur.addScore(points);
                modele.memoriserScore(points);

                scoreLabel.setText("SCORE : " + joueur.getScore());
                System.out.println(joueur);

                if(modele.jeuPerdu()) {
                    JOptionPane perdu = new JOptionPane();
                    int n = perdu.showConfirmDialog(null, "Vous avez perdu :( "
                                    + "Voulez-vous recommencer?", "AH! Perdu",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                    if(n == 0) {restart();}

                }
                else if (modele.jeuGagne()) {
                    JOptionPane gagne = new JOptionPane();
                    gagne.showMessageDialog(null, "Vous avez gagné :) ");
                    timer.stop();
                }


            }
        };
        timer = new Timer(1000,robot);
        timer.start();
    }

    public void restart() {
        timer.stop();
        super.restart();
        this.setVisible(false);
        PlateauRobot rob = new PlateauRobot(this.joueur);
        rob.setVisible(true);
        rob.setModele(modele);
        modele.setPlateau(rob);
    }
}

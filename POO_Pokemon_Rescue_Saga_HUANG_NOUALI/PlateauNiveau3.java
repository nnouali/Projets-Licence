import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PlateauNiveau3 extends Plateau{
    // dans niv3 on a un plateau de jeu 7*9 de cases qui sont de taille 50*50

    //CHAMPS
    JPanel panelGoal = new JPanel(); // contient le nb d'animaux qu'il reste a sauver
    JPanel panelHelp = new JPanel();// contient le button help et exit

    //CONSTRUCTEUR
    public PlateauNiveau3(Joueur joueur) {
        super(joueur);
         /** 0: case invisible
	        1: case animaux : Piakachu
	        2: vert
	        3: violet
	        4: jaune
	        5: rouge
	        6: bleu
	        7: case fixe
	        8: case animaux : Evoli
	        9: case aniamal : Mentali */
        this.color= new int[][] {{0,0,1,0,8,0,9},
                {0,0,6,3,5,3,5},
                {0,0,6,3,5,3,6},
                {0,0,3,5,6,3,6},
                {6,6,3,5,6,3,6},
                {6,5,5,6,5,6,5},
                {5,5,5,6,5,6,5},
                {5,5,3,5,3,7,7},
                {6,6,3,5,3,7,7}};


        this.bord = new boolean[color.length][color[0].length];
        for(int i = 0; i< color.length;i++) {
            for(int j = 0;j< color[i].length;j++) {
                this.bord[i][j] = false;
            }
        }

        this.setTitle("Pokemon Rescue Saga Niveau 3");
        this.setSize(450, 700);
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //Layout
        this.getContentPane().setLayout(new BorderLayout());
        this.add(panelGoal,BorderLayout.NORTH);
        panelGoal.setPreferredSize(new Dimension(590,90));
        this.add(panelPlateau,BorderLayout.CENTER);
        panelPlateau.setPreferredSize(new Dimension(590,650));
        this.add(panelHelp,BorderLayout.SOUTH);
        panelHelp.setPreferredSize(new Dimension(590,60));

//		setting panelGoal
        COLOR_BG= new Color(135,206,250);
        panelGoal.setBackground(COLOR_BG);

//		setting panelPlateau
        panelPlateau.setBackground(COLOR_BG);
        game = new JButton[9][7];
        SIZE = new Dimension(50,50);// set la taille des cases

        afficher();

//		setting panelHelp
        JButton help = new JButton("Help me");
        help.addActionListener(event ->{
            super.aide();
        });

        JButton exit = new JButton("Exit");
        exit.addActionListener((event)->{
            System.exit(0);
        });

        JButton restart = new JButton("Restart");
        restart.addActionListener(event ->{
            restart();
        });

        JButton nextLevel = new JButton("Next Level");
        nextLevel.setEnabled(false);

        reculer = new JButton("Undo");
        reculer.addActionListener(event ->{
            super.reculer();
        });

        panelHelp.setLayout(new GridLayout(1,5));
        panelHelp.add(help);
        panelHelp.add(exit);
        panelHelp.add(restart);
        panelHelp.add(nextLevel);
        panelHelp.add(reculer);
    }

    @Override
    public void restart() {
        super.restart();
        this.setVisible(false);
        PlateauNiveau3 lev3 = new PlateauNiveau3(this.joueur);
        lev3.setVisible(true);
        lev3.setModele(modele);
        modele.setPlateau(lev3);
    }

    @Override
    public void nextLev() {
        JOptionPane n = new JOptionPane(this.joueur);
        n.showMessageDialog(null, "Bravo ! C'est le dernier niveau!", "Conguatulation~!", JOptionPane.INFORMATION_MESSAGE);
    }
}

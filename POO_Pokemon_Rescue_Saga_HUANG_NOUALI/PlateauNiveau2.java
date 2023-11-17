import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class PlateauNiveau2 extends Plateau {
    // dans niv2 on a 5*8 de cases

    //CHAMPS
    private JPanel panelGoal = new JPanel(); // contient le nb d'animaux reste a sauver
    private JPanel panelHelp = new JPanel();// contient le button help et exit


    //CONSTRUCTEUR
    public PlateauNiveau2(Joueur joueur) {
        super(joueur);
    	/** 0: case invisible
		1: case animaux : Pikachu
		2: vert
		3: violet
		4: jaune
		5: rouge
		6: bleu
		8: case animaux : Aquali*/
        this.color = new int[][] {{0,5,8,5,0},
                {1,5,8,5,1},
                {2,6,8,6,2},
                {2,6,2,6,2},
                {6,5,2,5,6},
                {6,5,2,5,6},
                {5,6,2,6,5},
                {5,6,2,6,5}};

        this.bord = new boolean[color.length][color[0].length];
        for(int i = 0; i< color.length;i++) {
            for(int j = 0;j< color[i].length;j++) {
                this.bord[i][j] = false;
            }
        }


        this.setTitle("Pokemon Rescue Saga Niveau 2");
        this.setSize(450, 810);
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        //Layout
        this.getContentPane().setLayout(new BorderLayout());
        this.add(panelGoal,BorderLayout.NORTH);
        panelGoal.setPreferredSize(new Dimension(450,90));
        this.add(panelPlateau,BorderLayout.CENTER);
        panelPlateau.setPreferredSize(new Dimension(450,660));
        this.add(panelHelp,BorderLayout.SOUTH);
        panelHelp.setPreferredSize(new Dimension(450,60));

//		setting panelGoal
        COLOR_BG= new Color(230,230,255);
        panelGoal.setBackground(COLOR_BG);

//		setting panelPlateau
        panelPlateau.setBackground(COLOR_BG);
        game = new JButton[8][5];

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
        nextLevel.addActionListener(event ->{
            nextLev();
        });
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

    //METHODES

    @Override
    public void restart() {
        super.restart();
        this.setVisible(false);
        PlateauNiveau2 lev2 = new PlateauNiveau2(this.joueur);
        lev2.setVisible(true);
        lev2.setModele(modele);
        modele.setPlateau(lev2);
    }

    @Override
    public void nextLev() {
        super.nextLev();
        PlateauNiveau3 lev3 = new PlateauNiveau3(this.joueur);
        this.setVisible(false);
        lev3.setVisible(true);
        lev3.setModele(modele);
        modele.setPlateau(lev3);
        modele.newColorReculer();
    }
}

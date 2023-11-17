import java.awt.*;
import javax.swing.*;

public class  PlateauMain extends JFrame implements Serialisable{

    //le plateau main est notre page d'acceuil, elle contient les niveaux et les regles.
    //CHAMPS :
    public Plateau p;
    private Modele c;
    private Joueur joueur;

    private JPanel panneauImg = new JPanel();
    private JPanel panneauNiv = new JPanel();
    private JLabel label = new JLabel();
    //image accueil
    private ImageIcon image = new ImageIcon("jeu.JPG");
    //les boutons
    private JButton niv1 = new JButton("Niveau 1");
    private JButton niv2 = new JButton("Niveau 2");
    private JButton niv3 = new JButton("Niveau 3");
    private JButton robot = new JButton("Robot");
    private JButton help = new JButton("Help");
    private JButton exit = new JButton("Exit");

    //CONSTRUCTEUR
    public PlateauMain() {
        this.setTitle("Pokemon Rescue Saga");//titre du jeu
        this.setSize(350, 500);//taille de la fenetre d'acceuil
        setDefaultCloseOperation(EXIT_ON_CLOSE);//pour que l’application se termine lorsque l’utilisateur ferme la fenêtre
        this.setLocationRelativeTo(null);//ouvrir JFrame au centre d'écran

        label.setIcon(image);
        panneauImg.add(label);

        this.getContentPane().setLayout(new GridLayout(2,1));
        this.getContentPane().add(panneauImg);
        this.getContentPane().add(panneauNiv);

        panneauNiv.setLayout(new GridLayout(6,1));
        panneauNiv.add(niv1);
        panneauNiv.add(niv2);
        panneauNiv.add(niv3);
        panneauNiv.add(robot);
        panneauNiv.add(help);
        panneauNiv.add(exit);



        // Creer un joueur
        this.initJoueur();
        // acceder dans la fenetre niveau 1 et fermer le plateau main
        niv1.addActionListener((event) -> {
            p = new PlateauNiveau1(this.joueur);
            this.setVisible(false);
            p.setVisible(true);
            p.setModele(c);
            c.setPlateau(p);
            c.newColorReculer();
        });
        // acceder dans la fenetre niveau 2 et fermer le plateau main

        niv2.addActionListener((event) -> {
            p = new PlateauNiveau2(this.joueur);
            this.setVisible(false);
            p.setVisible(true);
            p.setModele(c);
            c.setPlateau(p);
            c.newColorReculer();
        });
        // acceder dans la fenetre niveau 3 et fermer le plateau main

        niv3.addActionListener((event) -> {
            p = new PlateauNiveau3(this.joueur);
            this.setVisible(false);
            p.setVisible(true);
            p.setModele(c);
            c.setPlateau(p);
            c.newColorReculer();
        });

        robot.addActionListener(event ->{
            this.joueur.setRobotName();
            p = new PlateauRobot(this.joueur);
            this.setVisible(false);
            p.setVisible(true);
            p.setModele(c);
            c.setPlateau(p);
            c.newColorReculer();
        });

        // Regles du jeu dans le boutons

        help.addActionListener(event ->{
            JOptionPane h = new JOptionPane();//affiche une boîte de dialogue qui invite l'utilisateur à saisir une valeur ou l'informe de quelque chose
            h.showMessageDialog(panneauNiv, "Régles du jeu:\nVous devez sauver les Pokemon des mains de la team Rocket! \nAfin de les liberer, les Pokemons doivent atteindre le sol! \nPour cela, sélectionner l’une des cases de couleurs et, dès lors que le nombre \nde cases contigües de même couleur est superieur à 1, celles-ci disparaitront.  ", "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        exit.addActionListener((event)->{
            System.exit(0);
        });
        // exit

    }

    //METHODES
    public void setModele(Modele c) {
        this.c = c;
    }

    public void initJoueur() {
        JOptionPane lePseudo = new JOptionPane();
        String pseudo = lePseudo.showInputDialog(panneauNiv,"Entrez votre pseudo s'il vous plait ","Bienvenue ",JOptionPane.QUESTION_MESSAGE);
        this.joueur = new Joueur(pseudo);
        System.out.println(this.joueur);
    }
}

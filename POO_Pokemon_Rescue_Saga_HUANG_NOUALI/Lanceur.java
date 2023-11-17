import java.awt.*;

public class Lanceur {

    //CHAMP
    private Modele m;

    //CONSTRUCTEUR
    public Lanceur() {
        m = new Modele();
        PlateauMain plateauM = new PlateauMain();
        plateauM.setVisible(true);
        plateauM.setModele(m);
        m.setPlateau(plateauM.p);

    }

    //METHODE
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new Lanceur();
        });
    }

}

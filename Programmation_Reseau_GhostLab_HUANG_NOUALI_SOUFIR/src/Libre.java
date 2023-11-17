import java.util.LinkedList;
import java.util.List;

public class Libre extends Case {

    private final List<Joueur> joueurs = new LinkedList<>();
    private final List<Fantome> fantomes = new LinkedList<>();

    public Libre(int line, int col) {
        super(line, col);
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public List<Fantome> getFantomes() {
        return fantomes;
    }
}
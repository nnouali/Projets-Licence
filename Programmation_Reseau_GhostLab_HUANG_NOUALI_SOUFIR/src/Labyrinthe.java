import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Labyrinthe implements Serializable {

    // Champs
    private int nbLine;
    private int nbCol;
    private Case[][] plateau;
    private Partie p;

    //relier partie
    
    // Constructeur
    public Labyrinthe(int d, Partie p) {

        try {
            Path path = Paths.get("plateaux/");
            Files.createDirectories(path);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        File f = new File("plateaux/plateau" + d + ".bin");
        if (f.exists() && !f.isDirectory()) {
            // on charge le plateau en le deserialisant
            plateau = Labyrinthe.deserializeLabyrinth("plateaux/plateau" + d + ".bin");
        } else {
            // on serialize le plateau (les fichiers de serialistion devraient etre rendus
            // dans l'archive)
            try {
                f.createNewFile();
                if (d == 1)
                    plateau = LabyrintheConsts.LABYRINTHE_1;
                if (d == 2)
                    plateau = LabyrintheConsts.LABYRINTHE_2;
                if (d == 3)
                    plateau = LabyrintheConsts.LABYRINTHE_3;
                serializeLabyrinth(plateau, d);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        this.nbLine = plateau.length;
        this.nbCol = plateau[0].length;
        this.p = p;
    }
    public int getNbLine() {
        return nbLine;
    }

    public int getNbCol() {
        return nbCol;
    }

    public Case[][] getPlateau() {
        return plateau;
    }

    public void printLabyrinth() {
        for (int i = 0; i < nbLine; i++) {
            for (int j = 0; j < nbCol; j++) {
                if (plateau[i][j] instanceof Mur)
                    System.out.print("███");
                else if (plateau[i][j] instanceof Libre) {
                    Libre libre = (Libre) plateau[i][j];
                    if (!libre.getJoueurs().isEmpty()) {
                        System.out.print("\ud83e\udd16 ");
                    } else if (!libre.getFantomes().isEmpty() && p.getFantomes().containsAll(libre.getFantomes())) {
                        System.out.print("\ud83d\udc7b ");
                    } else {
                        System.out.print("   ");
                    }
                }
            }
            System.out.println();
        }
    }

    public LinkedList<String> printHelpLabyrinth(int posX, int posY) {
        LinkedList<String> res = new LinkedList<>();
        for (int i = posX - 1; i <= posX + 1; i++) {
            for (int j = posY - 1; j <= posY + 1; j++) {
                if (i < 0 || i >= nbLine || j < 0 || j >= nbCol)
                    continue;
                if (plateau[i][j] instanceof Mur) {
                    res.add(String.format("OHELP M %03d %03d***", i, j));
                } else if (plateau[i][j] instanceof Libre) {
                    Libre libre = (Libre) plateau[i][j];
                    if (!libre.getJoueurs().isEmpty()) {
                        res.add(String.format("OHELP j %03d %03d***", i, j));
                    } else if (!libre.getFantomes().isEmpty()) {
                        res.add(String.format("OHELP f %03d %03d***", i, j));
                    } else {
                        res.add(String.format("OHELP l %03d %03d***", i, j));
                    }
                }
            }
        }
        return res;
    }

    public static void serializeLabyrinth(Case[][] p, int id) {
        try {
            FileOutputStream fos = new FileOutputStream("plateaux/plateau" + id + ".bin");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(p);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Case[][] deserializeLabyrinth(String name_file) {
        Case[][] l = null;
        try {
            FileInputStream fis = new FileInputStream(name_file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            l = (Case[][]) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return l;

    }
}
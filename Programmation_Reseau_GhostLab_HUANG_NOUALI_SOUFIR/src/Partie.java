
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Partie {

    private static final Object lockConstructor = new Object();
    private final Object lockMovements = new Object();

    private Joueur highjoueur;
    private LinkedList<Joueur> joueurs = new LinkedList<>();
    private LinkedList<Fantome> fantomes = new LinkedList<>();
    private final int nb_help;
    private Labyrinthe labyrinthe;
    private static int nbPartie = 0;// increment à chaque num de partie
    private int id_partie;// à coupler avec nbPartie
    private boolean state;// etat de la partie voir si int
    private static int port_multi_count = 9999;
    private boolean end = false;
    private final Random random;
    private final DatagramSocket dso;
    private final List<Thread> fantomeThreads;
    public final int nBreak;
    private final int difficulte;

    // Pour génenérer les addresses de multi-diffusion :
    private static int a = 225;
    private static int b = 1;
    private static int c = 2;
    private static int d = 4;

    // Pour le multi cast
    private String add_multi;
    private String add_multi_sending;
    private int port_multi;

    public Partie(Joueur j, int difficulte) throws IOException {
        synchronized (lockConstructor) {
            id_partie = nbPartie;
            nbPartie++;

            joueurs.add(j); // ajout du joueur à l'origine de la partie

            this.difficulte=difficulte;

            if (this.difficulte == 0) {
                nb_help = 1;
                nBreak = 1;
                labyrinthe = new Labyrinthe(1, this);
                fantomes.add(new Casper(this, 0, 0));
                fantomes.add(new Spectre(this, 0, 0));
                fantomes.add(new Mimi(this, 0, 0));

            } else if (this.difficulte == 1) {
                nb_help = 2;
                nBreak = 2;
                labyrinthe = new Labyrinthe(2,this);
                fantomes.add(new Casper(this, 0, 0));
                fantomes.add(new Casper(this, 0, 0));
                fantomes.add(new Mimi(this, 0, 0));
                fantomes.add(new Mimi(this, 0, 0));
                fantomes.add(new Spectre(this, 0, 0));
                fantomes.add(new Spectre(this, 0, 0));
            } else {
                nb_help = 3;
                nBreak = 3;
                labyrinthe = new Labyrinthe(3,this);
                fantomes.add(new Casper(this, 0, 0));
                fantomes.add(new Casper(this, 0, 0));
                fantomes.add(new Casper(this, 0, 0));
                fantomes.add(new Mimi(this, 0, 0));
                fantomes.add(new Mimi(this, 0, 0));
                fantomes.add(new Mimi(this, 0, 0));
                fantomes.add(new Spectre(this, 0, 0));
                fantomes.add(new Spectre(this, 0, 0));
                fantomes.add(new Spectre(this, 0, 0));

            }
            random = new Random();
            // Initialiser le multi cast
            this.add_multi = String.valueOf(a) + "." + String.valueOf(b) + "." + String.valueOf(c) + "."
                    + String.valueOf(d);
            if (d == 255) {
                d = 0;
                if (c == 255) {
                    c = 0;
                    if (b == 255) {
                        b = 0;
                        if (a == 255)System.exit(1);
                    } else b++;
                } else c++;
            } else d++;
            // Initialisation de l'adresse de multi d:
            this.add_multi_sending = adjustIP(add_multi);
            this.port_multi = port_multi_count;

            this.dso = new DatagramSocket();
            fantomeThreads = new ArrayList<>();
            this.highjoueur=joueurs.get(0);
        }
    }

    // Dans cette classe, que des méthodes pas de "main" ni de "run"

    public synchronized void init_partie() {
    	System.out.println("Difficulte de la partie "+this.difficulte+1);
        // On place les fantomes :
        for (Fantome f : fantomes) {
            boolean done = false;
            while (done == false) {
                int x = new Random().nextInt(this.labyrinthe.getNbLine());
                int y = new Random().nextInt(this.labyrinthe.getNbCol());
                if (this.labyrinthe.getPlateau()[x][y] instanceof Libre) {
                    if (((Libre) this.labyrinthe.getPlateau()[x][y]).getJoueurs().isEmpty()
                            && ((Libre) this.labyrinthe.getPlateau()[x][y]).getFantomes().isEmpty()) {
                        ((Libre) this.labyrinthe.getPlateau()[x][y]).getFantomes().add(f);
                        f.setX(x);
                        f.setY(y);
                        done = true;
                    }
                }
            }
        }
        // on place les joueurs:
        for (Joueur joueur : joueurs) {
            int x, y = 0;
            boolean done = false;
            while (done == false) {
                x = new Random().nextInt(this.labyrinthe.getNbLine());
                y = new Random().nextInt(this.labyrinthe.getNbCol());
                if (this.labyrinthe.getPlateau()[x][y] instanceof Libre) {
                    if (((Libre) this.labyrinthe.getPlateau()[x][y]).getJoueurs().isEmpty()
                            && ((Libre) this.labyrinthe.getPlateau()[x][y]).getFantomes().isEmpty()) {
                        ((Libre) this.labyrinthe.getPlateau()[x][y]).getJoueurs().add(joueur);
                        joueur.setX(x);
                        joueur.setY(y);
                        done = true;
                    }
                }
            }
        }
        for (Fantome f : this.getFantomes()) {
            Thread thread = new Thread(f);
            this.getFantomeThreads().add(thread);
            thread.start();
        }
    }

    public void welcome() {

        init_partie();
        this.state = true;
        for (Joueur j : joueurs) {
            try {
                byte m = (byte) this.id_partie;
                ByteBuffer b = ByteBuffer.allocate(2);//h
                b.putShort((short) this.getLabyrinthe().getNbLine());
                byte[] result = b.array();

                ByteBuffer b2 = ByteBuffer.allocate(2);//w
                b2.putShort((short) this.getLabyrinthe().getNbCol());
                byte[] result2 = b2.array();

                String tmp = " ";
                String tmp2 = " ";
                ByteBuffer b3 = ByteBuffer.allocate(1);
                b3.put((byte) this.getFantomes().size());
                byte[] fant = b3.array();
                String tmp3 = " " + this.add_multi_sending + " " + String.valueOf(this.port_multi) + "***";

                byte c[] = new byte[39];
                System.arraycopy("WELCO ".getBytes(), 0, c, 0, 6);// WELCO ...
                c[6] = m;
                System.arraycopy(" ".getBytes(), 0, c, 6 + 1, 1);//" "
                System.arraycopy(result, 0, c, 8, result.length);// h
                System.arraycopy(tmp.getBytes(), 0, c, 8 + result.length, 1); // " "
                System.arraycopy(result2, 0, c, 8 + result.length + 1, result2.length);// w
                System.arraycopy(tmp2.getBytes(), 0, c, 8 + result.length + 1 + result2.length, 1); // " "

                System.arraycopy(fant, 0, c, 8 + result.length + 1 + result2.length + 1, 1); // nb fantome
                System.arraycopy(tmp3.getBytes(), 0, c, 8 + result.length + 1 + result2.length + 1 + 1, 24); //ip+port

                j.getOw().write(c);
                j.getOw().flush();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        for (Joueur j : joueurs) {
            try {
                String m = "POSIT " + j.getIdString() + " " + this.adjustInteger(j.getX(), 3) + " "
                        + this.adjustInteger(j.getY(), 3) + "***";
                // envoi
                j.send(j.getOw(), m);
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    /* 				DEPLACEMENTS DES FANTOMES 					*/

    // Deplacement vers la gauche :
    public void leftMove(Fantome fantome, int dist) {
        synchronized (this) {
            int x = fantome.getX();
            int y = fantome.getY();
            int newY = y - dist;
            if (newY >= 0 && labyrinthe.getPlateau()[x][newY] instanceof Libre) {
                fantome.setY(newY);
                ((Libre) labyrinthe.getPlateau()[x][y]).getFantomes().remove(fantome);
                ((Libre) labyrinthe.getPlateau()[x][newY]).getFantomes().add(fantome);
                sendFantomePosition(fantome);
            }
        }
    }

    // Deplacement vers la droite :
    public void rightMove(Fantome fantome, int dist) {
        synchronized (this) {
            int x = fantome.getX();
            int y = fantome.getY();
            int newY = y + dist;
            if (newY < labyrinthe.getNbCol() && labyrinthe.getPlateau()[x][newY] instanceof Libre) {
                fantome.setY(newY);
                ((Libre) labyrinthe.getPlateau()[x][y]).getFantomes().remove(fantome);
                ((Libre) labyrinthe.getPlateau()[x][newY]).getFantomes().add(fantome);
                sendFantomePosition(fantome);
            }
        }
    }
    // Deplacement vers le haut :
    public void upMove(Fantome fantome, int dist) {
        synchronized (this) {
            int x = fantome.getX();
            int y = fantome.getY();
            int newX = x - dist;
            if (newX >= 0 && labyrinthe.getPlateau()[newX][y] instanceof Libre) {
                fantome.setX(newX);
                ((Libre) labyrinthe.getPlateau()[x][y]).getFantomes().remove(fantome);
                ((Libre) labyrinthe.getPlateau()[newX][y]).getFantomes().add(fantome);
                sendFantomePosition(fantome);
            }
        }
    }

    // Deplacement vers le bas :
    public void downMove(Fantome fantome, int dist) {
        synchronized (this) {
            int x = fantome.getX();
            int y = fantome.getY();
            int newX = x + dist;
            if (newX < labyrinthe.getNbLine() && labyrinthe.getPlateau()[newX][y] instanceof Libre) {
                fantome.setX(newX);
                ((Libre) labyrinthe.getPlateau()[x][y]).getFantomes().remove(fantome);
                ((Libre) labyrinthe.getPlateau()[newX][y]).getFantomes().add(fantome);
                sendFantomePosition(fantome);
            }
        }
    }

    // Actualise la partie apres chaque deplacement de fantomes
    public void sendFantomePosition(Fantome fantome) {
        synchronized (this) {
            // Vérifier si le fantome est toujours vivant
            if (!getFantomes().contains(fantome))return;
            try {
                List<Joueur> joueursInCase = new ArrayList<>();
                for (Joueur joueur : joueurs) {
                    if (joueur.getX() == fantome.getX() && joueur.getY() == fantome.getY()) {
                        joueursInCase.add(joueur);
                    }
                }
                if (joueursInCase.isEmpty()) {
                    // Multi-diffuser le message GHOST x y+++
                    byte[] data = ("GHOST " + adjustInteger(fantome.getX(), 3) + " " + adjustInteger(fantome.getY(), 3)
                            + "+++").getBytes();
                    multicast(data);
                } else {
                    // Associer à chaque joueur une probabilité d'attraper le fantôme
                    int nombreJoueurs = joueursInCase.size();
                    float[] probaJoueurs = new float[nombreJoueurs];
                    for (int i = 0; i < nombreJoueurs; i++) {
                        probaJoueurs[i] = random.nextFloat();
                    }
                    // Choisir le joueur avec la plus forte probabilité
                    Joueur j = joueursInCase.get(0);
                    float probaMax = probaJoueurs[0];
                    for (int i = 1; i < probaJoueurs.length; i++) {
                        if (probaMax < probaJoueurs[i]) {
                            probaMax = probaJoueurs[i];
                            j = joueursInCase.get(i);
                        }
                    }
                    // j : c'est le joueur qui a le droit de prendre le fantôme
                    if (this.labyrinthe.getPlateau()[fantome.getX()][fantome.getY()] instanceof Libre) {
                        for (Fantome fc : ((Libre) this.labyrinthe.getPlateau()[fantome.getX()][fantome.getY()])
                                .getFantomes()) {
                            j.setScore(j.getScore() + fc.score);
                            // Supprimer le fantôme de la partie
                            fantomes.remove(fc);   
                            fc.partie=null;
                            // Multi-diffuser le message : SCORE id p x y+++
                            byte[] message = String
                                    .format("SCORE %s %s %s %s+++", j.getIdString(), adjustInteger(j.getScore(), 4),
                                            adjustInteger(j.getX(), 3), adjustInteger(j.getY(), 3))
                                    .getBytes();
                            multicast(message);
                        }
                        ((Libre) this.labyrinthe.getPlateau()[fantome.getX()][fantome.getY()]).getFantomes()
                                .clear();
                    }
                    if (this.highjoueur == null || j.getScore() > this.highjoueur.getScore())this.highjoueur = j;
                    // Vérifier s'il reste encore des fantomes ?
                    // Oui -> Alors continuer la partie
                    // Non -> Arreter la partie
                    if (fantomes.isEmpty()) {
                        this.end = true;
                        this.isItTheEnd();
                    }
                }
                viewGameState();
            } catch (Exception e) {e.printStackTrace();}
        }
    }
    public void viewGameState() {
        synchronized (this) {
            this.labyrinthe.printLabyrinth();
            System.out.println(fantomes);
            System.out.println("\n\n");
        }
    }

    /* 					DEPLACEMENTS DES JOUEURS 				*/
    // Deplacement vers le haut
    public boolean upMove(Joueur joueur, int dist, boolean canBreak) throws IOException {
        synchronized (lockMovements) {
            int d = 0;
            int xdep = joueur.getX();
            int ydep = joueur.getY();
            int i = joueur.getX();
            int j = joueur.getY();
            boolean croise = false;
            // A chaque deplacement de 1, on vérifie si on croise des fantomes et on
            // actualise les points si necessaire
            while (d != dist) {
                if (i - 1 < labyrinthe.getNbLine() && i - 1 >= 0) {
                    if (labyrinthe.getPlateau()[i - 1][j] instanceof Mur) {
                        if (canBreak) {
                            joueur.setNbBreak(joueur.getNbBreak() - 1);
                            labyrinthe.getPlateau()[i - 1][j] = new Libre(i - 1, j);
                            ((Libre) labyrinthe.getPlateau()[i - 1][j]).getJoueurs().add(joueur);
                            ((Libre) labyrinthe.getPlateau()[i][j]).getJoueurs().remove(joueur);
                            joueur.setX(i - 1);
                            return true;
                        }
                        break;
                    } else {
                        i--;
                        joueur.setX(i);
                        if (this.labyrinthe.getPlateau()[xdep][ydep] instanceof Libre) {// Case de départ
                            ((Libre) this.labyrinthe.getPlateau()[i + 1][ydep]).getJoueurs().remove(joueur);

                        }
                        if (this.labyrinthe.getPlateau()[i][j] instanceof Libre) {// Case d'arrivée
                            ((Libre) this.labyrinthe.getPlateau()[i][j]).getJoueurs().add(joueur);
                            // on regarde s'il y a des fantomes sur la nouvelle case et si
                            if(updateJoueur(joueur, i, j))croise=true;
                        }
                    }
                }
                d++;
            }
            return croise;
        }
    }

    // Deplacement vers le bas :
    public boolean downMove(Joueur joueur, int dist, boolean canBreak) throws IOException {
        boolean croise = false;
        synchronized (lockMovements) {
            int d = 0;
            int xdep = joueur.getX();
            int ydep = joueur.getY();
            int i = joueur.getX();
            int j = joueur.getY();
            // A chaque deplacement de 1, on vérifie si on croise des fantomes et on
            // actualise les points si necessaire
            while (d != dist) {
                if (i + 1 < labyrinthe.getNbLine() && i + 1 >= 0) {
                    if (labyrinthe.getPlateau()[i + 1][j] instanceof Mur) {
                        if (canBreak) {
                            joueur.setNbBreak(joueur.getNbBreak() - 1);
                            labyrinthe.getPlateau()[i + 1][j] = new Libre(i + 1, j);
                            ((Libre) labyrinthe.getPlateau()[i + 1][j]).getJoueurs().add(joueur);
                            ((Libre) labyrinthe.getPlateau()[i][j]).getJoueurs().remove(joueur);
                            joueur.setX(i + 1);
                            return true;
                        }
                        break;
                    } else {
                        i++;
                        joueur.setX(i);
                        if (this.labyrinthe.getPlateau()[xdep][ydep] instanceof Libre) {
                            ((Libre) this.labyrinthe.getPlateau()[i - 1][ydep]).getJoueurs().remove(joueur);

                        }
                        if (this.labyrinthe.getPlateau()[i][j] instanceof Libre) {
                            ((Libre) this.labyrinthe.getPlateau()[i][j]).getJoueurs().add(joueur);
                            if(updateJoueur(joueur, i, j))croise=true;
                        }
                    }
                }
                d++;
            }
            return croise;
        }
    }

    // Deplacement vers la gauche :
    public boolean leftMove(Joueur joueur, int dist, boolean canBreak) throws IOException {
        boolean croise = false;
        synchronized (lockMovements) {
            int d = 0;
            int xdep = joueur.getX();
            int ydep = joueur.getY();
            int i = joueur.getX();
            int j = joueur.getY();
            while (d != dist) {
                if (j - 1 < labyrinthe.getNbCol() && j - 1 >= 0) {
                    if (labyrinthe.getPlateau()[i][j - 1] instanceof Mur) {
                        if (canBreak) {
                            joueur.setNbBreak(joueur.getNbBreak() - 1);
                            labyrinthe.getPlateau()[i][j - 1] = new Libre(i, j - 1);
                            ((Libre) labyrinthe.getPlateau()[i][j - 1]).getJoueurs().add(joueur);
                            ((Libre) labyrinthe.getPlateau()[i][j]).getJoueurs().remove(joueur);
                            joueur.setY(j - 1);
                            return true;
                        }
                        break;
                    } else {
                        j--;
                        joueur.setY(j);
                        if (this.labyrinthe.getPlateau()[xdep][ydep] instanceof Libre) {
                            ((Libre) this.labyrinthe.getPlateau()[xdep][j + 1]).getJoueurs().remove(joueur);

                        }
                        if (this.labyrinthe.getPlateau()[i][j] instanceof Libre) {
                            ((Libre) this.labyrinthe.getPlateau()[i][j]).getJoueurs().add(joueur);
                            if(updateJoueur(joueur, i, j))croise=true;
                        }
                    }
                }
                d++;
            }
            return croise;
        }
    }

    // Deplacement vers la droite :
    public boolean rightMove(Joueur joueur, int dist, boolean canBreak) throws IOException {
        boolean croise = false;
        synchronized (lockMovements) {
            int d = 0;
            int xdep = joueur.getX();
            int ydep = joueur.getY();
            int i = joueur.getX();
            int j = joueur.getY();
            while (d != dist) {
                if (j + 1 < labyrinthe.getNbCol() && j + 1 >= 0) {
                    if (labyrinthe.getPlateau()[i][j + 1] instanceof Mur) {
                        if (canBreak) {
                            joueur.setNbBreak(joueur.getNbBreak() - 1);
                            labyrinthe.getPlateau()[i][j + 1] = new Libre(i, j + 1);
                            ((Libre) labyrinthe.getPlateau()[i][j + 1]).getJoueurs().add(joueur);
                            ((Libre) labyrinthe.getPlateau()[i][j]).getJoueurs().remove(joueur);
                            joueur.setY(j + 1);
                            return true;
                        }
                        break;
                    } else {
                        j++;
                        joueur.setY(j);
                        if (this.labyrinthe.getPlateau()[i][j - 1] instanceof Libre) {
                            if (this.labyrinthe.getPlateau()[xdep][ydep] instanceof Libre) {
                                ((Libre) this.labyrinthe.getPlateau()[xdep][j - 1]).getJoueurs().remove(joueur);
                            }
                            if (this.labyrinthe.getPlateau()[i][j] instanceof Libre) {
                                ((Libre) this.labyrinthe.getPlateau()[i][j]).getJoueurs().add(joueur);
                                if(updateJoueur(joueur, i, j))croise=true;
                            }
                        }
                    }
                }
                d++;
            }
            return croise;
        }
    }

    //Mise à jour éventuels des points du joueur en fonction des potentiels fantomes croisés
    private boolean updateJoueur(Joueur joueur, int i, int j) throws IOException {
        boolean croise = false;
        synchronized (this) {
            if (!((Libre) this.labyrinthe.getPlateau()[i][j]).getFantomes().isEmpty())
                croise = true;
            for (Fantome f : ((Libre) this.labyrinthe.getPlateau()[i][j]).getFantomes()) {
                joueur.setScore(joueur.getScore() + f.score);
                if (this.highjoueur == null || joueur.getScore() > this.highjoueur.getScore()) {
                    this.highjoueur = joueur;
                }
                // Supprimer le fantôme de la partie
                fantomes.remove(f);
                f.partie=null;
                ((Libre) this.labyrinthe.getPlateau()[i][j]).getFantomes().remove(f);
                // Multi-diffuser le message : SCORE id p x y+++
                byte[] message = String
                        .format("SCORE %s %s %s %s+++", joueur.getIdString(), adjustInteger(joueur.getScore(), 4),
                                adjustInteger(joueur.getX(), 3), adjustInteger(joueur.getY(), 3))
                        .getBytes();
                multicast(message);
                // Vérifier s'il reste encore des fantomes ?
                // Oui -> Alors continuer la partie
                // Non -> Arreter la partie
            }
            fantomes.removeAll(((Libre) this.labyrinthe.getPlateau()[i][j]).getFantomes());
            ((Libre) this.labyrinthe.getPlateau()[i][j]).getFantomes().clear();
            if (fantomes.isEmpty())this.end = true;           
        }
        viewGameState();
        return croise;
    }
    
    // Verifie si la partie peut démarrer:
    public boolean readyToStart() {
        for (Joueur j : this.joueurs) if (!j.getStart())return false;
        return true;
    }

    public void end() {
        if (this.end) {
            for (Thread f : fantomeThreads) f.interrupt();
            for (Joueur j : this.getJoueurs()) {
                try {
                    j.getOw().write("GOBYE***".getBytes());
                    j.getOw().flush();
                    j.getIs().close();
                    j.getOw().close();
                    j.getSocket().close();
                } catch (IOException e) {e.printStackTrace();}
            }
        }
    }

    // Envoi un message en multicast
    public boolean multicast(byte[] data) {
        try {
            InetSocketAddress ia = new InetSocketAddress(add_multi, port_multi);
            DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
            dso.send(paquet);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Renvoi la chaine de caracteres correspondant à x completer jusqua size avec
    // des 0
    public String adjustInteger(int x, int size) {
        StringBuilder number = new StringBuilder(String.valueOf(x));
        for (int i = number.length(); i < size; i++) {
            number.insert(0, '0');
        }
        return number.toString();
    }

    // Complete l'adresse ip avec des ### jusqu'a une taille 15
    public String adjustIP(String ip) {
        if (ip.length() < 15)
            for (int i = ip.length(); i < 15; i++)
                ip += "#";
        return ip;
    }

    // Gestion de la fin de partie :
    public void isItTheEnd() {
        if (this.end) {
            // Envoi du message de fin :
            byte[] fin = String.format("ENDGA %s %s+++", this.highjoueur.getIdString(),
                    adjustInteger(this.highjoueur.getScore(), 4)).getBytes();
            multicast(fin);
            // Interruption de tous les threads de fantomes :
            for (Fantome f : this.fantomes)f.partie = null;
            for (Joueur j : this.joueurs) {
                try {
                    j.send(j.getOw(), "GOBYE***");
                    j.getOw().close();
                    j.getIs().close();
                    j.getSocket().close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Server.parties.remove(this);
            Server.joueurs.removeAll(this.joueurs);
        }
    }

    /* GETTEURS ET SETTERS */

    public LinkedList<Joueur> getJoueurs() {
        return this.joueurs;
    }

    public void setLabyrinthe(Labyrinthe l) {
        this.labyrinthe = l;
    }

    public static int getPartie() {
        return nbPartie;
    }

    public int getIdPartie() {
        return this.id_partie;
    }

    public Labyrinthe getLabyrinthe() {
        return this.labyrinthe;
    }

    public int getNbHelp() {
        return nb_help;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean b) {
        this.state = b;
    }

    public boolean getEnd() {
        return this.end;
    }

    public void setEnd(boolean b) {
        this.end = b;
    }

    public List<Fantome> getFantomes() {
        return this.fantomes;
    }

    public List<Thread> getFantomeThreads() {
        return fantomeThreads;
    }

}
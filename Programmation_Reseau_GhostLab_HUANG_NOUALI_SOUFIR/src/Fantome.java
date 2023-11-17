
public abstract class Fantome implements Runnable {

    // Attributs de classe
    private static final Object lock = new Object();
    private static int count = 0;
    private Thread currentThread;

    // Attributs d'objets
    protected final int id;
    protected int x;
    protected int y;
    protected int score;
    protected  Partie partie;

    // Constructeur
    public Fantome(Partie partie, int x, int y, int score) {
        synchronized (lock) {
            this.id = count++;
            this.x = x;
            this.y = y;
            this.score = score;
            this.partie = partie;
            setCurrentThread(Thread.currentThread());
        }
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(Thread currentThread) {
        this.currentThread = currentThread;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Partie getPartie() {
        return partie;
    }

    // Setters
    public void setScore(int score) {
        this.score = score;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Fantome fantome = (Fantome) o;
        return this.id == fantome.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Fantome{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
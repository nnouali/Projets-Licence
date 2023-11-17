import java.util.Random;

public class Mimi extends Fantome{
	
    private final Random random;

	public Mimi(Partie partie, int x, int y) {
		super(partie, x, y, Constants.MIMI_SCORE);
		this.random = new Random();
	}

	@Override
	public void run() {
		while (partie!=null) {
            try {
                // Déplacer ou non le fantome ?
                if (random.nextBoolean()) {
                    // Dans quelle direction ?
                    // Tirer un nombre aléatoire entre 0 et 3
                    // Si n = 0 --> Aller en haut
                    // Si n = 1 --> Aller en bas
                    // Si n = 2 --> Aller à gauche
                    // Si n = 3 --> Aller à droite
                    int direction = random.nextInt(4); // [0, 4[
                    int distance = random.nextInt(1) + 1;
                    synchronized (partie) {
                    	switch (direction) {
                        case 0:
                            this.partie.upMove(this, distance);
                            break;
                        case 1:
                            this.partie.downMove(this, distance);
                            break;
                        case 2:
                            this.partie.leftMove(this, distance);
                            break;
                        case 3:
                            this.partie.rightMove(this, distance);
                            break;
                        default:
                    	}
					} 
                    if(partie==null)break;
                }
                Thread.sleep(Constants.MIMI_VITESSE);
            } catch (InterruptedException e) {
                break;
            }
        }
        this.x=-1;
		this.y =-1;
		
	}

}

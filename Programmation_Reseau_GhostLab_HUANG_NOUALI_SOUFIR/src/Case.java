import java.io.Serializable;

public abstract class Case implements Serializable{

    private final int line;
    private final int col;

    public Case(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

}

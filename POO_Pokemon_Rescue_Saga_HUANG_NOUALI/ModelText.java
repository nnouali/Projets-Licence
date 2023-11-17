public class ModelText {
    //CHAMPS
    private PlateauText p;
    private int[][] colorReculer;
    int point;

    //METHODES
    public boolean jeuGagne() {
        boolean animauxExist = false;
        animaux:
        for(int i = 0; i<p.color.length; i++) {
            for(int j = 0; j<p.color[i].length;j++) {
                if(p.color[i][j]==1||p.color[i][j]==8||p.color[i][j]==9){//il y a encore des animaux
                    animauxExist = true;
                    break animaux;
                }
            }
        }

        if(!animauxExist)
            return true;
        return false;

    }

    public boolean jeuPerdu() {
        boolean animauxExist = false;//verifier s'il y a encore des animaux
        animaux:
        for(int i = 0; i<p.color.length; i++) {
            for(int j = 0; j<p.color[i].length;j++) {
                if(p.color[i][j]==1||p.color[i][j]==8||p.color[i][j]==9) {
                    animauxExist = true;
                    break animaux;
                }
            }
        }

        boolean touteCaseSeule = true; // verifer si toutes les cases sont isoles
        caseSeule:
        for(int i = 0; i<p.color.length; i++) {
            for(int j = 0; j<p.color[i].length;j++) {
                if(p.color[i][j]== 7) continue; //sauter des cases fixes
                if(p.caseSeul(i, j)==false) {
                    touteCaseSeule = false;
                    break caseSeule;
                }
            }
        }

        if(animauxExist && touteCaseSeule)
            return true;
        return false;
    }

    public void newColorReculer() {
        colorReculer = new int[p.color.length][p.color[0].length];
    }

    public void memoriser() { //memoriser l'etape precedente
        for(int i = 0; i<p.color.length; i++) {
            for(int j = 0; j<p.color[i].length; j++) {
                colorReculer[i][j]=p.color[i][j];
            }
        }
    }

    public void reculer() {
        for(int i = 0; i<p.color.length; i++) {
            for(int j = 0; j<p.color[i].length; j++) {
                p.color[i][j]=colorReculer[i][j];
            }
        }
    }

    public void memoriserScore(int pt) {
        this.point = pt;
    }

    public void setPlateau(PlateauText p) {
        this.p = p;
    }

}

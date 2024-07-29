package aula07;

public class Bola extends Movel {
    
    private String cor;

    public Bola(String cor, int x, int y, double dist) {
        super(x, y, dist);
        this.cor = cor;
    }

    public String getCor() {
        return cor;
    }


    @Override
    public String toString() {
        return String.format("Distância percorrida pela bola: %.2f m", getDist());
    }

}

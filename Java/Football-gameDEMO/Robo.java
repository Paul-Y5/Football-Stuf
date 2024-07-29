package aula07;

public class Robo extends Movel {

    private String id;
    private String position;
    private int golos = 0;

    public Robo(String id, String position, int golos, int x, int y, double dist) {
        
        super(x, y, dist);
        this. id = id;
        this.position = position;
        this.golos = golos;
    }

    public String getId() {
        return this.id;
    }
    public String getPosition() {
        return this.position;
    }
    public int getGolos() {
        return this.golos;
    }

    

    
    public void setPosition(String position) {
        this.position = position;
    }

    public void setGolos(int golos) {
        this.golos = golos;
    }

    public int marcarGolo() {
        return ++ this.golos;
    }


    @Override
    public String toString() {
        return String.format("O jogador de id:%s percorreu %.2f m", getId(), getDist());
    }

}

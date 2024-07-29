package aula07;

public class Movel {

    private int x;
    private int y;
    private double dist = 0;

    public Movel(int x, int y, double dist) {
        this.x = x;
        this.y = y;
        this.dist = dist;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getDist() {
        return dist;
    }


    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDist(double d) {
        dist = d;
    }


    public void move(int newX, int newY) {
        this.x = newX;
        this.y = newY;

        this.dist += Math.sqrt(newX * newX + newY * newY);
    }

}

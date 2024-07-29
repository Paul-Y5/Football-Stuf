package aula07;

public class Ponto {
    private double x;
    private double y;

    public Ponto(double X, double Y) {
        x = X;
        y = Y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double X) {
        x = X;
    }
    public void setY(double Y) {
        y = Y;
    }
/*     @Override
    public String toString() {
        return String.format("Centro: (%.2f, %.2f)", getX(), getY());
    } */
}

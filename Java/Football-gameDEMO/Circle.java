package aula07;

public class Circle extends Forma {
    private double raio;
    private Ponto centro;

    public Circle(double r, Ponto c, String cor) {
        super(cor);
        raio = r;
        centro = c;
    }

    public double getRaio() {
        return raio;
    }
    public Ponto getCentro() {
        return centro;
    }

    public void setRaio(double raio) {
        this.raio = raio;
    }
    public void setCentro(Ponto centro) {
        this.centro = centro;
    }

    public double area() {
        return (2 * Math.PI * Math.pow(raio, 2));
    }

    public double perimetro() {
        return (2 * Math.PI * raio);
    }


    @Override
    public String toString() {
        return String.format("Circulo de raio = %.2f || Área = %.2f || Perímetro = %.2f || Cor: %s",
         getRaio(), area(), perimetro(), getCor());
    }

    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Circle other = (Circle) obj;

        return this.raio == other.raio && this.centro == other.centro && super.equals(obj);
	}

    @Override
    public int hashCode() {
        return Double.hashCode(raio);
    }
}

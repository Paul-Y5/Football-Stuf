package aula07;

import java.util.Objects;

public class Retangle extends Forma {
    private double c;
    private double a;

    public Retangle(double comprimento, double altura, String cor) {

        super(cor);

        c = comprimento;
        a = altura;
    }

    public double getComprimento() {
        return c;
    }
    public double getAltura() {
        return a;
    }

    public void setComprimento(double c) {
        this.c = c;
    } 
    public void setAltura(double c) {
        this.c = c;
    } 

    public double area() {
        return c * a;
    }

    public double perimetro() {
        return 2 * c + 2 * a;
    }

    @Override
    public String toString() {
        return String.format("Retângulo de comprimento = %.2f e altura = %.2f || Área = %.2f || Perímetro = %.2f || Cor: %s",
         getComprimento(), getAltura(), area(), perimetro(), getCor());
    }

    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Retangle other = (Retangle) obj;

        return this.c == other.c && this.a == other.a && super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, a);
    }
}

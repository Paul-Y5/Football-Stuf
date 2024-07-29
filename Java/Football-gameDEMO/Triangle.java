package aula07;

import java.util.Objects;

public class Triangle extends Forma {
    private double l1;
    private double l2;
    private double l3;

    public Triangle(double l1, double l2, double l3, String cor) {

        super(cor);

        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
    }

    public double getLado1() {
        return l1;
    } 
    public double getLado2() {
        return l2;
    } 
    public double getLado3() {
        return l3;
    } 

    public void setLado1(double l1) {
        this.l1 = l1;
    }
    public void setLado2(double l2) {
        this.l2 = l2;
    }
    public void setLado3(double l3) {
        this.l3 = l3;
    }

    public double perimetro() {
        return this.l1 + this.l2 + this.l3;
    }

    public double area() {
        double semiP = this.perimetro() / 2;
        return Math.sqrt(semiP * (semiP - this.l1) * (semiP - this.l2) * (semiP - this.l3));
    }

    @Override
    public String toString() {
        return String.format("Triângulo de lados: %.2f, %.2f e %.2f || Área = %.2f || Perímetro = %.2f || Cor: %s", 
        getLado1(), getLado2(), getLado3(), area(), perimetro(), getCor());
    }

    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Triangle other = (Triangle) obj;

        return super.equals(obj) && this.l1 == other.l1 && this.l2 == other.l2 && this.l3 == other.l3;
    }

    @Override
    public int hashCode() {
        return Objects.hash(l1, l2, l3);
    }

}

package aula07;

public abstract class Forma {

    private String cor;

    public Forma(String cor) {
        this.setCor(cor);
    }

    public abstract double area();

    public abstract double perimetro();


    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        if (cor.equals(null) || cor.length() == 0) {
            System.out.print("Cor inválida!");
        }
        this.cor = cor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) { 
            return false;
        }
        Forma form = (Forma) obj;

        return this.cor.equals(form.cor);
    }
}
package aula07;

public class Jogo {

    private int tempo = 90;
    private int tempoDecorrido = 0;
    private Bola bola;
    private Equipa equipa1;
    private Equipa equipa2;
 
    public Jogo(int tempo, int tempoDecorrido, Bola bola, Equipa equipa1, Equipa equipa2) {
        this.tempo = tempo;
        this.tempoDecorrido = tempoDecorrido;
        this.bola = bola;
        this.equipa1 = equipa1;
        this.equipa2 = equipa2;
    }

    public int getTempo() {
        return this.tempo;
    }

    public int getTempoDecorrido() {
        return this.tempoDecorrido;
    }

    public Bola getBola() {
        return this.bola;
    }

    public Equipa getEquipa1() {
        return this.equipa1;
    }

    public Equipa getEquipa2() {
        return this.equipa2;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public void setTempoDecorrido(int tempoDecorrido) {
        this.tempoDecorrido = tempoDecorrido;
    }



    protected int startTempo() {
        return this.tempoDecorrido ++;
    }

    @Override
    public String toString() {
        return String.format("| %s | VS | %s |'\n\n", equipa1.getNome(), equipa2.getNome());
    }

}

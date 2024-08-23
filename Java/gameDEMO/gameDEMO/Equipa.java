package gameDEMO;

import java.util.ArrayList;

public class Equipa {

    private String nome;
    private String nomeResponsavel;
    private int totalgolosM;
    private int totalgolosS;
    private ArrayList<Robo> jogadores = new ArrayList<>();

    public Equipa (String nome, String nr, int totgm, int totgs, ArrayList <Robo> jogadores) {

        this.nome = nome;
        nomeResponsavel = nr;
        totalgolosM = totgm;
        totalgolosS = totgs;
        this.jogadores = jogadores; 

    }

    public String getResponsavel() {
        return nomeResponsavel;
    }
    public String getNome() {
        return this.nome;
    }
    public int getTotGs() {
        return totalgolosS;
    }
    public int getTotGm() {
        return totalgolosM;
    }
    public ArrayList<Robo> getjogadores() {
        return this.jogadores;
    }


    public void setTotalgolosM(int totalgolosM) {
        this.totalgolosM = totalgolosM;
    }

    public void setTotalgolosS(int totalgolosS) {
        this.totalgolosS = totalgolosS;
    }


    public void addRobo(Robo jogador) {
        this.jogadores.add(jogador);
    }

    public void removeRobo(Robo jogador) {
        this.jogadores.remove(jogador);
    }

    protected Robo ChoosePlayear(String id) {
        for(Robo r : jogadores) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }
}

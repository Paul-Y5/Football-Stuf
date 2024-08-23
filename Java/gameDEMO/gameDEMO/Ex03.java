package gameDEMO;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* Assumindo que um campo de futebol tem 50m por 90m (coordenadas em função destas medidas // Exemplo: Meio campo: x:45 e y:25) */

public class Ex03 {

    private static ArrayList <Robo> lJogadores = new ArrayList<>();
    private static String[] positions = {"GR", "DF", "MD", "AV"};
    private static List<String> positionsL = Arrays.asList(positions);
    private static ArrayList <Equipa> lEquipas = new ArrayList<>();
    public static void main(String[] args) {
        
        
        System.out.println("===== Futebol de Robos =====");
        
        
        while (true) {
            Scanner sc = new Scanner(System.in);
            String op;
            
            System.out.print("1 - Criar equipa\n2 - Adicionar jogador a equipa\n3 - Remover jogador de equipa\n4 - Listar equipas\n5 - Listar jogadores\n6 - Jogo\n0 - Sair\n");
            System.out.print("Opção: ");
            op = sc.nextLine();
            switch (op) {
                case "1":
                    ArrayList <Robo> jogadores = new ArrayList<>();
                    System.out.print("Nome da equipa: ");
                    String nome = sc.nextLine();

                    System.out.print("Nome do responsável: ");
                    String nomeR = sc.nextLine();
                    
                    Equipa equipa = new Equipa(nome, nomeR, 0, 0, jogadores);
                    System.out.printf("Equipa %s adicionada com sucesso!\n", equipa.getNome());
                    lEquipas.add(equipa);
                    break;
                    
                case "2":
                    
                    int eq;
                    do {
                        System.out.print("Index da equipa: ");
                        eq = sc.nextInt();
                        if (eq > lEquipas.size() || eq < 0) {
                            System.out.println("Index inválido!");
                        }
                    }while(eq > lEquipas.size() || eq < 0);

                    System.out.print("Id do robo: ");
                    String id = sc.next();
                    
                    String posit;
                    do {
                        System.out.print("Posição (Guarda-Redes[GR], Defesa[DF], Médio[MD], Avançado[AV]): ");
                        posit = sc.next().toUpperCase();
                        if (!positionsL.contains(posit)) {
                            System.out.println("Posição impossível!");
                        }
                    }while(!positionsL.contains(posit));

                    int x = 0;
                    int y = 0;
                    switch (posit) {
                        case "GR":
                            do {
                                System.out.print("Posição ox: ");
                                x = sc.nextInt();

                                System.out.print("Posição oy: ");
                                y = sc.nextInt();

                            }while(x < 0 || x > 5 || y < 0 || 14 > y || y > 32); 
                            break;

                        case "DF":
                            do {
                                System.out.print("Posição ox: ");
                                x = sc.nextInt();

                                System.out.print("Posição oy: ");
                                y = sc.nextInt();

                            }while(x < 0 || x > 45 || 14 > y || y > 45); 
                            break;

                        case "MD":
                            do {
                                System.out.print("Posição ox: ");
                                x = sc.nextInt();

                                System.out.print("Posição oy: ");
                                y = sc.nextInt();

                            }while(x < 0 || x > 45 || y < 0 || y > 45); 
                            break;

                        case "AV":
                            do {
                                System.out.print("Posição ox: ");
                                x = sc.nextInt();

                                System.out.print("Posição oy: ");
                                y = sc.nextInt();

                            }while(x < 0 || x > 45 || y < 0 || y > 45); 
                            break;

                        default:
                            if (!positionsL.contains(posit.toUpperCase())) {
                                System.out.print("Posição impossívle!");
                            }
                            break;
                    }

                    Robo jogador = new Robo(id, posit, 0, x, y, 0);

                    lEquipas.get(eq).addRobo(jogador);
                    lJogadores.add(jogador);
                    System.out.printf("Robo com id:%s adicionado com sucesso!\n", jogador.getId());
                    break;
                
                case "3":
                    do {
                        System.out.print("Index da equipa: ");
                        eq = sc.nextInt();
                        if (eq > lEquipas.size() || eq < 0) {
                            System.out.println("Index inválido!");
                        }
                    }while(eq > lEquipas.size() || eq < 0);

                    Equipa equipaSelect = lEquipas.get(eq);

                    System.out.print("Lista de jogadores da equipa " + equipaSelect.getNome());
                    listJogadores(eq);

                    int ind;
                    do {
                        System.out.print("Qual o index do jogador que queres remover: ");
                        ind = sc.nextInt();
                        if (ind > equipaSelect.getjogadores().size() || ind < 0) {
                            System.out.println("Index inválido!");
                        }
                    }while(ind > equipaSelect.getjogadores().size() || ind < 0);

                    jogador = equipaSelect.getjogadores().get(ind);

                    equipaSelect.removeRobo(jogador);
                    System.out.printf("Robo com id:%s removido com sucesso!\n", jogador.getId());
                    break;

                case "4":
                    listEquipas();
                    break;

                case "5":
                    System.out.print("Queres ver os jogadores de uma equipa (0) ou todos os jogadores e estatísticas (1): ");
                    op = sc.next();
                    if (op.equals("0")) {
                        System.out.print("Index da equipa: ");
                        ind = sc.nextInt();
                        if (lEquipas.get(ind).getjogadores().size() == 0) {
                            System.out.printf("Equipa: %d sem jogadores!\n", lEquipas.get(ind).getNome());
                            break;
                        }
                        else {
                            System.out.println("|Jogadores do " + lEquipas.get(ind).getNome() + "|");
                            listJogadores(ind);
                        }
                    }
                    else {
                        if (lJogadores.size() == 0) {
                            System.out.println("Nenhum jogador registado!");
                            break;
                        }
                        else {
                            listAllJogadores();
                        }
                    }
                    break;

                case "6":
                    int t = 0;
                    int eq1;
                    int eq2;
                    System.out.println("Vamos escolher as equipas para o jogo!");

                    do {
                        System.out.print("Index da equipa: ");
                        eq1 = sc.nextInt();
                        if (eq1 > lEquipas.size() || eq1 < 0) {
                            System.out.println("Index inválido!");
                        }
                    }while(eq1 > lEquipas.size() || eq1 < 0);
                    
                    do {
                        System.out.print("Index da equipa: ");
                        eq2 = sc.nextInt();
                        if (eq2 > lEquipas.size() || eq2 < 0) {
                            System.out.println("Index inválido!");
                        }
                    }while(eq2 > lEquipas.size() || eq2 < 0);
                    
                    if (lEquipas.get(eq1).getjogadores().size() == 0 || lEquipas.get(eq1).getjogadores().size() < 3) {
                        System.out.printf("Equipa: %s sem jogadores suficientes! (min: 3)\n", lEquipas.get(eq1).getNome());
                        break;
                    }
                    if (lEquipas.get(eq2).getjogadores().size() == 0 || lEquipas.get(eq1).getjogadores().size() < 3) {
                        System.out.printf("Equipa: %s sem jogadores suficientes! (min: 3)\n", lEquipas.get(eq2).getNome());
                        break;
                    }

                    System.out.print("Cor da bola: ");
                    String cor = sc.next();
                    Bola bola = new Bola(cor, 45, 25, 0);

                    /* Posicionar jogadores de cada lado do campo */
                    inGameXY(eq2);

                    Jogo jogo = new Jogo(90, t, bola, lEquipas.get(eq1), lEquipas.get(eq2));
                    
                    int eqS;
                    do {
                        System.out.print("Equipa começa com bola (index): ");
                        eqS = sc.nextInt();
                        if (eqS != eq1 && eqS != eq2) {
                            System.out.print("Index de equipas não correspondem!");
                        }
                    }while(eqS != eq1 && eqS != eq2);
                    
                    do {
                        System.out.print("Jogador que começa com bola (id): ");
                        id = sc.next();
                        if (!scanJogadores(id, eqS)) {
                            System.out.println("Jogador não pertence à equipa!");
                        };
                    }while(!scanJogadores(id, eqS));
                    
                    Robo jogadorP = lEquipas.get(eqS).ChoosePlayear(id); /* Jogador no meio campo para dar o pontapé de saída */
                    jogadorP.setX(45);
                    jogadorP.setY(25);
                    
                    System.out.println(jogo.toString());
                    int golos1 = 0;
                    int golos2 = 0;
                    while (jogo.getTempoDecorrido() <= jogo.getTempo()) {
                        System.out.printf("| %s | %d - %d | %s | == %02d'\n\n", jogo.getEquipa1().getNome(), golos1, golos2, jogo.getEquipa2().getNome(), jogo.getTempoDecorrido());
                        jogo.startTempo();
                        System.out.println();
                        if (jogo.getTempoDecorrido() == 90) {
                            System.out.print("Final do jogo!\n");
                            break;
                        }

                        System.out.println("Opções de jogadas: ");
                        System.out.print("1 - Passe\n2 - Remate\n3 - Movimentação\n4 - Substituição(BETA)\n5 - Posição da bola\n6 - Alterar a posição da bola\n7 - Passar tempo\n");
                        
                        System.out.print("Opção: ");
                        op = sc.next();


                        switch (op) {
                            case "1":
                                System.out.print("Equipa indx: ");
                                ind = sc.nextInt();

                                System.out.print("ID do jogador: ");
                                id = sc.next();
                                Robo jogadorE = lEquipas.get(eq1).ChoosePlayear(id);

                                if (bola.getX() == jogadorE.getX() && bola.getY() == jogadorE.getY()) {
                                    System.out.println("Coordenadas para onde a bola vai");
                                    
                                    do {
                                        System.out.print("Coordenada x:");
                                        x = sc.nextInt();
                                    }while(x < 0 || x > 90);
                                    
                                    do {
                                        System.out.print("Coordenada y:");
                                        y = sc.nextInt();
                                    }while(y < 0 || y > 50);

                                    bola.setX(x);
                                    bola.setY(y);

                                    bola.move(x, y);
                                }
                                else {
                                    System.out.print("Jogador não tem a bola!");
                                    break;
                                }
                                System.out.println(bola.toString());
                                break;

                            case "2":
                                System.out.print("Equipa indx: ");
                                ind = sc.nextInt();

                                System.out.print("ID do jogador: ");
                                id = sc.next();
                                jogadorE = lEquipas.get(eq1).ChoosePlayear(id);

                                if (bola.getX() == jogadorE.getX() && bola.getY() == jogadorE.getY()) {
                                    System.out.println("Coordenadas para onde a bola vai");
                                    do {
                                        System.out.print("Coordenada x:");
                                        x = sc.nextInt();
                                    }while(x < 0 || x > 90); /* x = 0 para ser na baliza */
                                    
                                    do {
                                        System.out.print("Coordenada y:");
                                        y = sc.nextInt();
                                    }while(y < 0 || y > 50);

                                    bola.setX(x);
                                    bola.setY(y);

                                    bola.move(x, y);

                                    /* Gostava que quando o gurada redes tivesse na mesma posição y que a bola fosse defesa, mas não consegui fazer isso (pensei em colocar a obrigação de possuir um guarda redes em cada equipa) */
                                    if (bola.getX() == 0 && bola.getY() >= 20 && bola.getY() <= 27) {
                                        jogadorE.marcarGolo();
                                        System.out.printf("Gooooloooo %s para o %s\n", jogadorE.getId(), lEquipas.get(ind).getNome());
                                        /* Bola ao centro */
                                        bola.setX(45);
                                        bola.setY(25);
                                        /* Jogadores de volta ás mesmas posições ?? */
                                        for(Robo j: lEquipas.get(eq1).getjogadores()) {
                                            j.setX(30);
                                            j.setY(20);
                                        }
                                        for(Robo j: lEquipas.get(eq2).getjogadores()) {
                                            j.setX(30);
                                            j.setY(20);
                                        }

                                        /* Implementar uma probabilidade através de radom para ser golo ou não de acordo com a distância */
                                        if (ind == 0) {
                                            golos1 += 1;
                                        }
                                        else {
                                            golos2 += 1;
                                        }
                                    }
                                }
                                else {
                                    System.out.println("Falhou!");
                                }
                                break;

                            case "3":
                                System.out.print("Equipa indx: ");
                                ind = sc.nextInt();

                                System.out.print("ID do jogador: ");
                                id = sc.next();

                                jogadorE = lEquipas.get(ind).ChoosePlayear(id);

                                do {
                                    System.out.print("Coordenada x:");
                                    x = sc.nextInt();
                                }while(x < 0 || x > 90); /* x = 0 para ser na baliza */
                                    
                                do {
                                    System.out.print("Coordenada y:");
                                    y = sc.nextInt();
                                }while(y < 0 || y > 50);

                                jogadorE.setX(x);
                                jogadorE.setY(y);
                                jogadorE.move(x, y);

                                System.out.println(jogadorE.toString());
                                break;

                            case "4":
                                /* Não desenvolvido... */
                                System.out.print("Equipa indx: ");
                                ind = sc.nextInt();

                                System.out.print("ID do jogador: ");
                                id = sc.next();
                                jogadorE = lEquipas.get(eq1).ChoosePlayear(id);
                                break;

                            case "5":
                                System.out.printf("Posição da bola: |x: %d| |y:%d|\n", bola.getX(), bola.getY());
                                break;

                            case "6":
                                do {
                                    System.out.print("Coordenada x:");
                                    x = sc.nextInt();
                                }while(x < 0 || x > 90); /* x = 0 para ser na baliza */
                                    
                                do {
                                    System.out.print("Coordenada y:");
                                    y = sc.nextInt();
                                }while(y < 0 || y > 50);

                                bola.setX(x);
                                bola.setY(y);

                                System.out.printf("Nova posição da bola: |x: %d| |y:%d|\n", bola.getX(), bola.getY());
                                break;
                            case "7":
                                int tempoP = 0;
                                do {
                                    System.out.print("Quanto tempo passar: ");
                                    tempoP = sc.nextInt();
                                }while(tempoP > jogo.getTempo() - jogo.getTempoDecorrido() || tempoP < 0);

                                jogo.setTempoDecorrido(tempoP + jogo.getTempoDecorrido() - 1);
                                break;

                            default:
                                System.out.println("Ação impossívle!");
                                break;
                        }
                    }
                case "0":
                    System.out.println("Obrigado por jogar!");
                    System.exit(0);
                    sc.close();
                    break;
                default:
                    System.out.println("Opção inválida tente novamente!");
                    break;
            }
        }
    }

    public static void listJogadores(int indx) {
        System.out.printf("| %10s | %9s | %s |\n", "ID", "Posição", "Golos Mc");
        for (Robo r: lEquipas.get(indx).getjogadores()) {
            System.out.printf("| %10s | %9s | %8d |\n", r.getId(), r.getPosition(), r.getGolos());
        }
    }

    public static void listEquipas() {
        int i = 0;
        System.out.printf("| %10s | %10s | %15s | %s | %s |\n", "Id", "Nome", "Nome Respons", "Golos Mc", "Golos Sf");
        for (Equipa r: lEquipas) {
            System.out.printf("| %10s | %10s | %15s | %8d | %8d |\n", i, r.getNome(), r.getResponsavel(), r.getTotGm(), r.getTotGs());
            i++;
        }
    }

    public static void listAllJogadores() {
        System.out.printf("| %10s | %9s | %s |\n", "ID", "Posição", "Golos Mc");
        for (Robo r: lJogadores) {
            System.out.printf("| %10s | %9s | %8d |\n", r.getId(), r.getPosition(), r.getGolos());
        }
    }

    public static void inGameXY(int eq2) {
        for (Robo  r : lEquipas.get(eq2).getjogadores()) {
            r.setX((45- r.getX()) + 45);
        }   
    }


    public static boolean scanJogadores(String id, int indx) {
        for (Robo r : lEquipas.get(indx).getjogadores()) {
            if (r.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static double calcularProbabilidade(double distancia) { /* Tirado da internet */
        double intercepto = 100; // Probabilidade máxima em metros
        double inclinacao = -0.5; // Taxa de queda da probabilidade por metro
        
        // Calcular a probabilidade de acordo com a função linear: probabilidade = intercepto + inclinação * distância
        double probabilidade = intercepto + inclinacao * distancia;
        
        // Garantir que a probabilidade esteja dentro do intervalo [0, 100]
        probabilidade = Math.max(0, Math.min(100, probabilidade));
        
        return probabilidade;
    }

}

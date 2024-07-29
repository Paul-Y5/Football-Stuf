package aula07;

import java.util.ArrayList;
import java.util.Scanner;
import utils.UserInput;

public class Ex01 {

    private static ArrayList<Forma> figuras = new ArrayList<>();
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean sair = false;
        while (!sair) {
            String resposta;
            System.out.println("====================");
            System.out.println("0 - Criar triângulo\n1 - Criar círculo\n2 - Criar retângulo\n3 - Listar figuras\n4 - Modificar figura\n5 - Comparar figuras\n6 - Remover Figura\n7 - Hash Code\n8 - Sair");
            System.out.println("====================");
            
            System.out.print("Opção: ");
            resposta = sc.next();
            System.out.println("");
            
            switch (resposta) {
                case "0":
                    System.out.println("===Triângulo===");
                    double l1, l2, l3;
                    System.out.print("Lado 1: ");
                    l1 = UserInput.validatenum(sc);
                    
                    System.out.print("Lado 3: ");
                    l2 = UserInput.validatenum(sc);
                    
                    System.out.print("Lado 2: ");
                    l3 = UserInput.validatenum(sc);

                    System.out.print("Cor:");
                    String cor = sc.next();
                    
                    if (l1 < l2 + l3 && l2 < l1 + l3 && l3 < l1 + l2) {
                        Triangle t = new Triangle(l1, l2, l3, cor);
                        addFigura(t);
                        System.out.println(t);
                    }
                    else{
                        System.out.println("Triângulo Impossível!");
                    }
                    break;
                    
                case "1":
                    System.out.println("===Circulo===");
                    double x, y, raio;
                    
                    System.out.print("Raio da circunferência: ");
                    raio = UserInput.validatenum(sc);
                    
                    System.out.print("Cor:");
                    cor = sc.next();
                    
                    System.out.print("X do centro: ");
                    x = UserInput.validatenum(sc);
                    
                    System.out.print("Y do centro: ");
                    y = UserInput.validatenum(sc);
                    
                    Ponto centro = new Ponto(x, y);
                    Circle c = new Circle(raio, centro, cor);
                    addFigura(c);
                    System.out.println(c);
                    break;
                    
                case "2":
                    System.out.println("===Retângulo===");
                    double comprimento, altura;
                    
                    System.out.print("Comprimento: ");
                    comprimento = UserInput.validatenum(sc);
                    
                    System.out.print("Altura: ");
                    altura = UserInput.validatenum(sc);
                    
                    System.out.print("Cor:");
                    cor = sc.next();
                    
                    Retangle r = new Retangle(comprimento, altura, cor);
                    addFigura(r); 
                    System.out.println(r);
                    break;

                case "3":
                    System.out.println("Figuras registadas:");
                    if (figuras.size() == 0) {
                        System.out.println("Nenhuma figura resgistada!");
                    }
                    else {
                        listfiguras();
                    }
                    break;

                case "4":
                    Object figura = getForma("Insira o indíce da figura: ", sc);
                    
                    try {
                        if (figura instanceof Circle) {
                            Circle círculo = ((Circle) figura);
                            Ponto centr = ((Ponto) figura);
                            
                            System.out.print("Novo raio: ");
                            raio = UserInput.validatenum(sc);
                            
                            System.out.print("Nova coordenada X:");
                            x = UserInput.validatenum(sc);
                            
                            System.out.print("Nova coordenada Y:");
                            y = UserInput.validatenum(sc);
                            
                            círculo.setRaio(raio);
                            centr.setX(x);
                            centr.setY(y);

                        } else if (figura instanceof Triangle) {
                            Triangle triângulo = ((Triangle) figura);

                            System.out.print("Novo lado1: ");
                            l1 = UserInput.validatenum(sc);

                            System.out.print("Novo lado2: ");
                            l2 = UserInput.validatenum(sc);

                            System.out.print("Novo lado3: ");
                            l3 = UserInput.validatenum(sc);

                            if (l1 < l2 + l3 && l2 < l1 + l3 && l3 < l1 + l2) {

                                triângulo.setLado1(l1);
                                triângulo.setLado2(l2);
                                triângulo.setLado3(l3);
                            }
                            else{
                                System.out.println("Triângulo Impossível!");
                            }

                        } else if (figura instanceof Retangle) {
                            Retangle retângulo = ((Retangle) figura);

                            System.out.print("Novo comprimento: ");
                            comprimento = UserInput.validatenum(sc);

                            System.out.print("Nova Altura: ");
                            altura = UserInput.validatenum(sc);

                            retângulo.setComprimento(comprimento);
                            retângulo.setAltura(altura);
                        }
                    } catch (AssertionError e) {
                        System.out.println("Valores inválidos");
                    }
                    break;

                case "5":
                    Object figura1 = getForma("Insira o indíce da figura: ", sc);
                    Object figura2 = getForma("Insira o indíce da figura: ", sc);
                    
                    if (figura1.equals(figura2)) {
                        System.out.println("Figuras iguais!");
                    }
                    else {
                        System.out.println("Figuras diferentes!");
                    }
                    break;

                case "6":
                    int ind;
                    do {
                        System.out.print("Index da figura: ");
                        ind = sc.nextInt();
                        if (ind > figuras.size() - 1 || ind < 0) {
                            System.out.println("Index impossível");
                        }
                    }while(ind > figuras.size() - 1 || ind < 0);

                    System.out.printf("Figura %s removida com sucesso!\n", figuras.get(ind));
                    figuras.remove(ind);
                    break;

                case "7":
                    do {
                        System.out.print("Index da figura: ");
                        ind = sc.nextInt();
                        if (ind > figuras.size() - 1 || ind < 0) {
                            System.out.println("Index impossível");
                        }
                    }while(ind > figuras.size() - 1 || ind < 0);

                    System.out.printf("Hash Code da figura %s é %s\n", figuras.get(ind), figuras.get(ind).hashCode());
                    break;

                case "8":
                    sair = true;
                    break;
                    
                default:
                    System.out.println("Opção inválida!");
                    break;
                }
            }
            
            sc.close();
        }
        
        private static Forma getForma(String prompt, Scanner s) {
            int index;
            do {
                System.out.print(prompt);
                index = s.nextInt();
            }while(index < 0 || index > figuras.size() - 1);

            return figuras.get(index);
        }

        private static void addFigura(Forma o) {
            int index = figuras.size();
            figuras.add(o);
            System.out.printf("Figura inserida no indíce %d\n", index);
        }

        private static void listfiguras() {
            for (Object f: figuras) {
                System.out.println(f);
            }
        }
}

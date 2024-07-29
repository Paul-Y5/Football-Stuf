package aula07;

import java.util.Scanner;

public class Ex02 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DateYMD date = new DateYMD(5, 1, 2005);

        String op;
        
        do {
            System.out.println("1 - Create new date\n2 - Show current date\n3 - Increment date\n4 - Decrement date\n5 - DateND\n0 - exit");
            System.out.print("Opção: ");
            op = sc.next();
            switch (op) {
                case "0":
                    System.exit(0); 
                    break;

                case "1":
                    System.out.print("Ano: ");
                    int year = sc.nextInt();

                    System.out.print("Mês: ");
                    int month = sc.nextInt();

                    System.out.print("Dia: ");
                    int day = sc.nextInt();

                    if (DateYMD.valid(day, month, year)) {
                        date = new DateYMD(year, month, day);
                        System.out.println("Data criada!");
                        break;   
                    }
                    else {
                        System.out.println("Data impossível!...Tente novamente");
                        break;
                    }
                case "2":
                    System.out.println("Data atual: " + date.toString());
                    break;

                case "3":
                    int dn;
                    System.out.print("Quantos dias queres incrementar: ");
                    dn = sc.nextInt();
                    date.increment(dn, date.getDay(), date.getMonth(), date.getYear());
                    System.out.println("Nova data: " + date.toString());
                    break;

                case "4":
                    System.out.print("Quantos dias queres decrementar: ");
                    dn = sc.nextInt();
                    date.decrement(dn, date.getDay(), date.getMonth(), date.getYear());
                    System.out.println("Nova data: " + date.toString());
                    break;

                case "5":
                    System.out.println("Contagem de dias de distância da data (01/01/2000)");
                    System.out.print("Ano: ");
                    year = sc.nextInt();

                    System.out.print("Mês: ");
                    month = sc.nextInt();

                    System.out.print("Dia: ");
                    day = sc.nextInt();

                    DateND dist = new DateND(day, month, year, new DateYMD(1, 1, 2000));
                    System.out.println(dist.toString());
                    break;

                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        }while(!op.equals("0"));
        sc.close();
    }
}

package aula07;

import java.util.Objects;

public abstract class Date implements Comparable<Date> {

    
    protected abstract int getYear();


    protected abstract int getMonth();


    protected abstract int getDay();
    

    public static boolean validMonth(int month) {
        if (month < 0 || month > 12) {
            return false;
        }
        else {
            return true;
        }
    }


    public static int monthDays(int month, int year) {
        int dias = 0;
        if (validMonth(month)) {
            switch(month) {
                case 4: case 6: case 9: case 11:
                    dias = 30;
                    break;
                case 2:
                    if (leapYear(year)) {
                        dias = 29;
                    }
                    else {
                        dias = 28;
                    }
                    break;
                default:
                    dias = 31;
                    break;
            }
        }
        return dias;
    }

    public static boolean leapYear(int year) {
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
    }

    public static boolean valid(int day, int month, int year) {
        if (year < 0) {
            System.out.println("Ano impossível!");
            return false;
        }
        if (month < 0 || month > 12) {
            System.out.print("Mês impossível!");
            return false;
        }
        if(day > monthDays(month, year)) {
            System.out.println("O mês " + month + " não tem " + day + " dias.");
            return false;
        }
        return true;
    }

    public void increment(int n, int day, int month, int year) {
        day += n;

        if (day > monthDays(month, year)) {

            do {
                day -= monthDays(month, year);
                month += 1;

                if (month > 12) {
                    month = 1;
                    year += 1;
                }
            }while(day > monthDays(month, year));
        }
    }

    public void decrement(int n, int day, int month, int year) {
        day -= n;

        if (day < 1) {
            do {
                month -= 1;
                day += monthDays(month, year);

                if (month < 1) {
                    year -= 1;
                    month = 12;
                }
            }while(day < 1);
        }
    }

    public static int numDays(int day, int month, int year, DateYMD date) {
        int monthRef = date.getMonth();
        int yearRef = date.getYear();
        
        int numDays = 0;
        int diffY = year - yearRef;

        if (diffY < 0 ) {
            for (int i = 0; year+1 + i <yearRef; i++) {
                if (leapYear(year)) {
                    numDays += 366;
                }
                else {
                    numDays += 365;
                }
            }
            for (int i = month+1; i >= 12; i++) {
                numDays += monthDays(month, year);
            }
            numDays += (day + 1);
        }
        if (diffY > 0) {
            for (int i = 0; year-1 - i >= yearRef; i++) {
                if (leapYear(year)) {
                    numDays += 366;
                }
                else {
                    numDays += 365;
                }
            }
            for (int i = month-1; i > 0; i--) {
                numDays += monthDays(month, year);
            }
            numDays += (day + 1);
        }
        
        
        if (diffY == 0) {
            for (int i = 0; month - i > monthRef; i++) {
                System.out.print(month - i);
                numDays += monthDays(month - i, yearRef);
            }
            if (day == 1) {
                return numDays;
            }
            else {
                numDays += (day - 1);
            }
        }
        return numDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Date date = (Date) o;
        return this.getDay() == date.getDay() && this.getMonth() == date.getMonth() && this.getYear() == date.getYear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getDay(), this.getMonth(), this.getYear());
    }

    @Override
    public int compareTo(Date o) {
        if (this.getYear() > o.getYear())
            return 1;
        if (this.getYear() < o.getYear())
            return -1;

        if (this.getMonth() > o.getMonth())
            return 1;
        if (this.getMonth() < o.getMonth())
            return -1;

        if (this.getDay() > o.getDay())
            return 1;
        if (this.getDay() < o.getDay())
            return -1;

        return 0;
    }


}

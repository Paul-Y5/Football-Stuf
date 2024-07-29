package aula07;

public class DateND extends Date {
    private int year;
    private int month;
    int day;
    private DateYMD date;
    
    public DateND(int day, int month, int year, DateYMD date) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.date = date;
    }

    public int getDay() {
        return day;
    }
    public int getMonth() {
        return month;
    }
    public int getYear() {
        return year;
    }

    public void setDay(int d) {
        day = d;
    }
    public void setMonth(int m) {
        month = m;
    }
    public void setYear(int y) {
        year = y;
    }


    @Override
    public String toString() {
        return String.format("Distância em dias a 01/01/2000: %d dias", numDays(day, month, year, date));
    }

}

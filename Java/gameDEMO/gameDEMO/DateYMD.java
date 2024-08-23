package gameDEMO;

public class DateYMD extends Date {
    private int year;
    private int month;
    private int day;

    public DateYMD(int d, int m, int y){
        year = y;
        month = m;
        day = d;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int y) {
        year = y;
    }
    public int getMonth() {
        return month;
    }

    public void setMonth(int m) {
        month = m;
    }
    public int getDay() {
        return day;
    }

    public void setDay(int d) {
        day = d;
    }

    @Override
    public String toString() {
        return String.format("%02d-%02d-%04d", day, month, year);
    }

}

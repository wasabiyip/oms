package oms.Grafica;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Clase que maneja fechas.
 *
 * @author Ivan - hermano de cesar.
 */
public class Date {

    private long time = 0;
    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;
    private int millis = 0;

    public Date(long time) {
        setTime(time);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(time);
        year = gc.get(Calendar.YEAR) < 1970 ? gc.get(Calendar.YEAR) + 1900 : gc.get(Calendar.YEAR);
        month = gc.get(Calendar.MONTH) + 1;
        hour = gc.get(Calendar.HOUR_OF_DAY);
        day = gc.get(Calendar.DAY_OF_MONTH);
        minute = gc.get(Calendar.MINUTE);
        second = gc.get(Calendar.SECOND);
        millis = gc.get(Calendar.MILLISECOND);
    }

    public Date() {
        setTime(new java.util.Date().getTime());
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(time);

        year = gc.get(Calendar.YEAR);
        month = gc.get(Calendar.MONTH);
        day = gc.get(Calendar.DAY_OF_MONTH);
        hour = gc.get(Calendar.HOUR_OF_DAY);
        minute = gc.get(Calendar.MINUTE);
        second = gc.get(Calendar.SECOND);
        millis = gc.get(Calendar.MILLISECOND);
    }

    public Date(int year, int month, int day) {
        this(year, month, day, 0, 0, 0);
    }

    public Date(int year, int month, int day, int hour, int minute, int second) {
        this(year, month, day, hour, minute, second, 0);
    }

    public Date(int year, int month, int day, int hour, int minute, int second, int millis) {
        setYear(year);
        setMonth(month);
        setDay(day);
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setMillis(millis);
    }

    // Epoch 1 Jan 1970 00H00M00S000MS
    private void calculateTime() {
        long time = millis;
        // Substract one to the month to specify 0-based month in GregorianCalendar
        GregorianCalendar gc = new GregorianCalendar(year, month - 1, day, hour, minute, second);
        time += gc.getTimeInMillis();
        System.out.println(gc.getTime());
        setTime(time);
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * @param day the day to set
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * @param hour the hour to set
     */
    public void setHour(int hour) {
        this.hour = hour;
    }

    /**
     * @return the minute
     */
    public int getMinute() {
        return minute;
    }

    /**
     * @param minute the minute to set
     */
    public void setMinute(int minute) {
        this.minute = minute;
    }

    /**
     * @return the second
     */
    public int getSecond() {
        return second;
    }

    /**
     * @param second the second to set
     */
    public void setSecond(int second) {
        this.second = second;
    }

    /**
     * @return the millis
     */
    public int getMillis() {
        return millis;
    }

    /**
     * @param millis the millis to set
     */
    public void setMillis(int millis) {
        this.millis = millis;
    }

    public String getDate() {
        StringBuffer sb = new StringBuffer();
        sb.append(getYear());
        sb.append("-");
        sb.append(getMonth());
        sb.append("-");
        sb.append(getDay());
        return sb.toString();
    }

    public String getDateTime(boolean millis) {
        StringBuffer sb = new StringBuffer();
        sb.append(getYear());
        sb.append("-");
        sb.append(getMonth());
        sb.append("-");
        sb.append(getDay());
        sb.append(" ");
        sb.append(getHour());
        sb.append(":");
        sb.append(getMinute());
        sb.append(":");
        sb.append(getSecond());
        if (millis) {
            sb.append(".");
            sb.append(getMillis());
        }
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Date)) {
            return false;
        }
        Date date = (Date) obj;
        if (getYear() == date.getYear() && getMonth() == date.getMonth() && getDay() == date.getDay()
                && getHour() == date.getHour() && getMinute() == date.getMinute() && getSecond() == date.getSecond()
                && getMillis() == date.getMillis()) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getYear());
        sb.append("-");
        sb.append(getMonth());
        sb.append("-");
        sb.append(getDay());
        sb.append(" ");
        sb.append(getHour());
        sb.append(":");
        sb.append(getMinute());
        sb.append(":");
        sb.append(getSecond());
        sb.append(".");
        sb.append(getMillis());
        return sb.toString();
    }
    public int getDayWeek(){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime().getDay();
    }
}

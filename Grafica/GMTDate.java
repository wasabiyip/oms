package oms.Grafica;

import java.text.*;
import java.util.*;

public class GMTDate {

    private static Date date;
    static int ajuste = 8;

    public static Date getDate() {

        SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm");
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        date = new Date();
        //return - Tiempo actual en GMT        
        return date;
    }

    public static Integer getTime() {

        int hora = getDate().getHour() + ajuste >= 24 ? (getDate().getHour() + ajuste) - 24 : getDate().getHour() + ajuste;
        int min = getDate().getMinute();
        int seg = getDate().getSecond();
        //String time = hora +""+ min +""+ ( (seg<9)? ("0"+seg):seg);
        String time = hora + "" + ((min < 9) ? ("0" + min) : min);
        return (new Integer(time));
    }

    public static Double getHora() {
        int hora = getDate().getHour() + ajuste >= 24 ? (getDate().getHour() + ajuste) - 24 : getDate().getHour() + ajuste;
        int min = getDate().getMinute();
        int seg = getDate().getSecond();
        //String time = hora +""+ min +""+ ( (seg<9)? ("0"+seg):seg);
        String time = hora + "." + ((min < 9) ? ("0" + min) : min);
        return (new Double(time));
    }

    /**
     * Calculamos la diferencia (mod) de un periodo respecto a la hora actual,
     * normalmente usariamos % pero al usar peridos que no sean 5 min el mod no
     * funciona como esperariamos.
     *
     * @param periodo
     * @return
     */
    public static Integer getMod(int periodo) {
        return getDate().getMinute() - (periodo * (getDate().getMinute() / periodo));
    }

    public static Integer getTimeFin(int i) {

        int hora = getDate().getHour();
        int min = getDate().getMinute() + i;
        int seg = getDate().getSecond();
        //String time = hora +""+ min +""+ ( (seg<9)? ("0"+seg):seg);
        String time = hora + "" + ((min < 9) ? ("0" + (min)) : min);
        return (new Integer(time));
    }
}
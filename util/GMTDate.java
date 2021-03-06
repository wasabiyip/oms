package oms.util;

import java.text.*;
import java.util.*;

public class GMTDate {

    private static Date date;

    public static Date getDate() {

        SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        date = new Date();

        //return - Tiempo actual en GMT        
        return date;
    }

    public static Integer getTime() {

        int hora = getDate().getHour();
        int min = getDate().getMinute();
        int seg = getDate().getSecond();
        //String time = hora +""+ min +""+ ( (seg<9)? ("0"+seg):seg);
        String time = hora + "" + ((min < 9) ? ("0" + min) : min);
        return (new Integer(time));
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
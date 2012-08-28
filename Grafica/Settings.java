package oms.Grafica;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author omar
 */
public abstract class Settings {

    public Integer MAGICMA = 0;
    public Integer boll1;
    public Integer boll2;
    public Integer boll3;
    public Integer bollS1;
    public Integer bollS2;
    public Integer bollS3;
    public Double boll_special;
    public Double spread;
    public Double lots;
    public Integer tp;
    public Integer sl;
    public double Point;
    public Integer limiteMagic = 0;
    public Integer limiteCruce = 0;
    public Integer spreadSalida = 0;
    public Integer velasS = 0;
    public Double horaIni = 0.0;
    public Double horaFin = 0.0;
    public Double horaIniS = 0.0;
    public Double horaFinS = 0.0;
    public String symbol;
    public boolean salidaVelas;
    public boolean salidaBollinger;
    public boolean salidaMin;
    public Integer spreadAsk;
    private Integer temp;
    
    
    public Settings(String symbol) {
        this.symbol = symbol;
        /**
         * Todas las monedas tienen el mismo Point excepto el Yen.
         */
        if(symbol.equals("USD/JPY"))
            this.Point = 0.001;
        else this.Point = 0.00001;
        
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("config/Estrategias/" + Graphic.unSlash(this.symbol) + ".set"));

            MAGICMA = new Integer(config.getProperty("MAGICMA"));
            boll1 = new Integer(config.getProperty("periodoBoll"));
            boll2 = new Integer(config.getProperty("periodoBoll2"));
            boll3 = new Integer(config.getProperty("periodoBoll3"));
            bollS1 = new Integer(config.getProperty("periodoBollsalida"));
            bollS2 = new Integer(config.getProperty("periodoBollsalida2"));
            bollS3 = new Integer(config.getProperty("periodoBollsalida3"));
            boll_special = new Double(config.getProperty("bollspecial"));
            spread = new Double(config.getProperty("spread"));
            lots = new Double(config.getProperty("Lots"));
            tp = new Integer(config.getProperty("tp"));
            sl = new Integer(config.getProperty("sl"));
            //velasS = new Integer(config.getProperty("numvelasS"));
            horaIni = new Double(config.getProperty("horainicial"));
            horaFin = new Double(config.getProperty("horafinal"));
            horaIniS = new Double(config.getProperty("timesalidainicial"));
            horaFinS = new Double(config.getProperty("timesalidafinal"));
            limiteCruce = new Integer(config.getProperty("limiteCruce"));
            //Hacemos esto por que las variables booleanas esperan leer desde el archivo
            // un true o un false y nosotros tenemos un 0 o un 1, y por eso tenemos
            // leerlo como un entero y despues asiganar el valor true o false.
            velasS = new Integer (config.getProperty("numvelasE"));
            if (velasS == 0)
                this.salidaVelas = false;
            else
                this.salidaVelas = true;
            temp = new Integer(config.getProperty("Salida"));
            if (temp==0)
                this.salidaBollinger = false;
            else
                this.salidaBollinger = true;
            //Lo mismo aca
            temp = new Integer(config.getProperty("SalidaHora"));
            if (temp==0)
                this.salidaMin = false;
            else
                this.salidaMin = true;
            spreadSalida = new Integer(config.getProperty("spread_salida"));
            spreadAsk = new Integer(config.getProperty("spread_ask"));
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

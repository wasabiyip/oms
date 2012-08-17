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
    public double Point = 0.0001;
    public Integer limiteMagic = 0;
    public Integer limiteCruce = 0;
    public Integer spreadSalida = 0;
    public Integer velasS = 0;
    public Double horaIni = 0.0;
    public Double horaFin = 0.0;
    public String symbol;
    public boolean salidaHora;
    public boolean salidaBollinger;

    public Settings(String symbol) {
        this.symbol = symbol;
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
            limiteCruce = new Integer(config.getProperty("limiteCruce"));
            salidaHora = new Boolean(config.getProperty("SalidaHora"));
            salidaBollinger = new Boolean(config.getProperty("Salida"));
            spreadSalida = new Integer(config.getProperty("spread_salida"));
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

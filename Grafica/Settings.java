package oms.Grafica;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author omar
 */
public class Settings {

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
    public Double tp;
    public double sl;
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
    //public boolean salidaVelas;
    public boolean salidaBollinger;
    public boolean salidaMin;
    public Integer spreadAsk;
    public String id;
    public Integer bollx1;
    public Integer bollx2;
    public Integer bollx3;
    public Integer periodo;
    public double bollxUp;
    public double bollxDn;
    public boolean volatilidad;
    public double volVal;
    public double nwTp;
    private Pattern p = Pattern.compile("(USDJPY|EURUSD|GBPUSD|USDCHF|EURGBP)");
    private final int  CUSTOM_HORA = 16; 
    public Settings(Properties config) {
        
        Matcher m = p.matcher(config.getProperty("symbol"));
        if(m.find()){
            this.symbol = m.group();
        }
        /**
         * Todas las monedas tienen el mismo Point excepto el Yen.
         */
        if(symbol.equals("USDJPY"))
            this.Point = 0.001;
        else this.Point = 0.00001;
                  
        this.periodo = new Integer(config.getProperty("period"));
        MAGICMA = new Integer(config.getProperty("MAGICMA"));
        boll1 = new Integer(config.getProperty("periodoBoll"));
        boll2 = new Integer(config.getProperty("periodoBoll2"));
        boll3 = new Integer(config.getProperty("periodoBoll3"));
        bollS1 = new Integer(config.getProperty("periodoBollsalida"));
        bollS2 = new Integer(config.getProperty("periodoBollsalida2"));
        bollS3 = new Integer(config.getProperty("periodoBollsalida3"));
        boll_special = new Double(config.getProperty("bollspecial"));
        System.out.println(MAGICMA);
        spread = new Double(config.getProperty("spread"));
        lots = new Double(config.getProperty("Lots"));
        tp = new Integer(config.getProperty("tp")) * this.Point;
        sl = new Integer(config.getProperty("sl")) * this.Point;
        horaIni = new Double(config.getProperty("horainicial"))+this.CUSTOM_HORA;
        horaFin = new Double(config.getProperty("horafinal"))+this.CUSTOM_HORA;
        horaIniS = new Double(config.getProperty("timesalidainicial"));
        horaFinS = new Double(config.getProperty("timesalidafinal"));
        limiteCruce = new Integer(config.getProperty("limiteCruce"));
        bollx1 = new Integer(config.getProperty("XBoll"));
        bollx2 = new Integer(config.getProperty("XBoll2"));
        bollx3 = new Integer(config.getProperty("XBoll3"));
        bollxUp = new Double(config.getProperty("BollxUp"));
        bollxDn = new Double(config.getProperty("BollxDn"));
        //Arreglar esta charrada
        bollxUp = 1000;
        bollxDn =0;
        volVal = new Double(config.getProperty("volatibidad"))* this.Point;
        velasS = new Integer (config.getProperty("num_velas_salida"));
        nwTp = new Double(config.getProperty("nwTP"))*this.Point;
        //Hacemos esto por que las variables booleanas esperan leer desde el archivo
        // un true o un false y nosotros tenemos un 0 o un 1, y por eso debemos
        // leerlo como un entero y despues asiganar el valor true o false.
        // Ternario -> <3
        //this.salidaVelas = (velasS == 0) ? false : true;
        this.salidaBollinger = (new Integer(config.getProperty("Salida")) == 0) ? false : true;
        this.salidaMin = (new Integer(config.getProperty("SalidaHora")) == 0) ? false : true;
        this.volatilidad = (new Integer(config.getProperty("Volatibidad")) == 0) ? false : true;
        ///-----------------------------------------------------------------///
        spreadSalida = new Integer(config.getProperty("spread_salida"));
        spreadAsk = new Integer(config.getProperty("spread_ask"));
        this.id = config.getProperty("grafid");
        //Si el archivo log no cuenta con un orden id generamos uno para el.
        if(this.id ==null){
            try {
                this.id = genId(symbol);
                config.setProperty("grafid", id);
                String path = config.getProperty("path");
                config.store(new FileOutputStream(path),null);
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        horaIni = horaIni>24? horaIni-24:horaIni;
        horaFin = horaFin>24? horaFin-24:horaFin;
        /*horaIni = 0.0;
        horaFin =24.0;*/
        
    }
    
    /**
     * Este método genera un id único para construir cada gráfica.
     *
     * @return id único.
     */
    private String genId(String id) {
        //TODO buscar donde poner esta cosa, o talvez no...
        StringBuffer str = new StringBuffer(id + "-");
        Random r = new Random();
        Random r2 = new Random();
        int n = 25; // 65-90 codigo ASCII.


        for (int j = 0; j < 4; j++) {
            int i = r.nextInt() % n;
            if ((65 + i <= 90) && (65 + i >= 65)) {
                str.append((char) (65 + i));
            } else {
                j--;//<---------|| ¡CUIDADO ESTO ES UNA CHARRADA!
            }
        }
        return str.toString();
    }
    public static String Slash(String symbol) {
        StringBuffer str = new StringBuffer(symbol.length());
        str.append(symbol.substring(0, 3)).append("/").append(symbol.substring(3));
        return str.toString();
    }
}

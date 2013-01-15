package oms.deliverer;

import CustomException.NoProfileFoundException;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.Graphic;
import oms.util.Console;
import quickfix.DoubleField;
import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;
import java.util.Properties;
import oms.CustomException.GraficaNotFound;


/**
 * Esta clase es a la cuál nos dirigimos cuando queremos comunicar eventos que 
 * provienen de fix protocol,
 * @author omar
 */
public class GraficaHandler {

    public static ArrayList<Graphic> graficas = new ArrayList();
    public ArrayList<Properties> chart_files = new ArrayList();
    private String last_profile;
    Properties prof_conf = new Properties();
    /**
     * Generamos las gráficas
     */
    public GraficaHandler(){
        
        try {
            prof_conf.load(new FileInputStream("/home/omar/OMS/profiles/profiles.cnf"));
            this.last_profile = (prof_conf.getProperty("last_profile"));
            setProfile(this.last_profile);
        } catch (IOException ex) {
            System.err.println("El archivo de configuracion de perfiles no está, vamos a restaurarlo por tí ;)");
            this.last_profile = "default";
            setProfile(this.last_profile);
        }
    }
    /**
     * Añadimos un nuevo perfil, si el perfil no existe o no se ha indicado usarremos
     * el último o el default.
     * @param profile 
     */
    void setProfile(String perfil){
        //Si es null cargamos el pérfil de default
        try {
            //Cargamos configuración inicial de perfiles.
            this.chart_files = this.getChartFiles(perfil);
            //salvamos perfil como el último perfil que se usó.
            prof_conf.setProperty("last_profile",perfil);
            prof_conf.store(new FileOutputStream("/home/omar/OMS/profiles/profiles.cnf"),"");
            //Si no existe el perfil llamas otra vez al método para usar el default. 
        } catch (NoProfileFoundException ex) {
            try {
                
                this.chart_files = this.getChartFiles(this.last_profile);
            } catch (NoProfileFoundException ex1) {
                Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }catch(IOException ex){
            System.err.println("El archivo de configuracion de perfiles no pudo ser creado, te fallé.");
        }   
       
    }
    public String getProfile(){
        return this.last_profile;
    }
    
    void runProfile(){
        for(Properties file : this.chart_files){
            System.err.println("Grafica "+file.getProperty("symbol") +" de " + file.getProperty("period") + " minutos cargada correctamente");
            this.addGrafica(file);
        }
    }
    /**
     * Si queremos añadir una nueva grafica acá es donde.
     *
     * @param symbol
     * @param periodo
     */
    void addGrafica(Properties log_file) {
        //Podriamos poner aquí algunas opciones mas como pasar el archivo log.
        graficas.add(new Graphic(log_file));
        Console.log("Grafica "+log_file.getProperty("symbol") +" de " + log_file.getProperty("period") + " minutos cargada correctamente");
        runGrafica(graficas.size() - 1);
    }
    /**
     * Las gráficas son thread así que tenemos que correrlos para que comienzen
     * su trabajo.
     * @param index 
     */
    private void runGrafica(int index) {
        graficas.get(index).start();
    }
    /**
     * Notificamos a una gráfica que su orden fué aceptada.
     * @param id identificador de la gráfica.
     * @param orden 
     */
    public static void orderAccept(String id, ExecutionReport orden) {
        try {
            getGraf(id).newOrder(orden);
        } catch (GraficaNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Notificamos a una grafica que su peticion de cierre fue aceptada.
     */
    public static void orderClose(String grafid, String id){
        try {
            getGraf(grafid).onOrderClose(id);
        } catch (GraficaNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    /*
     * Obtenemos el ask de una determinada grafica.
     */
    public static Double getAsk(String idgraf) {
        Double temp=null;
        try {
            temp = getGraf(idgraf).getAsk();
        } catch (GraficaNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp;
    }
    /*
     * Obtenemos el ask de una determinada grafica.
     */
    public static Double getBid(String idgraf) {
        Double temp=null;
        try {
            temp = getGraf(idgraf).getBid();
        } catch (GraficaNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp;
    }
    /**
     * obtenemos el Tp de una grafica determinada.
     * @param idgraf
     * @return 
     */
    public static Double getTp(String idgraf){
        Double temp=null;
        try {
            temp = getGraf(idgraf).getTP();
        } catch (GraficaNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp;
    }
    /**
     * obtenemos el SL de una graica determinada.
     * @param idgraf
     * @return 
     */
    public static Double getSl(String idgraf){
        Double temp=null;
        try {
            temp = getGraf(idgraf).getSL();
        } catch (GraficaNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp;
    }
    /**
     * Cuando entra la OCO de una orden noificamos a su grafica correspondiente que 
     * su orden fue aceptada.
     * @param id
     * @param ordid
     * @param sl
     * @param tp 
     */
    public static void setStop(String id, String ordid,double sl, double tp){
        try {
            getGraf(id).setStops(ordid, sl, tp);
        } catch (GraficaNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Notificamos que entro una modificación.
     * @param order 
     */
    public static void orderModify(ExecutionReport order){
        
        try {
            //getGraf(order.getClOrdID().getValue()).orderModify(OrderHandler.getGrafId(order.getClOrdID().getValue()),order.getLastPx().getValue());
            getGraf(OrderHandler.getGrafId(order.getClOrdID().getValue())).orderModify(order.getClOrdID().getValue(), order.getField(new DoubleField(7540)).getValue());
        } catch (FieldNotFound ex) {
            Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch(GraficaNotFound ex){
            System.err.println(ex);
        }
    }
    /**
     * Obtenemos una grafica determinada.
     * @param id
     * @return 
     */
    public static Graphic getGraf(String id) throws GraficaNotFound{
        Graphic temp = null;
        for (int i = 0; i < graficas.size(); i++) {
            if (graficas.get(i).getID().equals(id)) {
                temp = graficas.get(i);
                break;
            }
        }
        if(temp==null){
            throw new GraficaNotFound(id);
        }
        return temp;
    }
    /**
     * Obtenemos un array de los archivos contenidos en la carpeta del perfil.
     * @param perfil
     * @return 
     */
    private ArrayList<Properties> getChartFiles(String perfil) throws NoProfileFoundException{
        ArrayList<Properties> temp = new ArrayList();
        File folder = new File("/home/omar/OMS/profiles/" + perfil);
        //Si la carpeta de perfil no existe o esta vacía, lanzamos exceptión.
        if (!folder.exists() || folder.listFiles().length==0){
            throw new NoProfileFoundException(perfil);
        }
        File[] prof_files = folder.listFiles();
        
        for(File file : prof_files){
            try {
                Properties prop_temp = new Properties();
                prop_temp.load(new FileInputStream(file.getPath()));
                prop_temp.setProperty("path", file.getPath());
                prop_temp.store(new FileOutputStream(file.getPath()), null);
                temp.add(prop_temp);
            } catch (IOException ex) {
                Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return temp;
    }
}

package oms.deliverer;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.GraficaNotFound;
import oms.CustomException.NoProfileFound;
import oms.Grafica.Graphic;


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
        } catch (NoProfileFound ex) {
            try {
                
                this.chart_files = this.getChartFiles(this.last_profile);
            } catch (NoProfileFound ex1) {
                Logger.getLogger(GraficaHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }catch(IOException ex){
            System.err.println("El archivo de configuracion de perfiles no pudo ser creado, te fallé.");
        }   
       
    }
    public String getProfile(){
        return this.last_profile;
    }
    /**
     * Pequeño Main para correr el programa sin necesidad de conectarnos con fix.
     * @param args 
     */
    public static void main(String[] args) {
        new GraficaHandler().runProfile();
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
//        Console.log("Grafica "+log_file.getProperty("symbol") +" de " + log_file.getProperty("period") + " minutos cargada correctamente");
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
    private ArrayList<Properties> getChartFiles(String perfil) throws NoProfileFound{
        ArrayList<Properties> temp = new ArrayList();
        File folder = new File("/home/omar/OMS/profiles/" + perfil);
        //Si la carpeta de perfil no existe o esta vacía, lanzamos exceptión.
        if (!folder.exists() || folder.listFiles().length==0){
            throw new NoProfileFound(perfil);
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

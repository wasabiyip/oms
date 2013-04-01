package oms.deliverer;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.GraficaNotFound;
import oms.CustomException.NoProfileFound;
import oms.Grafica.Graphic;
import oms.util.Console;

/**
 * Esta clase es a la cuál nos dirigimos cuando queremos comunicar eventos que 
 * provienen de fix protocol,
 * @author omar
 */
public class GraficaHandler {
    
    public static ArrayList<Graphic> graficas = new ArrayList();
    public static ArrayList<Properties> chart_files = new ArrayList();
    private String last_profile;
    Properties prof_conf = new Properties();
    private static String path;
    /**
     * Generamos las gráficas
     */
    public GraficaHandler(String path){
        
        try {
            this.path = path;
            prof_conf.load(new FileInputStream(this.path+"/OMS/profiles/profiles.cnf"));
            this.last_profile = (prof_conf.getProperty("last_profile"));
            setProfile(this.last_profile);
        } catch (IOException ex) {
            Console.exception("El archivo de configuracion de perfiles no está, vamos a restaurarlo por tí\n"+ex);
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
            prof_conf.store(new FileOutputStream(this.path+"/OMS/profiles/profiles.cnf"),"");
            //Si no existe el perfil llamas otra vez al método para usar el default. 
        } catch (NoProfileFound ex) {
            try {
                this.chart_files = this.getChartFiles(this.last_profile);
            } catch (NoProfileFound ex1) {
                 Console.exception(ex1);
            }
        }catch(IOException ex){
            Console.exception("El archivo de configuracion de perfiles no pudo ser creado, te fallé.\n"+ex);
        }   
       
    }
    /**
     * @return perfil actual/ultimo perfil usado.
     */
    public String getProfile(){
        return this.last_profile;
    }
    
    /**
     * Creamos las graficas apartir de los archivos del perfil.
     */
    void runProfile(){
        
        for(Properties file : this.chart_files){
            this.addGrafica(file);
        }
    }
    /**
     * Si queremos añadir una nueva grafica acá es donde.
     *
     * @param symbol
     * @param periodo
     */
    public static void addGrafica(Properties log_file) {
        //Podriamos poner aquí algunas opciones mas como pasar el archivo log.
        graficas.add(new Graphic(log_file,path));
        Console.info("Grafica "+log_file.getProperty("symbol") +" de " + log_file.getProperty("period") + " minutos cargada correctamente");
        runGrafica(graficas.size() - 1);
    }
    /**
     * Reconstruimos una grafica.
     */
    public synchronized static void rebuildGrafica(Graphic grafica){
        Properties temp=null;
        
        for(Properties file : chart_files){
            if(grafica.getID().equals(file.getProperty("grafid"))){
                temp = file;
            }
        }
        
        for (int i = 0; i < graficas.size(); i++) {
            if(graficas.get(i)==grafica){
                graficas.remove(i);
                addGrafica(temp);
            }
        }
    }
    /**
     * Las gráficas son thread así que tenemos que correrlos para que comienzen
     * su trabajo.
     * @param index 
     */
    private static void runGrafica(int index) {
        graficas.get(index).start();
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
        File folder = new File(this.path+"/OMS/profiles/" + perfil );
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
                Console.exception(ex);
            }
        }
        return temp;
    }
    /**
     * 
     * @return Numero de graficas.
     */
    public static Integer getGraficasSize(){
        return graficas.size();
    }
    
}

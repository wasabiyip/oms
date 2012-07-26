package oms.deliverer;

import java.util.ArrayList;
import java.util.Random;
import oms.Grafica.Graphic;

/**
 *
 * @author omar
 */
public class GraficaHandler {
    
    public static ArrayList<Graphic> graficas = new ArrayList();
    
    /**
     * Si queremos añadir una nueva grafica acá es donde.
     * @param symbol
     * @param periodo 
     */
    public void addGrafica(String symbol, int periodo){
        //Podriamos poner aquí algunas opciones mas como pasar el archivo log
        graficas.add(new Graphic(symbol, periodo, this.getId(symbol)));
    }
    
    
    public void orderEntry(){
        
    }
    
    /**
     * Este método genera un id único para cada expert/gráfica.
     * @return id único.
     */
    private String getId(String id){
        //TODO buscar donde poner esta cosa, o talvez no...
        StringBuffer str = new StringBuffer(id+"-");
        Random r = new Random();
        Random r2 = new Random();
        int n = 25; // 65-90 codigo ASCII.
        
        
        for (int j=0; j<4; j++){
            int i = r.nextInt() % n;
            if ( (65 + i <= 90) && (65 + i >= 65)){
                str.append ((char) (65 + i));  
            }
            else j--;//<---------|| CUIDADO ESTO ES UNA CHARRADA!
        }
        return str.toString();
    }
}

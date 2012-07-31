package oms.deliverer;

import java.util.ArrayList;
import java.util.Random;
import oms.Grafica.Graphic;
import quickfix.fix42.ExecutionReport;

/**
 * Esta clase es a la cuál nos dirigimos cuando queremos comunicar eventos que 
 * provienen de fix protocol,
 * @author omar
 */
public class GraficaHandler {

    public static ArrayList<Graphic> graficas = new ArrayList();
    /**
     * Si queremos añadir una nueva grafica acá es donde.
     *
     * @param symbol
     * @param periodo
     */
    public void addGrafica(String symbol, int periodo) {
        //Podriamos poner aquí algunas opciones mas como pasar el archivo log.
        graficas.add(new Graphic(symbol, periodo, this.genId(symbol)));
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

    public static void orderAccept(String id, ExecutionReport orden) {
        //recorremo las gráficas en busca de el id correspondiente a la orden que
        //fue aceptada para notificarla de que su orden fue aceptada.
        for (int i = 0; i < graficas.size(); i++) {
            if(graficas.get(i).getID().equals(id)){
                graficas.get(i).newOrder(orden);
            }
            
        }
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
    
    /*
     * Obtenemos el ask de una determinada grafica.
     */
    public static Double getAsk(String idgraf) {
        return getGraf(idgraf).getAsk();
    }
    
    public static int getTp(String idgraf){
        return getGraf(idgraf).getTP();
    }
    public static int getSl(String idgraf){
        return getGraf(idgraf).getSL();
    }

    public static Graphic getGraf(String id) {
        Graphic temp = null;
        for (int i = 0; i <= graficas.size(); i++) {
            if (graficas.get(i).getID().equals(id)) {
                temp = graficas.get(i);
                break;
            }
        }
        return temp;
    }
}

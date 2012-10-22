package oms.deliverer;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.Graphic;
import quickfix.DoubleField;
import quickfix.FieldNotFound;
import quickfix.IntField;
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
        graficas.add(new Graphic(symbol, periodo));
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
        getGraf(id).newOrder(orden);
    }
    /**
     * Notificamos a una grafica que su peticion de cierre fue aceptada.
     */
    public static void orderClose(String grafid, String id){
        getGraf(grafid).onOrderClose(id);        
    }
        
    /*
     * Obtenemos el ask de una determinada grafica.
     */
    public static Double getAsk(String idgraf) {
        return getGraf(idgraf).getAsk();
    }
    /*
     * Obtenemos el ask de una determinada grafica.
     */
    public static Double getBid(String idgraf) {
        return getGraf(idgraf).getBid();
    }
    /**
     * obtenemos el Tp de una grafica determinada.
     * @param idgraf
     * @return 
     */
    public static Double getTp(String idgraf){
        return getGraf(idgraf).getTP();
    }
    /**
     * obtenemos el SL de una graica determinada.
     * @param idgraf
     * @return 
     */
    public static Double getSl(String idgraf){
        return getGraf(idgraf).getSL();
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
         getGraf(id).setStops(ordid, sl, tp);
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
        }
    }
    /**
     * Obtenemos una grafica determinada.
     * @param id
     * @return 
     */
    public static Graphic getGraf(String id) {
        Graphic temp = null;
        for (int i = 0; i < graficas.size(); i++) {
            if (graficas.get(i).getID().equals(id)) {
                temp = graficas.get(i);
                break;
            }
        }
        return temp;
    }
}

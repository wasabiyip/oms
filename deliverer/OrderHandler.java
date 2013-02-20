package oms.deliverer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.OrdenNotFound;
import oms.CustomException.TradeContextBusy;
import oms.dao.MongoDao;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;

/**
 *
 * @author omar
 */
public class OrderHandler {

    public static MongoDao mongo = new MongoDao();
    /**
     * Cada elemento de este array representa una orden que esta activa, las
     * guaradamos por la grafica que la metio y el ordid.
     */
    static ArrayList<Orden> ordersArr = new ArrayList();
    /**
     * Aquí yacen los cruces que están en Trade context busy.
     */
    private static ArrayList contextBusy = new ArrayList();
    /**
     * Metodo que envia las ordenes a Currenex, es sincronizado para que no se
     * confunda si muchas graficas quieren enviar ordenés al mismo tiempo.
     *
     * @param msj
     */
    public synchronized static void sendOrder(Orden orden) throws TradeContextBusy{
        /**
        * Si el trade context esta busy para ese cruce entoncés lanzamos 
        * la excepción, si no enviamos la orden.
        */
        if(isTradeBusy(orden.getSymbol())){
            throw new TradeContextBusy(orden.getId(),orden.getSymbol());                
        }else{
            try {
                Session.sendToTarget(orden.getNewOrderSingleMsg(), SenderApp.sessionID);
                //si es una operacion nueva bloqueamos el symbol
                if(orden.getEsNueva()){
                    contextBusy.add(orden.getSymbol());
                    ordersArr.add(orden);
                }
            } catch (SessionNotFound ex) {
                Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Revisamos si tenemos context busy.
     * @param symbol cruce a evaluar.
     * @return si es false ademas, marcamos esta moneda en context busy
     */
    private synchronized static boolean isTradeBusy(String symbol){
        boolean temp = false;
        for(int i=0; i<contextBusy.size();i++){
            if(contextBusy.get(i).equals(symbol)){
                temp = true;
            }            
        }
        return temp;
    }
    /**
     * Notificamos que una operacion fue aceptada correctamente.
     * @param orden
     * @throws Exception
     */
    public synchronized static void orderNotify(ExecutionReport msj) throws Exception {
       
        Orden temp = getOrdenById(msj.getClOrdID().getValue());
        temp.setFilled(msj);
        
        /**
         * liberamos el cruce del context busy.
         */
        for(int i=0;i<contextBusy.size();i++){
            if(contextBusy.get(i).equals(msj.getSymbol().getValue())){
                contextBusy.remove(i);
            }
        }
    }
    /**
     * Reenviamos un OCO si se el broker cerró/canceló uno existente
     * @param report 
     */
    public synchronized static void resendOCO(ExecutionReport report){
        try {
            Orden orden = getOrdenById(report.getClOrdID().getValue());
            System.out.println("Reenviando OCO de "+orden.getId());
            SendOCO(orden.getOcoOrden());
        } catch (FieldNotFound | OrdenNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * 
     * @param symbol
     * @param type
     * @param ordid
     * @param qty
     * @param precio
     * @param status 
     */
    public synchronized static void SendOCO(NewOrderSingle newOrderOco) {
        
        try{
            Session.sendToTarget(newOrderOco,SenderApp.sessionID);
        }catch (SessionNotFound ex){
            System.err.println("El horror! No se pudo enviar OCO " + ex );
        }
    }

    /**
     * Modificamos el registro de la orden determinada, y le agregamos los SL y TP calculados
     * previamente.
     * @param tipo
     * @param id
     * @param precio
     * @throws Exception
     */
    public synchronized static void ocoEntry(ExecutionReport msj) throws Exception {
        Orden temp = getOrdenById(msj.getClOrdID().getValue());
        //Si es null entonces no tenemos un oco previo asi que solo lo guardamos
        if(temp.getOco() == null){
            temp.setOco(msj);            
        }else{
            closeOCO(temp);
            temp.setOco(msj);
        }        
    }

    /**
     * Enviamos el request para borrar la OCO de una orden determinada.
     * @param order 
     */
    public synchronized static void closeOCO(Orden orden) {
        quickfix.fix42.OrderCancelRequest oco = new quickfix.fix42.OrderCancelRequest();
        oco.set(new ClOrdID(orden.getId()));
        oco.set(new OrigClOrdID(orden.getId()));
        oco.set(new Symbol(orden.getSymbol()));
        oco.set(new OrderID(orden.getOco()));
        oco.setChar(40, 'W');
        oco.set(new Side(orden.averse));
        oco.set(new TransactTime());
        try {
            Session.sendToTarget(oco, SenderApp.sessionID);
        }catch (SessionNotFound ex) {
            System.out.println(ex);
        }
        //GraficaHandler.orderClose(getGrafId(orden.id), order);   
    }
    
    /**
     * Sabiendo que una orden ya existe y que la orden recibida es de cierre,
     * este método cambia el status de la orden a 0 para que ya no este activa,
     * y guarda el precio en el que se cerro la orden.
     *
     * @param id
     * @param price
     * @param coll
     * @throws Exception
     */
    public synchronized static void shutDown(Orden orden) throws Exception {
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject set = new BasicDBObject().append("$set", new BasicDBObject().append("Status", 0));
        BasicDBObject push = new BasicDBObject().append("$set", new BasicDBObject().append("Close", orden.getClosePrice()));
        BasicDBObject hora = new BasicDBObject().append("$set", new BasicDBObject().append("horaClose", new Date().toString()));
        BasicDBObject sl = new BasicDBObject().append("$set", new BasicDBObject().append("StopL", orden.getSl()));
        BasicDBObject tp = new BasicDBObject().append("$set", new BasicDBObject().append("TakeP", orden.getTp()));
        coll.update(new BasicDBObject().append("OrderID", orden.getId()), set);
        coll.update(new BasicDBObject().append("OrderID", orden.getId()), push);
        coll.update(new BasicDBObject().append("OrderID", orden.getId()), hora);        
        coll.update(new BasicDBObject().append("OrderID", orden.getId()), sl);
        coll.update(new BasicDBObject().append("OrderID", orden.getId()), tp);
    }
    
    /**
     * 
     * @param msj
     * @return
     * @throws Exception 
     */
    public synchronized static boolean isFilled(quickfix.fix42.ExecutionReport msj) throws Exception {
        String id = msj.getClOrdID().getValue();
        boolean temp=false;
        for (int i = 0; i < getOrdersActivas().size(); i++) {
            Orden current = getOrdersActivas().get(i);
            if(current.getId().equals(id) && current.isFilled()){
                temp = true;
            }            
        }
        return temp;
    }    
    /**
     * buscamos en ordersArr para obtener el id de una grafica dependiendo de que 
     * orden entro
     * @param ordid
     * @return 
     */
    public synchronized static String getGrafId(String ordid)throws OrdenNotFound{
        String temp=null;
        for (int i = 0; i < ordersArr.size(); i++) {
            if(ordersArr.get(i).getId().equals(ordid)){
                temp = ordersArr.get(i).getGrafId().toString();
            }
        }
        if(temp == null && ordersArr.size() != 0){
            throw new OrdenNotFound(ordid);
        }
        return temp;
    }
    /**
     * De el array en donde estan las órdenes extraemos las que esten activas y sean
     * de es Symbol.
     * @return 
     */
    public synchronized static ArrayList<Orden> getOrdersActivas(){
        ArrayList temp= new ArrayList();
        for (int i = 0; i < ordersArr.size(); i++) {
            if(ordersArr.get(i).IsActiva()){
                temp.add(ordersArr.get(i));
            }            
        }
        return temp;
    }
    /**
     * Obtenemos El total de ordenes para determinada grafica.
     * @param grafId Id de la grafica
     * @return 
     */
    public synchronized static Orden getOrdenByGraf(String grafId)throws OrdenNotFound{
        Orden temp=null;
        for (int i = 0; i < ordersArr.size(); i++) {
            if(ordersArr.get(i).getGrafId().equals(grafId));
                temp = ordersArr.get(i);
        }
        if(temp == null && ordersArr.size() != 0){
            throw new OrdenNotFound(grafId);
        }
        return temp;
    }
    /**
     * Obtenemos Una orden por su ordID.
     * @param id
     * @return 
     */
    public synchronized static Orden getOrdenById(String id)throws OrdenNotFound{
        Orden temp=null;
        for (int i = 0; i < ordersArr.size(); i++) {
            if(ordersArr.get(i).getId().equals(id));
                temp = ordersArr.get(i);
        }
        if(temp == null && ordersArr.size() != 0){
            throw new OrdenNotFound(id);
        }
        return temp;
    }
    /**
     * Obtenemos El total de ordenes para determinado Symbol
     * @param symbol
     * @return 
     */
    public synchronized static ArrayList<Orden> getOrdersBySymbol(String symbol){
        ArrayList<Orden> temp= new ArrayList();
        
        for (int i = 0; i < ordersArr.size(); i++) {
            if(ordersArr.get(i).IsActiva() && ordersArr.get(i).getUnSymbol().equals(symbol)){
                temp.add(ordersArr.get(i));
            }            
        }
        
        return temp;
    }
}

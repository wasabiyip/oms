package oms.deliverer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.Graphic;
import oms.Grafica.Order;
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

    static MongoDao mongo = new MongoDao();
    //static DBObject obj;
    /**
     * Este array
     */
    static ArrayList<ArrayList> ordPool = new ArrayList();
    
    
    /**
     * Metodo que envia las ordenes a Currenex, es sincronizado para que no se
     * confunda si muchas graficas quieren enviar ordenés al mismo tiempo.
     *
     * @param msj
     */
    public synchronized static void sendOrder(NewOrderSingle msj, String id, boolean tipo ) {
        ArrayList temp = new ArrayList();
        try {
            Session.sendToTarget(msj, SenderApp.sessionID);
        } catch (SessionNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        } 
        if (tipo){
            temp.add(id);
            try {
                temp.add(msj.getClOrdID().getValue());
            } catch (FieldNotFound ex) {
                Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            ordPool.add(temp);
        }
        else{
            for(int i=0; i<ordPool.size();i++){
                if(ordPool.get(i).get(0) == id){
                    ordPool.remove(i);
                }
            }
        }
    }

    /**
     * Guardamos una cadena de Json que representa a una ordén. El array ordPool
     * asocia una orden con la gráfica que la envió, esto es por que cuando enviamos
     * una orden no tenemos que 
     * @param orden
     * @throws Exception
     */
    public static void orderNotify(ExecutionReport orden) throws Exception {
        String entry = "";
        for (int i = 0; i < ordPool.size(); i++) {
            //Buscamos la orden que entro en ordPool para obtener el ID de la gráfica
            //que lo envió y así notificar a la respectiva gráfica.
            if(ordPool.get(i).get(1).equals(orden.getClOrdID().getValue())){
                GraficaHandler.orderAccept((String)ordPool.get(i).get(0),orden);
                break;
            }
        }
    }

    /**
     * Enviamos OCO (One Cancels the Other) orden.
     *
     * @param type
     * @param ID
     * @param qty
     */
    public synchronized static void SendOCO(String symbol,char type, String ordid, int qty, double precio) {
        quickfix.fix42.NewOrderSingle oco = new quickfix.fix42.NewOrderSingle();
        char tipo = type =='1'?'2':'1';
        oco.set(new ClOrdID(ordid));
        oco.set(new HandlInst('1'));
        oco.set(new Currency(symbol.substring(0,3)));
        oco.set(new Symbol(symbol));
        oco.set(new TransactTime());
        oco.set(new OrderQty(qty));
        oco.set(new OrdType('W'));
        oco.set(new Side(tipo));
        
        oco.setField(new CharField(7541,'3'));
        oco.setField(new CharField(7553,tipo));
        double sl = GraficaHandler.getGraf(getGrafId(ordid)).getSL() * GraficaHandler.getGraf(getGrafId(ordid)).getPoint();
        double tp = GraficaHandler.getGraf(getGrafId(ordid)).getTP() * GraficaHandler.getGraf(getGrafId(ordid)).getPoint();
        if (type == 1) {
            oco.setField(new DoubleField(7540, redondear(GraficaHandler.getAsk(ordid) - sl)));
            oco.setField(new DoubleField(7542, redondear(GraficaHandler.getAsk(ordid) + tp)));
            oco.setField(new CharField(7543,'2'));
            
        }else{
            oco.setField(new DoubleField(7540, redondear(precio + sl)));
            oco.setField(new DoubleField(7542, redondear(precio - tp)));
            oco.setField(new CharField(7543,'2'));
        }
        try{
            Session.sendToTarget(oco,SenderApp.sessionID);
        }catch (SessionNotFound ex){
            System.err.println("El horror! No se pudo enviar OCO " + ex );
        }
    }

    /**
     * Modificamos el registro de la orden determinada, y le agregamos los SL y TP calculados
     * previamente.
     *
     * @param tipo
     * @param id
     * @param precio
     * @throws Exception
     */
    public static void ocoRecord(ExecutionReport msj) throws Exception {
        System.err.println("OCO record: "+ msj.getOrderID().getValue());
        DBCollection coll = Graphic.dao.getCollection("operaciones");
        BasicDBObject oco = new BasicDBObject();
        BasicDBObject sl = new BasicDBObject();
        BasicDBObject tp = new BasicDBObject();
                
        oco.append("$set", new BasicDBObject().append("OCO", msj.getOrderID().getValue()));
        sl.append("$set", new BasicDBObject().append("StopL", msj.getField(new DoubleField(7542)).getValue()));
        tp.append("$set", new BasicDBObject().append("TakeP", msj.getField(new DoubleField(7540)).getValue()));
        coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), oco);
        coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), sl);
        coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), tp);
    }

    /**
     * Obtenemos el número total de ordenés activas.
     *
     * @return
     * @throws Exception
     */
    public static DBCursor getTotal() {
        
        DBCollection coll = mongo.getCollection("log");
        DBCursor cur = coll.find();
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        cur = coll.find(query);
        return cur;
    }

    /**
     * Enviamos el request para borrar la OCO de una orden determinada.
     * @param order 
     */
    public static void closeOCO(String order) {
        
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        DBObject temp;
        query.put("OrderID", order);
        DBCursor cur = coll.find(query);
        DBObject res = cur.next();
        String tp = (String) res.get("TakeP");
        String sl = (String) res.get("StopL");
        String symbol = (String) res.get("Symbol");
        int side = ((int)res.get("Type"))==1? 2 : 1; //Guardamos el valor contrario al tipo de orden que queremos cerrar.
        
        if(cur.size()==1){//nos aseguramos que solo se encontro una orden

            quickfix.fix42.OrderCancelRequest stop = new quickfix.fix42.OrderCancelRequest();
            quickfix.fix42.OrderCancelRequest take = new quickfix.fix42.OrderCancelRequest();
            stop.set(new ClOrdID(order));
            take.set(new ClOrdID(order));
            stop.set(new OrigClOrdID(order));
            take.set(new OrigClOrdID(order));
            stop.set(new Symbol(symbol));
            take.set(new Symbol(symbol));
            stop.set(new OrderID(sl));
            take.set(new OrderID(tp));
            stop.setChar(40, '3');
            take.setChar(40, 'F');
            stop.set(new Side((char)side));
            take.set(new Side((char)side));
            stop.set(new TransactTime());
            take.set(new TransactTime());
            try {
                Session.sendToTarget(take, SenderApp.sessionID);
                Session.sendToTarget(stop, SenderApp.sessionID);
            }catch (SessionNotFound ex) {
                Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
    private static void shutDown(String id, Double price, DBCollection coll) throws Exception {

        BasicDBObject set = new BasicDBObject().append("$set", new BasicDBObject().append("Status", 0));
        BasicDBObject push = new BasicDBObject().append("$set", new BasicDBObject().append("Close", price));
        coll.update(new BasicDBObject().append("OrderID", id), set);
        coll.update(new BasicDBObject().append("OrderID", id), push);
    }

    public static boolean Exists(quickfix.fix42.ExecutionReport msj) throws Exception {
        
        String id = msj.getClOrdID().getValue();
        Double price = msj.getAvgPx().getValue();
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("OrderID", id);
        DBCursor cur = coll.find(query);
        if (cur.count() > 0) {
            shutDown(id, price, coll);
            return true;
        } else 
            return false;
    }

    /**
     * Verificamos si la orden existe en Mongo para ver si la que se envio
     * anteriormente es el cierre de la nueva.
     *
     * @param msj
     * @return
     * @throws Exception
     */
    public static Integer getId() throws Exception {
        
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 0);
        DBCursor cur = coll.find(query);
        return (Integer) cur.next().get("NoOrder");
    }

    public static String getCl() throws Exception {

        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 0);
        DBCursor cur = coll.find(query);
        return (String) cur.next().get("OrderID");
    }

    private static String sumLong(String ord, int num) {

        long orden = Long.valueOf(ord).longValue();
        orden = orden + num;
        return Long.toString(orden);
    }
    /**
     * redondeamos un double
     * @param num
     * @return 
     */
    private static Double redondear(double num) {
        return Math.rint(num * 1000) / 1000;
    }
    /**
     * buscamos en ordPool para obtener el id de una grafica dependiendo de que 
     * orden entro
     * @param ordid
     * @return 
     */
    private static String getGrafId(String ordid){
        String tmp="";
        for (int i = 0; i < ordPool.size(); i++) {
            if(ordPool.get(i).get(1).equals(ordid))
                tmp = ordPool.get(i).get(0).toString();
        }
        return tmp;
    }
}

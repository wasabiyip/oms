package oms.deliverer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.Order;
import oms.dao.MongoDao;
import quickfix.IntField;
import quickfix.Session;
import quickfix.SessionNotFound;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;
/**
 *
 * @author omar
 */
public class OrderHandler {
    static MongoDao mongo;
    static DBObject obj;
    static ArrayList<ArrayList> stops = new ArrayList();
    /**
     * Metodo que envia las ordenes a Currenex, es sincronizado para que no se 
     * confunda si muchas graficas quieren enviar ordenés al mismo tiempo.
     * @param msj 
     */
    public synchronized static void sendOrder(Object msj, String id){
        
        try {
            Session.sendToTarget((NewOrderSingle)msj, SenderApp.sessionID);    
        } catch (SessionNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void notifyOrder(){
         
    }
    
    /**
     * Guardamos una cadena de Json que representa a una ordén.
     *
     * @param orden
     * @throws Exception
     */
    public void orderRecord(String orden) throws Exception {

        mongo = new MongoDao();
        DBCollection coll = mongo.getCollection("log");
        obj = (DBObject) JSON.parse(orden);
        coll.insert(obj);
    }
    /**
     * Enviamos StropLoss y TakeProfit por cada operacion.
     *
     * @param type
     * @param ID
     * @param qty
     */
    public synchronized void SendStops(char type, String ID, int qty, double precio) {

        quickfix.fix42.NewOrderSingle nwsl = new quickfix.fix42.NewOrderSingle();
        quickfix.fix42.NewOrderSingle nwtp = new quickfix.fix42.NewOrderSingle();
        nwsl.set(new ClOrdID(ID));
        nwtp.set(new ClOrdID(ID));
        nwsl.set(new OrdType('3'));
        nwtp.set(new OrdType('F'));
        nwsl.set(new Symbol("EUR/USD"));
        nwtp.set(new Symbol("EUR/USD"));
        nwsl.set(new HandlInst('1'));
        nwtp.set(new HandlInst('1'));
        nwtp.set(new TransactTime());
        nwsl.set(new TransactTime());
        nwtp.set(new OrderQty(qty));
        nwsl.set(new OrderQty(qty));
        nwtp.set(new Currency("EUR"));
        nwsl.set(new Currency("EUR"));
        if (type == 1) {
            /*
             nwsl.set(new StopPx(MarketPool.getOffer()- (Settings.sl *
             Settings.Point))); nwtp.set(new Price(MarketPool.getOffer() + (Settings.tp*Settings.Point)));
*/
            nwsl.set(new Side('2'));
            nwtp.set(new Side('2'));
        } else {
            /*
             nwsl.set(new StopPx(precio + (Settings.sl *
              Settings.Point))); nwtp.set(new Price(MarketPool.getBid() - (Settings.tp*Settings.Point)));
             */
            nwsl.setField(new IntField(7534, 1));
            nwsl.set(new Side('1'));
            nwtp.set(new Side('1'));
        }
        try {
            Session.sendToTarget(nwsl, SenderApp.sessionID);
            Session.sendToTarget(nwtp, SenderApp.sessionID);
        } catch (SessionNotFound ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Modificamos el registro de una orden y le agregamos los limites.
     *
     * @param tipo
     * @param id
     * @param precio
     * @throws Exception
     */
    public void stopsRecord(char tipo, String id, Double precio, String order) throws Exception {

        mongo = new MongoDao();
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject stop = new BasicDBObject();

        if (tipo == '3') {
            stop.append("$set", new BasicDBObject().append("StopL", order));

        } else {
            stop.append("$set", new BasicDBObject().append("TakeP", order));
        }
        coll.update(new BasicDBObject().append("OrderID", id), stop);
    }
    

    /**
     * Obtenemos el número total de ordenés activas.
     *
     * @return
     * @throws Exception
     */
    public static DBCursor getTotal() {
        try {
            mongo = new MongoDao();
        } catch (Exception ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
        }
        DBCollection coll = mongo.getCollection("operaciones");
        DBCursor cur = coll.find();
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        cur = coll.find(query);

        return cur;
    }
    
    /**
     * Obtenemos el número total de ordenés por MAGICMA.
     *
     * @return
     * @throws Exception
     */
    public static int getTotalMagic() {
        try {
            mongo = new MongoDao();
        } catch (Exception ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
        }
        DBCollection coll = mongo.getCollection("operaciones");
        DBCursor cur = coll.find();
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        //query.put("MAGICMA", Settings.MAGICMA);
        cur = coll.find(query);
        return cur.count();
    }
    
    public void closeStops(String ID, char side, String order) {

        quickfix.fix42.OrderCancelRequest stop = new quickfix.fix42.OrderCancelRequest();
        quickfix.fix42.OrderCancelRequest take = new quickfix.fix42.OrderCancelRequest();

        stop.set(new ClOrdID(ID + "SL"));
        take.set(new ClOrdID(ID + "TP"));
        stop.set(new OrigClOrdID(ID));
        take.set(new OrigClOrdID(ID));
        stop.set(new Symbol("EUR/USD"));
        take.set(new Symbol("EUR/USD"));
        stop.set(new OrderID(this.sumLong(order, 1)));
        take.set(new OrderID(this.sumLong(order, 2)));
        stop.setChar(40, '3');
        take.setChar(40, 'F');
        stop.set(new Side(side));
        take.set(new Side(side));
        stop.set(new TransactTime());
        take.set(new TransactTime());
        try {
            Session.sendToTarget(take, SenderApp.sessionID);
            Session.sendToTarget(stop, SenderApp.sessionID);
        } catch (SessionNotFound ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
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
        mongo = new MongoDao();
        String id = msj.getClOrdID().getValue();
        Double price = msj.getAvgPx().getValue();
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("OrderID", id);
        DBCursor cur = coll.find(query);

        if (cur.count() > 0) {
            shutDown(id, price, coll);
            return true;
        } else {
            return false;
        }
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

        mongo = new MongoDao();
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 0);
        DBCursor cur = coll.find(query);
        return (Integer) cur.next().get("NoOrder");

    }

    public static String getCl() throws Exception {

        mongo = new MongoDao();
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
}

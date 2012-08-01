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
import oms.Grafica.Settings;
import oms.dao.MongoDao;
import oms.util.fixToJson;
import quickfix.FieldNotFound;
import quickfix.IntField;
import quickfix.Session;
import quickfix.SessionNotFound;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;

/**
 *
 * @author omar
 */
public class OrderHandler {

    static MongoDao mongo = new MongoDao();
    static DBObject obj;
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
    public synchronized static void sendOrder(NewOrderSingle msj, String id) {
        ArrayList temp = new ArrayList();
        try {
            temp.add(id);
            temp.add(msj.getClOrdID().getValue());
            ordPool.add(temp);
            Session.sendToTarget(msj, SenderApp.sessionID);
        } catch (SessionNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e){
            System.err.println("El horror! - ClOrdID no encontrado...");
        }
    }

    /**
     * Guardamos una cadena de Json que representa a una ordén. El array ordPool
     * asocia una orden con la gráfica que la envió, esto es por que cuando enviamos
     * una orden no tenemos que 
     * @param orden
     * @throws Exception
     */
    public static void orderRecord(ExecutionReport orden) throws Exception {
        String entry = "";
        for (int i = 0; i < ordPool.size(); i++) {
            //Buscamos la orden que entro en ordPool para obtener el ID de la gráfica
            //que lo envió y así notificar a la respectiva gráfica.
            if(ordPool.get(i).get(1).equals(orden.getClOrdID().getValue())){
                entry = (String) ordPool.get(i).get(0);
                GraficaHandler.orderAccept((String)ordPool.get(i).get(0), orden);
                break;
            }
        }
        String json = new fixToJson().parseOrder(orden,entry);
        DBCollection coll = mongo.getCollection("log");
        obj = (DBObject) JSON.parse(json);
        coll.insert(obj);
    }

    /**
     * Enviamos StropLoss y TakeProfit por cada operacion.
     *
     * @param type
     * @param ID
     * @param qty
     */
    public synchronized static void SendStops(String symbol,char type, String ordid, int qty, double precio) {
        
        quickfix.fix42.NewOrderSingle nwsl = new quickfix.fix42.NewOrderSingle();
        quickfix.fix42.NewOrderSingle nwtp = new quickfix.fix42.NewOrderSingle();
        nwsl.set(new ClOrdID(ordid));
        nwtp.set(new ClOrdID(ordid));
        nwsl.set(new OrdType('3'));
        nwtp.set(new OrdType('F'));
        nwsl.set(new Symbol(symbol));
        nwtp.set(new Symbol(symbol));
        nwsl.set(new HandlInst('1'));
        nwtp.set(new HandlInst('1'));
        nwtp.set(new TransactTime());
        nwsl.set(new TransactTime());
        nwtp.set(new OrderQty(qty));
        nwsl.set(new OrderQty(qty));
        nwtp.set(new Currency(symbol.substring(0, 3)));
        nwsl.set(new Currency(symbol.substring(0, 3)));
        //Asi lo voy a dejar por que soy flojo y además se ve bastante 'pro :)
        //acá calculamos los limites de la orden que entró, 
        double sl = GraficaHandler.getGraf(getGrafId(ordid)).getSL() * GraficaHandler.getGraf(getGrafId(ordid)).getPoint();
        double tp = GraficaHandler.getGraf(getGrafId(ordid)).getTP() * GraficaHandler.getGraf(getGrafId(ordid)).getPoint();
        if (type == 1) {
            
            nwsl.set(new StopPx(redondear(GraficaHandler.getAsk(ordid) - sl))); 
            nwtp.set(new Price(redondear(GraficaHandler.getAsk(ordid) - tp)));
            
            nwsl.set(new Side('2'));
            nwtp.set(new Side('2'));
        } else {
            
            nwsl.set(new StopPx(redondear(precio + sl)));
            nwtp.set(new Price(redondear(precio - tp)));
             
            nwsl.setField(new IntField(7534, 1));
            nwsl.set(new Side('1'));
            nwtp.set(new Side('1'));
        }
        try {
            Session.sendToTarget(nwsl, SenderApp.sessionID);
            Session.sendToTarget(nwtp, SenderApp.sessionID);
            //notificamos acerca de los stops
            GraficaHandler.setStop(getGrafId(nwsl.getClOrdID().getValue()),nwsl.getClOrdID().getValue(),nwsl.getOrdType().getValue(), nwsl.getStopPx().getValue());
            GraficaHandler.setStop(getGrafId(nwsl.getClOrdID().getValue()),nwsl.getClOrdID().getValue(),nwtp.getOrdType().getValue(), nwtp.getPrice().getValue());
        } catch (SessionNotFound ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
        } catch ( FieldNotFound ex){
            System.out.println("¡El horror!: No se encontro el campo " + ex);
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
    public static void stopsRecord(char tipo, String id, Double precio, String order) throws Exception {
        
        DBCollection coll = mongo.getCollection("log");
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
        
        DBCollection coll = mongo.getCollection("operaciones");
        DBCursor cur = coll.find();
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        //query.put("MAGICMA", Settings.MAGICMA);
        cur = coll.find(query);
        return cur.count();
    }

    public static void closeStops(String ID, char side, String order) {

        quickfix.fix42.OrderCancelRequest stop = new quickfix.fix42.OrderCancelRequest();
        quickfix.fix42.OrderCancelRequest take = new quickfix.fix42.OrderCancelRequest();

        stop.set(new ClOrdID(ID + "SL"));
        take.set(new ClOrdID(ID + "TP"));
        stop.set(new OrigClOrdID(ID));
        take.set(new OrigClOrdID(ID));
        stop.set(new Symbol("EUR/USD"));
        take.set(new Symbol("EUR/USD"));
        stop.set(new OrderID(sumLong(order, 1)));
        take.set(new OrderID(sumLong(order, 2)));
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
     * Cerramos una orden enviando la orden opuesta al tipo que se recibe.
     *
     * @param tipo
     */
    public void Close(Integer tipo) {
        
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        DBObject temp;
        query.put("Status", 1);
        //query.put("MAGICMA", Settings.MAGICMA);
        DBCursor cur = coll.find(query);

        while (cur.hasNext()) {
            temp = cur.next();

            if (((Integer) temp.get("Type")) == tipo) {
                if (tipo == 1) {
                    //Send(MarketPool.getOffer(), '2',(String) temp.get("OrderID"));
                    closeStops((String) temp.get("OrderID"), '2', temp.get("NoOrder").toString());
                } else {
                    //Send(MarketPool.getBid(), '1', (String) temp.get("OrderID"));
                    closeStops((String) temp.get("OrderID"), '1', temp.get("NoOrder").toString());
                }
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

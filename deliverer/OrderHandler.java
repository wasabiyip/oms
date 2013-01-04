package oms.deliverer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.TradeContextBusy;
import oms.Grafica.Graphic;
import oms.Grafica.Order;
import oms.dao.MongoDao;
import oms.util.Console;
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
    static ArrayList<ArrayList> ordPool = new ArrayList();
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
    public synchronized static void sendOrder(NewOrderSingle msj, String id, boolean tipo ) throws TradeContextBusy{
        ArrayList temp = new ArrayList();
        String symbol="";
        String order="";
        //este try es por si no encuentra los campos en el mensaje, no te asustes.
        try {
            symbol = msj.getSymbol().getValue();
            order = msj.getClOrdID().getValue();
        } catch (FieldNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        /**
        * Si el trade context esta busy para ese cruce entoncés lanzamos 
        * la excepción, si no enviamos la orden.
        */
        if(isTradeBusy(symbol)){
            throw new TradeContextBusy(order,symbol);                
        }else{
            try {
                Session.sendToTarget(msj, SenderApp.sessionID);
                //si es una operacion nueva bloqueamos el symbol
                if(tipo)
                    contextBusy.add(symbol);
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
    }
    /**
     * Revisamos si tenemos context busy.
     * @param symbol cruce a evaluar.
     * @return si es false ademas, marcamos esta moneda en context busy
     */
    public static boolean isTradeBusy(String symbol){
        boolean temp = false;
        for(int i=0; i<contextBusy.size();i++){
            if(contextBusy.get(i).equals(symbol)){
                temp = true;
            }            
        }
        return temp;
    }
    /**
     * Guardamos una cadena de Json que representa a una ordén. El array ordPool
     * asocia una orden con la gráfica que la envió, ademas una vez que la orden
     * fue aceptada, desbloqueamos la moneda del context busy.
     * @param orden
     * @throws Exception
     */
    public static void orderNotify(ExecutionReport msj) throws Exception {
        String entry = "";
        String temp = "";
        for (int i = 0; i < ordPool.size(); i++) {
            //Buscamos la orden que entro en ordPool para obtener el ID de la gráfica
            //que lo envió y así notificar a la respectiva gráfica.
            if (ordPool.get(i).get(1).equals(msj.getClOrdID().getValue())) {
                GraficaHandler.orderAccept((String) ordPool.get(i).get(0), msj);
                break;
            }
        }
        /**
         * Despues de haber recibido la confirmación de la ordén, enviamos su 
         * correspondiente OCO.
         */
        if (msj.getSide().getValue() == '1') {
            OrderHandler.SendOCO(msj.getSymbol().getValue(), '1', msj.getClOrdID().getValue(), (int) msj.getOrderQty().getValue(), (double) msj.getLastPx().getValue(), 'N');
            temp = "Se abrió una orden: #" + msj.getClOrdID().getValue() + " Compra " + msj.getOrderQty().getValue() / 10000 + " "
                    + msj.getSymbol().getValue() + " a: " + msj.getLastPx().getValue();
            System.out.println(temp);
            Console.msg(temp);
        }else if (msj.getSide().getValue() == '2') {
            OrderHandler.SendOCO(msj.getSymbol().getValue(), '2', msj.getClOrdID().getValue(), (int) msj.getOrderQty().getValue(), (double) msj.getLastPx().getValue(), 'N');
            temp = "Se abrió una orden: #" + msj.getClOrdID().getValue() + " Venta " + msj.getOrderQty().getValue() / 10000 + " "
                    + msj.getSymbol().getValue() + " a: " + msj.getLastPx().getValue();
            System.out.println(temp);
            Console.msg(temp);
        }
        /**
         * Una vez enviado el OCO liberamos el cruce del context busy.
         */
        for(int i=0;i<contextBusy.size();i++){
            if(contextBusy.get(i).equals(msj.getSymbol().getValue())){
                contextBusy.remove(i);
            }
        }
    }
    
    public static void ocoResend(quickfix.fix42.ExecutionReport msj){
        try {
            String ordid = msj.getClOrdID().getValue();
            Character tipo = msj.getSide().getValue() =='1'?'2':'1';
            double precio =  msj.getField(new DoubleField(7542)).getValue()-GraficaHandler.getGraf(getGrafId(ordid)).getSL();
            
            SendOCO(msj.getSymbol().getValue(),tipo, ordid,(int)msj.getOrderQty().getValue(),precio,'N');
        } catch (FieldNotFound ex) {
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
    public synchronized static void SendOCO(String symbol,Character type, String ordid, int qty, double precio, Character mov) {
        quickfix.fix42.NewOrderSingle oco = new quickfix.fix42.NewOrderSingle();
        Character tipo = type =='1'?'2':'1';
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
        double sl = GraficaHandler.getGraf(getGrafId(ordid)) .getSL();
        double tp = 0;
        oco.setField(new CharField(7543,type));
        //Si es una modificacion de OCO
        if(mov.equals('M')){
            oco.set(new Text("Mod"));
            tp = GraficaHandler.getGraf(getGrafId(ordid)).getNwTp();
            
        }else if(mov.equals('N')){//Si es una nueva OCO.
            oco.set(new Text("New"));
            tp = GraficaHandler.getGraf(getGrafId(ordid)).getTP();
        }
        if (type.equals('1')) {
            try{
                oco.setField(new DoubleField(7542, redondear(symbol, precio - sl)));
                oco.setField(new DoubleField(7540, redondear(symbol, precio + tp)));
            }catch (NullPointerException ex){
                //En caso de que recibamos un NullPointer al obtener el ask, 
                //Esperamos 5 millis y hacemos una llamada recursiva a el método con los
                //mismos valores que recibimos en primera instancia.
                try {
                    Thread.sleep(5);
                    SendOCO(symbol,type, ordid, qty, precio,mov);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            
        }else if(type.equals('2')){
            oco.setField(new DoubleField(7542, redondear(symbol,precio + sl)));
            oco.setField(new DoubleField(7540, redondear(symbol,precio - tp)));
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
        BasicDBObject mod = new BasicDBObject();
                
        oco.append("$set", new BasicDBObject().append("OCO", msj.getOrderID().getValue()));
        sl.append("$set", new BasicDBObject().append("StopL", msj.getField(new DoubleField(7542)).getValue()));
        tp.append("$set", new BasicDBObject().append("TakeP", msj.getField(new DoubleField(7540)).getValue()));
        mod.append("$set", new BasicDBObject().append("Modify", "N"));
        coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), oco);
        coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), sl);
        coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), tp);
        coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), mod);
        GraficaHandler.setStop(getGrafId(msj.getClOrdID().getValue()), msj.getClOrdID().getValue(),msj.getField(new DoubleField(7542)).getValue(), msj.getField(new DoubleField(7540)).getValue());
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
    public static void closeOCO(String order, char mov) {
        
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        DBObject temp;
        query.put("OrderID", order);
        DBCursor cur = coll.find(query);
        DBObject res = cur.next();
        String id = (String) res.get("OCO");
        String symbol = (String) res.get("Symbol");
        char side = ((int)res.get("Type"))==1? '2' : '1'; //Guardamos el valor contrario al tipo de orden que queremos cerrar.
        if(cur.size()==1){//nos aseguramos que solo se encontro una orden
            quickfix.fix42.OrderCancelRequest oco = new quickfix.fix42.OrderCancelRequest();
            oco.set(new ClOrdID(order));
            oco.set(new OrigClOrdID(id));
            oco.set(new Symbol(symbol));
            oco.set(new OrderID(id));
            oco.setChar(40, 'W');
            oco.set(new Side(side));
            oco.set(new TransactTime());
            try {
                Session.sendToTarget(oco, SenderApp.sessionID);
            }catch (SessionNotFound ex) {
                Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(mov == 'N')
            //Notificamos a la gráfica que cerró una orden
            GraficaHandler.orderClose(getGrafId(order), order);
        else if(mov == 'M'){
            //Notificamos a la gráfica que abrió una orden
        }else System.err.println("Colapso -- OrdenHandler/CloseOCO method");
    }
    /**
     * Método que notifica acerca del cierre mediante Tp o SL.
     * @param order 
     */
    public static void closeFromOco(String order){
        GraficaHandler.orderClose(getGrafId(order), order);
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
    public static void shutDown(String id, Double price) throws Exception {
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject set = new BasicDBObject().append("$set", new BasicDBObject().append("Status", 0));
        BasicDBObject push = new BasicDBObject().append("$set", new BasicDBObject().append("Close", price));
        BasicDBObject hora = new BasicDBObject().append("$set", new BasicDBObject().append("horaClose", new Date().toString()));
        coll.update(new BasicDBObject().append("OrderID", id), set);
        coll.update(new BasicDBObject().append("OrderID", id), push);
        coll.update(new BasicDBObject().append("OrderID", id), hora);        
    }
    /**
     * Revisamos si existe una orden.
     * @param msj
     * @return
     * @throws Exception 
     */
    public static boolean Exists(quickfix.fix42.ExecutionReport msj) throws Exception {
        String id = msj.getClOrdID().getValue();
        Double price = msj.getAvgPx().getValue();
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("OrderID", id);
        DBCursor cur = coll.find(query);
        if (cur.count() > 0) {
            return true;
        } else 
            return false;
    }
    /**
     * Verificamos si una orden ya ha sido modificada.
     * @param msj
     * @return 
     */
    public static boolean isModify(quickfix.fix42.ExecutionReport msj){
        boolean temp= false;
        DBCursor res;
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        try {
            query.put("OrderID", msj.getClOrdID().getValue());
        } catch (FieldNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        res = coll.find(query);
        if(res.count()>0 && res.next().get("OCO")!=null) {
            temp= true;
        }            
        return temp;
    }
    public static void ocoModify(quickfix.fix42.ExecutionReport msj){
        DBCollection coll = OrderHandler.mongo.getCollection("operaciones");
        BasicDBObject mod = new BasicDBObject();
        String temp;
        try {
            mod.append("$set", new BasicDBObject().append("TakeP", msj.getField(new DoubleField(7540)).getValue()));
            coll.update(new BasicDBObject().append("OrderID", msj.getClOrdID().getValue()), mod);
            temp = "Modificando : "+ msj.getClOrdID().getValue();
            System.out.println(temp);
            Console.msg(temp)            ;
            GraficaHandler.orderModify(msj);
        } catch (FieldNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * verificamos si existe una orden con el valor de oco especificado.
     * @param msj orden a evaluar.
     * @return 
     */
    public static boolean ocoExists(quickfix.fix42.ExecutionReport msj){
        boolean temp = false;
        try {
            DBCollection coll = mongo.getCollection("operaciones");
            BasicDBObject query = new BasicDBObject();
            query.put("OCO", msj.getOrderID().getValue());
            DBCursor cur = coll.find(query);
            if (cur.count() > 0) 
                temp = true;
        } catch (FieldNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp;
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
    private static Double redondear(String symbol,double num) {

        if(symbol.equals("USD/JPY"))
            return Math.rint(num * 1000) / 1000;
        else
            return num;        
    }
    /**
     * buscamos en ordPool para obtener el id de una grafica dependiendo de que 
     * orden entro
     * @param ordid
     * @return 
     */
    public static String getGrafId(String ordid){
        String tmp="";
        for (int i = 0; i < ordPool.size(); i++) {
            if(ordPool.get(i).get(1).equals(ordid))
                tmp = ordPool.get(i).get(0).toString();
        }
        return tmp;
    }
}

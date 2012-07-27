package oms.Grafica;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.Settings;
import oms.dao.MongoDao;
import oms.deliverer.OrderHandler;
import oms.deliverer.SenderApp;
import oms.util.idGenerator;
import quickfix.IntField;
import quickfix.Session;
import quickfix.SessionNotFound;
import quickfix.field.*;

/**
 * Clase que se encarga de todo lo relacionado con las ordenés.
 *
 * @author omar
 */
public class Order {

    String ordid;
    /**
     * Enviamos una orden...
     *
     * @param price
     * @param type
     * @throws SessionNotFound
     * @throws Exception
     */
    public void Send(double price, char type, String id ) {
        ordid = new idGenerator().getID();
        quickfix.fix42.NewOrderSingle nworder = new quickfix.fix42.NewOrderSingle();
        nworder.set(new ClOrdID((ordid)));
        nworder.set(new HandlInst('1'));
        nworder.set(new Side(type));
        nworder.set(new Currency("EUR"));
        nworder.set(new Symbol("EUR/USD"));
        nworder.set(new TransactTime());
        nworder.set(new OrderQty(10000));
        nworder.set(new OrdType('C'));
        nworder.set(new Price(price));
        System.out.println("Enviando orden...");
        //enviamos orden
        OrderHandler.sendOrder(nworder, id);
        
    }
}

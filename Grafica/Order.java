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
import oms.deliverer.SenderApp;
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

    /**
     * Enviamos una orden...
     *
     * @param price
     * @param type
     * @throws SessionNotFound
     * @throws Exception
     */
    public void Send(double price, char type, Object ID) {

        quickfix.fix42.NewOrderSingle nworder = new quickfix.fix42.NewOrderSingle();
        nworder.set(new ClOrdID((String) ID));
        nworder.set(new HandlInst('1'));
        nworder.set(new Side(type));
        nworder.set(new Currency("EUR"));
        nworder.set(new Symbol("EUR/USD"));
        nworder.set(new TransactTime());
        nworder.set(new OrderQty(10000));
        nworder.set(new OrdType('C'));
        nworder.set(new Price(price));
        System.out.println("Enviando orden...");
        try {
            System.out.println(nworder);
            
            Session.sendToTarget(nworder, SenderApp.sessionID);
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
        try {
            mongo = new MongoDao();
        } catch (Exception ex) {
            Logger.getLogger(Order.class.getName()).log(Level.SEVERE, null, ex);
        }
        DBCollection coll = mongo.getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        DBObject temp;
        query.put("Status", 1);
        //query.put("MAGICMA", Settings.MAGICMA);
        DBCursor cur = coll.find(query);

        while (cur.hasNext()) {
            temp = cur.next();

            if (((Integer) temp.get("Type")) == tipo) {
                System.out.println(temp.get("Type"));
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
}

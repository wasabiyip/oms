package oms.deliverer;

import oms.util.GMTDate;
import quickfix.SessionNotFound;
import quickfix.field.*;
/**
 * Clase que maneja precios.
 * @author omar
 */
public class PriceSensitive {
    
    
    /**
     * Constructor.
     */
    public PriceSensitive (){
        
        GMTDate.getDate();
        
    }
    
    public boolean sendOrderRequest() throws SessionNotFound{
        boolean exito= false;
        quickfix.fix42.NewOrderSingle order = new quickfix.fix42.NewOrderSingle();
        order.set(new ClOrdID("123"));
        order.set(new HandlInst('1'));
        order.set(new Currency("EUR"));
        order.set(new Side('2'));
        order.set(new OrderQty(100000));
        order.set(new TransactTime());
        order.set(new OrdType('C'));
        order.set(new Symbol("EUR/USD"));
        //order.set(new Price(mp.getBid()));
        
        //Session.sendToTarget(order,SenderApp.sessionID); 
              
        return exito;
    }
    
}

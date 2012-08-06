package oms.Grafica;

import oms.deliverer.OrderHandler;
import oms.util.idGenerator;
import quickfix.SessionNotFound;
import quickfix.field.*;

/**
 * Clase que se encarga de todo lo relacionado con las ordenés.
 *
 * @author omar
 */
public class Order {

    String ordid;
    String symbol;
    String currency;
    int magicma;
    public Order(String symbol, int magicma){
        this.symbol = symbol;
        this.currency = symbol.substring(0, 3);
        this.magicma = magicma;
    }
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
        nworder.set(new Currency(this.currency));
        nworder.set(new Symbol(symbol));
        nworder.set(new TransactTime());
        nworder.set(new OrderQty(10000));
        nworder.set(new OrdType('C'));
        nworder.set(new Price(price));
        //enviamos orden
        OrderHandler.sendOrder(nworder, id);
    }
}

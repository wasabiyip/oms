package oms.Grafica;

import com.mongodb.DBObject;
import java.util.ArrayList;
import oms.deliverer.GraficaHandler;
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
    String grafid;
    public Order(String symbol, int magicma, String grafid){
        this.symbol = symbol;
        this.currency = symbol.substring(0, 3);
        this.magicma = magicma;
        this.grafid = grafid;
    }
    /**
     * Enviamos una orden, si tipo es true entonces la ordene es apertura y si es
     * falso entonces es cierre de una operación.
     * @param price
     * @param type
     * @throws SessionNotFound
     * @throws Exception
     */
    private void Send(double price, char type, String id , boolean tipo) {
        if(tipo)
            ordid = new idGenerator().getID();
        else
            ordid = id;
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
        OrderHandler.sendOrder(nworder, id, tipo);
    }
    
    /**
     * Cerramos una operación, enviando la operacion contraria a la recibida.
     * @param order
     * @param price 
     */
    public void Close(Double price, char type){
        ArrayList <DBObject> temp = Graphic.dao.getTotalGraf(this.grafid);
        for(int i=0; i<temp.size();i++){
            Close(this.grafid, temp.get(i));
        }
    }
    
    /**
     * version sobrecargada del método close que envia un cierre determinado por el usuario
     * desde la interfaz web.
     * @param grafica
     * @param order 
     */
    public void Close(String grafica, DBObject order){
        
        if (((int)order.get("Type"))==1)
            this.Send(GraficaHandler.getAsk(grafica),'2', (String)order.get("OrderID"),false);
        else{
            this.Send(GraficaHandler.getBid(grafica),'1', (String)order.get("OrderID"),false);
        }
    }
    
    /**
     * Abrimos una posición.
     * @param price
     * @param type
     * @param id 
     */
    public void Open(double price, char type){
        this.Send(price, type, this.grafid, true);
    }
    /*
    public ArrayList<DBObject> getTotal(){
        ArrayList temp = new ArrayList();
        DBCursor res = dao.getTotalMagic(this.magicma);
        while(res.hasNext()){
            temp.add(res.next());
        }
        return temp;
        }*/
}

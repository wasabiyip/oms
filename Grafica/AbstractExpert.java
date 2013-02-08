/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.indicators.Indicador;
import oms.deliverer.OrderHandler;
import oms.util.Console;
import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;

/**
 *
 * @author omar
 */
public abstract class AbstractExpert {
    Indicador indicator;
    String Symbol;
    int Periodo;
    Settings setts;
    Order order;
    ExecutionReport lastOrder;
    Double Point;
    char currentOrder = '0';
    double lastOrderPrice;
    boolean modify = false;
    
    public double open_min = 0.0;
    public Double Ask;
    public Double Bid;

    public void absInit(String symbol, int periodo, Settings setts) {
        this.Symbol = symbol;
        this.Periodo = periodo;
        this.setts = setts;
        this.indicator = new Indicador(symbol, periodo);
        order = new Order(setts.symbol, setts.MAGICMA, setts.id);
        this.Point = setts.Point;
    }

    /**
     * Nos avisan si una orden entro
     *
     * @param type
     */
    public void openNotify(quickfix.fix42.ExecutionReport order) {
        try {
            this.lastOrder = order;
            currentOrder = order.getSide().getObject();
            System.err.println("Orden abrio");
            lastOrderPrice = order.getLastPx().getValue();
        } catch (FieldNotFound ex) {
            Logger.getLogger(Jedi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Nuestra operacion cerró.
     *
     * @param price
     * @param type
     */
    public void orderClose(Double price, char type) {
        order.Close(price, '1');
        this.lastOrderPrice = price;
    }

    /**
     *
     * @param price
     * @param type
     */
    public void orderSend(Double price, char type) {
        String msj = type == '1' ? "Procesando compra..." : "Procesando Venta...";
        Console.msg(msj);
        this.currentOrder = type;
        System.err.println(msj);
        order.Open(price, type);
    }
    /**
     * En realidad lo único que se modifica es el TP y SL de la orden.
     */
    public void orderModify(){
        try {
            DBCollection coll = OrderHandler.mongo.getCollection("operaciones");
            BasicDBObject mod = new BasicDBObject();
            mod.append("$set", new BasicDBObject().append("Modify", "Y"));
            coll.update(new BasicDBObject().append("OrderID", lastOrder.getOrigClOrdID().getValue()), mod);
            ///Cuando se quiere modificar un OCO, primero cerramos la orden.
            OrderHandler.closeOCO(lastOrder.getClOrdID().getValue(),'M');
            Thread.sleep(5);
            //Enseguida enviamos una orden nueva.
            OrderHandler.SendOCO(lastOrder.getSymbol().getValue(), lastOrder.getSide().getValue(), lastOrder.getClOrdID().getValue(), 
                    (int)lastOrder.getOrderQty().getValue(), lastOrder.getLastPx().getValue(),'M');
             
        } catch (InterruptedException ex) {
            Logger.getLogger(Jedi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FieldNotFound ex) {
            Logger.getLogger(Jedi.class.getName()).log(Level.SEVERE, null, ex);
        }        
        this.modify = true;
    }
    /**
     * verificamos que nos encontremos en horas de operacion.
     *
     * @param hora
     * @return
     */
    public boolean isActive() {

        boolean temp = false;
        if (this.hora() && this.open_min > 0 && Ask > 0 && Bid > 0) {

            temp = true;
        }
        return temp;
    }

    /**
     * Nos dicen si una orden cerro.
     */
    public void closeNotify() {
        currentOrder = '0';
        this.modify = false;
    }

    public void indicatorDataIn(Double precio) {
        this.indicator.appendBollsData(precio);
    }

    /**
     * Ponemos modify como true para asegurarnos que no se vuelva a modificar
     * una orden.
     */
    public void modNotify() {
        this.modify = true;
    }

    public abstract void Init();

    public abstract void onTick();

    boolean hora() {
        Date date = new oms.Grafica.Date();
        double hora = date.getHour() + (date.getMinute() * 0.01);
        return hora < setts.horaFin && hora >= setts.horaIni;
    }

    /**
     * Solo redondeamos números.
     *
     * @param val
     * @return
     */
    double redondear(double val) {
        double temp;
        temp = Math.rint(val * 1000000) / 1000000;
        return temp;
    }

    /**
     * regresamos si nos encontramos dentro del limite de operaciones por cruce.
     * (por cruce significa por el symbol).
     *
     * @return
     */
    public Integer limiteCruce() {

        Integer temp = Graphic.dao.getTotalCruce(setts.symbol);
        return temp;
    }
    
    public Integer TotalMagic(){
        Integer temp = Graphic.dao.getTotalMagic(this.setts.MAGICMA);
        return temp;
    }
    public Integer TimeCurrent(){
        return GMTDate.getTime();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.TradeContextBusy;
import oms.Grafica.indicators.Indicador;
import oms.deliverer.Orden;
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
    Double Point;
    
    public double open_min = 0.0;
    public Double Ask;
    public Double Bid;

    public void absInit(String symbol, int periodo, Settings setts) {
        this.Symbol = symbol;
        this.Periodo = periodo;
        this.setts = setts;
        this.indicator = new Indicador(symbol, periodo);
        this.Point = setts.Point;
    }
    
    public void orderSend(Double price,Double lotes, char side,Double sl, Double tp) {
        try {
            if(sl == 0 && tp == 0){
                OrderHandler.sendOrder(new Orden(this.setts.id, this.Symbol, lotes, this.setts.MAGICMA, price, side));
            }
            else{
                OrderHandler.sendOrder(new Orden(this.setts.id, this.Symbol, lotes, this.setts.MAGICMA, price, side,sl ,tp));
            }            
        } catch (TradeContextBusy ex) {
            Logger.getLogger(AbstractExpert.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * verificamos que nos encontremos en horas de operacion.
     *
     * @param hora
     * @return
     */
    public boolean isActive() {

        boolean temp = false;
        /*if (this.hora() && this.open_min > 0 && Ask > 0 && Bid > 0) {

            temp = true;
        }*/
        return temp;
    }

    

    public void indicatorDataIn(Double precio) {
        this.indicator.appendBollsData(precio);
    }

    public abstract void Init();

    public abstract void onTick();
    
    public Double CurrentHora(){
        Date date = new oms.Grafica.Date();
        return (date.getHour() + (date.getMinute() * 0.01));
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
    public int OrdersCount(){
        
        return OrderHandler.getOrdersActivas().size();
    }
    public ArrayList<Orden> OrdersTotal(){
        return OrderHandler.getOrdersActivas();
    }
    
    public Integer TimeCurrent(){
        return GMTDate.getTime();
    }
}

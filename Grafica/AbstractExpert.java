/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.indicators.Indicador;
import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;

/**
 *
 * @author omar
 */
public abstract class AbstractExpert {
    private boolean grafic_lock = false;
    Indicador indicator;
    String Symbol;
    int Period;
    Settings setts;
    Order order;
    ExecutionReport lastOrder;
    char currentOrder = '0';
    boolean lock_op= true;
    double lastOrderPrice;
    boolean modify= false;
    public int contVelas=0;
    public double open_min=0.0;    
    public Double Ask;
    public Double Bid;
    
    public void absInit(String symbol, int periodo,Settings setts){
        this.Symbol = symbol;
        this.Period = periodo;
        this.setts = setts;
        this.indicator = new Indicador(symbol, periodo);
        order = new Order(setts.symbol,setts.MAGICMA,setts.id);
    }
    /**
     * Nos avisan si una orden entro
     * @param type 
     */
    public void openNotify(quickfix.fix42.ExecutionReport order){
        try {
            this.lastOrder = order;
            currentOrder = order.getSide().getObject();
            System.err.println("Orden abrio");
            this.lock_op = false;
            lastOrderPrice= order.getLastPx().getValue();
            contVelas =0;
        } catch (FieldNotFound ex) {
            Logger.getLogger(Jedi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Nuestra operacion cerró.
     * @param price
     * @param type 
     */
    public void orderClose(Double price, char type){
        order.Close(price, '1');
        this.lastOrderPrice = price;
    }
    /**
     * verificamos que nos encontremos en horas de operacion.
     * @param hora
     * @return 
     */
    public boolean isActive(){
        
        boolean temp=false;
        if( this.hora() && !this.grafic_lock && this.open_min>0 && Ask>0 && Bid>0){
            
           temp=true; 
        }
        return temp;
    }
    /**
     * Nos dicen si una orden cerro.
     */
    public void closeNotify(){
        currentOrder = '0';
        this.lock_op = true;
        this.contVelas = 0;
        this.modify = false;
    }
    
    
    public void indicatorDataIn(Double precio){
        this.indicator.appendBollsData(precio);
    }
    /**
     * Ponemos modify como true para asegurarnos que no se vuelva a modificar una orden.
     */
    public void modNotify(){
        this.modify = true;
    }
    
    
    public abstract void Init();
    public abstract void onTick();
    
    boolean hora(){
        Date date = new oms.Grafica.Date();
        double hora = date.getHour() + (date.getMinute()*0.01);
        return hora < setts.horaFin && hora >= setts.horaIni;
    }
    /**
     * Solo redondeamos números.
     * @param val
     * @return 
     */
    double redondear(double val){
        double temp;
        temp = Math.rint(val * 1000000) / 1000000;
        return temp;
    }
    /**
     * regresamos si nos encontramos dentro del limite de operaciones por cruce.
     * (por cruce significa por el symbol).
     * @return 
     */
    public Integer limiteCruce(){
       
        Integer temp = Graphic.dao.getTotalCruce(setts.symbol);
        return temp;
    }
}

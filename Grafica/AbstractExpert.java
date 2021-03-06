/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.IndicatorLengthGap;
import oms.CustomException.TradeContextBusy;
import oms.Grafica.indicators.BollingerBands;
import oms.Grafica.indicators.Indicador;
import oms.deliverer.Orden;
import oms.deliverer.OrderHandler;
import oms.util.Console;


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
    public Double open_min=0.0;
    public Double Ask=0.0;
    public Double Bid=0.0;
    /**
     * Este funje como constructor de la clase, ya que la clase que hereda no 
     * tiene constructor por lo tanto no podemos mandar llamar a este constructor,
     * favor de llamarlo justo despues de un objeto que herede de esta clase.
     * @param symbol
     * @param periodo
     * @param setts 
     */
    public void absInit(String symbol, int periodo, Settings setts) {
        this.Symbol = symbol;
        this.Periodo = periodo;
        this.setts = setts;
        this.indicator = new Indicador(symbol, periodo);
        this.Point = setts.Point;
    }
    /**
     * Enviamos una orden. Podemos enviar una órden sin SL/TP si ponemos estos valores
     * en 0.
     * @param price
     * @param lotes
     * @param side
     * @param sl
     * @param tp 
     */
    public void orderSend(Double price,Double lotes, char side,Double sl, Double tp) {
        Console.info("Open request in process at: "+ price + " sl:"+sl + " tp:"+tp);
        try {
            if(sl == 0 && tp == 0){
                OrderHandler.sendOrder(new Orden(this.setts.id, this.Symbol, lotes, this.setts.MAGICMA, price, side));
            }
            else{
                OrderHandler.sendOrder(new Orden(this.setts.id, this.Symbol, lotes, this.setts.MAGICMA, price, side,sl ,tp));
            }            
        } catch (TradeContextBusy ex) {
            Console.warning(ex);
        }
    }
    /**
     * Actualizamos los indicadores que se hayan creado para esta gráfica.
     * @param precio 
     */
    public void indicatorDataIn(Double precio) {
        this.indicator.appendBollsData(precio);
    }
    BollingerBands iBands(Integer boll){
        BollingerBands temp = null;
        try {
           temp = this.indicator.createBollinger(boll);
        } catch (IndicatorLengthGap ex) {
            Console.exception(ex + " " + this.setts.MAGICMA);
        }
        return temp;
    }
    /**
     * hora actual con formato de DB
     * @return 
     */
    public Double CurrentHora(){
       
        return GMTDate.getHora();
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
     * Obetenemos El numero de ordenes que podemos cerrar, cada gráfica esta
     * restringida a sólo poder cerrar ordenes de su Symbol
     * @return 
     */
    public int OrdersBySymbol(){
        
        Integer temp = null;
        temp = OrderHandler.getOrdersBySymbol(this.Symbol).size();
        return temp;
    }
    /**
     * @return número de ordenes que hay para este magic
     */
    public int OrdersByMagic(){
        int temp=0;
        temp = OrderHandler.getOrdersByMagic(this.Symbol, this.setts.MAGICMA).size();
        return temp;
    }
    /**
     * @return número de ordenes que hay para este magic
     */
    public int OrdersByGraph(){
        int temp=0;
        temp = OrderHandler.getOrdersByGraph(this.setts.id).size();
        return temp;
    }
    /**
     * Obetenemos El total de ordenes que podemos cerrar, una gráfica sólo 
     * puede cerrar ordenés que tengan su id.
     * @return Ordenes en forma de ArrayList
     */
    public ArrayList<Orden> OrdersTotal() {
        
        ArrayList<Orden> temp = null;
        temp = OrderHandler.getOrdersByGraph(this.setts.id);
        return temp;
    }
    /**
     * Hora acual.
     * @return 
     */
    public Integer TimeCurrent(){
        return GMTDate.getTime();
    }
    /**
     * Obtenemos la diferencia del periodo de la grafica respecto a la hora.
     * @return 
     */
    public Integer getMod(){
        return GMTDate.getMod(Periodo);
    }
    /**
     * Evaluamos si esta gráfica esta en condiciones de operar
     * @return evaluacion.
     */
    public boolean isActive(){
        //TODO Implementar esta función.
        return true;
    }
    /**
     * Métodos que deben de ser implementados si se quiere heredar de esta clase.
     */
    public abstract void Init();

    public abstract void onTick();
    
}

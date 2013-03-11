package oms.Grafica;

import java.util.Properties;
import oms.deliverer.Orden;

/**
 * Expert de pruebitas :)
 */
public class Expert extends AbstractExpert{

    Properties config = new Properties();
    private int startTime;
    
    /**
     * Constructor...
     * @param symbol Indica el par de monedas con el se va a trabajar.
     */
    public Expert() {
        //llamamos a el constructor de el padre (Settings).

    }
    @Override
    public void Init() {
        this.startTime = this.TimeCurrent() - (this.TimeCurrent()%this.Periodo);
    }
    
    @Override
    public void onTick() {
        
        if (Math.abs(this.startTime - this.TimeCurrent()) >= this.Periodo) {
            
        }   
        if(this.OrdersCount() < 1){
            orderSend(this.Ask, this.setts.lots, '1', 0.0, 0.0);
        }else if(this.OrdersCount()>1){
             for (int i = 0; i < OrdersTotal().size(); i++) {
                 Orden currentOrden = OrdersTotal().get(i);
                 if(currentOrden.getSl() == 0 || currentOrden.getTp() == 0){
                     if (currentOrden.getSide() == '1') {
                         currentOrden.Modify(Ask-this.setts.sl, Ask+this.setts.tp);
                     }else if (currentOrden.getSide() == '2'){
                         currentOrden.Modify(Bid+this.setts.sl, Bid-this.setts.tp);
                     }
                 }
             }
        }
    }
}
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
        
       
    }
}
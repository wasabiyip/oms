/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import oms.Grafica.indicators.BollingerBands;

/**
 *
 * @author omar
 */
public class ExpertMoc extends AbstractExpert{
        
    private BollingerBands bollBand1;
    private BollingerBands bollBand2;
    private BollingerBands bollBand3;
    private BollingerBands bollBandS1;
    private BollingerBands bollBandS2;
    private BollingerBands bollBandS3 ;
    private BollingerBands bollBandx1;
    private BollingerBands bollBandx2;
    private BollingerBands bollBandx3;
    
    
    
    @Override
    public void Init() {
        bollBand1 = indicator.createBollinger(setts.boll1);
        bollBand2 = indicator.createBollinger(setts.boll2);
        bollBand3 = indicator.createBollinger(setts.boll3);
        
        bollBandS1 = indicator.createBollinger(setts.bollS1);
        bollBandS2 = indicator.createBollinger(setts.bollS2);
        bollBandS3 = indicator.createBollinger(setts.bollS3);
        
        bollBandx1 = indicator.createBollinger(setts.bollx1);
        bollBandx2 = indicator.createBollinger(setts.bollx2);
        bollBandx3 = indicator.createBollinger(setts.bollx3);
    }

    @Override
    public void onTick() {
       
    }
    /*
     * Método que promedia un Promedio de bollingers con la variable spreadAask.
     */
    public double getAvgBoll(double boll){
        double temp = (boll  + (boll +(this.setts.spreadAsk * setts.Point)))/2;
        return this.redondear(temp);
    }
     
    /**
     * @return retornamos un promedio del precio de apertura relacionado con el Spread
     */
    public double getAvgOpen(){
        double temp = 0.0;
        temp = ((this.open_min) + (this.open_min +(this.Ask - this.Bid)))/2;
        return temp;
    }
    /**
     * Obtenemos el promedio de bollinger de entrada (UP).
     * @return 
     */
    public double bollUp() {
        return (bollBand1.getUpperBand() + bollBand2.getUpperBand() + 
                            bollBand3.getUpperBand())/3;
        
    }
    
    /**
     * Obtenemos el promedio de bollinger de salida (UP).
     * @return 
     */
    double bollUpS() {
        return (bollBandS1.getUpperBand() + bollBandS2.getUpperBand() + 
                            bollBandS3.getUpperBand()) / 3;
    }

    /**
     * Obtenemos el promedio de bollinger de entrada (Down).
     * @return 
     */
    double bollDn() {

        return (bollBand1.getLowerBand() + bollBand2.getLowerBand() + 
                            bollBand3.getLowerBand()) / 3;
    }
    
    /**
     * Obtenemos el promedio de bollinger de salida (Down).
     * @return 
     */
    double bollDnS() {

        return (bollBandS1.getLowerBand() + bollBandS2.getLowerBand() + 
                            bollBandS3.getLowerBand()) / 3;
    }
    
    double bollingerDif(){
        double tempUp = (bollBandx1.getUpperBand() + bollBandx2.getUpperBand() + bollBand3.getUpperBand())/3 ;
        double tempDn = (bollBandx1.getLowerBand() + bollBandx2.getLowerBand() + bollBand3.getLowerBand())/3;
        double temp = tempUp - tempDn;
        return temp;
    }
}

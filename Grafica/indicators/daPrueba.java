/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica.indicators;

import oms.Grafica.Graphic;

/**
 *
 * @author omar
 */
public class daPrueba {
    
    daPrueba(){
        Indicador indicador = new Indicador("GBPUSD", 5);
        BollingerBands bollBand1 = indicador.createBollinger(20);
        System.out.println("Up: "+bollBand1.getUpperBand());
        System.out.println("Dn: "+bollBand1.getLowerBand());
    }
    public static void main(String[] args) {
        new daPrueba();
    }
}

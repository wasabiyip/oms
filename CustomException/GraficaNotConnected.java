/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.CustomException;

import oms.Grafica.Graphic;

/**
 *
 * @author omar
 */
public class GraficaNotConnected extends Exception{
    public GraficaNotConnected(Graphic graf){
        super(graf.toString());
    }
}

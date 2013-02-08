package oms.Grafica;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Clase que calcula cuando las velas de un período determinado.
 *
 * @author omar
 */
public class Candle {

    private int periodo;
    private int cont_time=0;
    private int last_hora=0;

    /**
     * @param periodo perido de la vela,
     * @param dif esto es para mantener la vela sincronizada.
     */
    public Candle(int periodo) {
        this.periodo = periodo;
        //cuantos minutos van en la vela inicial.
        cont_time = GMTDate.getTime() % this.periodo;
        System.out.println("dif "+cont_time);
    }
    /**
     * Es una nueva vela, si la hora es mod del periodo o 
     * @param hora
     * @return 
     */
    public int isNewCandle(Integer hora){
        int temp = 0;
        if(hora%this.periodo == 0 || 
                cont_time%this.periodo ==0){
            temp = 1;
            cont_time=0;
            System.out.println("Nueva Candle");
        }        
        cont_time++;
        return temp;
    }
}

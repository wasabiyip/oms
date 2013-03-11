package oms.Grafica;

import static oms.Grafica.GMTDate.getDate;

/**
 * Clase que calcula cuando las velas de un período determinado.
 *
 * @author omar
 */
public class Candle {

    private int periodo;
    private int cont_time=0;

    /**
     * @param periodo perido de la vela,
     * @param dif esto es para mantener la vela sincronizada.
     */
    public Candle(int periodo) {
        this.periodo = periodo;
        //cuantos minutos van en la vela inicial.
        this.cont_time = GMTDate.getMod(this.periodo);
    }
    /**
     * Es una nueva vela, si la hora es mod del periodo o 
     * @param hora
     * @return 
     */
    public boolean isNewCandle(){
        boolean temp= false;
        this.cont_time++;
        if(getDate().getMinute() == 0 || 
                cont_time%this.periodo ==0){
            temp = true;
            cont_time=0;
        }
        return temp;
    }
}

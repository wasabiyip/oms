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
    private ArrayList<ArrayList> Candles = new ArrayList();
    public double iOpen = 0;
    private ArrayList temp = new ArrayList();
    private Integer horaActual;
    private Integer velas = 1;
    private ArrayList precios = new ArrayList();
    private Double openPrice;

    /**
     * @param periodo perido de la vela,
     * @param historial esto es para mantener la vela sincronizada.
     */
    public Candle(int periodo, ArrayList historial) {

        if (historial.get(0) == null) {
        } else {
            this.periodo = periodo;
            precios = historial;
            velas = precios.size();
        }
    }

    /**
     * onTick significa que recibimos un precio de apertura de minuto.
     *
     * @param price este precio de minuto es usado para conformar la vela
     */
    public void onTick(Double price) {
        precios.add(price);
        //Cada vez que se cambia vela de 1 min entramos acá.
        if (precios.size() >= periodo) {
            //Cada vez que se cambia de vela del periodo entramos acá.
            temp.add(GMTDate.getDate().toString());
            temp.add(precios.get(0));
            temp.add(Collections.max(precios));
            temp.add(Collections.min(precios));
            temp.add(precios.get(precios.size() - 1));
            Candles.add(new ArrayList(temp));
            this.openPrice = (double) precios.get(0);
            precios.clear();
            temp.clear();
        }
    }

    /**
     * Cambiamos el periodo de la vela.
     *
     * @param per
     */
    public void setPeriodo(int per) {
        this.periodo = per;
    }

    /**
     * regresamos el precio de apertura de la vela.
     *
     * @return
     */
    public Double getOpenPrice() {
        return this.openPrice;
    }
}

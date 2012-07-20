/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica.indicators;

import oms.Grafica.DAO.MongoDao;
import oms.Grafica.GMTDate;
import oms.Grafica.indicators.BollingerBands;
import java.util.ArrayList;

/**
 * Esta clase regula la creacion de Bollingers se encarga de llenar con valores
 * históricos, de esta forma cuando se crea un bollinger este listo para hacer
 * calculos, de otra forma tendriamos que esperar hasta que se llene de forma
 * natural.
 *
 * @author omar
 */
public class Indicador {

    private String symbol;
    private int periodoGrafica;
    private int dif;

    /**
     * El constructor!
     *
     * @param symbol Simbolo del cual vamos a extraer datos de mongo
     * @param periodoGrafica periodo de la gráfica.
     */
    public Indicador(String symbol, int periodoGrafica) {
        this.symbol = symbol;
        this.periodoGrafica = periodoGrafica;
        dif = GMTDate.getDate().getMinute() % periodoGrafica;
    }

    /**
     * Solo se pueden crear bollinger através de este método.
     * @param periodo
     * @return el bollinger
     */
    public BollingerBands createBollinger(int periodo) {

        ArrayList data = new ArrayList();
        MongoDao dao = new MongoDao();
        ArrayList temp = dao.getCandleData(this.symbol, (periodo * periodoGrafica) + this.dif);

        for (int i = 0; i < dif; i++) {
            temp.remove(0);
        }

        for (int i = 0; i < temp.size(); i++) {
            if (i == 0 || i % periodoGrafica == 0) {
                data.add(temp.get(i));
            }
        }
        BollingerBands tempBoll = new BollingerBands(periodo, data);
        return tempBoll;
    }
}

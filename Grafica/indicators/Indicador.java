package oms.Grafica.indicators;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import oms.Grafica.GMTDate;
import java.util.ArrayList;
import java.util.Hashtable;
import oms.CustomException.IndicatorLengthGap;
import oms.dao.MongoDao;
import oms.deliverer.SenderApp;

/**
 * Esta clase regula la creacion de Bollingers se encarga de llenar con valores
 * históricos, de esta forma cuando se crea un bollinger este listo para hacer
 * calculos, de otra forma tendriamos que esperar hasta que se llene de forma
 * natural. Tenemos un Hash de bollinger para cada moneda, todas las graficas del
 * mismo symbol comparten éste.
 *
 * @author omar
 */
public class Indicador {

    private String symbol;
    private int periodoGrafica;
    private int dif;
    private ArrayList<BollingerBands> bolls_arr = new ArrayList();
    private MongoDao dao = SenderApp.getDAO();
    //Pool Party yei!
    private static Hashtable<String,ArrayList<BollingerBands>> bandsPoolParty;
    /**
     * El constructor!
     *
     * @param symbol Simbolo del cual vamos a extraer datos de mongo
     * @param periodoGrafica periodo de la gráfica.
     */
    public Indicador(String symbol, int periodo) {
        this.symbol = symbol;
        
        this.periodoGrafica = periodo;
        this.dif = GMTDate.getMod(periodo);
    }

    /**
     * Solo se pueden crear bollinger através de este método.
     *
     * @param periodo
     * @return el bollinger regresamos el bollinger creado para que sea
     * referenciado.
     */
    public BollingerBands createBollinger(int periodo) throws IndicatorLengthGap {
        int exists = this.getExistingBoll(periodo);
        if (exists >= 0) {
            return this.bolls_arr.get(exists);
        } else {
            ArrayList data = new ArrayList();
            DBCursor cursor = this.dao.getCandleData(this.symbol, (periodo * periodoGrafica) + this.dif);
            int last_hora = 0;
            Double last_open = 0.0;
            int resta;
            while (cursor.hasNext() && data.size() < periodo) {
                DBObject temp = cursor.next();
                Double open = (Double) temp.get("Open");
                Integer hora = 0;
                hora = (Integer) temp.get("hour");
                resta = last_hora - hora;
                /**
                 * Usamos el tipo de técnica para obtener si un minuto es la
                 * apertura de vela, como en el metodo GMTDate.getMod.
                 *
                 */
                int mins = getMinVela(hora / 100);
                if (mins - (this.periodoGrafica * (mins / this.periodoGrafica)) == 0) {
                    data.add(open);
                    /**
                     * Si la resta es mayor a 100 quiere decir que hay un gap,
                     * asi que la apertura de vela será el último open recibido.
                     */
                } else if (resta > 100 && !(resta == 4100)) {
                    /**
                     * Si la diferencia es mayor a 4100 es por que es al cambio
                     * de hora, asi que lo normalizamos.
                     */
                    if (resta > 4100) {
                        resta = resta - 4100;
                    }
                    /**
                     * recorremos la diferencia para buscar un supuesto cambio
                     * de vela en ese rango.
                     */
                    for (int i = 1; i <= resta / 100; i++) {
                        //Misma forma de "mod" 
                        if ((mins + i) - (this.periodoGrafica * ((mins + i) / this.periodoGrafica)) == 0) {
                            data.add(last_open);
                        }
                    }
                }
                last_hora = hora;
                last_open = open;
            }
            /**
             * Si al obtener los datos resuta que nos son del periodo que
             * esperabamos lanzamos una excepción ya que deberían.
             */
            if (data.size() != periodo) {
                throw new IndicatorLengthGap(this.symbol, periodo);
            }
            this.bolls_arr.add(new BollingerBands(periodo, data));
            return this.bolls_arr.get(this.bolls_arr.size() - 1);
        }
    }

    /**
     * alimentamos los bollingers con un nuevo valor, normalmente todos los
     * indicadores se deberan de actualizar con este precio.
     *
     * @param precio
     */
    public void appendBollsData(Double precio) {
        for (int i = 0; i < this.bolls_arr.size(); i++) {
            this.bolls_arr.get(i).setPrice(precio);
        }
    }

    /**
     * Revisamos si un bollinger de este periodo ya fue creado y regresamos su
     * posición.
     *
     * @param periodo
     * @return
     */
    private int getExistingBoll(int periodo) {
        int temp = -1;
        for (int i = 0; i < this.bolls_arr.size(); i++) {
            if (this.bolls_arr.get(i).getSize() == periodo) {
                temp = i;
                break;
            }
        }
        return temp;
    }
    /**
     * Obtenemos minutos de hora en formato 1234 = 34.
     *
     * @param hora
     * @return
     */
    private Integer getMinVela(int hora) {
        int uni = (int) (hora % 100 / 1);
        return uni;
    }
}


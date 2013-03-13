package oms.Grafica.indicators;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import oms.Grafica.DAO.MongoDao;
import oms.Grafica.GMTDate;
import java.util.ArrayList;
import static oms.Grafica.GMTDate.getDate;

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
    private ArrayList<BollingerBands> bolls_arr = new ArrayList();

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
     * @param periodo
     * @return el bollinger regresamos el bollinger creado para que sea referenciado.
     */
    public BollingerBands createBollinger(int periodo) {
        int exists = this.getExistingBoll(periodo);
        if(exists>=0){
            return this.bolls_arr.get(exists);
        }else{
            ArrayList data = new ArrayList();
            MongoDao dao = new MongoDao();
            int cont =0;
            DBCursor cursor = dao.getCandleData(this.symbol, (periodo * periodoGrafica) + this.dif);
            int last_hora=0;
            Double last_close=0.0;
            int resta;

            while(cursor.hasNext() && data.size()< periodo) {
                DBObject temp = cursor.next();
                Double open = (Double)temp.get("Open");
                Integer hora=0;
                hora = (Integer)temp.get("hour");
                resta = last_hora - hora;
                //getDate().getMinute()-(periodo*(getDate().getMinute()/periodo));
                /**
                 * Usamos el tipo de técnica para obtener si un minuto es la apertura
                 * de vela, como en el metodo GMTDate.getMod.
                 **/
                int mins=getMinVela(hora/100);
                if(mins -(this.periodoGrafica*(mins/this.periodoGrafica))== 0){
                    data.add(open);
                /**
                 * Si la hora no es del mod de la grafica, pero hay una diferencia
                 *entre las horas debemos de revisar que dentro de esa diferencia no
                 * haya una apertura de vela
                */ 
                }else if(resta>100 && !(resta >= 4100)){

                    for(int i=last_hora-100;i> hora;i=i-100){
                        /**
                         * si en i debería de haber una apertura de grafica, guardamos
                         * el último close 
                         */
                        if((i/100)%this.periodoGrafica==0 && data.size()<periodo){
                            data.add(last_close);
                        }
                    }
                }else if((resta - 4100)>100){
                    //Si hay un lag al cambiar de hora.
                }
                cont++;
                last_hora = hora;
                last_close = (Double)temp.get("Close");
            }
            this.bolls_arr.add(new BollingerBands(periodo, data));
            return this.bolls_arr.get(this.bolls_arr.size()-1);
        }
    }
    
    /**
     * alimentamos los bollingers con un nuevo valor, normalmente todos los indicadores
     * se deberan de actualizar con este precio.
     * @param precio 
     */
    public void appendBollsData(Double precio){
        for(int i=0; i<this.bolls_arr.size();i++){
            this.bolls_arr.get(i).setPrice(precio);
        }
    }
    /**
     * Revisamos si un bollinger de este periodo ya fue creado y regresamos su 
     * posición.
     * @param periodo 
     * @return 
     */
    private int getExistingBoll(int periodo){
        int temp = -1;
        for (int i = 0; i < this.bolls_arr.size(); i++) {
            if(this.bolls_arr.get(i).getSize() == periodo){
                temp = i;
                break;
            }
        }
        return temp;
    }
    /**
     * Obtenemos minutos de hora en formato 1234 = 34.
     * @param hora
     * @return 
     */
    private Integer getMinVela(int hora){
        int uni=(int)(hora%100/1);
                
        return uni;
    }
    
}


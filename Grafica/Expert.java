package oms.Grafica;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.util.Properties;
import oms.Grafica.DAO.MongoDao;
import oms.Grafica.indicators.BollingerBands;
import oms.Grafica.indicators.Indicador;
import oms.util.idGenerator;

/**
 * Un Expert es el la parte del código que controla la apertura y cierre de las
 * operaciones, esto se hace mediante el uso de indicadores en nuestro caso 
 * particular usamos las "Bollinger Bands".
 * @author omar
 */
public class Expert extends Settings {

    Properties config = new Properties();
    
    private Indicador indicador;
    public String id;
    BollingerBands bollBand1;
    private BollingerBands bollBand2;
    private BollingerBands bollBand3;
    private BollingerBands bollBandS1;
    private BollingerBands bollBandS2;
    private BollingerBands bollBandS3 ;
    
    private double promedio;
    private int velasCont = 0;
    private double point = 0.0001;
    private int velas = 0;
    private double open_min=0.0;
    private double bid=0.0;
    private double ask=0.0;
    public Order order;
    private Date date = new oms.Grafica.Date();
    private idGenerator idord = new idGenerator();
    private int cont=0;
    /**
     * Constructor...
     * @param symbol Indica el par de monedas con el se va a trabajar.
     */
    public Expert(String symbol, String id) {
        //llamamos a el constructor de el padre (Settings).
        super(symbol);
        order = new Order(symbol,this.MAGICMA, id);
        indicador = new Indicador(Graphic.unSlash(this.symbol),5);
        
        /**
         * Añadimos los periodos a las bandas.
         */
        bollBand1 = indicador.createBollinger(this.boll1);
        bollBand2 = indicador.createBollinger(this.boll2);
        bollBand3 = indicador.createBollinger(this.boll3);
        
        bollBandS1 = indicador.createBollinger(this.bollS1);
        bollBandS2 = indicador.createBollinger(this.bollS2);
        bollBandS3 = indicador.createBollinger(this.bollS3);
        this.id = id;
    }
    
    /**
     * Se llama cuando un se recibe un bid.
     * **NOTA:
     * Este método es muy importante ya que es el que trata la apertura y cierre
     * de operaciones, así que: ¡tratalo con RESPETO!
     * @param price precio de apertura del minuto!
     */
    public void onTick(Double bid) {
        this.bid = bid;
        //Si no es sabado
       
        if (date.getDayWeek() != 6 && open_min>0) {
            if (ask-bid <= this.spread* this.point) {
                //System.out.println((this.open_min + this.boll_special) +" - "+ this.bollDn());
                //System.out.println(  this.bollDn() + " - "+this.open_min + " "+ this.boll_special+ " - "+this.bollUp());
                if((this.open_min + this.boll_special)<=this.bollDn() && limiteCruce()){
                    //Compra
                    System.err.println("Venta desde expert");
                    order.Open(this.ask, '2');
                }else if((this.open_min - this.boll_special)>=this.bollUp() && limiteCruce()){
                    //Venta
                    System.err.println("Compra desde expert");
                    order.Open(this.bid, '1');
                }
            }
        }
        
    }
    
    /**
     * Se llama cuando se recibe un cambio de vela.
     * @param price precio de apenew idGenerator().getID()rtura de la nueva vela.
     */
    public void onCandle(Double price){
        setPriceBoll(price);
         
    }
    public void onOpen(double price){
        open_min = price;
    }
    /**
     * Refrescamos las bandas con el precio de apertura de la vela.
     * @param price 
     */
    private void setPriceBoll(double price){
        bollBand1.setPrice(price);
        bollBand2.setPrice(price);
        bollBand3.setPrice(price);
        bollBandS1.setPrice(price);
        bollBandS2.setPrice(price);
        bollBandS3.setPrice(price);
    }
    
    /**
     * Obtenemos el promedio de bollinger de entrada (UP).
     * @return 
     */
    private double bollUp() {
        return Math.rint(((bollBand1.getUpperBand() + bollBand2.getUpperBand() + 
                            bollBand3.getUpperBand()) / 3)*100000)/100000;
    }
    
    /**
     * Obtenemos el promedio de bollinger de salida (UP).
     * @return 
     */
    private double bollUpS() {
        return Math.rint(((bollBandS1.getUpperBand() + bollBandS2.getUpperBand() + 
                            bollBandS3.getUpperBand()) / 3)*100000)/100000;
    }

    /**
     * Obtenemos el promedio de bollinger de entrada (Down).
     * @return 
     */
    private double bollDn() {

        return Math.rint(((bollBand1.getLowerBand() + bollBand2.getLowerBand() + 
                            bollBand3.getLowerBand()) / 3)*100000)/100000;
    }
    
    /**
     * Obtenemos el promedio de bollinger de salida (Down).
     * @return 
     */
    private double bollDnS() {

        return Math.rint(((bollBandS1.getLowerBand() + bollBandS2.getLowerBand() + 
                            bollBandS3.getLowerBand()) / 3)*100000)/100000;
    }
    
    /**
     * Método que regresa los valores que la clase Settings lee del archivo de
     * configuración .set
     * @return settings del expert.
     */
    public StringBuffer getExpertInfo() {

        StringBuffer init = new StringBuffer();

        init.append("\"settings\" : {");
            init.append("\"symbol\" : \"" + this.symbol+"\",");
            init.append("\"ID\" : \"" + this.id + "\",");
            init.append("\"magicma\" : " + this.MAGICMA + ",");
            init.append("\"lots\" : " + this.lots + ",");
            init.append("\"boll1\" : " + this.boll1 + ",");
            init.append("\"boll2\" : " + this.boll2 + ",");
            init.append("\"boll3\" : " + this.boll3 + ",");
            init.append("\"bollS1\" : " + this.bollS1 + ",");
            init.append("\"bollS2\" : " + this.bollS2 + ",");
            init.append("\"bollS3\" : " + this.bollS3 + ",");
            init.append("\"tp\" : " + this.tp +",");
            init.append("\"sl\" : " + this.sl +",");
            init.append("\"velasS\": " + this.velasS+",");
            init.append("\"horaIni\":" + this.horaIni+",");
            init.append("\"horaFin\" :" + this.horaFin);
            init.append("}");
        return init;
    }
    
    /**
     * Método usaddo para informar sobre el estado actual del expert, regresa los
     * valores de promedios, de velas, etc. Todos los valores que influyen en el
     * comportamiento actual o futuro del expert.
     * @return estado de los valoes.
     */
    public String getExpertState(){
        StringBuffer temp = new StringBuffer();
        temp.append("\"variables\":{");
            temp.append("\"bollUp\":"+this.bollUp() + ",");
            temp.append("\"bollDn\":"+this.bollDn() + ",");
            temp.append("\"bollUpS\":"+this.bollUpS() + ",");
            temp.append("\"bollDnS\":"+this.bollDnS());
        
        temp.append("}");
        return temp.toString();
    }
    /**
     * Guardamos el valor del ask.
     * @param ask 
     */
    public void setAsk(Double ask){
        this.ask = ask;
    }
    
    /**
     * regresamos si nos encontramos dentro del limite de operaciones por cruce.
     * (por cruce significa por el symbol).
     * @return 
     */
    public boolean limiteCruce(){
        boolean temp = false;
        int count = Graphic.dao.getTotalCruce(this.symbol);
        if(count<this.limiteCruce)
            temp = true;
        return temp;
    }
}
package oms.Grafica;

import java.util.Properties;
import oms.Grafica.indicators.BollingerBands;
import oms.Grafica.indicators.Indicador;
import java.util.Random;

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
    private double lastOpen=0.0;
    private double ask=0.0;
    private double bid=0.0;
    /**
     * Constructor...
     * @param symbol Indica el par de monedas con el se va a trabajar.
     */
    public Expert(String symbol) {
        //llamamos a el constructor de el padre (Settings).
        super(symbol);
        indicador = new Indicador(Graphic.unSlash(this.symbol),5);
        this.id = this.getId();
        System.out.println("Expert Listo..");
        /**
         * Añadimos los periodos a las bandas.
         */
        bollBand1 = indicador.createBollinger(this.boll1);
        bollBand2 = indicador.createBollinger(this.boll2);
        bollBand3 = indicador.createBollinger(this.boll3);
        
        bollBandS1 = indicador.createBollinger(this.bollS1);
        bollBandS2 = indicador.createBollinger(this.bollS2);
        bollBandS3 = indicador.createBollinger(this.bollS3);
      
        System.out.println(this.getExpertState().toString());
    }
    
    /**
     * Se llama cuando un se recibe un precio de apertura de minuto.
     * **NOTA:
     * Este método es muy importante ya que es el que trata la apertura y cierre
     * de operaciones, así que tratalo con RESPETO!
     * @param price precio de apertura del minuto!
     */
    public void onTick(Double price) {
    
        System.out.println("tick " + price);
    }
    
    /**
     * Se llama cuando se recibe un cambio de vela.
     * @param price precio de apertura de la nueva vela.
     */
    public void onCandle(Double price){
        setPriceBoll(price); 
        System.out.println("Up " + this.bollUp() + " Dn " + this.bollDn());
    }
    
    /**
     * Refrescamos las bandas con el precio de apertura de la vela.
     * @param price 
     */
    private void setPriceBoll(double price){
        System.out.println(price);
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

        StringBuffer setts = new StringBuffer();

        setts.append("\"settings\" : {");
            setts.append("\"symbol\" : \"" + this.symbol+"\",");
            setts.append("\"ID\" : \"" + this.id + "\",");
            setts.append("\"magicma\" : " + this.MAGICMA + ",");
            setts.append("\"lots\" : " + this.lots + ",");
            setts.append("\"boll1\" : " + this.boll1 + ",");
            setts.append("\"boll2\" : " + this.boll2 + ",");
            setts.append("\"boll3\" : " + this.boll3 + ",");
            setts.append("\"bollS1\" : " + this.bollS1 + ",");
            setts.append("\"bollS2\" : " + this.bollS2 + ",");
            setts.append("\"bollS3\" : " + this.bollS3 + ",");
            setts.append("\"tp\" : " + this.tp +",");
            setts.append("\"sl\" : " + this.sl +",");
            setts.append("\"velasS\": " + this.velasS+",");
            setts.append("\"horaIni\":" + this.horaIni+",");
            setts.append("\"horaFin\" :" + this.horaFin);
        setts.append("}");
            
        return setts;
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
     * Este método genera un id único para cada expert/gráfica.
     * @return id único.
     */
    private String getId(){
        
        StringBuffer str = new StringBuffer(this.symbol+"-");
        Random r = new Random();
        Random r2 = new Random();
        int n = 25; // 65-90 codigo ASCII.
        
        
        for (int j=0; j<4; j++){
            int i = r.nextInt() % n;
            if ( (65 + i <= 90) && (65 + i >= 65)){
                str.append ((char) (65 + i));  
            }
            else j--;//<---------|| CUIDADO ESTO ES UNA CHARRADA!
        }
        return str.toString();
    }
    /**
     * Guardamos el valor del bid.
     * @param bid 
     */
    public void setBid(Double bid){
        this.bid = bid;
    }
    /**
     * Guardamos el valor del ask.
     * @param ask 
     */
    public void setAsk(Double ask){
        this.ask = ask;
    }
}
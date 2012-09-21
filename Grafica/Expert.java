package oms.Grafica;

import java.util.Properties;
import oms.Grafica.indicators.BollingerBands;
import oms.Grafica.indicators.Indicador;
import oms.util.idGenerator;

/**
 * Un Expert es el la parte del código que controla la apertura y cierre de las
 * operaciones, esto se hace mediante el uso de indicadores en nuestro caso 
 * particular usamos las "Bollinger Bands". Está basada en el expert "Bollinger
 * sin FIFO 1_8 velas entrada y salida minuto Spread SV".
 * @author omar
 */
public class Expert extends Jedi{

    Properties config = new Properties();
    
    private Indicador indicador;
    BollingerBands bollBand1;
    private BollingerBands bollBand2;
    private BollingerBands bollBand3;
    private BollingerBands bollBandS1;
    private BollingerBands bollBandS2;
    private BollingerBands bollBandS3 ;
    
    private double promedio;
    private int velasCont = 0;
    private int velas = 0;
    private double open_min=0.0;
    private double bid=0.0;
    private double ask=0.0;
    private Date date = new oms.Grafica.Date();
    private idGenerator idord = new idGenerator();
    private int contVelas=0;
    //Esta la usamos para que no entre a revisar la salida de operaciones si no 
    //hay operaciones.
    private double askMinuto;
    private double bollSell;
    private int periodo;
   
    /**
     * Constructor...
     * @param symbol Indica el par de monedas con el se va a trabajar.
     */
    public Expert(String symbol,int periodo, Settings setts) {
        //llamamos a el constructor de el padre (Settings).
        super(setts, periodo);
        order = new Order(symbol,setts.MAGICMA, setts.id);
        indicador = new Indicador(Graphic.unSlash(setts.symbol),periodo);
        
        /**
         * Añadimos los periodos a las bandas.
         */
        bollBand1 = indicador.createBollinger(setts.boll1);
        bollBand2 = indicador.createBollinger(setts.boll2);
        bollBand3 = indicador.createBollinger(setts.boll3);
        
        bollBandS1 = indicador.createBollinger(setts.bollS1);
        bollBandS2 = indicador.createBollinger(setts.bollS2);
        bollBandS3 = indicador.createBollinger(setts.bollS3);
        this.periodo = periodo;
    }
    
    /**
     * Se llama cuando un se recibe un bid.
     * **NOTA:
     * Este método es muy importante ya que es el que trata la apertura y cierre
     * de operaciones, así que: ¡tratalo con RESPETO!
     * @param price precio de apertura del minuto!
     */
    @Override
    public void onTick(Double bid) {
        this.bid = bid;
        askMinuto = this.open_min + (ask-bid);
        bollSell = this.bollDnS() + (setts.Point * setts.spreadAsk); //Este promedio es usado para sacar las ventas.
        //Si no es sabado trabajamos, si es sabado no hacemos nada. Sí, hasta los programas
        //descansan por lo menos un día de la semana...
        if (open_min > 0 && this.range(date.getHour())) { //TODO Borrar la condicion de open_min.
            //Revisamos que los precios se encuentren dentro de el rango de entrada.
            if (lock && ask - bid <= setts.spread * setts.Point){
                //entrada de operaciones
                if ((this.open_min + setts.boll_special) <= this.bollDn() && limiteCruce()) {
                    //Compra
                    orderSend(this.bid, '1');
                    contVelas =0;
                } else if ((this.open_min - setts.boll_special) >= this.bollUp() && limiteCruce()) {
                    //Venta
                    orderSend(this.ask, '2');
                    contVelas =0;
                }
            //Revisamos que haya entrado alguna operación y que los precios se 
            //encuentren dentro de el rango de salida.    
            } 
            if (!lock && (ask - bid <= setts.spreadSalida * setts.Point)) {
                if (setts.salidaBollinger) {
                    //Cierre de compras por promedios bollinger.
                    if (this.currentOrder == '1') {
                        //si el precio de apertura supera a el promedio de salida
                        //entonces debemos cerrar todas las compras
                        if (this.open_min >= this.bollUpS()) {
                            System.out.println("Cerrado orden por bollinger");
                            orderClose(bid,'1');
                        }
                    } else if (this.currentOrder == '2') {
                        //si el precio de apertura es inferior a el promedio de salida
                        //entonces debemos cerrar todas las ventas.
                        //esta salida es especifica de la version 1.8 velas entrada y salida cierre minuto spread SV C1.
                        if ( ((this.open_min + this.askMinuto)/2) <= (this.bollDnS() + this.bollSell)/2) {
                            System.out.println("Cerrado orden por bollinger");
                            orderClose(this.ask,'2');
                            //Cerramos las ordenes...
                        }
                    } else if (this.currentOrder == '0') {
                        System.err.println("Fuckin fuck - Nunca debimos entrar aqui Salida Boll");
                    }
                } 
                /**
                 * si el numero de velas que van desde que entro la operación es igual
                 * a las velas de salida (velasS) tenemos que cerrar las operaciones
                 */
               if (contVelas==setts.velasS || this.rangeSalida(date.getHour())) {
                    if (this.currentOrder == '1') {
                        System.out.println("Cerrando orden por velas");
                        order.Close(bid,'1');
                    }else if (this.currentOrder == '2') {
                        System.out.println("Cerrando orden por velas");
                        order.Close(this.ask,'2');
                        
                    }else if (this.currentOrder == '0') {
                        System.err.println("Fuckin fuck - Nunca debimos entrar aqui Salida Variada");
                    }
                }
            }
        }
    }
    
    /**
     * Se llama cuando se recibe un cambio de vela.
     * @param price precio de apenew idGenerator().getID()rtura de la nueva vela.
     */
    @Override
    public void onCandle(Double price){
        setPriceBoll(price);
        if(currentOrder!='0')
            contVelas ++;
         
    }
    @Override
    public void onOpen(Double price){
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
     * Método usaddo para informar sobre el estado actual del expert, regresa los
     * valores de promedios, de velas, etc. Todos los valores que influyen en el
     * comportamiento actual o futuro del expert.
     * @return estado de los valoes.
     */
    public String getExpertState(){
        StringBuffer temp = new StringBuffer();
        temp.append("\"variables\":{");
            temp.append("\"bollUp\":"+ this.bollUp()+ ",");
            temp.append("\"bollDn\":"+ this.bollDn()+ ",");
            temp.append("\"bollUpS\":"+this.bollUpS() + ",");
            temp.append("\"bollDnS\":"+this.bollDnS()+ ",");
            temp.append("\"Velas\":"+this.contVelas);
        
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
     * Desbloqueamos al expert para que deje de buscar cierre de operaciones y busque
     * aperturas.
     */
    /*public void unlock(){
        this.lock = true;
    }
    */
    /**
     * verificamos que nos encontremos en horas de operacion.
     * @param hora
     * @return 
     */
    public boolean range(int hora){
        boolean temp=false;
        if(hora < setts.horaFin && hora >= setts.horaIni)
            temp=true;
        return temp;
    }
    /*
     * verificamos que nos encontremos en horas de salida de operaciones.
     */
    public boolean rangeSalida(int hora){
        boolean temp=false;
        if(hora < setts.horaFinS && hora >= setts.horaIniS)
            temp=true;
        return temp;
    }
}
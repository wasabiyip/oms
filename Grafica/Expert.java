package oms.Grafica;

import java.util.Properties;
import oms.Grafica.indicators.BollingerBands;
import oms.Grafica.indicators.Indicador;
import oms.util.Console;
import oms.util.idGenerator;

/**
 * Un Expert es el la parte del código que controla la apertura y cierre de las
 * operaciones, esto se hace mediante el uso de indicadores en nuestro caso 
 * particular usamos las "Bollinger Bands". Está basada en el expert "Bollinger
 * sin FIFO 1_8 velas entrada y salida minuto Spread SV" con severos ajustes en la
 * entrada y salida de operaciones.
 * @author omar
 */
public class Expert extends Jedi{

    Properties config = new Properties();
    
    private Indicador indicador;
    private BollingerBands bollBand1;
    private BollingerBands bollBand2;
    private BollingerBands bollBand3;
    private BollingerBands bollBandS1;
    private BollingerBands bollBandS2;
    private BollingerBands bollBandS3 ;
    private BollingerBands bollBandx1;
    private BollingerBands bollBandx2;
    private BollingerBands bollBandx3 ;
    
    private double promedio;
    
    private int velas = 0;    
    
    private idGenerator idord = new idGenerator();
    private double volB;
    private double volS;
    boolean temp =  true;
    //Esta la usamos para que no entre a revisar la salida de operaciones si no 
    //hay operaciones.
    private int periodo;
    /**
     * Constructor...
     * @param symbol Indica el par de monedas con el se va a trabajar.
     */
    public Expert(Settings setts) {
        //llamamos a el constructor de el padre (Settings).
        super(setts);
        
        indicador = new Indicador(setts.symbol,setts.periodo);
        
        /**
         * Añadimos los periodos a las bandas.
         */
        bollBand1 = indicador.createBollinger(setts.boll1);
        bollBand2 = indicador.createBollinger(setts.boll2);
        bollBand3 = indicador.createBollinger(setts.boll3);
        
        bollBandS1 = indicador.createBollinger(setts.bollS1);
        bollBandS2 = indicador.createBollinger(setts.bollS2);
        bollBandS3 = indicador.createBollinger(setts.bollS3);
        
        bollBandx1 = indicador.createBollinger(setts.bollx1);
        bollBandx2 = indicador.createBollinger(setts.bollx2);
        bollBandx3 = indicador.createBollinger(setts.bollx3);
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
        //Si no es sabado trabajamos, si es sabado no hacemos nada. Sí, hasta los programas
        //descansan por lo menfos un día de la semana...
        
        if (this.isActive()) { 
            if(limiteCruce() && (this.getAvgOpen() + this.setts.boll_special) <= this.getAvgBoll(this.bollDn())){
                System.err.println("Deberiamos de meter Compras!! -> " + this.setts.symbol + " - " + this.setts.MAGICMA);
                System.err.println("Spread "+(ask - bid <= setts.spread));
                System.err.println("bollX " + (this.bollingerDif() < this.setts.bollxUp && 
                    this.bollingerDif()> setts.bollxDn));               
                System.err.println("Lock: "+ lock_op); 
            }else if (limiteCruce() && this.getAvgOpen() - this.setts.boll_special >= this.getAvgBoll(this.bollUp())){
                System.err.println("Deberiamos de meter ventas!! -> "+ this.setts.symbol + " - " + this.setts.MAGICMA);
                System.err.println("Spread "+(ask - bid <= setts.spread));
                System.err.println("bollX " + (this.bollingerDif() < this.setts.bollxUp && 
                    this.bollingerDif()> setts.bollxDn));                
                System.err.println("Lock: "+ lock_op);                
            }
            
            //Revisamos que los precios se encuentren dentro de el rango de entrada.
            if (ask - bid <= setts.spread * setts.Point && lock_op && 
                    this.bollingerDif() < this.setts.bollxUp && 
                    this.bollingerDif()> setts.bollxDn && limiteCruce()){
                //entrada de operaciones.
                if ((this.getAvgOpen() + this.setts.boll_special) <= this.getAvgBoll(this.bollDn())) {
                    //Compra
                    orderSend(this.bid, '1');
                    
                } else if (this.getAvgOpen() - this.setts.boll_special >= this.getAvgBoll(this.bollUp())) {
                    //Venta
                    orderSend(this.ask, '2');
                }
            //Revisamos que haya entrado alguna operación y que los precios se 
            //encuentren dentro de el rango de salida.    
            }
            //System.out.println(!lock+" "+ (ask - bid) +" "+ (setts.spreadSalida * setts.Point));
            if (!lock_op && (ask - bid <= setts.spreadSalida * setts.Point)) {
                if (setts.salidaBollinger) {
                    //Cierre de compras por promedios bollinger.
                    if (this.currentOrder == '1') {
                        //si el promedio de el precio de apertura supera a el promedio de salida
                        //entonces debemos cerrar todas las compras
                        if (this.getAvgOpen() >= this.getAvgBoll(this.bollUpS())) {
                            System.out.println("Cerrado orden por bollinger");
                            orderClose(this.bid,'1');
                        }
                    } else if (this.currentOrder == '2') {
                        //si el precio de apertura es inferior a el promedio de salida
                        //entonces debemos cerrar todas las ventas.
                        //esta salida es especifica de la version 1.8 velas entrada y salida cierre minuto spread SV.
                        if ( ((this.getAvgOpen())) <= (this.getAvgBoll(this.bollDnS()))) {
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
               
               if (contVelas == setts.velasS || this.rangeSalida()) {
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
            /**
             * Volatilidad: Si al haber entrado una orden regresa al punto de entrada movemos 
             * el tp.
             */
            if(!this.lock_op && !modify && this.setts.volatilidad){
                this.volB = this.lastOrderPrice - setts.volVal;
                this.volS = this.lastOrderPrice + setts.volVal;
                if(this.currentOrder == '1' && this.bid <= volB){
                    orderModify();

                }else if(this.currentOrder == '2' && this.ask >= volS){
                    orderModify();
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
        this.contVelas ++;
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
    public double bollUp() {
        return (bollBand1.getUpperBand() + bollBand2.getUpperBand() + 
                            bollBand3.getUpperBand())/3;
        
    }
    
    /**
     * Obtenemos el promedio de bollinger de salida (UP).
     * @return 
     */
    private double bollUpS() {
        return (bollBandS1.getUpperBand() + bollBandS2.getUpperBand() + 
                            bollBandS3.getUpperBand()) / 3;
    }

    /**
     * Obtenemos el promedio de bollinger de entrada (Down).
     * @return 
     */
    public double bollDn() {

        return (bollBand1.getLowerBand() + bollBand2.getLowerBand() + 
                            bollBand3.getLowerBand()) / 3;
    }
    
    /**
     * Obtenemos el promedio de bollinger de salida (Down).
     * @return 
     */
    private double bollDnS() {

        return (bollBandS1.getLowerBand() + bollBandS2.getLowerBand() + 
                            bollBandS3.getLowerBand()) / 3;
    }
    
    private double bollingerDif(){
        double tempUp = (bollBandx1.getUpperBand() + bollBandx2.getUpperBand() + bollBand3.getUpperBand())/3 ;
        double tempDn = (bollBandx1.getLowerBand() + bollBandx2.getLowerBand() + bollBand3.getLowerBand())/3;
        double temp = tempUp - tempDn;
        return temp;
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
            temp.append("\"bollUp\":"+ redondear(this.getAvgBoll(this.bollUp()))+ ",");
            temp.append("\"bollDn\":"+ redondear(this.getAvgBoll(this.bollDn()))+ ",");
            temp.append("\"bollUpS\":"+redondear(this.getAvgBoll(this.bollUpS())) + ",");
            temp.append("\"bollDnS\":"+redondear(this.getAvgBoll(this.bollDnS()))+ ",");
            temp.append("\"Velas\":"+this.contVelas + ",");
            temp.append("\"limite\":"+this.limiteCruce() + ",");
            temp.append("\"hora\":"+this.hora()+ ",");
            temp.append("\"bollX\":"+(this.bollingerDif() < this.setts.bollxUp && 
                    this.bollingerDif()> setts.bollxDn)+ ",");
            temp.append("\"Active\":"+this.isActive());
        temp.append("}");
        return temp.toString();
    }
    public String getRemain(){
        StringBuffer temp = new StringBuffer();
        temp.append("\"variables\":{");
            temp.append("\"bollUp\":"+ redondear((this.getAvgOpen() + this.setts.boll_special) - this.getAvgBoll(this.bollDn())) + ",");
            temp.append("\"bollDn\":"+ redondear(redondear(this.getAvgBoll(this.bollUp()))-redondear(this.getAvgOpen() - this.setts.boll_special)) + ",");
            temp.append("\"bollUpS\":"+ redondear(((this.getAvgOpen())) - (this.getAvgBoll(this.bollUpS()))) + ",");
            temp.append("\"bollDnS\":"+ redondear(redondear(this.getAvgBoll(this.bollDnS())) - redondear((this.getAvgOpen()))) + ",");
            temp.append("\"Hora\":"+ this.rangeSalida());        
        temp.append("}");
        return temp.toString();
    }
    
}
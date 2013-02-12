/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import oms.Grafica.indicators.BollingerBands;
import oms.deliverer.Orden;

/**
 *
 * @author omar
 */
public class ExpertMoc extends AbstractExpert{
        
    private BollingerBands bollBand1;
    private BollingerBands bollBand2;
    private BollingerBands bollBand3;
    private BollingerBands bollBandS1;
    private BollingerBands bollBandS2;
    private BollingerBands bollBandS3 ;
    private BollingerBands bollBandx1;
    private BollingerBands bollBandx2;
    private BollingerBands bollBandx3;
    private double volB;
    private double volS;
    private double bollUp;
    private double bollDn;
    private double bollDif;
    private double bollUpS;
    private double bollDnS;
    private Integer startTime;
    private int cont_velas;
    @Override
    public void Init() {
        
        bollBand1 = indicator.createBollinger(setts.boll1);
        bollBand2 = indicator.createBollinger(setts.boll2);
        bollBand3 = indicator.createBollinger(setts.boll3);
        
        bollBandS1 = indicator.createBollinger(setts.bollS1);
        bollBandS2 = indicator.createBollinger(setts.bollS2);
        bollBandS3 = indicator.createBollinger(setts.bollS3);
        
        bollBandx1 = indicator.createBollinger(setts.bollx1);
        bollBandx2 = indicator.createBollinger(setts.bollx2);
        bollBandx3 = indicator.createBollinger(setts.bollx3);
        bollUp = this.getAvgBoll(this.bollUp());
        bollDn = this.getAvgBoll(this.bollDn());
        bollDif = this.bollingerDif();
        bollUpS = this.getAvgBoll(this.bollUpS());
        bollDnS = this.getAvgBoll(this.bollDnS());
        this.cont_velas = 0;
        this.startTime = this.TimeCurrent() - (this.TimeCurrent()%this.Periodo);
    }
    /**
     * Se llama cuando un se recibe un bid.
     * **NOTA:
     * Este método es muy importante ya que es el que trata la apertura y cierre
     * de operaciones, así que: ¡tratalo con RESPETO!
     * @param price precio de apertura del minuto!
     */
    @Override
    public void onTick() {
        if(this.OrdersCount() < 1){
            orderSend(this.Ask, this.setts.lots, '1', Ask-this.setts.sl, Ask+this.setts.tp);
        }else if(this.OrdersCount()>0){
            for (int i = 0; i < OrdersTotal().size(); i++) {
                Orden currentOrden = OrdersTotal().get(i);
                if (currentOrden.getSide() == '1') {
                    currentOrden.close(this.Ask);
                }else if (currentOrden.getSide() == '2'){
                    currentOrden.close(this.Bid);
                }     
            }
        }
        if (Math.abs(this.startTime - this.TimeCurrent()) >= this.Periodo) {
            this.startTime = this.TimeCurrent();
            bollUp = this.getAvgBoll(this.bollUp());
             bollDn = this.getAvgBoll(this.bollDn());
            /*bollUp = this.bollUp();
            bollDn = this.bollDn();*/
            bollDif = this.bollingerDif();
            bollUpS = this.getAvgBoll(this.bollUpS());
            bollDnS = this.getAvgBoll(this.bollDnS());
            /*bollUpS = this.bollUpS();
            bollDnS = this.bollDnS();*/
            this.cont_velas++;
            
        }
        
        //Revisamos que los precios se encuentren dentro de el rango de entrada.
        /*if ((this.CurrentHora() < this.setts.horaFin) && (this.CurrentHora() >= this.setts.horaIni)
                && (this.OrdersCount() < 1) && (bollDif < this.setts.bollxUp && bollDif > setts.bollxDn)) {
            //entrada de operaciones.
            if ((this.open_min+ this.setts.boll_special) <= bollDn) {
                //Compra
                orderSend(this.Ask, this.setts.lots, '1', 0.0, 0.0);
                this.cont_velas = 0;
            } else if (this.open_min - this.setts.boll_special >= bollUp) {
                //Venta
                orderSend(this.Bid, this.setts.lots, '2', 0.0 ,0.0);
                this.cont_velas = 0;
            }
            //Revisamos que haya entrado alguna operación y que los precios se 
            //encuentren dentro de el rango de salida.    
            
        }else if (this.OrdersCount() > 0) {
            //System.out.println(contVelas);
            for (int i = 0; i < OrdersTotal().size(); i++) {
                Orden currentOrden = OrdersTotal().get(i);
                if (currentOrden.getSide() == '1') {
                    if (setts.salidaBollinger && this.open_min  >= bollUpS) {
                        //System.out.println("Cerrando orden por bollinger");
                        orderClose(this.Bid, '1');
                        break;
                    } else if (cont_velas == setts.velasS) {
                        //System.out.println("Cerrando orden por velas");
                        orderClose(this.Bid, '1');
                        break;
                    } else if (this.rangeSalida()) {
                        //System.out.println("Cerrando orden por minutos");
                        orderClose(this.Bid, '1');
                        break;
                    }else if(currentOrden.getSl() == 0 || currentOrden.getTp() == 0){
                        currentOrden.OrderModify(this.Bid-this.setts.sl, this.Bid+this.setts.tp);
                    }
                } else if (currentOrden.getSide() == '2') {
                    if (setts.salidaBollinger && this.open_min  <= bollDnS) {
                        //System.out.println("Cerrando orden por bollinger");
                        orderClose(this.Ask, '2');
                        break;
                        //Cerramos las ordenes...
                    } else if (cont_velas == setts.velasS) {
                        //System.out.println("Cerrando orden por velas");
                        orderClose(this.Ask, '2');
                        break;
                    } else if (this.rangeSalida()) {
                        //System.out.println("Cerrando orden por minutos");
                        orderClose(this.Ask, '2');
                        break;
                    }else if(currentOrden.getSl() == 0 || currentOrden.getTp() == 0){
                        currentOrden.OrderModify(this.Ask+this.setts.sl, this.Ask-this.setts.tp);
                    }
                }
                /**
                 * Volatilidad: Si al haber entrado una orden regresa al punto
                 * de entrada movemos el tp.
                 */
                /*if (this.setts.volatilidad) {
                    double volB = currentOrden.getOpenPrice() - this.setts.volVal;
                    double volS = currentOrden.getOpenPrice() + this.setts.volVal;
                    if(currentOrden.getSide() == '1') {
                        if(this.Bid <= volB) {
                            currentOrden.OrderModify(currentOrden.getOpenPrice() + this.setts.nwTp,currentOrden.getSl());
                        }
                    } else if(currentOrden.getSide() == '2') {
                        
                        if(this.Ask >= volS) {
                            currentOrden.OrderModify(currentOrden.getOpenPrice() - this.setts.nwTp, currentOrden.getSl());
                        }
                    }
                }
            }
        }*/
    }
    /*
     * Método que promedia un Promedio de bollingers con la variable spreadAask.
     */
    public double getAvgBoll(double boll){
        double temp = (boll  + (boll +(this.setts.spreadAsk * setts.Point)))/2;
        return this.redondear(temp);
    }
     
    /**
     * @return retornamos un promedio del precio de apertura relacionado con el Spread
     */
    public double getAvgOpen(){
        double temp = 0.0;
        temp = ((this.open_min) + (this.open_min +(this.Ask - this.Bid)))/2;
        return temp;
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
    double bollUpS() {
        return (bollBandS1.getUpperBand() + bollBandS2.getUpperBand() + 
                            bollBandS3.getUpperBand()) / 3;
    }

    /**
     * Obtenemos el promedio de bollinger de entrada (Down).
     * @return 
     */
    double bollDn() {

        return (bollBand1.getLowerBand() + bollBand2.getLowerBand() + 
                            bollBand3.getLowerBand()) / 3;
    }
    
    /**
     * Obtenemos el promedio de bollinger de salida (Down).
     * @return 
     */
    double bollDnS() {

        return (bollBandS1.getLowerBand() + bollBandS2.getLowerBand() + 
                            bollBandS3.getLowerBand()) / 3;
    }
    
    double bollingerDif(){
        double tempUp = (bollBandx1.getUpperBand() + bollBandx2.getUpperBand() + bollBand3.getUpperBand())/3 ;
        double tempDn = (bollBandx1.getLowerBand() + bollBandx2.getLowerBand() + bollBand3.getLowerBand())/3;
        double temp = tempUp - tempDn;
        return temp;
    }
    /*
     * verificamos que nos encontremos en horas de salida de operaciones.
     */
    boolean rangeSalida(){
        Date date = new oms.Grafica.Date();  
        double hora = date.getHour() + (date.getMinute()*0.01);
        boolean temp=false;
        if(hora < setts.horaFinS && hora >= setts.horaIniS)
            temp=true;
        return temp;
    }
}

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
        /*bollUp = this.bollUp();
        bollDn = this.bollDn();
        bollDif = this.bollingerDif();
        bollUpS = this.bollUpS();
        bollDnS = this.bollDnS();*/
        
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
        if(true){
            //System.out.println(this.CurrentHora()+" hora: "+((this.CurrentHora() < this.setts.horaFin)&& (this.CurrentHora() >= this.setts.horaIni)));
            //System.out.println("hora: "+((this.CurrentHora() < this.setts.horaFin)&& (this.CurrentHora() >= this.setts.horaIni))+
              //   " ordenes: "+(this.OrdersCount() < 1) + " bollDif:"+(bollDif < this.setts.bollxUp && bollDif > setts.bollxDn));
           
        }
        //Revisamos que los precios se encuentren dentro de el rango de entrada.
        if ((this.CurrentHora() < this.setts.horaFin) && (this.CurrentHora() >= this.setts.horaIni)
                && (this.OrdersCount() < 1) && (bollDif < this.setts.bollxUp && bollDif > setts.bollxDn)) {
            //entrada de operaciones.
            if ((this.open_min+ this.setts.boll_special) <= bollDn) {
                //Compra
                orderSend(this.Ask, this.setts.lots, '1', this.Bid - this.setts.sl, this.Bid + this.setts.tp);
                this.cont_velas = 0;
            } else if (this.open_min - this.setts.boll_special >= bollUp) {
                //Venta
                orderSend(this.Bid, this.setts.lots, '2', this.Ask + this.setts.sl, this.Ask - this.setts.tp);
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
                        currentOrden.close(this.Bid);
                        break;
                    } else if (cont_velas == setts.velasS) {
                        //System.out.println("Cerrando orden por velas");
                        currentOrden.close(this.Bid);
                        break;
                    } else if (this.rangeSalida()) {
                        //System.out.println("Cerrando orden por minutos");
                        currentOrden.close(this.Bid);
                        break;
                    }else if(currentOrden.getSl() == 0 || currentOrden.getTp() == 0){
                        //currentOrden.Modify(this.Bid-this.setts.sl, this.Bid+this.setts.tp);
                    }
                } else if (currentOrden.getSide() == '2') {
                    if (setts.salidaBollinger && this.open_min  <= bollDnS) {
                        //System.out.println("Cerrando orden por bollinger");
                        currentOrden.close(this.Ask);
                        break;
                        //Cerramos las ordenes...
                    } else if (cont_velas == setts.velasS) {
                        //System.out.println("Cerrando orden por velas");
                        currentOrden.close(this.Ask);
                        break;
                    } else if (this.rangeSalida()) {
                        //System.out.println("Cerrando orden por minutos");
                        currentOrden.close(this.Ask);
                        break;
                    }else if(currentOrden.getSl() == 0 || currentOrden.getTp() == 0){
                        //currentOrden.Modify(this.Ask+this.setts.sl, this.Ask-this.setts.tp);
                    }
                }
            }
        }
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
        return this.open_min;
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

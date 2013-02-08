/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import oms.Grafica.indicators.BollingerBands;

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
        System.out.println(this.bollDn + " "+ this.bollUp);
        if(Math.abs(this.startTime - this.TimeCurrent()) >=this.Periodo){
            this.startTime = this.TimeCurrent();
            bollUp = this.getAvgBoll(this.bollUp());
            bollDn = this.getAvgBoll(this.bollDn());
            bollDif = this.bollingerDif();
            bollUpS = this.getAvgBoll(this.bollUpS());
            bollDnS = this.getAvgBoll(this.bollDnS());
            System.out.println("Nueva vela Expert");
            this.cont_velas++;
        }
        
        if (this.isActive()) { 
            
            /*if(limiteCruce() < this.setts.limiteCruce && (this.getAvgOpen() + this.setts.boll_special) <= this.getAvgBoll(this.bollDn())){
                System.err.println("Deberiamos de meter Compras!! -> " + this.setts.symbol + " - " + this.setts.MAGICMA);
                System.err.println("Spread "+(this.Ask - this.Bid <= setts.spread));
                System.err.println("bollX " + (this.bollingerDif() < this.setts.bollxUp && 
                    this.bollingerDif()> setts.bollxDn));               
                
            }else if ( limiteCruce() < this.setts.limiteCruce && this.getAvgOpen() - this.setts.boll_special >= this.getAvgBoll(this.bollUp())){
                System.err.println("Deberiamos de meter ventas!! -> "+ this.setts.symbol + " - " + this.setts.MAGICMA);
                System.err.println("Spread "+(this.Ask - this.Bid <= setts.spread));
                System.err.println("bollX " + (this.bollingerDif() < this.setts.bollxUp && 
                    this.bollingerDif()> setts.bollxDn));
            }*/
            if(this.isActive()){
                System.out.println("MAGIC "+this.setts.MAGICMA +"\n"+
                 "Up: "+this.getAvgBoll(this.bollUp()) + "\n"+
                 "Dn: "+this.getAvgBoll(this.bollDn()));
            }
            //Revisamos que los precios se encuentren dentro de el rango de entrada.
            if (this.Ask - this.Bid <= setts.spread * Point  && TotalMagic()< this.setts.limiteMagic &&
                    this.bollingerDif() < this.setts.bollxUp && 
                    this.bollingerDif()> setts.bollxDn && limiteCruce() < this.setts.limiteCruce){
                //entrada de operaciones.
                if ((this.getAvgOpen() + this.setts.boll_special) <= bollDn) {
                    //Compra
                    orderSend(this.Bid, '1');
                    //Iniciamos a contar velas
                    this.cont_velas =0;
                } else if (this.getAvgOpen() - this.setts.boll_special >= bollUp) {
                    //Venta
                    orderSend(this.Ask, '2');
                    //Iniciamos a contar velas
                    this.cont_velas =0;
                }
            //Revisamos que haya entrado alguna operación y que los precios se 
            //encuentren dentro de el rango de salida.    
            }
            //System.out.println(!lock+" "+ (ask - bid) +" "+ (setts.spreadSalida * setts.Point));
            if(TotalMagic()>0 && (this.Ask - this.Bid <= setts.spreadSalida * setts.Point)) {
                if (setts.salidaBollinger) {
                    //Cierre de compras por promedios bollinger.
                    if (this.currentOrder == '1') {
                        //si el promedio de el precio de apertura supera a el promedio de salida
                        //entonces debemos cerrar todas las compras
                        if (this.getAvgOpen() >= bollUpS) {
                            System.out.println("Cerrado orden por bollinger");
                            orderClose(this.Bid,'1');
                        }
                    } else if (this.currentOrder == '2') {
                        //si el precio de apertura es inferior a el promedio de salida
                        //entonces debemos cerrar todas las ventas.
                        //esta salida es especifica de la version 1.8 velas entrada y salida cierre minuto spread SV.
                        if ( ((this.getAvgOpen())) <= bollDnS) {
                            System.out.println("Cerrado orden por bollinger");
                            orderClose(this.Ask,'2');
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
               
               if (cont_velas == setts.velasS || this.rangeSalida()) {
                    if (this.currentOrder == '1') {
                        System.out.println("Cerrando orden por velas");
                        order.Close(this.Bid,'1');
                    }else if (this.currentOrder == '2') {
                        System.out.println("Cerrando orden por velas");
                        order.Close(this.Ask,'2');
                        
                    }else if (this.currentOrder == '0') {
                        System.err.println("Fuckin fuck - Nunca debimos entrar aqui Salida Variada");
                    }
                }
            //Fin Salidas
            }
            /**
             * Volatilidad: Si al haber entrado una orden regresa al punto de entrada movemos 
             * el tp.
             */
            //TODO quitar esto del Modify
            if(!modify && this.setts.volatilidad){
                this.volB = this.lastOrderPrice - setts.volVal;
                this.volS = this.lastOrderPrice + setts.volVal;
                if(this.currentOrder == '1' && this.Bid <= volB){
                    orderModify();

                }else if(this.currentOrder == '2' && this.Ask >= volS){
                    orderModify();
                }
            }
        //Fin isActive
        }
    //Fin onTick
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

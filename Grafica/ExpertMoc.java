/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.Grafica;

import oms.Grafica.indicators.BollingerBands;
import oms.deliverer.Orden;
import oms.util.Console;

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
        
        bollBand1 = this.iBands(5);
        System.out.println(bollBand1);
        /*bollBand2 = this.iBands(setts.boll2);
        bollBand3 = this.iBands(setts.boll3);
        
        bollBandS1 = this.iBands(setts.bollS1);
        bollBandS2 = this.iBands(setts.bollS2);
        bollBandS3 = this.iBands(setts.bollS3);
        
        bollBandx1 = this.iBands(setts.bollx1);
        bollBandx2 = this.iBands(setts.bollx2);
        bollBandx3 = this.iBands(setts.bollx3);
        /*bollUp = this.getAvgBoll(this.bollUp());
        bollDn = this.getAvgBoll(this.bollDn());
        bollDif = this.bollingerDif();
        bollUpS = this.getAvgBoll(this.bollUpS());
        bollDnS = this.getAvgBoll(this.bollDnS());
        bollUp = this.bollUp();
        bollDn = this.bollDn();
        bollDif = this.bollingerDif();
        bollUpS = this.bollUpS();
        bollDnS = this.bollDnS();
        this.cont_velas = 0;*/
        this.startTime = this.TimeCurrent() - this.getMod();
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
        
        if(this.TimeCurrent()-startTime >= this.Periodo){
            bollUp = this.bollUp();
            bollDn = this.bollDn();
            bollDif = this.bollingerDif();
            bollUpS = this.bollUpS();
            bollDnS = this.bollDnS();
            this.cont_velas++;
            Console.log(this);
            this.startTime = this.TimeCurrent();
        }
       // System.out.println(this);
        //Revisamos que los rmprecios se encuentren dentro de el rango de entrada.
        if ((this.Ask-this.Bid)<=(this.setts.spread*this.Point)&&(this.CurrentHora() < this.setts.horaFin) && (this.CurrentHora() >= this.setts.horaIni)
                && (this.OrdersBySymbol() < this.setts.limiteCruce) && this.OrdersByGraph()<1 && (bollDif < this.setts.bollxUp 
                && bollDif > setts.bollxDn)) {
            //entrada de operaciones.
            if ((this.open_min+ this.setts.boll_special) <= bollDn) {
                //Compra
                orderSend(this.Ask, this.setts.lots, '1', this.Bid - this.setts.sl, this.Bid + this.setts.tp);
                //Iniciamos conteo de velas.
                this.cont_velas = 0;
            } else if (this.open_min - this.setts.boll_special >= bollUp) {
                //Venta
                orderSend(this.Bid, this.setts.lots, '2', this.Ask + this.setts.sl, this.Ask - this.setts.tp);
                //Iniciamos conteo de velas.
                this.cont_velas = 0;
            }
        //Revisamos que haya entrado alguna operación      
        }else if ( this.OrdersByGraph()> 0) {
            //Recorremos las operaciones.
            for (int i = 0; i < OrdersTotal().size(); i++) {
                Orden currentOrden = OrdersTotal().get(i);
                //Si es compra
                if (currentOrden.getSide() == '1') {
                    if (setts.salidaBollinger && this.open_min  >= bollUpS) {
                        currentOrden.close(this.Bid, "cierre por bollinger");
                        break;
                    } else if (cont_velas == setts.velasS) {
                        currentOrden.close(this.Bid, "cierre por velas");
                        break;
                    } 
                //Si es venta.
                } else if (currentOrden.getSide() == '2') {
                    if (setts.salidaBollinger && this.open_min  <= bollDnS) {
                        currentOrden.close(this.Ask, "cierre por bollinger");
                        break;
                    } else if (cont_velas == setts.velasS) {
                        currentOrden.close(this.Ask, "cierre por velas");
                        break;
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
    
    public double bollingerDif(){
        double tempUp = (bollBandx1.getUpperBand() + bollBandx2.getUpperBand() + bollBand3.getUpperBand())/3 ;
        double tempDn = (bollBandx1.getLowerBand() + bollBandx2.getLowerBand() + bollBand3.getLowerBand())/3;
        //System.out.println("XUp: "+tempUp + " - XDn: "+tempDn);
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
    @Override
    public String toString(){
        return this.Symbol+" "+"MAGIC:"+this.setts.MAGICMA+" Open:"+ this.open_min +" Up:"+this.bollUp+" Dn:"+this.bollDn+" UpS:"+this.bollUpS +" DnS:"+this.bollDnS+" Velas"+this.cont_velas;
    }
}

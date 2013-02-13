package oms.deliverer;

import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.TradeContextBusy;
import oms.Grafica.Graphic;
import oms.Grafica.Settings;
import oms.util.idGenerator;
import quickfix.CharField;
import quickfix.DoubleField;
import quickfix.FieldNotFound;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;


/**
 * Objecto orden, aqui tenemos la representacion de una orden,
 * @author omar
 */
public class Orden {
    private String symbol;
    private String currency;
    private Double open_price= -1.0;
    private Double close_price= 0.0;
    private Double sl = 0.0;
    private Double tp = 0.0;
    private char side;
    Character averse;
    private double lotes;
    private boolean isActiva;
    private String ordId;
    private String grafId;
    private String execId;
    private int date;
    private int hora;
    private String open_time = "----";
    private String close_time = "----";
    private String ocoId = null;
    private NewOrderSingle newOrderSingle;
    private NewOrderSingle newOrderOco;
    private ExecutionReport closeOrderSingle;
    private ExecutionReport executionReport;
    private boolean esNueva = false;
    private boolean filled = false;
    private Integer magicma;
    /**
     * Constructor que inicializa con datos de una orden.
     * @param grafId id de la grafica que envia.
     * @param symbol Moneda de la orden
     * @param magicma numero identificador de la orden/gráfica
     * @param price precio de la orden
     * @param tipo 
     */
    public Orden(String grafId, String symbol, Double lotes, Integer magicma, Double price, char side){
        this.grafId = grafId;
        this.open_price = price;
        this.side = side;
        this.averse = this.side =='1'?'2':'1';
        this.lotes = lotes*10000;
        this.isActiva = true;
        this.magicma = magicma;
        this.symbol = Settings.Slash(symbol);
        this.currency = symbol.substring(0,3);
        this.ordId = new idGenerator().getID();
        //this.open_time = time;
        this.newOrderSingle = new NewOrderSingle();
        this.newOrderSingle.set(new ClOrdID((this.ordId)));
        this.newOrderSingle.set(new HandlInst('1'));
        this.newOrderSingle.set(new Side(this.side));
        this.newOrderSingle.set(new Currency(this.currency));
        this.newOrderSingle.set(new Symbol(this.symbol));
        this.newOrderSingle.set(new TransactTime());
        this.newOrderSingle.set(new OrderQty(this.lotes));
        this.newOrderSingle.set(new OrdType('C'));
        this.newOrderSingle.set(new Price(this.open_price));
        
        this.esNueva = true;
    }
    /**
     * Método sobrecargado del constructor para poder enviar ordenes con sl y tp.
     * @param grafId
     * @param symbol
     * @param lotes
     * @param magicma
     * @param price
     * @param side
     * @param sl
     * @param tp 
     */
    public Orden(String grafId, String symbol, Double lotes, Integer magicma, 
            Double price, char side,Double sl, Double tp){
        this.grafId = grafId;
        this.open_price = price;
        this.side = side;
        this.averse = this.side =='1'?'2':'1';
        this.lotes = lotes*10000;
        this.isActiva = true;
        this.magicma = magicma;
        this.symbol = Settings.Slash(symbol);
        this.currency = symbol.substring(0,3);
        this.ordId = new idGenerator().getID();
        //this.open_time = time;
        this.newOrderSingle = new NewOrderSingle();
        this.newOrderSingle.set(new ClOrdID((this.ordId)));
        this.newOrderSingle.set(new HandlInst('1'));
        this.newOrderSingle.set(new Side(this.side));
        this.newOrderSingle.set(new Currency(this.currency));
        this.newOrderSingle.set(new Symbol(this.symbol));
        this.newOrderSingle.set(new TransactTime());
        this.newOrderSingle.set(new OrderQty(this.lotes));
        this.newOrderSingle.set(new OrdType('C'));
        this.newOrderSingle.set(new Price(this.open_price));
        this.esNueva = true;
        
        newOrderOco = new NewOrderSingle();
        newOrderOco.set(new ClOrdID(this.ordId));
        newOrderOco.set(new HandlInst('1'));
        newOrderOco.set(new Currency(symbol.substring(0,3)));
        newOrderOco.set(new Symbol(this.symbol));
        newOrderOco.set(new TransactTime());
        newOrderOco.set(new OrderQty(this.lotes));
        newOrderOco.set(new OrdType('W'));
        newOrderOco.set(new Side(averse));
        newOrderOco.setField(new CharField(7541,'3'));
        newOrderOco.setField(new CharField(7553,averse));
        newOrderOco.setField(new DoubleField(7542, redondear(sl)));
        newOrderOco.setField(new DoubleField(7540, redondear(tp)));
        OrderHandler.SendOCO(this.newOrderOco);
    }
    /**
     * Enviamos cierre de la orden.
     * @param time
     * @param close 
     */
    public void close(Double close){
        this.newOrderSingle = new NewOrderSingle();
        
        this.newOrderSingle.set(new ClOrdID((this.ordId)));
        this.newOrderSingle.set(new HandlInst('1'));
        this.newOrderSingle.set(new Side(this.averse));
        this.newOrderSingle.set(new Currency(this.currency));
        this.newOrderSingle.set(new Symbol(this.symbol));
        this.newOrderSingle.set(new TransactTime());
        this.newOrderSingle.set(new OrderQty(this.lotes));
        this.newOrderSingle.set(new OrdType('C'));
        this.newOrderSingle.set(new Price(close));
        try {
            System.err.println("Cerrando orden "+ this.ordId);
            OrderHandler.sendOrder(this);
            //System.out.println("Close: #" +  this.id + " "+time +" $"+close);
        } catch (TradeContextBusy ex) {
            Logger.getLogger(Orden.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Modificamos una orden cambiandole el TP y/o SL.
     * @param oco Id del oco entrante
     * @param nwTp
     * @param nwSl 
     */
    public void Modify(Double newSl, Double newTp){
        //Datos para construir un mensaje OCO.
        //Tenemos que enviar la orden contraria
        newOrderOco = new NewOrderSingle();
        newOrderOco.set(new ClOrdID(this.ordId));
        newOrderOco.set(new HandlInst('1'));
        newOrderOco.set(new Currency(symbol.substring(0,3)));
        newOrderOco.set(new Symbol(symbol));
        newOrderOco.set(new TransactTime());
        newOrderOco.set(new OrderQty(this.lotes));
        newOrderOco.set(new OrdType('W'));
        newOrderOco.set(new Side(this.averse));
        newOrderOco.setField(new CharField(7541,'3'));
        newOrderOco.setField(new CharField(7553,averse));
        newOrderOco.setField(new DoubleField(7542, redondear(newSl)));
        newOrderOco.setField(new DoubleField(7540, redondear(newTp)));
        System.out.println("sl: "+ redondear(sl) + " tp:"+redondear(tp));
        //Si esta órden no tiene SL o Tp entonces esta modificacion es nueva 'N'.
        if(this.sl == 0 || this.tp == 0){
            newOrderOco.set(new Text("New"));
        }
        else{
            newOrderOco.set(new Text("Mod"));
        }
        OrderHandler.SendOCO(this.newOrderOco);
            
    }
    /**
     * Verificamos si esta posicion esta abierta.
     * @return 
     */
    public boolean IsActiva(){
        return this.isActiva;
    }
    /**
     * <<<<<<<<<<------------------------GETTERS!
     */
    /**
     * StopLoss de la orden.
     * @return 
     */
    public Double getSl(){
        return this.redondear(this.sl);
    }
    /**
     * TakeProfit de la orden.
     * @return 
     */
    public Double getTp(){
        return this.redondear(this.tp);
    }
    /**
     * 
     * @return 
     */
    public String getOco(){
        return this.ocoId;
    }
    /**
     * 
     * @return Tipo de orden 1 para compra 2 para venta.
     */
    public char getSide(){
        return this.side;
    }
    /**
     * @return Id de la orden generalmente sera el orden ascendente de la orden 
     * cuando abrio.
     */
    public String getId(){
        return this.ordId;
    }
    /**
     * @return Id de la grafica que abrio esta orden.
     */
    public String getGrafId(){
        return this.grafId;
    }
    /**
     * @return Symbol de la orden.
     */
    public String getSymbol(){
        return this.symbol;
    }
    /**
     * 
     * @return Mensaje fix de la orden actual.
     */
    public NewOrderSingle getNewOrderSingleMsg(){
        return this.newOrderSingle;
    }
    public boolean getEsNueva(){
        return this.esNueva;
    }
    /**
     * @return Precio de apertura de la orden.
     */
    public Double getOpenPrice(){
        return this.redondear(this.open_price);
    }
    /**
     * @return Hora de apertura de la orden.
     */
    public String getOpenTime(){
        return this.open_time;
    }
    /**
     * @return Hora de cierre de la orden null si no ha cerrado.
     */
    public String getcloseTime(){
        return this.close_time;
    }
    /**
     * @return Precio de cierre de la orden, -1 si no ha cerrado.
     */
    public Double getClosePrice(){
        return this.redondear(this.close_price);
    }
    /**
     * @return Si la orden fué aceptada correctammente por el broker
     */
    public boolean isFilled(){
        return this.filled;
    }
    /**
     * @return MagicMa de la orden.
     */
    public int getMagic(){
        return this.magicma;
    }
    /**
     * SETTERS! ------------------------>>>>>>>>>>
     */
    /**
     * Añadimos fecha por tick para saber que fecha/hora una orden cierra.
     * @param date
     * @param hora 
     */
    public void setDate(int date, int hora){
        this.date = date;
        this.hora = hora;
    }
    /**
     * Cambiamos/añadimos stops de la orden.
     * @param nwTp
     * @param nwSl 
     */
    public void setStops(Double nwTp, Double nwSl){
        this.sl = nwSl;
        this.tp = nwTp;
    }
    /**
     * Marcamos esta orden como aceptada.
     * @param msj 
     */
    public void setFilled(ExecutionReport msj){
        this.executionReport = msj;
        this.filled = true;
        this.esNueva = false;
        try {
            this.execId = msj.getExecID().getValue();
            System.err.println("Abrimos posicion: "+this + " correctamente! :)");
        } catch (FieldNotFound ex) {
            Logger.getLogger(Orden.class.getName()).log(Level.SEVERE, null, ex);
        }
        Graphic.dao.recordOrden(this.grafId,this.executionReport,this.magicma);   
    }
    /**
     * Añadimos el TP/SL de la orden.
     * @param msj 
     */
    public void setOco(ExecutionReport msj){
        
        try {
            if(this.sl ==0 && this.tp == 0){
                this.sl = msj.getDouble(7540);
                this.tp = msj.getDouble(7542);
            }else{
                this.sl = msj.getDouble(7540);
                this.tp = msj.getDouble(7542);
                System.err.println("Modificando : "+this + " correctamente! :)");
            }
            
            this.ocoId = msj.getOrderID().getValue();
        } catch (FieldNotFound ex) {
            Logger.getLogger(Orden.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Marcamos esta órden como cerrada.
     * @param msj 
     */
    public void setClose(ExecutionReport msj){
        this.isActiva = false;
        try {
            this.closeOrderSingle = msj;
            this.close_price = msj.getAvgPx().getValue();
        } catch (FieldNotFound ex) {
            Logger.getLogger(Orden.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("Cerramos posicion: "+this + " correctamente! :)");
    }
    /**
     * redondeamos un valor a un decimal quirandole todos los valores, después del
     * cuarto dígito despues del punto. 
     * @param val
     * @return 
     */
    private Double redondear(Double val){
        return Math.round(val*Math.pow(10, 4))/Math.pow(10,4);
    }
   
    /**
     * Autodescripción de la orden.
     * @return 
     */
    @Override
   public String toString(){
        String tipo = this.getSide() == '1' ? "Compra" : "Venta ";
        return "#"+this.getId() +"  OT:"+this.getOpenTime() + " " + tipo + " OP: " + this.getOpenPrice() + " SL:"+
                    this.getSl() + " TP:"+this.getTp() + " CT:" + this.getcloseTime() +" CP:" + this.getClosePrice();
   }
}
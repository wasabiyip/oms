package oms.deliverer;

import java.util.Date;
import oms.CustomException.TradeContextBusy;
import oms.Grafica.Settings;
import oms.util.Console;
import oms.util.idGenerator;
import quickfix.CharField;
import quickfix.DoubleField;
import quickfix.FieldNotFound;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;

/**
 * Objecto orden, aqui tenemos la representacion de una orden,
 *
 * @author omar
 */
public class Orden implements java.io.Serializable {

    private String symbol;
    private String unSymbol;
    private String currency;
    private Double open_price = -1.0;
    private Double close_price = 0.0;
    private Double sl = 0.0;
    private Double tp = 0.0;
    private char side;
    Character averse;
    private double lotes;
    private boolean isActiva;
    private String ordId;
    private String grafId;
    private String execId;
    private String type;
    private int date;
    private int hora;
    private String ocoId = null;
    private String brokerOrderId;
    private NewOrderSingle newOrderSingle;
    private NewOrderSingle newOrderOco;
    private ExecutionReport closeOrderSingle;
    private ExecutionReport executionReport;
    private boolean esNueva = false;
    private boolean filled = false;
    private Integer magicma;
    private Date horaOpen;
    private Date horaClose;
    private String account;
    private String reason = "";
    private long orderMillsInit;
    private long orderMillsFin;
    private long OrderExecTime;
    
    /**
     * Constructor que inicializa con datos de una orden, sin SL/TP.
     *
     * @param grafId id de la grafica que envia.
     * @param symbol Moneda de la orden
     * @param magicma numero identificador de la orden/gráfica
     * @param price precio de la orden
     * @param tipo
     */
    public Orden(String grafId, String symbol, Double lotes, Integer magicma, Double price, Character side) {
        this.grafId = grafId;
        this.side = side;
        this.type = this.side == '1' ? "Buy" : "sell";
        this.averse = this.side == '1' ? '2' : '1';
        this.lotes = lotes * 10000;
        this.isActiva = true;
        this.magicma = magicma;
        this.unSymbol = symbol;
        this.symbol = Settings.Slash(symbol);
        this.currency = symbol.substring(0, 3);
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
        this.orderMillsInit = System.currentTimeMillis();
    }

    /**
     * Método sobrecargado del constructor para poder enviar ordenes con sl y
     * tp.
     *
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
            Double price, char side, Double sl, Double tp) {
        this.grafId = grafId;
        this.side = side;
        this.type = this.side == '1' ? "Buy" : "sell";
        this.averse = this.side == '1' ? '2' : '1';
        this.lotes = lotes * 10000;
        this.isActiva = true;
        this.magicma = magicma;
        this.unSymbol = symbol;
        this.symbol = Settings.Slash(symbol);
        this.currency = symbol.substring(0, 3);
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
        this.sl = this.redondear(sl);
        this.tp = this.redondear(tp);
        this.orderMillsInit = System.currentTimeMillis();
    }

    /**
     * Enviamos cierre de la orden.
     *
     * @param time
     * @param close
     */
    public void close(Double close) {
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
            Console.info("close request in process " + this.ordId + " " + reason);
            OrderHandler.sendOrder(this);
        } catch (TradeContextBusy ex) {
            Console.exception(ex);
        }
    }

    /**
     * Sobrecargado para que acepte una razon de cierre.
     *
     * @param close
     * @param reason
     */
    public void close(Double close, String reason) {
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
            Console.info("close request in process " + this.ordId + " " + reason);
            OrderHandler.sendOrder(this);
        } catch (TradeContextBusy ex) {
            Console.exception(ex);
        }
        this.reason = reason;
    }

    /**
     * Modificamos una orden cambiandole el TP y/o SL.
     *
     * @param oco Id del oco entrante
     * @param nwTp
     * @param nwSl
     */
    public void Modify(Double newSl, Double newTp) {
        //Datos para construir un mensaje OCO.
        //Tenemos que enviar la orden contraria
        newOrderOco = new NewOrderSingle();
        newOrderOco.set(new ClOrdID(this.ordId));
        newOrderOco.set(new HandlInst('1'));
        newOrderOco.set(new Currency(symbol.substring(0, 3)));
        newOrderOco.set(new Symbol(symbol));
        newOrderOco.set(new TransactTime());
        newOrderOco.set(new OrderQty(this.lotes));
        newOrderOco.set(new OrdType('W'));
        newOrderOco.set(new Side(this.averse));
        newOrderOco.setField(new CharField(7541, '3'));
        newOrderOco.setField(new CharField(7553, averse));
        newOrderOco.setField(new DoubleField(7542, redondear(newSl)));
        newOrderOco.setField(new DoubleField(7540, redondear(newTp)));
        //Si esta órden no tiene SL o Tp entonces esta modificacion es nueva 'N'.
        if (this.sl == 0 || this.tp == 0) {
            newOrderOco.set(new Text("New"));
        } else {
            newOrderOco.set(new Text("Mod"));
        }
        OrderHandler.SendOCO(this.newOrderOco);

    }

    /**
     * Verificamos si esta posicion esta abierta.
     *
     * @return
     */
    public boolean IsActiva() {
        return this.isActiva;
    }

    /**
     * <<<<<<<<<<-------------------------------------------------------GETTERS!
     */
    /**
     * StopLoss de la orden.
     *
     * @return
     */
    public Double getSl() {
        return this.redondear(this.sl);
    }

    /**
     * TakeProfit de la orden.
     *
     * @return
     */
    public Double getTp() {
        return this.redondear(this.tp);
    }

    /**
     *
     * @return
     */
    public String getOco() {
        return this.ocoId;
    }

    /**
     *
     * @return Tipo de orden 1 para compra 2 para venta.
     */
    public char getSide() {
        return this.side;
    }

    /**
     * @return Id de la orden generalmente sera el orden ascendente de la orden.
     * cuando abrio.
     */
    public String getId() {
        return this.ordId;
    }

    /**
     * @return Id de la grafica que abrio esta orden.
     */
    public String getGrafId() {
        return this.grafId;
    }

    /**
     * @return Symbol de la orden.
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Symbolo sin Slash /
     *
     * @return
     */
    public String getUnSymbol() {
        return this.unSymbol;
    }

    /**
     *
     * @return Mensaje fix de la orden actual.
     */
    public NewOrderSingle getNewOrderSingleMsg() {
        return this.newOrderSingle;
    }

    public boolean getEsNueva() {
        return this.esNueva;
    }

    /**
     * @return Precio de apertura de la orden.
     */
    public Double getOpenPrice() {
        return this.redondear(this.open_price);
    }

    /**
     * @return Precio de cierre de la orden, -1 si no ha cerrado.
     */
    public Double getClosePrice() {
        return this.redondear(this.close_price);
    }

    /**
     * @return Si la orden fué aceptada correctammente por el broker
     */
    public boolean isFilled() {
        return this.filled;
    }

    /**
     * @return MagicMa de la orden.
     */
    public int getMagic() {
        return this.magicma;
    }

    /**
     * @return La OCO que previamente creamos.
     */
    public NewOrderSingle getOcoOrden() {
        return this.newOrderOco;
    }

    public Double getLotes() {
        return this.lotes;
    }

    /**
     * @return La hora en que abrió la orden.
     */
    public Date getHoraOpen() {
        return this.horaOpen;
    }

    /**
     * @return La hora en que abrió la orden.
     */
    public Date getHoraClose() {
        return this.horaClose;
    }

    /**
     * @return Cuenta de la orden.
     */
    public String getAccount() {
        return this.account;
    }

    /**
     * @return Id de execucion de la orden.
     */
    public String getExecId() {
        return this.execId;
    }

    /**
     * @return Id de la orden asignada por el broker.
     */
    public String getBrokerOrdId() {
        return this.brokerOrderId;
    }

    /**
     * @return Razon de cierre de la operacion, si es que hay...
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * SETTERS!------------------------------------------------------->>>>>>>>>>
     */
    /**
     * Añadimos fecha por tick para saber que fecha/hora una orden cierra.
     *
     * @param date
     * @param hora
     */
    public void setDate(int date, int hora) {
        this.date = date;
        this.hora = hora;
    }

    /**
     * Marcamos esta orden como aceptada.
     *
     * @param msj
     */
    public void setFilled(ExecutionReport msj) {
        this.executionReport = msj;
        this.filled = true;
        this.esNueva = false;
        this.orderMillsFin = System.currentTimeMillis();
        try {
            this.open_price = msj.getLastPx().getObject();
            this.account = msj.getAccount().getValue();
            this.horaOpen = msj.getTransactTime().getValue();
            this.execId = msj.getExecID().getValue();
            this.brokerOrderId = msj.getOrderID().getValue();
            Console.warning("Order was opened: #" + this.ordId + " " + this.type
                    + " " + this.lotes + " " + this.symbol + " at " + this.getOpenPrice() + " "+(this.orderMillsFin - this.orderMillsInit)+" milliseconds");
        } catch (FieldNotFound ex) {
            Console.exception(ex);
        }
        //Graphic.dao.recordOrden(this.grafId,this.executionReport,this.magicma);
        //Si tenemos pendiente la OCO.
        if (this.newOrderOco == null && this.sl != 0 && this.tp != 0) {
            newOrderOco = new NewOrderSingle();
            newOrderOco.set(new ClOrdID(this.ordId));
            newOrderOco.set(new HandlInst('1'));
            newOrderOco.set(new Currency(symbol.substring(0, 3)));
            newOrderOco.set(new Symbol(this.symbol));
            newOrderOco.set(new TransactTime());
            newOrderOco.set(new OrderQty(this.lotes));
            newOrderOco.set(new OrdType('W'));
            newOrderOco.set(new Side(averse));
            newOrderOco.setField(new CharField(7541, '3'));
            newOrderOco.setField(new CharField(7553, averse));
            newOrderOco.setField(new DoubleField(7542, this.sl));
            newOrderOco.setField(new DoubleField(7540, this.tp));
            OrderHandler.SendOCO(this.newOrderOco);
        }
    }

    /**
     *
     * @param nueva
     */
    public void setIsNueva(Boolean nueva) {
        this.esNueva = nueva;
    }

    /**
     * Añadimos el TP/SL de la orden.
     *
     * @param msj
     */
    public void setOco(ExecutionReport msj) {
        try {
            if (this.sl == 0 && this.tp == 0) {
                this.sl = msj.getDouble(7542);
                this.tp = msj.getDouble(7540);
            } else {
                this.sl = msj.getDouble(7540);
                this.tp = msj.getDouble(7542);
            }

            this.ocoId = msj.getOrderID().getValue();
            Console.warning("order #" + this.ordId + " " + this.type
                     + " was modified -> sl: "+this.sl + " tp:"+this.tp);
        } catch (FieldNotFound ex) {
            Console.exception(ex);
        }
    }

    /**
     * Marcamos esta órden como cerrada.
     *
     * @param msj
     */
    public void setOrdenClose(ExecutionReport msj) {
        this.isActiva = false;
        try {
            this.horaClose = msj.getTransactTime().getValue();
            this.closeOrderSingle = msj;
            this.close_price = msj.getAvgPx().getValue();
        } catch (FieldNotFound ex) {
            Console.exception(ex);
        }
        Console.warning("order #" + this.ordId + " " + this.type
                     + " closed at price: "+this.close_price + " => "+this.reason);
    }

    /**
     * Añadimos razon de cierre de la órden.
     *
     * @param reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * redondeamos un valor a un decimal quirandole todos los valores, después
     * del cuarto dígito despues del punto.
     *
     * @param val
     * @return
     */
    private Double redondear(Double val) {
        return Math.round(val * Math.pow(10, 5)) / Math.pow(10, 5);
    }

    /**
     * Autodescripción de la orden.
     *
     * @return
     */
    @Override
    public String toString() {
        return "#" + this.getId() + " Symbol:" + this.symbol + "  OT:" + this.horaOpen + " " + this.type + " OP: " + this.getOpenPrice() + " SL:"
                + this.getSl() + " TP:" + this.getTp() + " CT:" + this.horaClose + " CP:" + this.getClosePrice();
    }
}
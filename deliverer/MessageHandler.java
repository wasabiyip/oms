package oms.deliverer;

import java.util.Date;
import oms.deliverer.SenderApp;
import quickfix.FieldNotFound;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.*;

/**
 *
 * @author Omar Clase la cuál obtiene algún valor que obtiene de un mensaje
 * recibido desde Currenex através de quickfix.
 */
public class MessageHandler {

    quickfix.fix42.TradingSessionStatus status;
    SessionID sessionID;
    public static Date date = null;
    /**
     * Método que recibe un mensaje, busca en este mensaje y obtiene el bid y
     * offer.
     *
     * @param msj
     */
    /**
     * Recibimos mensaje con Bid y/o Offer
     *
     * @param msj
     */
    public static void marketDataPx(quickfix.fix42.MarketDataIncrementalRefresh msj) {

        try {
            SendingTime time = new SendingTime();
            MDReqID mdreqid = new MDReqID();
            NoMDEntries nomdentries = new NoMDEntries();
            quickfix.fix42.MarketDataIncrementalRefresh.NoMDEntries group = new quickfix.fix42.MarketDataIncrementalRefresh.NoMDEntries();
            MDUpdateAction mdupdateaction = new MDUpdateAction();
            MDEntryType mdentrytype = new MDEntryType();
            MDEntryID mdentryid = new MDEntryID();
            Symbol symbol = new Symbol();
            MDEntryPx mdentrypx = new MDEntryPx();
            MDEntrySize entrySize = new MDEntrySize();
            Currency currency = new Currency();
            msj.get(nomdentries);

            if (nomdentries.getValue() == 1) {
                msj.getGroup(1, group);
                group.get(mdentrytype);
                group.get(mdupdateaction);
                group.get(mdentrypx);
                group.get(mdentrytype);
                if (mdentrytype.getValue() == 0) {
                    //MarketPool.setBid(mdentrypx.getValue());
                } else if (mdentrytype.getValue() == 1) {
                    //MarketPool.setOffer(mdentrypx.getValue());
                }

            } else if (nomdentries.getValue() == 2) {

                for (int i = 1; i <= 2; i++) {
                    msj.getGroup(i, group);
                    group.get(mdentrytype);
                    group.get(mdupdateaction);
                    group.get(mdentrypx);
                    if (i == 1) {
                        //MarketPool.setBid(mdentrypx.getValue());
                    } else {
                        //MarketPool.setOffer(mdentrypx.getValue());
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     * Mètodo que maneja el mensaje recibido despues de haber enviado una orden.
     *
     * @param msj
     * @throws FieldNotFound
     */
    public static void executionReport(quickfix.fix42.ExecutionReport msj) throws FieldNotFound, Exception {
        /**
         * Aquí recibimos la respuesta del servidor de como fué tratada nuestra
         * orden el campo ExecType del mensaje contiene esta información.
         * Primero revisamos que la orden entrante no sea un limit o estop, sino
         * revisamos que el OrderID no exista, por que si existe quiere decir
         * que la orden entrante es el cierre de una existente.
         */
        if(msj.getOrdType().getValue() == 'V'){
            System.out.println("Modificamos OCO");
            
        }else if (msj.getOrdType().getValue() == 'W' && !OrderHandler.ocoExists(msj)) {
            //Si la entrante es un oco y ya fue modifica notificamos que entro un ajuste 
            //de oco
            if(OrderHandler.isModify(msj)){
                OrderHandler.ocoModify(msj);
            }else{
                //Si no pues que entro una OCO neuva.
                OrderHandler.ocoRecord(msj);
            }
        } else if (OrderHandler.Exists(msj) && msj.getOrdStatus().getValue() == '2' && msj.getOrdType().getValue()=='C') {
            //Cerramos la OCO de la orden que recibimos un cierre.
            System.out.println("Cerrando oco "+msj.getClOrdID());
            OrderHandler.closeOCO(msj.getOrigClOrdID().getValue(),'N');
        }else if(OrderHandler.Exists(msj)&& msj.getExecType().getValue() =='2' && msj.getOrdType().getValue()=='W'){
            //Cuando una operacion fue cerrada por TP o SL.
            System.err.println("La orden " +msj.getClOrdID().getValue()+" cerro por SL o TP");
            OrderHandler.closeFromOco(msj.getClOrdID().getValue());
        } else {
            //Entrada de orden
            switch (msj.getExecType().getValue()) {
                case '0':
                    System.err.println("Pensando " + msj.getClOrdID().getValue() + "...");
                    break;

                case '1':
                    System.err.println("La orden fue: \"Partial filled\"");
                    break;

                case '2':
                    /**
                     * Si es 2 quiere decir que la orden fué aceptada y
                     * procedemos a guardarla en Mongo.
                     */
                    OrderHandler.orderNotify(msj);
                    if (msj.getSide().getValue() == '1') {
                        OrderHandler.SendOCO(msj.getSymbol().getValue(),'1', msj.getClOrdID().getValue(), (int) msj.getOrderQty().getValue(),(double)msj.getLastPx().getValue(),'N');
                        System.err.println("Se abrió una orden: #" + msj.getClOrdID().getValue() + " Buy " + msj.getOrderQty().getValue() / 10000 + " "
                               + msj.getSymbol().getValue() + " a: " + msj.getLastPx().getValue());
                    }
                    if (msj.getSide().getValue() == '2') {
                        OrderHandler.SendOCO(msj.getSymbol().getValue(),'2', msj.getClOrdID().getValue(), (int) msj.getOrderQty().getValue(),(double)msj.getLastPx().getValue(),'N');
                        System.err.println("Se abrió una orden: #" + msj.getClOrdID().getValue() + " Sell " + msj.getOrderQty().getValue() / 10000 + " "
                              + msj.getSymbol().getValue() + " a: " + msj.getLastPx().getValue());
                    }
                    break;

                case '4':
                    //System.err.println("La orden: " + msj.getClOrdID().getValue() + " fué rechazada por el servidor (Insufficient Margin).");
                    break;

                case '8':
                    System.err.println("La orden: " + msj.getClOrdID().getValue() + "fué rechazada por el servidor.");
                    break;
            }
        }
    }

    public static void errorHandler(quickfix.fix42.Reject msj) throws FieldNotFound {

        System.out.println("Error: " + msj.getText().getValue());
    }
}

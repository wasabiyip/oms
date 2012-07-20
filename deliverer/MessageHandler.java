package oms.deliverer;

import java.util.Date;
import oms.deliverer.SenderApp;
import oms.util.Order;
import oms.util.fixToJson;
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

    public static void orderStatusRequest() throws SessionNotFound {
        quickfix.fix42.OrderStatusRequest status = new quickfix.fix42.OrderStatusRequest();
        status.set(new OrderID("OPEN_ORDER"));
        status.set(new ClOrdID("OPEN_ORDER"));
        status.set(new Symbol("EUR/USD"));
        status.set(new Side('7'));
        Session.sendToTarget(status, SenderApp.sessionID);
    }

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
    public static void orderHandler(quickfix.fix42.ExecutionReport msj) throws FieldNotFound, Exception {
        /**
         * Aquí recibimos la respuesta del servidor de como fué tratada nuestra
         * orden el campo ExecType del mensaje contiene esta información.
         * Primero revisamos que la orden entrante no sea un limit o estop, sino
         * revisamos que el OrderID no exista, por que si existe quiere decir
         * que la orden entrante es el cierre de una existente.
         */
        if (msj.getOrdType().getValue() == '3') {
            //StopLoss
            if (msj.getExecType().getValue() == 0) {
                //Nueva    
                Order.stopsRecord('3', msj.getClOrdID().getValue(), msj.getStopPx().getValue(),
                        msj.getOrderID().getValue());
            } else {
                //Close
            }
        } else if (msj.getOrdType().getValue() == 'F') {
            //TakeProfit
            if (msj.getExecType().getValue() == 0) {
                //Nuevo
                Order.stopsRecord('F', msj.getClOrdID().getValue(), msj.getPrice().getValue(),
                        msj.getOrderID().getValue());
            }else {
                //Close
                
            }
        } else if (Order.Exists(msj) && msj.getOrdStatus().getValue() == '2') {
            //Cierre de operacion
            System.out.println("Cerrando orden: " + msj.getExecID().getValue());
        } else {
            //Entrada de orden
            switch (msj.getExecType().getValue()) {
                case '0':
                    System.out.println("Procesando orden " + msj.getClOrdID().getValue() + "...");
                    break;

                case '1':
                    System.out.println("La orden fue: \"Partial filled\"");
                    break;

                case '2':
                    /**
                     * Si es 2 quiere decir que la orden fué aceptada y
                     * procedemos a guardarla en Mongo.
                     */
                    Order.orderRecord(new fixToJson().parseOrder(msj));
                    if (msj.getSide().getValue() == '1') {
                        Order.SendStops('1', msj.getClOrdID().getValue(), (int) msj.getOrderQty().getValue());
                        System.out.println("Se abrió una orden: #" + msj.getClOrdID().getValue() + " Buy " + msj.getOrderQty().getValue() / 10000 + " "
                                + msj.getSymbol().getValue() + " a: " + msj.getLastPx().getValue());
                    }
                    if (msj.getSide().getValue() == '2') {
                        Order.SendStops('2', msj.getClOrdID().getValue(), (int) msj.getOrderQty().getValue());
                        System.out.println("Se abrió una orden: #" + msj.getClOrdID().getValue() + " Sell " + msj.getOrderQty().getValue() / 10000 + " "
                                + msj.getSymbol().getValue() + " a: " + msj.getLastPx().getValue());
                    }

                    break;

                case '4':
                    System.out.println("La orden: " + msj.getClOrdID().getValue() + " fué rechazada por el servidor (Insufficient Margin).");
                    break;

                case '8':
                    System.out.println("La orden: " + msj.getClOrdID().getValue() + "fué rechazada por el servidor.");
                    break;
            }
        }
    }

    public static void errorHandler(quickfix.fix42.Reject msj) throws FieldNotFound {

        System.out.println("Error: " + msj.getText().getValue());
    }
}

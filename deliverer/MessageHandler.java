package oms.deliverer;

import java.util.Date;
import oms.util.Console;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.*;

/**
 *
 * @author Omar Clase la cuál obtiene algún valor que obtiene de un mensaje
 * recibido desde Currenex através de quickfix.
 */
public class MessageHandler {

    quickfix.fix42.TradingSessionStatus status;
    SessionID sessionID;
    static String temp_msj = new String();
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
       switch (msj.getExecType().getValue()) {
           /*
            * Aqui entramos por cuatros motivos:
            * C - Por que una C/V fué aceptada por el servidor y procedera a mandarnos un
            *     un mensaje de que la orden fue "filled", asi que solo esperamos por el 
            *     siguiente mensaje para actuar.
            * W - N - Por que un entro una OCO nueva.
            * W - M - Por que se modifico una OCO.
            */
           case '0':
               if(msj.getOrdType().getValue() == 'C'){
                   temp_msj = "Procesando orden " + msj.getSymbol().getValue() + "...";
                   System.out.println(temp_msj);
                   Console.msg(temp_msj);
               }else if(msj.getOrdType().getValue() == 'W'){
                   //si regresa true quiere decir que la entrante es una modificación.
                   if(OrderHandler.isModify(msj)){
                        temp_msj= "Modificando OCO " + msj.getClOrdID().getValue();
                        System.out.println(temp_msj);
                        Console.msg(temp_msj);
                        OrderHandler.ocoModify(msj);
                    }else{
                        //Si no pues que entro una OCO nueva.
                        temp_msj = "Guardando OCO " + msj.getClOrdID().getValue();
                        System.out.println(temp_msj);
                        OrderHandler.ocoRecord(msj);
                    }
               }
               break;
           /**
            * Acualmente no hemos visto que nos llene parcialmente alguna orden, nos mantenemos
            * escépticos.
            */
           case '1':
               temp_msj = "**¡Peligro: Partial fill " + msj.getClOrdID().getValue() + " algo fué mal!...";
               System.out.println(temp_msj);
               Console.msg(temp_msj);
               break;
           /**
            * La orden fue aceptada correctamente, asi que emitimos la notificación
            * relacionada con las ordenés nuevas o ordenes que cierran.
            */
           case '2':
               if (OrderHandler.Exists(msj)) {
                   if (msj.getOrdType().getValue() == 'C') {
                       //Cerramos la orden en el sistema.
                       OrderHandler.shutDown(msj.getClOrdID().getValue(), msj.getAvgPx().getValue());
                       //Enviamos cierre de OCO
                       OrderHandler.closeOCO(msj.getOrigClOrdID().getValue(), 'N');
                   }else if(msj.getOrdType().getValue() == 'W'){
                       temp_msj = "La orden " + msj.getExecType().getValue() + " cerró por Sl o TP";
                       System.out.println(temp_msj);
                       Console.msg(temp_msj);
                       OrderHandler.closeFromOco(msj.getClOrdID().getValue());
                   }
               }else{
                   //Si no existe pues notificamos de orden nueva.
                   OrderHandler.orderNotify(msj);
               }
               break;
           case '3':
               temp_msj = "Done for a day " + msj.getClOrdID().getValue() + " favor de revisar currenex...";
               System.out.println(temp_msj);
               Console.msg(temp_msj);
               break;
           case '4':
               if(msj.getOrdType().getValue() == 'W'){
                   System.err.println("Cerramos OCO " + msj.getClOrdID().getValue());
               }else{
                   System.err.println("El horror! la orden fue cancelada " + msj.getClOrdID().getValue() + " no deberiamos entrar aqui, revisar log!");
               }
               break;
           case '5':
               System.err.println("El horror! *Replace* no esta soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
               break;
           case '6':
               System.err.println("El horror! Pending Cancel no esta soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
               break;
           case '7':
               System.err.println("El horror!  Stopped soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
               break;
           case '8':
               System.err.println("El horror!  Order Rejected " + msj.getClOrdID().getValue() + " -> Colapso, ¡NO ESTA SOPORTADO!");
               break;
           case '9':
               System.err.println("El horror!  Suspended soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
               break;
           case 'C':
               if(msj.getOrdType().getValue() == 'W'){
                   //Expiro una OCO
               }else{
                    System.err.println("El horror!  Expiro algo que no es OCO " + msj.getClOrdID().getValue() + " -> revisar log!");
               }
               break;
       }
            
    }

    public static void errorHandler(quickfix.fix42.Reject msj) throws FieldNotFound {
        temp_msj = "Error: " + msj.getText().getValue();
        System.out.println(temp_msj);
        Console.msg(temp_msj);
    }
}

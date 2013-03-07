package oms.deliverer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.Graphic;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import quickfix.FieldNotFound;
import quickfix.SessionID;

/**
 *
 * @author Omar
 */
public class MessageHandler {

    quickfix.fix42.TradingSessionStatus status;
    SessionID sessionID;
    static String temp_msj = new String();
    public static Date date = null;
    static MsjStreaming mStreaming = new MsjStreaming();
    /**
     * Pequeña clase tipo constructior.
     */
    public static void Init(){
        mStreaming.start();
    }
    /**
     * Evaluamos todas las ordenes de tipo Execution report (35=8)
     *
     * @param msj
     * @throws FieldNotFound
     */
    public static void executionReport(quickfix.fix42.ExecutionReport msj) throws FieldNotFound, Exception {
        /**
         * Evaluanos el ExecType(150) que contiene el tipo de execution que
         * recibimos del broker.
         */
        Orden tempOrden = OrderHandler.getOrdenById(msj.getClOrdID().getValue());
        switch (msj.getExecType().getValue()) {
            /**
             * 150=0 -> New: Quiere decir que es una nueva orden, aqui entramos
             * cuando hay un cierre/apertura de Market orders (40=1) pero solo
             * entramos cuando 40=W, por que cuando una market orden entra aquí
             * solo quiere decir que el broker la esta procesando, y enseguida
             * nos enviará si fué aceptada o rechazada.
             */
            case '0':
                if (msj.getOrdType().getValue() == 'W') {
                    //Guardamos el OCO.
                    OrderHandler.ocoEntry(msj);
                    mStreaming.modOrden(tempOrden);
                }
                /**
                 * 150=1 -> Partial Fill:Acualmente no hemos visto que nos llene
                 * parcialmente alguna orden, nos mantenemos escépticos.
                 */
                break;
            case '1':
                temp_msj = "**¡Peligro: Partial fill " + msj.getClOrdID().getValue() + " algo fué mal!...";
                System.out.println(temp_msj);
                mStreaming.msg(temp_msj);
                break;
            /**
             * 150=2 -> Fill: La orden fue aceptada(fill) correctamente, asi que
             * emitimos la notificación relacionada con las ordenés nuevas o
             * ordenes que cierran.
             */
            case '2':
                //Obtenemos la Orden entrante...
                
                /**
                 * Si la orden entrante: 40=C -> Forex - Market, la orden
                 * entrante es de un Apertura/Cierre de posicion.
                 */
                if (msj.getOrdType().getValue() == 'C') {
                    /**
                     * Si la orden ya fué aceptada previamente entonce es un
                     * cierre de posicion, sino pues solo es una órden nueva.
                     */
                    if (tempOrden.isFilled()) {
                        //TODO La marcamos como cerrada
                        tempOrden.setClose(msj);
                        //La cerramos en mongo:
                        OrderHandler.shutDown(tempOrden);
                        //Node notification
                        mStreaming.clOrden(tempOrden);
                        //Cerramos el oco de esta orden
                        OrderHandler.closeOCO(tempOrden);
                    } else {
                        OrderHandler.orderNotify(msj);
                        mStreaming.nwOrden(tempOrden);
                    }
                    /**
                     * Si 40=W -> OCO Entonce la orden cerro por OCO.
                     */
                } else if (msj.getOrdType().getValue() == 'W') {
                    temp_msj = "La orden cerro por OCO #" + msj.getClOrdID().getValue() + ".";
                    System.out.println(temp_msj);
                    mStreaming.msg(temp_msj);
                    //La marcamos como cerrada.
                    tempOrden.setClose(msj);
                    //Notificamos a node
                    mStreaming.clOrden(tempOrden);
                    //La cerramos en mongo:
                    OrderHandler.shutDown(tempOrden);
                }
                break;
            /**
             * 150=3 -> Done for day: Actualmente no hemos visto que pase esto.
             */
            case '3':
                temp_msj = "Done for a day " + msj.getClOrdID().getValue() + " favor de revisar currenex...";
                System.out.println(temp_msj);
                mStreaming.msg(temp_msj);
                break;
            /**
             * 150=4 -> Canceled : Si una orden fué cerrada insperadamente por
             * el broker, solo hemos visto que pase con OCO orders cuando el
             * broker hace rollovers(corte de caja, toma te intereses.
             */
            case '4':
                if (msj.getOrdType().getValue() == 'W' && msj.getOrdStatus().getValue() == 'C') {
                    System.err.println("OCO cancelada " + msj.getClOrdID().getValue() + " reenviando.");
                    OrderHandler.resendOCO(msj);
                } else if(msj.getOrdStatus().getValue() == '4') {
                    System.err.println("OCO cerrada de "+tempOrden.getId());
                }
                break;
            /**
             * 150=5 -> Replace : NO SOPORTADO.
             */
            case '5':
                System.err.println("El horror! *Replace* no esta soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
                break;
            /**
             * 150=6 -> Pending Cancel: NO SOPORTADO.
             */
            case '6':
                System.err.println("El horror! Pending Cancel no esta soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
                break;
            /**
             * 150=7 -> Stopped: NO SOPORTADO.
             */
            case '7':
                System.err.println("El horror!  Stopped soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
                break;
            /**
             * 150=8 -> Rejected: NO SOPORTADO.
             */
            case '8':
                System.err.println("El horror!  Order Rejected " + msj.getClOrdID().getValue() + " -> Colapso, ¡NO ESTA SOPORTADO!");
                break;
            /**
             * 150=9 -> Suspended: NO SOPORTADO.
             */
            case '9':
                System.err.println("El horror!  Suspended soportado " + msj.getClOrdID().getValue() + " -> revisar log!");
                break;
            /**
             * 150=7 -> Expired: NO SOPORTADO.
             */
            case 'C':
                System.err.println("El horror!  Expiro algo que no es OCO " + msj.getClOrdID().getValue() + " -> revisar log!");
                break;

            default:
                System.err.println("El horror!  150 NO SOPORTADO! " + msj + "\n ---> revisar log!");
        }

    }
    /**
     * Cada que recibimos un mensaje de Node lo mandamos aquí para ser evaluado.
     *
     * @param msj
     */    
    public static void errorHandler(quickfix.fix42.Reject msj) throws FieldNotFound {
        temp_msj = "Error: " + msj.getText().getValue();
        System.out.println(temp_msj);
        mStreaming.msg(temp_msj);
    }   
}

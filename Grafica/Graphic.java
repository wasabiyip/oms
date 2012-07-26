package oms.Grafica;

import oms.Grafica.DAO.MongoDao;
import oms.util.Console;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Esta clase Maneja a todas es el "main" de las gráficas, ya que mantiene
 * comunicación directa con Node, maneja el envio/recibo de mensajes y también
 * alimenta a la clase Candle y al Expert de datos.
 *
 * @author omar
 */
public class Graphic extends Thread {

    private ArrayList observers = new ArrayList();
    private Socket socket;
    private BufferedReader inFromNode;
    private DataOutputStream outNode;
    private Expert expert;
    private Candle candle;
    private String symbol;
    private int periodo;
    private int cont;
    private int dif;
    private String id;

    /**
     * Constructor!
     *
     * @param symbol par con el que trabajaremos
     * @param periodo tiempo con el que trabajaremos.
     * @throws IOException
     */
    public Graphic(String symbol, int periodo, String id) {
        System.out.println("Grafica de " + symbol + " " + periodo +  " " + id);
        expert = new Expert(symbol, id);
        dif = GMTDate.getDate().getMinute() % periodo;
        this.symbol = symbol;
        this.periodo = periodo;
        this.candle = new Candle(periodo, this.getHistorial(dif));
        cont = dif;
        this.id = id;
        
    }

    /**
     * Como cada gráfica es un thread...
     */
    @Override
    public void run() {

        try {
            String inputLine;
            String modifiedSentence;
            System.out.println("Conectando con Node");
            this.socket = new Socket("127.0.0.1", 8080);
            this.outNode = new DataOutputStream(this.socket.getOutputStream());

            //al iniciar enviamos a Node los settings de el expert.
            outNode.writeUTF("{\"type\": \"login\", "
                    + "\"name\":\"CLIENT_TCP\", "
                    + "\"symbol\":\"" + this.symbol + "\","
                    + this.expert.getExpertInfo()
                    + "}\n");

            StringBuffer msjout = new StringBuffer();
            StringBuffer msjin = new StringBuffer();
            msjout.append("{");
            msjout.append("\"type\": \"onCandle\",");

            msjout.append(expert.getExpertState());
            msjout.append("}");
            this.writeNode(msjout.toString());

            /*
             * utNode.writeUTF(this.expert.getExpertInfo().toString());
             * System.out.println("{\"type\": \"login\", " +
             * "\"name\":\"CLIENT_TCP\", " + "\"symbol\":\""+ this.symbol +"\","
             * + this.expert.getExpertInfo()
                    +"}");
             */
            //Leemos mensajes de node
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
            //this.inFromNode = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int c;
            while (isr.read() > 0) {
                while ((c = isr.read()) != 10) {
                    msjin.append((char) c);
                }
                //Evaluamos cada cadena recibida.
                handler(msjin.toString());
                //borramos el contenido pa' que no se acumule...
                msjin.delete(0, msjin.length());
            }

            /*
             * while ((inputLine = inFromNode.) != null) { //onTick((new
             * Double(inputLine))); System.out.println(inputLine);
            }
             */

        } catch (UnknownHostException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Cada que se recibe un precio de apertura se envia un mensaje a Node con
     * el precio recibido, y se alimenta a Candle y Expert de este suceso.
     *
     * @param price
     */
    public void onOpen(Double price) {
        StringBuffer msj = new StringBuffer();
        msj.append("{");
        msj.append("\"type\": \"onOpen\",");
        msj.append("\"precio\": " + price);
        msj.append("}");

        this.writeNode(msj.toString());
        /**
         * PRECAUCION**------------------------------------------------------
         */
        /**
         * Esto es inseguro por que si en un minuto no se recibe un precio el /
         * conteo de velas se va a desfasar. /  
         /*------------------------------------------------------------------
         */
        this.cont++;

        candle.onTick(price);
        expert.onOpen(price);
        if (this.cont >= this.periodo) {
            this.onCandle();
            cont = 0;
        }
    }

    /**
     * Cada vez que que tenemos una vela nueva detonamos este método.
     */
    public void onCandle() {
        this.expert.onCandle(this.candle.getOpenPrice());

        StringBuffer msj = new StringBuffer();
        msj.append("{");
        msj.append("\"type\": \"onCandle\",");

        msj.append(expert.getExpertState());


        msj.append("}");
        System.out.println(msj);
        this.writeNode(msj.toString());
    }

    /**
     * Obtenemos un historial cuando se inicia la grafica para sincronizar la
     * grafica con el tiempo actual.
     *
     * @param cant número que representa los minutos transcurridos al momento de
     * iniciar, si non las 10:23 y la grafica es de 5 minutos, entonces este
     * numero va a ser 3.
     * @return Un ArrayList con los precios que se pidieron.
     */
    private ArrayList getHistorial(int cant) {
        ArrayList data = new ArrayList();
        MongoDao dao = new MongoDao();

        //Utilizamos unSlash para quitar el / ya que en la base de datos tenemos las monedas
        //sin este.
        ArrayList temp = dao.getCandleData(this.unSlash(symbol), cant);
        return temp;
    }

    /**
     * Metodo que quita el / en un symbol, EUR/USD resulta en EURUSD ya que en
     * mongo los nombres de las monedas se encuentran así.
     *
     * @param symbol
     * @return cadena formateada
     */
    static String unSlash(String symbol) {
        StringBuffer str = new StringBuffer(symbol.length() - 1);
        str.append(symbol.substring(0, 3)).append(symbol.substring(4));
        return str.toString();
    }

    /**
     * Cada que recibimos un mensaje de Node lo mandamos aquí para ser evaluado.
     *
     * @param msj
     */
    private void handler(String msj) {

        try {
            JSONObject root = (JSONObject) new JSONParser().parse("{" + msj);
            JSONObject json = (JSONObject) root.get("msj");

            switch ((String) json.get("type")) {
                case "open":
                    this.onOpen((double) json.get("precio"));
                    break;
                case "get-state":
                    StringBuffer txt = new StringBuffer();
                    txt.append("{");
                    txt.append("\"type\": \"expert-state\",");
                    txt.append("\"id\":\"" + expert.id + "\",");
                    txt.append(expert.getExpertState());
                    txt.append("}");
                    System.out.println(txt.toString());
                    this.writeNode(txt.toString());
                    break;
                case "ask":
                    expert.setAsk((double) json.get("precio"));
                    break;
                case "bid":
                    expert.onTick((double) json.get("precio"));
                    break;
                default:
                    System.out.println("Mensaje no identificado"+ json.toString());
            }

        } catch (ParseException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método que envia mensajes a node
     *
     * @param msj
     */
    private void writeNode(String msj) {
        try {
            this.outNode.writeUTF(msj + "\n");
        } catch (IOException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
    public static void main(String[] args) throws IOException {

        //Graphic grafica = new Graphic("EUR/GBP", 5);
        //Graphic grafica = new Graphic("EUR/USD", 5);
        Graphic grafica = new Graphic("GBP/USD", 5);
        //Graphic grafica = new Graphic("USD/CHF", 5);
        //Graphic grafica = new Graphic("USD/JPY", 5);      
        grafica.start();
    }*/
}

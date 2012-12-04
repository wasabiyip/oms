package oms.Grafica;

import com.mongodb.DBObject;
import oms.Grafica.DAO.MongoDao;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;

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
    private Double bid, ask;
    //Guardamos las ordenes de que entran en cada gráfica.
    private ArrayList<ArrayList> operaciones = new ArrayList();
    public static MongoDao dao = new MongoDao();
    private Settings setts;
    private int lastOpen = GMTDate.getDate().getMinute();
    /**
     * Constructor!
     *
     * @param symbol par con el que trabajaremos
     * @param periodo tiempo con el que trabajaremos.
     * @throws IOException
     */
    public Graphic(String symbol, int periodo) {
        setts = new Settings(symbol);
        expert = new Expert(symbol, periodo, setts);
        this.id = expert.getID();
        dif = GMTDate.getDate().getMinute() % periodo;
        this.symbol = symbol;
        this.periodo = periodo;
        this.candle = new Candle(periodo, this.getHistorial(dif));
        cont = dif;
        System.out.println("Grafica de " + symbol + " " + periodo +  " " + id);
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
                    + this.expert.getExpertInfo() +","
                    + this.expert.getExpertState() 
                    + "}");
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
            }
            StringBuffer msjout = new StringBuffer();
            StringBuffer msjin = new StringBuffer();
            msjout.append("{");
            msjout.append("\"type\": \"onCandle\",");

            msjout.append(expert.getExpertState());
            msjout.append("}\n");
            this.writeNode(msjout.toString());
            //Leemos mensajes de node
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
            
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
        oms.util.Console.log("open: "+price);
        this.cont++;
        //Restamos el minuto actual 
        int dif = GMTDate.getDate().getMinute() - lastOpen;
        //System.out.println(GMTDate.getDate() +" - "+ lastOpen + " " +dif);
        if(dif > 1){
            System.err.println("Desfase: "+ this.symbol + " " +dao.getCloseAnterior(this.symbol));
            for(int i=1; i<=dif;i++){
                System.out.println("Descolapsando...");
                //Si 
                expert.onOpen(dao.getCloseAnterior(this.symbol));
                this.cont++;
            }
        }
        candle.onTick(price);
        expert.onOpen(price);
        if (this.cont >= this.periodo) {
            this.onCandle(price);
            cont = 0;
        }
        StringBuffer msj = new StringBuffer();
        msj.append("{");
        msj.append("\"type\": \"onOpen\",");
        msj.append("\"precio\": " + expert.getAvgOpen());
//        msj.append(expert.getRemain());
        msj.append("}");
        this.writeNode(msj.toString());
        this.lastOpen = GMTDate.getDate().getMinute();
    }

    /**
     * Cada vez que que tenemos una vela nueva detonamos este método.
     */
    public void onCandle(double openCandle) {
        this.expert.onCandle(openCandle);
        
        StringBuffer msj = new StringBuffer();
        msj.append("{");
        msj.append("\"type\": \"onCandle\",");
        msj.append(expert.getExpertState());
        msj.append("}");
        this.writeNode(msj.toString());
    }

    /**
     * Obtenemos un historial cuando se inicia la grafica para sincronizar la
     * grafica con el tiempo actual.
     *
     * @param cant número que representa los minutos transcurridos al momento de
     * iniciar, si son las 10:23 y la grafica es de 5 minutos, entonces este
     * numero va a ser 3.
     * @return Un ArrayList con los precios que se pidieron.
     */
    private ArrayList getHistorial(int cant) {
        ArrayList data = new ArrayList();
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
                case "close":
                    //TODO hacer algo con este precio de cierre
                    break;
                case "get-state":
                    StringBuffer txt = new StringBuffer();
                    txt.append("{");
                    txt.append("\"type\": \"expert-state\",");
                    txt.append("\"id\":\"" + setts.id + "\",");
                    txt.append(expert.getExpertState());
                    txt.append("}");
                    this.writeNode(txt.toString());
                    this.ordersInit();
                    break;
                case "ask":
                    this.ask = (double) json.get("precio");
                    expert.setAsk((double) json.get("precio"));
                    break;
                case "bid":
                    this.bid = (double) json.get("precio");
                    expert.onTick((double) json.get("precio"));
                    break;
                case "close-order":
                    expert.order.Close(this.id, dao.getOrder((String)json.get("value")));
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
    /**
     * notificamos a node que una nueva entro.
     * @param orden 
     */
    private void onOrder(ArrayList orden){
        try {
            
            ExecutionReport report = (ExecutionReport)orden.get(0);
            expert.openNotify(report);
            double sl = (double)orden.get(1);
            double tp = (double)orden.get(2); 
            StringBuffer nworden = new StringBuffer();
            nworden.append("{");        
                nworden.append("\"type\":\"onOrder\",");
                nworden.append("\"data\":");
                    nworden.append("{");        
                        nworden.append("\"id\":\""+this.id+"\",");
                        nworden.append("\"ordid\":\""+report.getClOrdID().getValue()+"\",");
                        nworden.append("\"tipo\":\""+report.getSide().getValue()+"\","); //tipo de operacion
                        nworden.append("\"lotes\":\""+(report.getOrderQty().getValue()/100000)+"\","); 
                        nworden.append("\"symbol\":\""+report.getSymbol().getValue()+"\",");
                        nworden.append("\"precio\":\""+report.getAvgPx().getValue()+"\",");                
                        nworden.append("\"sl\":\""+sl+"\",");                
                        nworden.append("\"tp\":\""+tp+"\"");                
                    nworden.append("}");
            nworden.append("}");
            this.writeNode(nworden.toString());
            nworden = null;
            
        } catch (FieldNotFound ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Notificamos que una orden fué cerrada exitosamente.
     * @param id 
     */
    public void onOrderClose(String id){
        expert.closeNotify();
        StringBuffer temp = new StringBuffer();
            temp.append("{");        
                temp.append("\"type\":\"onOrderClose\",");
                temp.append("\"data\":");
                temp.append("{"); 
                    temp.append("\"id\":\""+id+"\"");
                temp.append("}"); 
            temp.append("}");
            this.writeNode(temp.toString());
            //Sí entro el cierre de una operacion entonce borramos esa operación 
            //entonces borramos esa operacion de nuestro array de operaciones.
            for(int i=0; i<operaciones.size();i++){
                try {
                    ExecutionReport ord = (ExecutionReport)operaciones.get(i).get(0);
                    if(ord.getClOrdID().getValue().equals(id)){
                        operaciones.remove(i);
                    }
                } catch (FieldNotFound ex) {
                    Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
    /**
     * Enviamos ordenes actuales de la grafica.
     */
    private void ordersInit(){
         ArrayList<DBObject>temp=dao.getTotalGraf(this.id);
         StringBuffer nworden = new StringBuffer();
         for(int i=0; i<temp.size();i++){
            nworden.append("{");        
                nworden.append("\"type\":\"onOrderInit\",");
                nworden.append("\"data\":");
                    nworden.append("{");        
                        nworden.append("\"id\":\""+this.id+"\",");
                        nworden.append("\"ordid\":\""+temp.get(i).get("OrderID") +"\",");
                        nworden.append("\"tipo\":\""+temp.get(i).get("Type")+"\","); //tipo de operacion
                        nworden.append("\"lotes\":\""+((Double)temp.get(i).get("Size")/100000)+"\","); 
                        nworden.append("\"symbol\":\""+temp.get(i).get("Symbol")+"\",");
                        nworden.append("\"precio\":\""+temp.get(i).get("Price")+"\",");   
                        nworden.append("\"sl\":\""+temp.get(i).get("StopL") +"\",");                
                        nworden.append("\"tp\":\""+temp.get(i).get("TakeP")+"\"");
                    nworden.append("}");
            nworden.append("}\n");
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.writeNode(nworden.toString());
         }
    }
    
    /**
     * Añadimos una orden al array list para que cada gráfica sepa cuales son sus 
     * ordenés.
     * @param orden 
     */
    public void newOrder(ExecutionReport orden){
        
        ArrayList temp = new ArrayList();
        temp.add(orden);
        this.dao.recordOrden(this.id,orden,setts.MAGICMA);
        this.operaciones.add(temp);
    }
    
    public void orderModify(String ordid, Double precio){
        expert.modNotify();
        StringBuffer temp = new StringBuffer();
            temp.append("{");        
                temp.append("\"type\":\"orderModify\",");
                temp.append("\"data\":");
                temp.append("{"); 
                    temp.append("\"id\":\""+ordid+"\",");
                    temp.append("\"nwTp\":\""+precio+"\"");
                temp.append("}"); 
            temp.append("}");
            this.writeNode(temp.toString());
    }
    /**
     * Asignamos los Stops a la orden debida.
     * @param ordid
     * @param type
     * @param value 
     */
    public void setStops(String ordid,double tp, double sl){
        ExecutionReport temp;
        for (int i = 0; i < operaciones.size(); i++) {
            temp= (ExecutionReport)operaciones.get(i).get(0);
            try {//¡¡¡Siempre tenemos que comparar cadenas asi por que si no es un dolor de cabeza!!!
                if(temp.getClOrdID().getValue().equals(ordid)){
                    operaciones.get(i).add(tp);
                    operaciones.get(i).add(sl);
                    this.onOrder(operaciones.get(i));
                }
            } catch (FieldNotFound ex) {
                Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    /**
     * @return ArrayList de las operaciones.
     */
    public ArrayList<ArrayList> getOps(){
        return this.operaciones;
    }
    /**
     * @return ultimo precio de compra.
     */
    public Double getBid()throws NullPointerException{
        return this.bid;
    }
    
    /**
     * @return Ultimo precio de venta.
     */
    public Double getAsk() throws NullPointerException{
        return this.ask;
    }
    
    /**
     * @return id de la gráfica.
     */
    public String getID(){
        return this.id;
    }
    /**
     * @return Tp de la grafica obtenido de archivo .set
     */
    public double getTP(){
        return setts.tp;
    }
    /**
     * @return Sl de la grafica obtenido de archivo .set
     */
    public double getSL(){
        return setts.sl;
    }
    
    public double getNwTp(){
        return setts.nwTp;
    }
    /**
     * @return Point de la grafica obtenido de archivo .set
     */
    public double getPoint(){
        return setts.Point;
    }
    
    public int getMagic(){
        return setts.MAGICMA;
    }
}

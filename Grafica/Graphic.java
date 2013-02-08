package oms.Grafica;

import com.mongodb.DBObject;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.DAO.MongoDao;
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
    //private Expert expert;
    private ExpertMoc expert;
    private Candle candle;
    private String symbol;
    private int periodo;
    private String id;
    private Double bid, ask;
    //Guardamos las ordenes de que entran en cada gráfica.
    private ArrayList<ArrayList> operaciones = new ArrayList();
    public static MongoDao dao = new MongoDao();
    private Settings setts;
    private int lastOpen = GMTDate.getDate().getMinute();
    private PrintWriter blackBox;
    private StateFeed stateFeed;
    private SendMessage sendMessage;
    
    /**
     * Constructor!
     *
     * @param symbol par con el que trabajaremos
     * @param periodo tiempo con el que trabajaremos.
     * @throws IOException
     */
    public Graphic(Properties log_file) {
        setts = new Settings(log_file);
        this.symbol = setts.symbol;
        this.periodo = setts.periodo; 
        //Estos es para expert alternativo
        expert = new ExpertMoc();
        expert.absInit(setts.symbol, setts.periodo, setts);
        expert.Init();
        stateFeed = new StateFeed(expert);
        this.candle = new Candle(this.periodo);
        //expert = new Expert(setts);
        
        String path = "/home/omar/OMS/log/"+setts.symbol;
        try {
            blackBox = new PrintWriter(path+"/"+setts.symbol+setts.periodo+"-"+setts.MAGICMA + ".log","UTF-8");
         //Si no se encuentra la carpeta de log para esta moneda, la creamos y volvemos a
         // crear el archivo.
        } catch (IOException ex) {
            System.err.println("creando directorio de log para "+setts.symbol+"...");
            new File(path).mkdir();
            try {
                blackBox = new PrintWriter(path+"/"+setts.symbol+setts.periodo+"-"+setts.MAGICMA + ".log","UTF-8");
            } catch (Exception ex1) {
                Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex1);
            } 
        }
        this.writeBlackBoxFile("sesión iniciada...");
        this.id = setts.id;       
    }

    /**
     * Como cada gráfica es un thread...
     */
    @Override
    public void run() {

        try {
            String inputLine;
            String modifiedSentence;
            this.socket = new Socket("127.0.0.1", 3000);
            this.outNode = new DataOutputStream(this.socket.getOutputStream());
            sendMessage = new SendMessage(outNode, stateFeed);
            //al iniciar enviamos a Node los settings de el expert.
            this.sendMessage.logIn();
            this.sendMessage.ExpertState();
            //Leemos mensajes de node
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
            StringBuffer msjin = new StringBuffer();
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
     * Cada vez que que tenemos una vela nueva detonamos este método.
     */
    public void onCandle(double openCandle) {
        this.sendMessage.Candle();
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
        ArrayList temp = (ArrayList) dao.getCandleData(symbol, cant).toArray();
        return temp;
    }

    /**
     * Metodo que quita el / en un symbol, EUR/USD resulta en EURUSD ya que en
     * mongo los nombres de las monedas se encuentran así.
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
                    double open = (double) json.get("precio");
                    
                    
                    //Este evento puede ser llamado cada tick, auqque mosotros
                    //lo llamamos cada open para ahorrar recursos.
                    this.expert.open_min = open;
                    this.expert.onTick();
                    //Si es una nueva vel
                    if (candle.isNewCandle(GMTDate.getTime()) == 1) {
                        this.expert.indicator.appendBollsData(open);
                        this.sendMessage.ExpertState();
                    }
                    //Tenemos una vela muerta, osea que abrió con precios muertos.
                    else if(candle.isNewCandle(GMTDate.getTime()) == -1){
                        
                    }
                    //Si el expert puede operar
                    if(expert.isActive()){
                        this.writeBlackBoxFile(stateFeed.getExpertState());
                    }
                    this.sendMessage.Open();
                    break;
                case "close":
                    //TODO hacer algo con este precio de cierre
                    System.err.println("Close: "+msj+ "!");
                    break;
                case "get-state":
                    this.sendMessage.ExpertState();
                    this.ordersInit();
                    break;
                case "ask":
                    //expert.setAsk((double) json.get("precio"));
                    expert.Ask = ((double) json.get("precio"));
                    break;
                case "bid":
                    //expert.onTick((double) json.get("precio"));
                    expert.Bid = ((double) json.get("precio"));
                    break;
                case "close-order":
                    expert.order.Close(this.id, dao.getOrder((String)json.get("value")));
                    break;
                default:
                    System.out.println("Mensaje no identificado"+ json.toString());
            }

        } catch (ParseException ex) {
            System.out.println("Colapso!: " + msj);
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Escrimos un archivo de log para la grafica en donde almacenaremos, información
     * relacionada con el comportamiento de la grafica, un tipo de caja negra.
     */
    private void writeBlackBoxFile(Object log){
        
        this.blackBox.println(GMTDate.getDate()+" -> "+log);
        this.blackBox.flush();
    }
    
    /**
     * notificamos a node que una nueva entro.
     * @param orden 
     */
    private void onOrder(ArrayList orden){
        
        ExecutionReport report = (ExecutionReport)orden.get(0);
        double sl = (double)orden.get(1);
        double tp = (double)orden.get(2);
        expert.openNotify(report);
        sendMessage.nwOrden(report, sl, tp); 
    }
    /**
     * Notificamos que una orden fué cerrada exitosamente.
     * @param id 
     */
    public void onOrderClose(String id){
        expert.closeNotify();
        sendMessage.clOrden(id);
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
         sendMessage.ordersInit(dao.getTotalGraf(this.id),this.id);
    }
    
    /**
     * Añadimos una orden al array list para que cada gráfica sepa cuales son sus 
     * ordenés y guardamos la orden en Mongo.
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
        this.sendMessage.modOrden(ordid,precio);
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
        return expert.Bid;
    }
    
    /**
     * @return Ultimo precio de venta.
     */
    public Double getAsk() throws NullPointerException{
        return expert.Ask;
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

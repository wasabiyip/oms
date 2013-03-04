package oms.Grafica;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.CustomException.OrdenNotFound;
import oms.Grafica.DAO.MongoDao;
import oms.deliverer.Orden;
import oms.deliverer.OrderHandler;
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
    //private Expert expert;
    private ExpertMoc expert;
    private Candle candle;
    private String symbol;
    private int periodo;
    private String id;
    private Double bid, ask;
    //Guardamos las ordenes de que entran en cada gráfica.
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
     * Como cada gráfica es un thread entonces...
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
                    
                    //Si es una nueva vel
                    if (candle.isNewCandle(GMTDate.getTime())) {
                        this.expert.indicator.appendBollsData(open);
                        this.expert.onTick();
                        this.sendMessage.ExpertState();
                        this.sendMessage.Open();
                       
                    }else{
                        this.expert.onTick();
                        this.sendMessage.Open(); 
                    }
                    
                    //Si el expert puede operar
                    /*if(expert.isActive()){
                        this.writeBlackBoxFile(stateFeed.getExpertState());
                    }*/
                    break;
                case "close":
                    //TODO hacer algo con este precio de cierre
                    System.err.println("Close: "+msj+ "!");
                    break;
                case "get-state":
                    this.sendMessage.ExpertState();
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
                    try {
                        //expert.order.Close(this.id, dao.getOrder((String)json.get("value")));
                        Orden orden = OrderHandler.getOrdenById(id);
                        if(orden.getSide() == '1'){
                            orden.close(expert.Bid);
                        }else if(orden.getSide() == '1'){
                            orden.close(expert.Ask);
                        }
                    } catch (OrdenNotFound ex) {
                        Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
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

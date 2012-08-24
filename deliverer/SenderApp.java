package oms.deliverer;


import oms.Grafica.Graphic;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.dao.MongoConnection;
import oms.util.Console;
import oms.deliverer.MessageHandler;
import oms.util.Node;
import quickfix.*;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix42.MarketDataRequest;

/**
 * Clase que maneja mensajes recibidos del servidor remoto. 
 * @author omar
 */
public class SenderApp extends MessageCracker implements Application{
    private String passWord = "omar2012";
    private String userName = "GMIDemo00292fix";
    public static PriceBeat pricebeat = new PriceBeat();
    PriceSensitive pricesense = new PriceSensitive();
    public static MongoConnection mongo;
    public static SessionID sessionID;
    private GraficaHandler graficaHandler = new GraficaHandler();
    
    /**
     * Método que se ejecuta al crear aplicación.
     * @param id 
     */
    @Override
    public void onCreate(SessionID id){
        
        
        /*pricebeat.addObserver(new Expert());*/
        //Console console = new Console();   
        mongo = MongoConnection.getInstance();
        /*try {
            Graphic eurusd = new Graphic("EUR/USD", 5);
            //Graphic usdjpy = new Graphic("USD/JPY", 5);
            eurusd.start();
            //usdjpy.start();
        } catch (IOException ex) {
            Logger.getLogger(SenderApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }
    
    /**
     * Método que se ejecuta al iniciar sesión correctamente.
     * @param id 
     */
    @Override
    public void onLogon(SessionID id) {
        SenderApp.sessionID = id;
        this.graficaHandler.addGrafica("EUR/USD", 5);
        this.graficaHandler.addGrafica("GBP/USD", 5);
        //this.graficaHandler.addGrafica("USD/JPY", 5);
        //this.graficaHandler.addGrafica("USD/CHF", 5);
    }
    
    /**
     * Método que se ejecuta al cerrar sesión correctamente.
     * @param id 
     */
    @Override
    public void onLogout(SessionID id){
        System.out.println("onLogout->>");
    }
    /**
     * Método que envia al servidor el Usuario y la contraseña
     * @param msg
     * @param id 
     */
    @Override
    public void toAdmin(quickfix.Message msg, SessionID id){
        final Message.Header header = msg.getHeader();
        try{
            if(header.getField(new MsgType()).valueEquals(MsgType.LOGON)){
                msg.setField(new Username(userName));
                msg.setField(new Password(passWord));
                msg.setField( new ResetSeqNumFlag(true));
            }
        }catch(FieldNotFound e){
            System.out.println(msg.getHeader()+ " " + e);
        }
    }
    
    /**
     * Método que se ejecuta al recibir un mensaje de administrador.
     * @param msg
     * @param id
     * @throws FieldNotFound
     * @throws IncorrectDataFormat
     * @throws IncorrectTagValue
     * @throws RejectLogon 
     */
    @Override
    public void fromAdmin(quickfix.Message msg, SessionID id) throws FieldNotFound, 
                            IncorrectDataFormat, IncorrectTagValue, RejectLogon{
        System.out.println("fromAdmin->>");
        
    }
    
    /**
     * Se ejecuta al recibir mensajes de aplicación
     * @param msg
     * @param id
     * @throws DoNotSend 
     */
    @Override
    public void toApp(quickfix.Message msg, SessionID id) throws DoNotSend{
        
    }
    
    /**
     * Este método se ejecuta cada vez que el servidor envia un mensaje
     * aqui se usa el método crack que es heredado de MessageCracker.
     * @param msg
     * @param id
     * @throws FieldNotFound
     * @throws IncorrectDataFormat
     * @throws IncorrectTagValue
     * @throws UnsupportedMessageType 
     */
    @Override
    public void fromApp(quickfix.Message msg, SessionID id) throws FieldNotFound,
                IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType{
       
        this.crack(msg,id);
    }
    
    /**
     * Método que se ejecuta al recibir un TradingSessionStatus, al recibir este
     * mensaje procedemos a pedir un MarketDataRequest.
     * @param status
     * @param sessionID
     * @throws SessionNotFound 
     */
    public void onMessage(quickfix.fix42.TradingSessionStatus status,
                        SessionID sessionID) throws SessionNotFound{
                /*        
                quickfix.fix42.MarketDataRequest mdr = new quickfix.fix42.MarketDataRequest();
                this.sessionID = sessionID;
                mdr.set(new MDReqID("A"));
                mdr.set(new SubscriptionRequestType('1'));
                mdr.set(new MarketDepth(1));
                mdr.set(new MDUpdateType(1));
                mdr.set(new AggregatedBook(true));
                                
                MarketDataRequest.NoRelatedSym relatedSymbols = new MarketDataRequest.NoRelatedSym(); 
                relatedSymbols.set(new Symbol("EUR/USD"));
                
                MarketDataRequest.NoMDEntryTypes mdEntryTypes = new MarketDataRequest.NoMDEntryTypes();
                mdEntryTypes.set(new MDEntryType('0')); // bid
                mdr.addGroup(mdEntryTypes);
                mdEntryTypes.set(new MDEntryType('1')); // Offer = Ask
                
                mdr.addGroup(mdEntryTypes);
                mdr.addGroup(relatedSymbols); 
                
                Session.sendToTarget(mdr, sessionID);   */
    }
    /**
     * Este se ejecuta al recibir un MarketDataIncrementalRefresh, que en pocas
     * palabras es un streaming de precios.
     * @param msj
     * @param sessionID
     * @throws FieldNotFound 
     */
    public void onMessage(quickfix.fix42.MarketDataIncrementalRefresh msj,
            SessionID sessionID) throws FieldNotFound {
        
        MessageHandler.marketDataPx(msj);
        pricebeat.notifyObservers();        
    }
    
    public void onMessage(quickfix.fix42.ExecutionReport msj, SessionID sessionID) throws FieldNotFound, Exception{
        
        MessageHandler.executionReport(msj);
        
    }
    
    public void onMessage(quickfix.fix42.Reject msj) throws FieldNotFound{
        
        MessageHandler.errorHandler(msj);
    }
}

package oms.deliverer;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.Grafica.Graphic;
import oms.dao.MongoConnection;
import quickfix.*;
import quickfix.field.*;

/**
 * Clase que maneja mensajes recibidos del servidor remoto. 
 * @author omar
 */
public class SenderApp extends MessageCracker implements Application{
    private String passWord ;
    private String userName ;
    public static MongoConnection mongo;
    public static SessionID sessionID;
    private GraficaHandler graficaHandler = new GraficaHandler();
    boolean lock = false;
    
    
    /**
     * Constructor nos loggeamos a node al construir esta clase.
     */
    public SenderApp(String userName, String passWord){
        this.userName = userName;
        this.passWord = passWord;
        MessageHandler.Init();
        OrderHandler.Init();                
    }
    
    /**
     * Método que se ejecuta al crear aplicación.
     * @param id 
     */
    @Override
    public void onCreate(SessionID id){
        //Si queremos usar la consola personalizada.
        //Console console = new Console();   
        mongo = MongoConnection.getInstance();
    }
    
    /**
     * Método que se ejecuta al iniciar sesión correctamente.
     * @param id 
     */
    @Override
    public void onLogon(SessionID id) {
        MessageHandler.mStreaming.msg("Conectados exitosasmente con "+id+" desde la cuenta " + this.userName);
       
        SenderApp.sessionID = id;
        //Para que los threads no se dupliquen cuando el servidor nos desconecta.
        if(!lock){
            //Enviamos el login de app.            
            MessageHandler.mStreaming.login(this.graficaHandler.getProfile());
            //corremos la gráfica.
            this.graficaHandler.runProfile();
            lock = true;
        }
    }
    
    /**
     * Método que se ejecuta al cerrar sesión correctamente.
     * @param id 
     */
    @Override
    public void onLogout(SessionID id){
        MessageHandler.mStreaming.msg("Recibimos Logout del broker esperando para volver a conectarnos-...");
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
        //Pues nada...
    }
    
    /**
     * Se ejecuta al recibir mensajes de aplicación
     * @param msg
     * @param id
     * @throws DoNotSend 
     */
    @Override
    public void toApp(quickfix.Message msg, SessionID id) throws DoNotSend{
        //Pues nada...
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
    }
    
    public void onMessage(quickfix.fix42.ExecutionReport msj, SessionID sessionID) throws FieldNotFound, Exception{
        MessageHandler.executionReport(msj);
    }
    
    public void onMessage(quickfix.fix42.OrderCancelReject ordCancelRej,SessionID sessionID){
        System.err.println("El Horror!... no se pudo modificar la orden: " + ordCancelRej);
    }
    public void onMessage(quickfix.fix42.Reject msj, SessionID sessionID) throws FieldNotFound{
        MessageHandler.errorHandler(msj);
    }
}

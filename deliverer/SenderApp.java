package oms.deliverer;


import java.util.logging.Level;
import java.util.logging.Logger;
import oms.dao.MongoConnection;
import oms.dao.MongoDao;
import oms.util.Console;
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
    private GraficaHandler graficaHandler ;
    boolean lock = false;
    private boolean logged = false;
    private String path;
    private SessionSettings settings;
    private static MongoDao dao;
    /**
     * Constructor nos loggeamos a node al construir esta clase.
     */
    public SenderApp(SessionSettings settings, String path){
        try {
            this.userName = settings.getString("UserName");
            this.passWord = settings.getString("PassWord");
            this.settings = settings;
            MessageHandler.Init();
            OrderHandler.setPath(path);
            OrderHandler.Init();                
            this.path = path;
            graficaHandler = new GraficaHandler(this.path);
             /**
            * Construimos Data Access Obj.
            */
            dao = new MongoDao(settings.getString("RemoteHost"),Integer.parseInt(settings.getString("RemotePort")),"history");
        } catch (ConfigError | FieldConvertError ex) {
            Logger.getLogger(SenderApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void setLogIn(boolean log){
        
        this.logged = log;
        Console.warning("Iniciando perfil: "+this.graficaHandler.getProfile());
        if(this.logged){
            //corremos la gráfica.
            this.graficaHandler.runProfile();
        }
    }
    /**
     * 
     * @return 
     */
    public static MongoDao getDAO(){
        return dao;
    }
    /**
     * Método que se ejecuta al crear aplicación.
     * @param id 
     */
    @Override
    public void onCreate(SessionID id){
       
    }
    
    /**
     * Método que se ejecuta al iniciar sesión correctamente.
     * @param id 
     */
    @Override
    public void onLogon(SessionID id) {
        //MessageHandler.mStreaming.msg("Conectados exitosasmente con "+id+" desde la cuenta " + this.userName);
        //Console.info("Conectados exitosasmente con "+id+" desde la cuenta " + this.userName);
        SenderApp.sessionID = id;
        //Para que los threads no se dupliquen cuando el servidor nos desconecta.
        if(!lock){ 
            MessageHandler.mStreaming.login(this.graficaHandler.getProfile());
            //Enviamos el login de app.            
            lock = true;            
        }
    }
    
    /**
     * Método que se ejecuta al cerrar sesión correctamente.
     * @param id 
     */
    @Override
    public void onLogout(SessionID id){
        Console.info("Recibimos Logout del broker esperando para volver a conectarnos-...");
        Console.warning("LOGOUT! -> "+id);
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
        }catch(FieldNotFound ex){
            Console.exception(msg.getHeader()+ " " + ex);
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
        //Enviamos cada mensaje recibido al manejador de mensajes.
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
                //Nada.
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
        //Nada
    }
    
    public void onMessage(quickfix.fix42.ExecutionReport msj, SessionID sessionID) throws FieldNotFound, Exception{
        //Evaluamos ExcecutionReport
        MessageHandler.executionReport(msj);
    }
    
    public void onMessage(quickfix.fix42.Reject msj, SessionID sessionID) throws FieldNotFound{
        MessageHandler.errorHandler(msj);
    }
}

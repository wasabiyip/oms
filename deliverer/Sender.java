package oms.deliverer;

import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import oms.Grafica.GMTDate;
import oms.util.Console;
import quickfix.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase que inicia la aplicación, este es el punto de partida.
 * @author Omar
 */
public class Sender  {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private Initiator initiator;
    private static Sender sender;
    private boolean initStarted = false;
    private static Logger log = LoggerFactory.getLogger(Sender.class);
    /**
     * 
     * @throws Exception 
     */
    public Sender() throws Exception{
        System.out.println("Ingresa el nombre del usuario:");
        String input = new Scanner(System.in).next();
        String path = "/home/"+input;
        SessionSettings settings = new SessionSettings("/home/"+input+"/OMS/config/app.cnf");
        /**
         * El path es la raíz en donde se encuentra la carpeta del programa, 
         * localmente: /home/omar
         * Server: /home/omarloren.
         * TODO: Validar o algo.
         */ 
        System.out.println("Hora del servidor:"+GMTDate.getTime());
        Console.setPath(path);
        SenderApp application = new SenderApp(settings, path);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true,true, true, true);
        MessageFactory messageFactory = new DefaultMessageFactory();
        initiator =  new SocketInitiator(application, messageStoreFactory, settings, 
                                        logFactory, messageFactory);        
    }
    
    public synchronized void logon(){
        if (!initStarted){
            try{
                initiator.start();
                initStarted = true;
                
            }catch(Exception ex){
                Console.error(ex);
                log.error("Colapso en Login", ex);
            }   
        }else{
            Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
            while(sessionIds.hasNext()){
                SessionID sessionId = (SessionID) sessionIds.next();
                Session.lookupSession(sessionId).logon();
            }
        }
    }
    public void stop(){
        shutdownLatch.countDown();
    }
    /**
     * Donde todo inicia.
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception{
        try{
            
        }catch(Exception ex){
            Console.exception(ex);
            log.info(ex.getMessage(), ex);
        }
        
        sender =new Sender();
        sender.logon();
                
        shutdownLatch.await();
    }
}

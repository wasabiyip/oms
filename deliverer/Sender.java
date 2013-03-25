package oms.deliverer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
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
        System.out.println("Ingresa la raíz:");
        String input = new Scanner(System.in).next();
        /*InputStream inputS = new BufferedInputStream(
                                new FileInputStream(
                                new File(input)));*/
                                //new File("/home/omar/OMS/config/GMIDemo00292str.cnf")));
        String path = input;
        SessionSettings settings = new SessionSettings(input+"/OMS/config/app.cnf");
        /**
         * El path es la raíz en donde se encuentra la carpeta del programa, 
         * localmente: /home/omar
         * Server: /home/omarloren.
         * TODO: Validar o algo.
         */ 
        
        SenderApp application = new SenderApp(settings.getString("UserName"),settings.getString("PassWord"), path);
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
            }catch(Exception e){
                log.error("Colapso en Login", e);
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
    public static void main(String[] args) throws Exception{
        try{
            
        }catch(Exception e){
            log.info(e.getMessage(), e);
        }
        
        sender =new Sender();
        sender.logon();
                
        shutdownLatch.await();
    }
}

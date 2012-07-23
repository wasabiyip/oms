package oms.deliverer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
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
        InputStream inputS = new BufferedInputStream(
                                new FileInputStream(
                                new File("config/sender.cnf")));
     
        SessionSettings settings = new SessionSettings(inputS);
        inputS.close();
        
        SenderApp application = new SenderApp();
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

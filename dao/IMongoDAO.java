package oms.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.dao.MongoConnection;

/**
 *
 * @author omar
 */
public abstract class IMongoDAO {

    public String host;
    public Integer puerto;
    public String datab;
    public String jsonF;
    public String collection;
    public String history;
    public final String PATTERN [] = {"Symbol", "Date","Time","Open", "High", "Low", "Close", "Vol"};
    private String path;
    public IMongoDAO(String path){
        Properties config = new Properties();
        this.path = this.path;
        try {
                /**
                 * TODO hacer que este archivo sea variable.
                 */
                config.load(new FileInputStream(path+"/OMS/config/app.cnf"));
                
            } catch (IOException ex) {
                Logger.getLogger(MongoConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        host = config.getProperty("host"); 
        puerto = new Integer( 27017);;
        datab = config.getProperty("operacions");
        collection = config.getProperty("log");        
    }
}

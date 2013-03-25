package oms.dao;

import com.mongodb.*;
import com.mongodb.util.JSON;
import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author omar
 * Esta clase Guarda los históricos en mongo.
 */
public class MongoPopulation extends IMongoDAO {
    private BufferedReader bfr;
    String strline;
    DBObject doc;
    MongoConnection mongo;
    DBCollection coll;
        
    public MongoPopulation(String str, String path) throws Exception {
        super(path);
        mongo = MongoConnection.getInstance();
        mongo.connect();
        DB database = mongo.getDataBase();
        coll = database.getCollection(str);
        
        
        
    }
    public void doFilePop()throws UnknownHostException, Exception{
        
        bfr = new BufferedReader(new InputStreamReader(
                    new DataInputStream( new FileInputStream(super.jsonF))));
        while ((strline = bfr.readLine()) != null){
         
            doc = (DBObject)JSON.parse(strline);
            coll.insert(doc);
        }
    }
    
    public void singleDataPop(ArrayList data){
        BasicDBObject obj =  new BasicDBObject();
        
        for (int i=0; i < super.PATTERN.length; i++){
            obj.put(super.PATTERN[i], data.get(i));
           
        }
        coll.insert(obj);
        
    }
}

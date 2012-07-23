package oms.dao;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.MongoException;
import oms.dao.IMongoDAO;

/**
 *
 * @author Omar
 * 
 * Esta clase crea un Singletone de la conexion a mongodb, regresa la conexion o
 * tambien regresa una colleccion, Tenemos algunas variables estáticas por 
 * el Singletone.
 */
public class MongoConnection extends IMongoDAO {

    private static MongoConnection INSTANCE;
    private static Mongo m = null;
    DB db;  
        
    /**
     * Constructor vacio de acuerdo para hacer Singletone.
     */
    private MongoConnection(){
        //---//
    }
    
    /**
     * Aqui es donde creamos el objeto mongo si es que no se ha creado, y este
     * es el que es retornado.
     */
    public synchronized static MongoConnection getInstance(){
        if(INSTANCE == null){
            INSTANCE = new MongoConnection();
            
        }
        return INSTANCE;
    }
    
    /**
     * Este método inicia la conexión con Mongo.
     * @throws Exception 
     */
    public synchronized void connect() throws Exception{
        
        try{
            
            MongoConnection.m = new Mongo(super.host, super.puerto);
            
        }catch (MongoException.Network e){
            throw new Exception("No se pudo conectar a MongoDB");
        }
    }
    
    /**
     * 
     * @return El objeto mongo que contiene la conexión  actual con la base de
     *         datos.
     */
    public Mongo getConnection(){
        
        return MongoConnection.m;
    }
    
    /**
     * 
     * @return La base de datos especificada en el archivo de configuración.
     */
    public DB getDataBase(){
        
        this.db = m.getDB(super.datab);
        return this.db;
    }
}

package oms.Grafica.DAO;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.util.Console;

/**
 *
 * @author Omar
 *
 * Esta clase crea un Singletone de la conexion a mongodb, regresa la conexion o
 * tambien regresa una colleccion, Tenemos algunas variables estáticas por el
 * Singletone.
 */
public class MongoConnection {

    private static MongoConnection INSTANCE;
    public static Mongo m;
    DB db;

    /**
     * Constructor vacio de acuerdo para hacer Singletone.
     */
    private MongoConnection() {
        try {
            connect();
            this.db = MongoConnection.m.getDB("history");
        } catch (Exception ex) {
            Logger.getLogger(MongoConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Aqui es donde creamos el objeto mongo si es que no se ha creado, y este
     * es el que es retornado.
     */
    public synchronized static MongoConnection getInstance() {
        if (INSTANCE == null) {
            try {
                m = new Mongo("localhost", 27017);
                INSTANCE = new MongoConnection();
                
            } catch (UnknownHostException ex) {
               Console.exception(ex);
            } catch (MongoException ex) {
                Console.exception(ex);
            }
        }
        return INSTANCE;
    }

    /**
     * Este método inicia la conexión con Mongo.
     *
     * @throws Exception
     */
    public static void connect() throws Exception {

       
    }

    /**
     *
     * @return El objeto mongo que contiene la conexión actual con la base de
     * datos.
     */
    public Mongo getConnection() {

        return MongoConnection.m;
    }
}

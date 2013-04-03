package oms.dao;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 *
 * @author Omar
 *
 * Esta clase crea un Singletone de la conexion con mongo para asegur que sólo
 * haya una instancia de la DB.
 */
public class MongoConnection {

    private static MongoConnection INSTANCE;
    //Representacion de mongo.
    Mongo m = null;

    /**
     * Constructor vacio de acuerdo para hacer Singletone.
     */
    private MongoConnection() {
        //---//
    }

    /**
     * Aqui es donde creamos el objeto mongo si es que no se ha creado, y este
     * es el que es retornado.
     */
    public synchronized static MongoConnection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MongoConnection();

        }
        return INSTANCE;
    }

    /**
     * Establecemos conexión con host.
     *
     * @throws Exception
     */
    public synchronized void connect(String host, Integer port) throws Exception {

        try {
            System.err.println("Conectando "+host + " "+ port);
            this.m = new Mongo(host, port);

        } catch (MongoException.Network e) {
            throw new Exception("No se pudo conectar a MongoDB");
        }
    }
}

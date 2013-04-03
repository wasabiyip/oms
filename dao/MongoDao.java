package oms.dao;

import com.mongodb.*;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.deliverer.Orden;
import oms.util.fixToJson;

/**
 *
 * @author omar Clase que se comunica con mongo para obtener datos.
 */
public class MongoDao {

    protected MongoConnection mongo;
    private DB db;
    private DBCollection coll;
    private DBCursor cursor;
    private BasicDBObject query = new BasicDBObject();
    private BasicDBObject sort;
    private ArrayList<Double> arrList = new ArrayList();

    /**
     * Constructor Obtiene la conexión con mongo y hace un query con el
     * parametro recibido.
     *
     * @param date fecha apartir de la cual queremos obtener datos.
     * @throws Exception
     */
    public MongoDao(String host, Integer port, String db) {
       
        mongo = MongoConnection.getInstance();
        try {
            
            mongo.connect(host, port);
            this.db = mongo.m.getDB(db);
        } catch (Exception ex) {
            Logger.getLogger(MongoDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DB getDB() {
        return this.db;
    }

    public void setDB(String db) {
        this.db = mongo.m.getDB(db);
    }

    public void setCollection(String coll) {
        this.coll = this.db.getCollection(coll);
    }

    /**
     * Obtenemos un campo especifico desde.
     *
     * @param field ccampo que queremos obtener.
     * @return
     */
    private ArrayList getField(String field) {
        ArrayList<ArrayList> doble;
        ArrayList temp = new ArrayList();
        doble = new ArrayList();
        DBObject obj;

        while (this.cursor.hasNext()) {
            obj = cursor.next();
            temp.add(obj.get("Date"));
            temp.add(obj.get("Time"));
            temp.add((Double) (obj.get(field)));
            doble.add(new ArrayList(temp));

            temp.clear();
        }
        return doble;
    }

    public DBCursor getCandleData(String symbol, int periodo) {
        ArrayList precios = new ArrayList();
        this.setDB("history");
        this.setCollection(symbol);
        DBCursor cursor;
        if (periodo > 0) {
            //Obtenemos la cantidad de datos necesarios para formar velas de ciertos
            //periodos
            cursor = this.coll.find().sort(new BasicDBObject("$natural", -1)).limit(periodo);

        } else {
            return null;
        }
        return cursor;
    }
    /**
     * Guardamos la representacion de una orden.
     * @param orden 
     */
    public void recordOrden(Orden orden){
        DBObject obj;
        String entry = "";
        String json = new fixToJson().parseOrder(orden);
        this.setCollection("operaciones");
        obj = (DBObject) JSON.parse(json);
        this.coll.insert(obj);
    }
}

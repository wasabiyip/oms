package oms.Grafica.DAO;

import com.mongodb.*;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import oms.Grafica.Settings;
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
    private BasicDBObject fechaMin;
    private BasicDBObject sort;
    private ArrayList<Double> arrList = new ArrayList();
    
    /**
     * Constructor Obtiene la conexión con mongo y hace un query con el
     * parametro recibido.
     *
     * @param date fecha apartir de la cual queremos obtener datos.
     * @throws Exception
     */
    public MongoDao() {

        mongo = MongoConnection.getInstance();
        
    }

    public DBCursor query(int ini, int fin) {

        ArrayList<String> temp;
        DBCursor cursor;

        query.put("Date", new BasicDBObject("$gte", ini).append("$lte", fin));
        cursor = coll.find(query);
        return cursor;
    }

    public DB getDB() {
        return this.db;
    }

    public DBCollection getCollection(String coll) {
        return this.mongo.db.getCollection(coll);
    }

    public void setDB(String db) {
        this.db = mongo.getConnection().getDB(db);
    }

    public void setCollection(String coll) {
        this.coll = this.db.getCollection(coll);
    }

    /**
     * Cambia la fecha que se esta utilizado.
     *
     * @param date Nueva fecha con la que trabajaremos.
     */
    public void setDate(int date) {

        query.put("Date", new BasicDBObject("$gt", date));
        this.cursor = coll.find(query).sort((new BasicDBObject("Date", -1)));
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
    
    public void recordOrden(Orden orden){
        DBObject obj;
        String entry = "";
        String json = new fixToJson().parseOrder(orden);
        DBCollection coll = getCollection("operaciones");
        obj = (DBObject) JSON.parse(json);
        coll.insert(obj);
    }
    
    /**
     * Obtenemos el número total de ordenés por Grafica.
     *
     * @return
     * @throws Exception
     */
    public ArrayList <DBObject> getTotalGraf(String id) {
        ArrayList <DBObject> temp = new ArrayList();
        DBCursor res;
        DBCollection coll = getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        query.put("grafica", id);
        res = coll.find(query);
        while(res.hasNext()){
            temp.add(res.next());
        }
        return temp;
    }
    /**
     * Obtenemos el número total de ordenés por Magic number.
     *
     * @return
     * @throws Exception
     */
    public ArrayList <DBObject> getTotalMagic(int magic) {
        ArrayList <DBObject> temp = new ArrayList();
        DBCursor res;
        DBCollection coll = getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        query.put("MAGICMA", magic);
        res = coll.find(query);
        while(res.hasNext()){
            temp.add(res.next());
        }
        return temp;
    }
    /**
     * Obtenemos el número total de ordenés por Magic number.
     *
     * @return
     * @throws Exception
     */
    public int getTotalMagic(Integer magic) {
        ArrayList <DBObject> temp = new ArrayList();
        DBCursor res;
        DBCollection coll = getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        query.put("MAGICMA", magic);
        res = coll.find(query);
        while(res.hasNext()){
            temp.add(res.next());
        }
        return temp.size();
    }
    /**
     * Obtenemos el número todatal de ordenes de un cruce determinado.
     * @param symbol
     * @return 
     */
    public int getTotalCruce(String symbol){
        ArrayList <DBObject> temp = new ArrayList();
        DBCursor res;
        DBCollection coll = getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        query.put("Symbol", Settings.Slash(symbol));
        res = coll.find(query);
        return res.count();
    }
    
    public DBObject getOrder(String ordid){
        DBObject temp = null ;
        DBCursor res;
        DBCollection coll = getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("OrderID", ordid);
        res = coll.find(query);
        while(res.hasNext()){
            temp = res.next();
        }
        if(res.count()>1)
            System.err.println("Colapso en oms.Grafica.DAO.MongoDao se encontro mas de una orden");
        return temp;
    }
    public int getTotalActivas(){
        DBCursor res;
        DBCollection coll = getCollection("operaciones");
        BasicDBObject query = new BasicDBObject();
        query.put("Status", 1);
        res = coll.find(query);
        return res.count();
    }
    public double getCloseAnterior(String symbol){
        DBCursor cursor = this.coll.find().sort(new BasicDBObject("$natural", -1)).limit(2);
        cursor.next();
        return (double)cursor.next().get("Close");
    }
}

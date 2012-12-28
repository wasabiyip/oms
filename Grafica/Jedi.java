package oms.Grafica;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.deliverer.OrderHandler;
import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;

/**
 * Esta clase maneja funciones del expert de mas alto nivel. como saber en tiempo de
 * ejecucion si tenemos una orden para bloquear o desbloquer entradas/salidas.
 * @author omar
 */
public abstract class Jedi {
    Settings setts;
    private int periodo;
    public Order order;
    //La usamos para guardar el tipo de orden que esta entrando. 1 es compra y 2 
    //es venta
    public char currentOrder = '0';
    public boolean lock_op= true;
    boolean modify= false;
    public int velasCont = 0;
    public double bid=0.0;
    public double ask=0.0;
    public double open_min=0.0;
    public double lastOrderPrice;
    public ExecutionReport lastOrder;
    public int contVelas=0;
    private boolean grafic_lock = false;
    Jedi(Settings setts){
        this.setts = setts;
        order = new Order(setts.symbol,setts.MAGICMA,setts.id);
    }
    public abstract void onTick(Double price);
    public abstract void onCandle(Double price);
    public void onOpen(Double price){
        open_min = price;
        //System.out.println(this.setts.symbol + " " + this.setts.periodo + " " + this.isActive());
    }
    /**
     * Método que regresa los valores que la clase Settings lee del archivo de
     * configuración .set
     * @return settings del expert.
     */
    public StringBuffer getExpertInfo() {

        StringBuffer init = new StringBuffer();
        
        init.append("\"settings\" : {");
            init.append("\"symbol\" : \"" + setts.symbol+"\",");
            init.append("\"ID\" : \"" + setts.id + "\",");
            init.append("\"Magicma\" : " + setts.MAGICMA + ",");
            init.append("\"Lotes\" : " + setts.lots + ",");
            init.append("\"Boll1\" : " + setts.boll1 + ",");
            init.append("\"Boll2\" : " + setts.boll2 + ",");
            init.append("\"Boll3\" : " + setts.boll3 + ",");
            init.append("\"BollS1\" : " + setts.bollS1 + ",");
            init.append("\"BollS2\" : " + setts.bollS2 + ",");
            init.append("\"BollS3\" : " + setts.bollS3 + ",");
            init.append("\"TP\" : " + setts.tp + ",");
            init.append("\"SL\" : " + setts.sl + ",");
            init.append("\"VelasSalida\": " + setts.velasS + ",");
            init.append("\"horaInicial\":" + setts.horaIni + ",");
            init.append("\"horaFinal\" :" + setts.horaFin +",");
            init.append("\"horaSalida\" :" + setts.horaIniS+",");
            init.append("\"Periodo\" :" + setts.periodo + ",");
            init.append("\"bollSpecial\" :" + setts.boll_special+",");
            init.append("\"spreadAsk\" :" + setts.spreadAsk + ",");
            init.append("\"limiteCruce\" :" + setts.limiteCruce);
            init.append("}");
        return init;
    }
    /**
     * regresamos si nos encontramos dentro del limite de operaciones por cruce.
     * (por cruce significa por el symbol).
     * @return 
     */
    public boolean limiteCruce(){
        boolean temp = false;
        int count = Graphic.dao.getTotalCruce(setts.symbol);
        if(count<setts.limiteCruce)
            temp = true;
        return temp;
    }
    /**
     * 
     * @return el id de la grafica.
     */
    public String getID(){
        return setts.id;
    }
    /**
     * 
     * @param price
     * @param type 
     */
    public void orderClose(Double price, char type){
        order.Close(price, '1');
        this.lastOrderPrice = price;
    }
    /**
     * 
     * @param price
     * @param type 
     */
    public void orderSend(Double price, char type){
        order.Open(price, type);
    }
    /**
     * Nos dicen si una orden cerro.
     */
    public void closeNotify(){
        currentOrder = '0';
        this.lock_op = true;
        this.velasCont = 0;
        this.modify = false;
    }
    /**
     * Ponemos modify como true para asegurarnos que no se vuelva a modificar una orden.
     */
    public void modNotify(){
        this.modify = true;
    }
    /**
     * Nos avisan si una orden entro
     * @param type 
     */
    public void openNotify(quickfix.fix42.ExecutionReport order){
        try {
            this.lastOrder = order;
            currentOrder = order.getSide().getObject();
            System.err.println("Orden abrio");
            this.lock_op = false;
            lastOrderPrice= order.getLastPx().getValue();
            contVelas =0;
        } catch (FieldNotFound ex) {
            Logger.getLogger(Jedi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /*
     * Método que promedia un Promedio de bollingers con la variable spreadAask.
     */
    public double getAvgBoll(double boll){
        double temp = (boll  + (boll +(this.setts.spreadAsk * setts.Point)))/2;
        return redondear(temp);
    }
    /**
     * @return retornamos un promedio del precio de apertura relacionado con el Spread
     */
    public double getAvgOpen(){
        double temp = 0.0;
        temp = ((this.open_min) + (this.open_min +(this.ask - this.bid)))/2;
        return temp;
    }
    
    public void orderModify(){
        try {
            DBCollection coll = OrderHandler.mongo.getCollection("operaciones");
            BasicDBObject mod = new BasicDBObject();
            mod.append("$set", new BasicDBObject().append("Modify", "Y"));
            coll.update(new BasicDBObject().append("OrderID", lastOrder.getOrigClOrdID().getValue()), mod);
            ///Cuando se quiere modificar un OCO, primero cerramos la orden.
            OrderHandler.closeOCO(lastOrder.getClOrdID().getValue(),'M');
            Thread.sleep(5);
            //Enseguida enviamos una orden nueva.
            OrderHandler.SendOCO(lastOrder.getSymbol().getValue(), lastOrder.getSide().getValue(), lastOrder.getClOrdID().getValue(), 
                    (int)lastOrder.getOrderQty().getValue(), lastOrder.getLastPx().getValue(),'M');
             
        } catch (InterruptedException ex) {
            Logger.getLogger(Jedi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FieldNotFound ex) {
            Logger.getLogger(Jedi.class.getName()).log(Level.SEVERE, null, ex);
        }        
        this.modify = true;
    }
    
    public double redondear(double val){
        double temp;
        temp = Math.rint(val * 1000000) / 1000000;
        return temp;
    }
    /**
     * Guardamos el valor del ask.
     * @param ask 
     */
    public void setAsk(Double ask){
        this.ask = ask;
    }
    /**
     * verificamos que nos encontremos en horas de operacion.
     * @param hora
     * @return 
     */
    public boolean isActive(){
        Date date = new oms.Grafica.Date();
        boolean temp=false;
        double hora = date.getHour() + (date.getMinute()*0.01);
        if(hora < setts.horaFin && hora >= setts.horaIni && !this.grafic_lock && this.open_min>0)
            temp=true;
        return temp;
    }
    /*
     * verificamos que nos encontremos en horas de salida de operaciones.
     */
    public boolean rangeSalida(){
        Date date = new oms.Grafica.Date();  
        double hora = date.getHour() + (date.getMinute()*0.01);
        boolean temp=false;
        if(hora < setts.horaFinS && hora >= setts.horaIniS)
            temp=true;
        return temp;
    }
}

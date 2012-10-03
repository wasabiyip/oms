package oms.Grafica;

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
    public boolean lock= true;
    public int velasCont = 0;
    public double bid=0.0;
    public double ask=0.0;
    public double open_min=0.0;
    Jedi(Settings setts, int periodo){
        this.setts = setts;
    }
    public abstract void onTick(Double price);
    public abstract void onCandle(Double price);
    public abstract void onOpen(Double price);
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
            init.append("\"Velas Salida\": " + setts.velasS + ",");
            init.append("\"Hora Inicial\":" + setts.horaIni + ",");
            init.append("\"Hora Final\" :" + setts.horaFin +",");
            init.append("\"Hora Salida\" :" + setts.horaIniS+",");
            init.append(" \"Periodo\" :" + periodo + ",");
            init.append(" \"Boll Special\" :" + setts.boll_special+",");
            init.append(" \"Spread Ask\" :" + setts.spreadAsk);
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
        System.err.println("Orden cerro");
        this.lock = true;
        this.velasCont = 0;
    }
    /**
     * Nos avisan si una orden entro
     * @param type 
     */
    public void openNotify(char type){
        currentOrder = type;
        System.err.println("Orden abrio");
        this.lock = false;
    }
    /*
     * Método que promedia un Promedio de bollingers con la variable spreadAask.
     */
    public double getAvgBoll(double boll){
        double temp = (boll  + (boll +(this.setts.spreadAsk * setts.Point)))/2;
        return redondear(temp);
    }
    
    public double getAvgOpen(){
        double temp = 0.0;
        temp = ((this.open_min) + (this.open_min +(this.ask - this.bid)))/2;
        return temp;
    }
    
    public double redondear(double val){
        double temp;
        temp = Math.rint(val * 1000000) / 1000000;
        return temp;
    }
}

package oms.Grafica;

/**
 *
 * @author omar
 */
public abstract class Jedi {
    Settings setts;
    private int periodo;
    
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
            init.append(" \"Boll Special\" :" + setts.boll_special);
            init.append("}");
        return init;
    }
}

package oms.Grafica;

/**
 * Clase que reporta acerca del expert, como se esta comportando, estado de sus
 * variables, etc.
 * @author omar
 */
public class StateFeed {
    //referencia del expert
    private ExpertMoc expert;
    private Settings setts;
    //Solo aceptamos clases que hayan implementado ek AbstractExpert.
    public StateFeed(ExpertMoc expert){
        this.expert = expert;
        this.setts = expert.setts;
    }
    public String getSymbol(){
        return this.setts.symbol;
    }
    public Double getAvgOpen(){
        return this.expert.getAvgOpen();
    }
    public String getId(){
        return this.expert.setts.id;
    }
    /**
     * Método usaddo para informar sobre el estado actual del expert, regresa los
     * valores de promedios, de velas, etc. Todos los valores que influyen en el
     * comportamiento actual o futuro del expert.
     * @return estado de los valoes.
     */
    public String getExpertState(){
        
        StringBuffer temp = new StringBuffer();
        temp.append("\"variables\":{");
            temp.append("\"bollUp\":"+ expert.getAvgBoll(expert.bollUp())+ ",");
            temp.append("\"bollDn\":"+ expert.getAvgBoll(expert.bollDn())+ ",");
            temp.append("\"bollUpS\":"+ expert.getAvgBoll(expert.bollUpS()) + ",");
            temp.append("\"bollDnS\":"+ expert.getAvgBoll(expert.bollDnS())+ ",");
            //temp.append("\"Velas\":"+expert.cont_velas + ",");
            temp.append("\"limite\":"+(expert.OrdersCount()<expert.setts.limiteCruce?true:false) + ",");
            //temp.append("\"hora\":"+expert.hora()+ ",");
            temp.append("\"bollX\":"+(expert.bollingerDif() < expert.setts.bollxUp && 
                    expert.bollingerDif()> expert.setts.bollxDn)+ ",");
            temp.append("\"Active\":"+expert.isActive());
        temp.append("}");
        return temp.toString();
    }
    
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
            init.append("\"limiteCruce\" :" + setts.limiteCruce + ",");
            init.append("\"limiteMagic\" :" + setts.limiteMagic);
            init.append("}");
        return init;
    }
}

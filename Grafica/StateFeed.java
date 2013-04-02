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
            temp.append("\"bollUp\":"+ expert.redondear(expert.bollUp())+ ",");
            temp.append("\"bollDn\":"+ expert.redondear(expert.bollDn())+ ",");
            temp.append("\"bollUpS\":"+ expert.redondear(expert.bollUpS()) + ",");
            temp.append("\"bollDnS\":"+ expert.redondear(expert.bollDnS())+ ",");
            temp.append("\"bollDif\":"+ expert.redondear(expert.bollingerDif())+ ",");
            temp.append("\"limite\":"+(expert.OrdersBySymbol()<expert.setts.limiteCruce?true:false) + ",");
            temp.append("\"hora\":"+ (expert.CurrentHora()< expert.setts.horaFin && expert.CurrentHora()>=expert.setts.horaIni)+ ",");
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
            init.append("\"Boll1\" : " + setts.boll1 + ",");
            init.append("\"Boll2\" : " + setts.boll2 + ",");
            init.append("\"Boll3\" : " + setts.boll3 + ",");
            init.append("\"BollS1\" : " + setts.bollS1 + ",");
            init.append("\"BollS2\" : " + setts.bollS2 + ",");
            init.append("\"BollS3\" : " + setts.bollS3 + ",");
            init.append("\"XBoll1\" : " + setts.bollx1+ ",");
            init.append("\"XBoll2\" : " + setts.bollx2 + ",");
            init.append("\"XBoll3\" : " + setts.bollx3 + ",");
            init.append("\"TP\" : " + setts.tp + ",");
            init.append("\"SL\" : " + setts.sl + ",");
            init.append("\"VelasSalida\": " + setts.velasS + ",");
            init.append("\"Periodo\" :" + setts.periodo + ",");
            init.append("\"bollSpecial\" :" + setts.boll_special+",");
            init.append("\"spreadAsk\" :" + setts.spreadAsk + ",");
            init.append("\"limiteCruce\" :" + setts.limiteCruce);
            init.append("}");
        return init;
    }
}

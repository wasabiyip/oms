
package oms.util;
import java.util.Random;

/**
 *
 * @author omar
 * Clase que genera un numero ùnico para ser usado como Ticker de cada orden.
 */
public class idGenerator {
    /**
     * 
     * @return regresa el codigo generado.
     */
    public String getID(){
        
        return (this.getMesCod() + GMTDate.getDate().getYear() + getDay()
                + getChar() + getInt() + getInt()*10);
    }
    
    /**
     * 
     * @return returna el dia actual segun GMT.
     */
    private String getDay(){
        if (GMTDate.getDate().getDay() < 10){
            return (String)"0"+GMTDate.getDate().getDay();
        }else return ""+ GMTDate.getDate().getDay();
    }
    
    /**
     * 
     * @return regresa un numero aleatorio.
     */
    private int getInt(){
        Random r = new Random();
        return Math.abs(r.nextInt() % 10);
    }
    /**
     * 
     * @return regresa una letra aleatoria.
     */
    private char getChar(){
        Random r = new Random();
        int n = 25; // 65-90 codigo ASCII.
        int i = r.nextInt() % n;
        /**
         * TODO mejorar esta charrada...
         */
        if ( (65 + i <= 90) && (65 + i >= 65))
            return (char) (65 + i);
        else return 'A';
    }
    
    /**
     * 
     * @return Un código dependiendo de el mes.
     */
    private String getMesCod() {
        int mes = GMTDate.getDate().getMonth();
        switch (mes+1) {
            case 1:
                return "EN";

            case 2:
                return "FB";

            case 3:
                return "MR";

            case 4:
                return "AB";

            case 5:
                return "MY";

            case 6:
                return "JN";

            case 7:
                return "JL";

            case 8:
                return "AG";
            
            case 9:
                return "SP";
            
            case 10:
                return "OC";
                
            case 11:
                return "NB";
                
            case 12:
                return "DC";
             
            default:
                return "00";
        }
    }
}

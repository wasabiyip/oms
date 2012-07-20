
package oms.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Omar
 * Esta clase genera un archivo Json para ser usado por MongoDB, prácticamente
 * convierte un de Historico normal a uno listo para ser cargado en el motor
 * de datos.
 */

public class DoHistoryFile extends IMongoDAO{
    
    private BufferedReader bfr;
    private PrintWriter out;
    private boolean bool=false;
   
    /**
     * Constructor sobrecargado, recibe el nombre del archivo.
     * @param file ruta del archivo a convertir.
     * @throws IOException  
     */
    public DoHistoryFile() throws IOException{
        String  strline;
        StringBuffer str= new StringBuffer("");
        
        try {
            this.bfr = new BufferedReader(new InputStreamReader(
                        new DataInputStream( new FileInputStream(super.history))));
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DoHistoryFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
                        
             this.out = new PrintWriter(new FileWriter((super.jsonF)));
        } catch (IOException ex) {
            Logger.getLogger(DoHistoryFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            while ((strline = bfr.readLine()) != null){
                if (!bool){
                    bool=true;
                    continue;
                }
                str.append("{");
                for (int i=0; i< PATTERN.length;i++){
                    
                    str.append(PATTERN[i] + ":");
                    if(i==0)
                        str.append("\'"+formatString(strline).get(i).toString()+ "\',");
                    else if(i==7)
                            str.append(formatString(strline).get(i).toString());
                         else str.append(formatString(strline).get(i).toString()+",");
                }
                
                str.append("}");
                
                out.println(str);
                
                str.delete(0, str.length());
            }
        } catch (IOException ex) {
            Logger.getLogger(DoHistoryFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        out.close();
    }
    
  /**
   * Recibe un String y la transforma a Json en forma de ArrayList.
   *@param Input Cadena de texto a formatear.
   */
    private ArrayList formatString(String input) {
        int index = 0;
        int l = 0;
        String str;
        ArrayList array = new ArrayList();
        
        while (input.indexOf(",") > 0) {

            index = input.indexOf(",");
            array.add(input.substring(0, index));

            input = input.substring(index + 1);

        }
        array.add(input);

        
        return array;

    }
    /**
     * Lee el total de lineas en archivo (sin uso).
     * @return int
     */
    private int getRows(File file){
        int cnt = 0;
        try {
            FileInputStream fis =
                new FileInputStream(file);
            BufferedInputStream bis =
                new BufferedInputStream(fis);
            
            int b;
            while ((b = bis.read()) != -1) {
                if (b == '\n')
                cnt++;
                }
            bis.close();
            
            }
            catch (IOException e) {
            System.err.println(e);
            }
            return cnt;
        }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CustomException;

/**
 *
 * @author omar
 */
public class NoProfileFoundException extends Exception{
    public NoProfileFoundException(){
        super("No se encontro el perfil o está vacío, por favor crea un un nuevo"
                + " perfil o añade una gráfica, por lo pronto usaremos el deafult");
    }   
    public NoProfileFoundException(String perfil){
        super("No se encontro el perfil \""+ perfil +"\" o está vacío, por favor crea un un nuevo"
                + " perfil o añade una gráfica, por lo pronto usaremos el deafult");
    }
}

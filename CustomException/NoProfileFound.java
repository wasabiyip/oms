/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.CustomException;

/**
 *
 * @author omar
 */
public class NoProfileFound extends Exception{
    public NoProfileFound(){
        super(" No se encontro el perfil o está vacío, por favor crea un un nuevo"
                + " perfil o añade una gráfica, por lo pronto usaremos el deafult");
    }   
    public NoProfileFound(String perfil){
        super(" No se encontro el perfil \""+ perfil +"\" o está vacío, por favor crea un un nuevo"
                + " perfil o añade una gráfica, por lo pronto usaremos el deafult");
    }
}

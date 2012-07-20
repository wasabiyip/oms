/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.util;

/**
 *
 * @author omar
 */
public interface ISubject {
    public void addObserver( ITicker o );
     public void removeObserver( ITicker o );
}

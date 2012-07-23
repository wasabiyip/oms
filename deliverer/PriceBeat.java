package oms.deliverer;

import oms.util.ISubject;
import oms.util.ITicker;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *Clase que genera ticks y los propaga a otras clases que estan esperando este
 * evento. Esta basada en el Observer pattern design.
 * @author omar
 */
public class PriceBeat implements ISubject{
    private static Double precio = 0.0;
    private ArrayList list = new ArrayList();
    private ArrayList observers = new ArrayList();
    
    @Override
    public void addObserver(ITicker o) {
        observers.add( o );
    }

    @Override
    public void removeObserver(ITicker o) {
        observers.remove( o ); 
    }
    
    /**
     * Notifica a los observadores acerca de un nuevo tick.
     */
    public void notifyObservers() {
            // loop through and notify each observer
            Iterator i = observers.iterator();
            while( i.hasNext() ) {
                  ITicker o = ( ITicker ) i.next();
                  //o.onTick( this );
            }
      }
}

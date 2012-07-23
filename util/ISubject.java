package oms.util;

/**
 *
 * @author omar
 */
public interface ISubject {
    public void addObserver( ITicker o );
     public void removeObserver( ITicker o );
}

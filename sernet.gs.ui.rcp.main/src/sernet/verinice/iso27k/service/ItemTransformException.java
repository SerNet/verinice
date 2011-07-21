package sernet.verinice.iso27k.service;

/**
 * User can drag item from catalog view to
 * ism view. Item will be tranformed to
 * a specific verinice element aufter dropping.
 * 
 * This exception is thrown on problems during this transformation.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ItemTransformException extends RuntimeException {

    public ItemTransformException(String message) {
        super(message);
    }

}

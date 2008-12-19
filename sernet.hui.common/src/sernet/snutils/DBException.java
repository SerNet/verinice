/*
 * Created on 13.11.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package sernet.snutils;

import java.sql.SQLException;

/**
 * @author prack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DBException extends Exception {


    /**
     * 
     */
    public DBException() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param message
     */
    public DBException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param message
     * @param cause
     */
    public DBException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param cause
     */
    public DBException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
}

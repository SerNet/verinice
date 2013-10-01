package sernet.verinice.web;

import sernet.gs.web.Util;

/**
 * ManagedBean to show error and info messages.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class MessageBean {

    private String info;
    
    private String error;
    
    public void showInfo() {
        Util.addInfo("massagePanel", getInfo()); //$NON-NLS-1$
        setInfo(null);
    }
    
    public void showError() {
        Util.addError("massagePanel", getError()); //$NON-NLS-1$
        setError(null);
    }
    
    public String getInfo() {
        return info;
    }

    public void setInfo(String message) {
        this.info = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
    public void repeat() {
        Util.repeatMessage();
    }
    
    public void english() {
        Util.english();
    }
    
    public void german() {
        Util.german();
    }
}

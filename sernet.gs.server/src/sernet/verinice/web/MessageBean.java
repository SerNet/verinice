package sernet.verinice.web;

import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import sernet.gs.web.Util;

/**
 * ManagedBean to show error and info messages.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@ManagedBean(name = "message")
@SessionScoped
public class MessageBean {

    private String error;

    private Locale locale = null;

    @PostConstruct
    public void init() {
        locale = Locale.getDefault();
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(locale);
    }

    /**
     * Returns an checked icon if the language match the active setting.
     */
    public String getIcon(String language) {
        if (locale.getLanguage().equalsIgnoreCase(language)) {
            return "fa fa-check-circle-o";
        } else {
            return "fa fa-circle-o";
        }
    }

    public void showError() {
        Util.addError("messagePanel", getError()); //$NON-NLS-1$
        setError(null);
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
        setLocale(Locale.ENGLISH);
    }

    public void german() {
        setLocale(Locale.GERMAN);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        Locale.setDefault(locale);
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(locale);
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    public String getcurrentLanguageTag() {
        return Util.getcurrentLanguageTag();
    }
}

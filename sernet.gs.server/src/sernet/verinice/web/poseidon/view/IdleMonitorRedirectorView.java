
package sernet.verinice.web.poseidon.view;

import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ManagedBean
public class IdleMonitorRedirectorView {

    public void onIdle() throws IOException {

        FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("/veriniceserver/auth/login.xhtml?faces-redirect=true");
    }
}

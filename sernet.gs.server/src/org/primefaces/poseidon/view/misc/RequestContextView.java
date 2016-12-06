/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.poseidon.view.misc;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;
import org.primefaces.poseidon.domain.User;

@ManagedBean
public class RequestContextView {

    private User user;

    @PostConstruct
    public void init() {
        user = new User();

        if(!FacesContext.getCurrentInstance().isPostback()) {
            RequestContext.getCurrentInstance().execute("alert('This onload script is added from backing bean.')");
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

	public void save() {
		RequestContext context = RequestContext.getCurrentInstance();
		context.addCallbackParam("saved", true);    //basic parameter
		context.addCallbackParam("user", user);     //pojo as json

        //execute javascript oncomplete
        context.execute("PrimeFaces.info('Hello from the Backing Bean');");

        //update panel
        context.update("form:panel");

        //scroll to panel
        context.scrollTo("form:panel");

        //add facesmessage
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Success"));
	}
}

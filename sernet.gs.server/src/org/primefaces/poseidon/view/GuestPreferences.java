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
package org.primefaces.poseidon.view;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Switches main menu position/behaviour.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean
@SessionScoped
public class GuestPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    private String menuLayout = "static";

    public String getMenuLayout() {
        if(this.menuLayout.equals("static"))
            return "menu-layout-static";
        else if(this.menuLayout.equals("overlay"))
            return "menu-layout-overlay";
        else if(this.menuLayout.equals("horizontal"))
            return "menu-layout-static menu-layout-horizontal";
        else
            return "menu-layout-static";
    }


    public void setMenuLayout(String menuLayout) {
        this.menuLayout = menuLayout;
    }

}

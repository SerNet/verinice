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

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class GuestPreferences implements Serializable {

    private Map<String,String> themeColors;

    private String theme = "turquoise";

    private String menuLayout = "static";

    @PostConstruct
    public void init() {
        themeColors = new HashMap<String,String>();
        themeColors.put("turquoise", "#00acac");
        themeColors.put("blue", "#2f8ee5");
        themeColors.put("orange", "#efa64c");
        themeColors.put("purple", "#6c76af");
        themeColors.put("pink", "#f16383");
        themeColors.put("light-blue", "#63c9f1");
        themeColors.put("green", "#57c279");
        themeColors.put("deep-purple", "#7e57c2");
    }

	public String getTheme() {
		return theme;
	}

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

	public void setTheme(String theme) {
		this.theme = theme;
	}

    public void setMenuLayout(String menuLayout) {
        this.menuLayout = menuLayout;
    }

    public Map getThemeColors() {
        return this.themeColors;
    }
}

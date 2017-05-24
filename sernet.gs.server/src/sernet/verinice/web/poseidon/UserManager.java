/*
 * Copyright (C) 2017 Moritz Reiter
 *
 * This file is part of Verinice.
 *
 * Verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Verinice. If not, see http://www.gnu.org/licenses/.
 */

package sernet.verinice.web.poseidon;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 * Handles user interaction with its session.
 *
 * @author Moritz Reiter
 */
@SessionScoped
@ManagedBean
public class UserManager {

    public String logout() {

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/misc/home.xhtml?faces-redirect=true";
    }
}

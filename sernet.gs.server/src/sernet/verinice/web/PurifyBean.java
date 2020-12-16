/*******************************************************************************
 * Copyright (c) 2019 Finn Westendorf
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package sernet.verinice.web;

import java.util.UUID;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 * <h1>Bean that provides code for &lt;vn:purify&gt; (misc/purify.xhtml).</h1>
 * 
 * The provided html & js will only work when DOMPurify js already imported.
 * <br>
 * <code>(&lt;h:outputScript name="PATHTODOMPURIFY/purify.min.js" /&gt;).<br> 
 * &lt;vn:purify&gt;</code> should surround entity escaped html (< =
 * &amp;lt;).<br>
 * Read more:<br>
 * <a href="https://www.owasp.org/index.php/Cross-site_Scripting_(XSS)">XSS</a>
 * <br>
 * <a href="https://github.com/cure53/DOMPurify">DOMPurify</a>
 */
@ManagedBean(name = "purify")
@ApplicationScoped
public class PurifyBean {

    public String getPurifier() {
        String id = UUID.randomUUID().toString();//This is to prevent collisions, not sure if this is necessary.
        return String.format("<div id='%s'></div>"
                + "<script>var toPurify = document.getElementById('%<s').parentNode;"
                + "var purified = DOMPurify.sanitize(toPurify.innerText, {USE_PROFILES:{html:true}});"
                + "toPurify.innerHTML = purified;</script>", id);
    }
}
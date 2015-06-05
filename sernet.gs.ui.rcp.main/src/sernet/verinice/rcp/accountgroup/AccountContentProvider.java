/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.accountgroup;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;

/**
 * extended ArrayContentProvider that is enabled to deal with {@link PlaceHolder}
 */
public class AccountContentProvider extends ArrayContentProvider {
    
    private Logger log;
    
    private TableViewer tableView;
    
    public AccountContentProvider(TableViewer tableView){
        this.tableView = tableView;
    }
    
    
    @Override
    public void inputChanged(Viewer v, Object oldInput, Object newInput){
        if (newInput instanceof PlaceHolder){
            return;
        }
        try{
            v.refresh();           
        } catch (Exception e){
            getLog().error("Wrong input for viewer", e);
        }
    }
    
    @Override
    public Object[] getElements(Object obj) {
        if (obj instanceof PlaceHolder) {
            return new Object[] { obj };
        }

        if (tableView == null || tableView.getInput() == null) {
            return new Object[] {};
        }
        
        Object input = tableView.getInput(); 
        if(input instanceof TreeSet){
            List<String> list = new ArrayList<String>(0);
            if(((TreeSet<Object>)input).size() > 0){
                list.addAll((TreeSet<String>)input);
                return list.toArray(new Object[((TreeSet<String>)input).size()]);
            } else {
                return new Object[]{};
            }
        } else if(input instanceof String[]){
            return (String[])input;
        } 
        return new Object[]{new PlaceHolder(Messages.GroupView_41)};
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(AccountContentProvider.class);
        }
        return log;
    }

}

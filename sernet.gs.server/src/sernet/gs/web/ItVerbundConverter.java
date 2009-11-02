package sernet.gs.web;

import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import sernet.gs.server.ServerInitializer;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;

public class ItVerbundConverter implements Converter {

	ToDoBean toDoBean;
	
	public ItVerbundConverter(ToDoBean toDoBean) {
		super();
		this.toDoBean = toDoBean;
	}

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String text) {
		Object value = null;
		if(toDoBean!=null && text!=null) {
			ServerInitializer.inheritVeriniceContextState();
			List<ITVerbund> itVerbundList = toDoBean.getItVerbundList();
			if(itVerbundList!=null) {
				for (Iterator<ITVerbund> iterator = itVerbundList.iterator(); iterator.hasNext();) {
					ITVerbund itVerbund = iterator.next();
					if(text.equals(itVerbund.getTitel())) {
						value = itVerbund;
						break;
					}
				}
			}
		}
		return value;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		ServerInitializer.inheritVeriniceContextState();
		ITVerbund itVerbund = (ITVerbund) value;
		return (itVerbund==null) ? null : itVerbund.getTitel();
	}

}

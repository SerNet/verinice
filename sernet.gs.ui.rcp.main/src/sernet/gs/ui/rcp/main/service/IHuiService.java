package sernet.gs.ui.rcp.main.service;

import java.util.List;

import sernet.hui.common.connect.Property;

public interface IHuiService {
	public List<Property> findAllPropertiesForTypeId(String propertyTypeId);
}

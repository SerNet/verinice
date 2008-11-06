package sernet.gs.ui.rcp.main.service;

import java.util.List;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;

public interface IHuiService {
	public List<Property> findAllPropertiesForTypeId(String propertyTypeId);
	public void migratePersonsForTypeId(String oldPropTypeID, String newPropTypeID);
}

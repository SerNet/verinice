package sernet.gs.ui.rcp.main.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.hui.common.connect.Property;


public class HuiServiceTest {

	@Test
	public void testFindAllPropertiesForTypeId() {
		ServiceFactory factory = new ServiceFactory();
		IHuiService huiService = factory.getHuiService();
		List<Property> list = huiService.findAllPropertiesForTypeId(MassnahmenUmsetzung.P_UMSETZUNGDURCH_LINK);
		assertTrue(list.size()>0);
		
	}
}

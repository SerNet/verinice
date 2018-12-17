package sernet.client.service.handlerrights;

import org.osgi.service.component.annotations.Component;

import sernet.client.services.rights.HandlerRightsService;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;

@Component
public class HandlerRightsServiceImpl implements HandlerRightsService {

	@Override
	public boolean isEnabled(String actionId) {
		RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
		return service != null && service.isEnabled(actionId);
	}

}

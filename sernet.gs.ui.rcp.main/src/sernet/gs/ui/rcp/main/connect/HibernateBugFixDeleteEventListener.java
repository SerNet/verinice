package sernet.gs.ui.rcp.main.connect;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.event.EventSource;
import org.hibernate.event.def.DefaultDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Workaround for Hibernate Bug HHH-2146, see
 * http://opensource.atlassian.com/projects/hibernate/browse/HHH-2146?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#action_26488
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HibernateBugFixDeleteEventListener extends
		DefaultDeleteEventListener {
	protected void deleteTransientEntity(EventSource session, Object entity,
			boolean cascadeDeleteEnabled, EntityPersister persister,
			Set transientEntities) {
		super.deleteTransientEntity(session, entity, cascadeDeleteEnabled,
				persister, transientEntities == null ? new HashSet()
						: transientEntities);
	}
}

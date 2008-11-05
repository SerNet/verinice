package sernet.hui.common.connect;

import java.util.List;

import sernet.hui.common.multiselectionlist.IMLPropertyOption;

public interface IReferenceResolver {

	public List<IMLPropertyOption> getAllEntitesForType(String referencedEntityTypeId);

}

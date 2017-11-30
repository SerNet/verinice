/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 *     Urs Zeidler uz[at]sernet.de - Add generic value change methods
 ******************************************************************************/
package sernet.verinice.interfaces;

import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This contract is used by the {@link CnATreeElement} to trigger a reevaluation
 * of property values along the graph of linked {@link CnATreeElement}. If a
 * CnATreeElement need to propagate a property value change to linked elements
 * this element need an instance of the {@link IReevaluator} @see
 * {@link CnATreeElement#isProtectionRequirementsProvider()} and
 * {@link CnATreeElement#getProtectionRequirementsProvider()}. The reevaluate
 * signal is invoked by the corresponding
 * {@link CnATreeElement#fireXXXChanged(CascadingTransaction ta)} in
 * CnATreeElement which should be called in the event of changes to the values
 * of the CnATreeElement properties. @see
 * {@link sernet.verinice.hibernate.TreeElementDao#fireChange(CnATreeElement elmt)}
 * and @see {@link sernet.verinice.model.common.CnALink#remove()}.<br/>
 * There are currently two use cases for this kind of reevaluation: <br/>
 * 1. The reevaluation of the base protection requirements.<br/>
 * 2. The reevaluation of the implementation status in the modernized base
 * protection.<br/>
 *
 * @author Alexander Koderman
 *
 */
public interface IReevaluator {
	int getConfidentiality();
	int getAvailability();
	int getIntegrity();

	void setConfidentiality(int i);
	void setIntegrity(int i);
	void setAvailability(int i);

	String getConfidentialityDescription();
	String getIntegrityDescription();
	String getAvailabilityDescription();

	boolean isCalculatedConfidentiality();
	boolean isCalculatedIntegrity();
	boolean isCalculatedAvailability();

	void setConfidentialityDescription(String text);
	void setIntegrityDescription(String text);
	void setAvailabilityDescription(String text);

	void updateConfidentiality(CascadingTransaction ta);
	void updateIntegrity(CascadingTransaction ta);
	void updateAvailability(CascadingTransaction ta);

    /**
     * Is triggered by
     * {@link CnATreeElement#fireValueChanged(CascadingTransaction)} to indicate
     * a change in the properties values of an {@link CnATreeElement}.
     */
	void updateValue(CascadingTransaction ta);

    /**
     * Can be used to set a value to a property which is determine in the
     * {@link IReevaluator#updateValue(CascadingTransaction)} method.
     */
	void setValue(CascadingTransaction ta, String properyName, Object value);
}
